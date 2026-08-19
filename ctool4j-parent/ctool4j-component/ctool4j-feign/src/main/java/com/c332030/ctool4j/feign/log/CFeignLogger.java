package com.c332030.ctool4j.feign.log;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.util.CBoolUtils;
import com.c332030.ctool4j.definition.function.CBiFunction;
import com.c332030.ctool4j.feign.config.CFeignClientLogConfig;
import com.c332030.ctool4j.feign.util.CFeignUtils;
import com.c332030.ctool4j.web.model.CRequestLog;
import com.c332030.ctool4j.web.util.CCommUtils;
import com.c332030.ctool4j.web.util.CRequestLogUtils;
import feign.Logger;
import feign.Request;
import feign.Response;
import feign.Util;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.val;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * <p>
 * Description: CFeignLogger
 * </p>
 *
 * @since 2025/12/2
 */
@CustomLog
@AllArgsConstructor
public class CFeignLogger extends Logger {

    /**
     * 本线程进行中的 feign 请求日志（发起后、回调前），供回调取回打印。
     * <p>标准 feign（同步阻塞 / AsyncFeign 在 executor 线程内串行执行）中，
     * logRequest 与对应回调恒在同一线程且"进行中"的请求最多一个（重试/嵌套均串行交替），单值即可；
     * 回调时取走并清除。若自定义 Client 等包装导致回调跨线程，本线程值为空，日志降级为不打印</p>
     *
     * <p><b>方案演进与设计思路</b>（记录取舍，避免后来者误改）：
     * 最初用 {@code Caffeine<Request, CRequestLog> ConcurrentMap} 按 Request 引用精确取回 + ThreadLocal 兜底异常取回。
     * 后简化为仅 ThreadLocal<CRequestLog>，依据如下：</p>
     * <ol>
     * <li><b>并发隔离靠 ThreadLocal 自身</b>：每线程独立实例，并发多线程下同接口并发日志不串（"线程内单线程"），
     * 无需全局 map 按引用隔离。</li>
     * <li><b>回调必在同线程</b>：标准 feign 中 logRequest 与 logAndRebufferResponse/logIOException 恒在同一线程
     * 且"进行中"请求最多一个，ThreadLocal 单值即可配对，map 成为冗余。</li>
     * <li><b>异常回调无 request 参数</b>（feign Logger API 限制），只能靠线程线索取回，ThreadLocal 是唯一载体；
     * 故 ThreadLocal 本身不可删，否则所有连接类异常日志整条丢失。</li>
     * <li><b>残留防护</b>：logRequest 无条件写入本次请求标记（未记录请求写 null），覆盖上次异常残留
     * （如自定义 Client 抛非 IOException 时无回调），杜绝"上次残留被下次未记录请求误取"的错配。</li>
     * </ol>
     * <p><b>已接受的边界</b>：回调跨线程的自定义包装（如自定义 Client/InvocationHandler/Hystrix）下，
     * 回调线程本值为空，该请求成功/异常日志降级为不打印。项目使用标准 feign，此边界已确认可接受。</p>
     */
    final ThreadLocal<CRequestLog> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

    CFeignClientLogConfig config;

    /**
     * 记录请求日志：开启日志时写入本次请求标记
     *
     * @param configKey 配置键
     * @param logLevel  日志级别
     * @param request   请求
     */
    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        if (CBoolUtils.isTrue(config.getEnable())) {
            // 无条件写入本次请求的日志标记：记录请求时存入 CRequestLog，未记录（黑名单等）时存入 null，
            // 覆盖上次异常残留，保证回调 get 到的一定是本次请求的标记，避免日志错配
            REQUEST_THREAD_LOCAL.set(enableLog(request) ? setRequestLog(request) : null);
        }
    }

    /**
     * 记录响应日志并重新缓冲响应
     *
     * @param configKey   配置键
     * @param logLevel    日志级别
     * @param response    响应
     * @param elapsedTime 耗时（毫秒）
     * @return 重新缓冲后的响应
     */
    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime) {
        return this.dealResponse(response, elapsedTime, this::dealResponseLog);
    }

    /**
     * 记录连接类异常日志
     *
     * @param configKey   配置键
     * @param logLevel    日志级别
     * @param ioe         异常
     * @param elapsedTime 耗时（毫秒）
     * @return 原异常
     */
    @Override
    protected IOException logIOException(String configKey, Level logLevel, IOException ioe, long elapsedTime) {
        // feign 异常回调不提供 request 参数，只能通过线程兜底取回本线程最近一次未完成的请求
        return this.dealResponse(ioe, elapsedTime, this::dealErrorLog);
    }

    /**
     * 通用日志输出（本类不采用，提示调用方勿直接使用）
     *
     * @param configKey 配置键
     * @param format    日志格式
     * @param args      日志参数
     */
    @Override
    protected void log(String configKey, String format, Object... args) {
        log.warn("Don't call this log method");
    }

    /**
     * 是否记录该请求日志。
     * <p>优先级：白名单（host/path/api 任一命中）优先放行；未命中白名单时黑名单拦截；
     * 均未命中时按 logAll 开关决定。白名单命中即返回，黑名单不参与判断</p>
     *
     * @param request feign 请求
     * @return 是否记录
     */
    @SneakyThrows
    private boolean enableLog(Request request) {

        val url = new URL(request.url());
        val host = url.getHost();
        val path = url.getPath();

        if (config.getHostWhiteList().contains(host)
            || config.getPathWhiteList().contains(path)
        ) {
            return true;
        }

        val apiType = CFeignUtils.getApiType(request.requestTemplate());
        val api = apiType.getSimpleName();
        if (config.getApiWhiteList().contains(api)) {
            return true;
        }

        if (config.getHostBlackList().contains(host)
            || config.getPathBlackList().contains(path)
            || config.getApiBlackList().contains(api)
        ) {
            return false;
        }

        return CBoolUtils.isTrue(config.getLogAll());
    }

    /**
     * 构建请求日志模型 CRequestLog：method、path、请求头、请求体，拼接在打印时执行
     *
     * @param request feign 原始请求
     * @return 请求日志模型
     */
    private CRequestLog setRequestLog(Request request) {
        val requestLog = new CRequestLog();
        requestLog.setMethod(request.httpMethod().name());
        setPathAndParams(requestLog, request);
        if (BooleanUtil.isTrue(config.getEnableHeader())) {
            // feign 请求头为外部可变对象，深拷贝为不可变后再保存，避免日志模型被外部修改
            requestLog.setHeaders(toImmutableHeaders(request.headers()));
        }
        // 请求体文本放入 req，与 MVC 场景记录位置一致，拼接时统一从 req 取请求体
        requestLog.setReq(getBodyText(request.body(), request.headers()));
        return requestLog;
    }

    /**
     * feign 请求头（外部可变对象）深拷贝为不可变 Map：
     * 外层 Map 与内层 Collection 均不可变，避免日志模型持有外部可变引用
     *
     * @param headers feign 请求头
     * @return 不可变请求头，空 headers 返回 null
     */
    private Map<String, Collection<String>> toImmutableHeaders(Map<String, Collection<String>> headers) {
        if (MapUtil.isEmpty(headers)) {
            return null;
        }
        val headerMap = new LinkedHashMap<String, Collection<String>>();
        headers.forEach((key, values) -> headerMap.put(
            key,
            null == values ? null : Collections.unmodifiableList(new ArrayList<>(values))
        ));
        return Collections.unmodifiableMap(headerMap);
    }

    /**
     * 设置日志 path（相对路径，含 query string）：
     * <p>feign 的 @RequestParam 无论 GET/POST 都已拼入 URL query string，
     * 随 path 一并输出，避免非 GET 请求在请求体区重复输出 form 段（双 body 误导排障）；
     * 服务端日志的 params 仍走拼接层，互不影响</p>
     *
     * @param requestLog 请求日志模型
     * @param request    feign 原始请求
     */
    @SneakyThrows
    private void setPathAndParams(CRequestLog requestLog, Request request) {
        val url = new URL(request.url());
        requestLog.setPath(appendQuery(url.getPath(), url.getQuery()));
    }

    /**
     * 路径后拼接 query string（无 query 时原样返回）
     *
     * @param path  请求路径
     * @param query query string（URL 原样，已编码）
     * @return 完整展示路径
     */
    private String appendQuery(String path, String query) {
        return StrUtil.isEmpty(query) ? path : path + "?" + query;
    }

    /**
     * 请求/响应体字节数组转换为可打印文本，非文本 body 输出占位符
     *
     * @param bodyBytes 字节数组
     * @param headers   请求头（用于判断 Content-Type 是否文本）
     * @return 可打印文本，空 body 返回 null
     */
    private String getBodyText(byte[] bodyBytes, Map<String, Collection<String>> headers) {
        if (ArrayUtil.isEmpty(bodyBytes)) {
            return null;
        }
        if (CCommUtils.isTextBody(headers)) {
            return new String(bodyBytes, CCommUtils.getCharsetOrDefault(headers));
        }
        return "[not text body]";
    }

    /**
     * 统一处理响应/异常日志：取回并清除本线程请求日志，设置属性（耗时、响应体、异常）后走公共出口打印 http 格式
     *
     * @param t           原始对象（响应或异常）
     * @param elapsedTime 耗时（毫秒）
     * @param function    设置响应体/异常的具体逻辑
     * @param <T>         原始对象类型
     * @return 原始对象（响应会被重新缓冲）
     */
    private <T> T dealResponse(T t, long elapsedTime, CBiFunction<T, CRequestLog, T> function) {

        // 取回并清除本线程进行中的请求日志，无论后续是否打印都先清理，避免线程复用泄漏
        val requestLog = REQUEST_THREAD_LOCAL.get();
        REQUEST_THREAD_LOCAL.remove();

        if (CBoolUtils.isTrue(config.getEnable())) {
            if (null == requestLog || StrUtil.isEmpty(requestLog.getMethod())) {
                return t;
            }
            try {
                return function.apply(t, requestLog);
            } catch (Throwable e) {
                log.error("处理响应日志失败", e);
                return t;
            } finally {
                // 设置属性：耗时由 CCommUtils.appendHttpLog 按起止时间计算，业务数据区恒输出 rt（elapsedTime 为 0 的快速请求输出 rt: 0ms）
                val now = System.currentTimeMillis();
                requestLog.setBeginTimeMillis(now - elapsedTime);
                requestLog.setEndTimeMillis(now);
                // 统一出口同步打印，与服务端日志一致不丢数据；如需异步请在日志配置中使用 AsyncAppender
                // 日志打印失败不影响业务结果（避免 finally 抛异常覆盖返回值导致请求失败）
                try {
                    CRequestLogUtils.logWrite(requestLog);
                } catch (Throwable e) {
                    log.error("日志写入失败", e);
                }
            }
        }

        return t;
    }

    /**
     * 设置响应体并重新缓冲响应
     *
     * @param response   原始响应
     * @param requestLog 请求日志模型
     * @return 重新缓冲后的响应
     */
    private Response dealResponseLog(Response response, CRequestLog requestLog) {
        val responseBodyBytes = getBodyBytes(response);
        requestLog.setRsp(getBodyText(responseBodyBytes, response.headers()));
        return CFeignUtils.newResponse(response, responseBodyBytes);
    }

    /**
     * 设置异常信息
     *
     * @param ioException 异常
     * @param requestLog  请求日志模型
     * @return 原异常
     */
    private IOException dealErrorLog(IOException ioException, CRequestLog requestLog) {
        requestLog.setErrorMessage(ioException.getMessage());
        return ioException;
    }

    private byte[] getBodyBytes(Response response) {
        try {
            val inputStream = CObjUtils.convert(response.body(), Response.Body::asInputStream);
            return CObjUtils.convert(inputStream, Util::toByteArray);
        } catch (Exception e) {
            log.debug("获取响应体失败", e);
            return null;
        }
    }

}
