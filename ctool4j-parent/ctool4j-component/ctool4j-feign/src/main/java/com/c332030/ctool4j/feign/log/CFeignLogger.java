package com.c332030.ctool4j.feign.log;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.util.CBoolUtils;
import com.c332030.ctool4j.core.util.CCommUtils;
import com.c332030.ctool4j.definition.function.CBiFunction;
import com.c332030.ctool4j.feign.config.CFeignClientLogConfig;
import com.c332030.ctool4j.feign.util.CFeignUtils;
import com.c332030.ctool4j.log.model.CRequestLog;
import com.c332030.ctool4j.log.util.CRequestLogUtils;
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
import java.nio.charset.StandardCharsets;
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
     * 进行中的 feign 请求日志，以 Request 引用为 key。
     * <p>同一线程内嵌套、并发/异步等多个 feign 请求互不串扰：
     * logAndRebufferResponse 通过 response.request() 精确取回对应请求</p>
     */
    final Map<Request, CRequestLog> REQUEST_LOG_MAP = Collections.synchronizedMap(new IdentityHashMap<>());

    /**
     * 兜底记录本线程进行中的 feign 请求（按栈维护，支持同一线程嵌套 feign），
     * 供 logIOException（feign API 不提供 request 参数）取回日志
     */
    final ThreadLocal<Deque<Request>> REQUEST_DEQUE_THREAD_LOCAL = ThreadLocal.withInitial(ArrayDeque::new);

    CFeignClientLogConfig config;

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        if (CBoolUtils.isTrue(config.getEnable())) {
            if (enableLog(request)) {
                // 永远先记录请求信息，统一保存到 CRequestLog，拼接在打印时执行
                setRequestLog(request);
                pushRequest(request);
            }
        }
    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime) {
        val requestLog = REQUEST_LOG_MAP.remove(response.request());
        removeFromRequestDeque(response.request());
        return dealResponse(response, requestLog, elapsedTime, this::dealResponseLog);
    }

    @Override
    protected IOException logIOException(String configKey, Level logLevel, IOException ioe, long elapsedTime) {
        // feign 异常回调不提供 request 参数，只能通过线程栈兜底取回最近一次未完成的请求
        val deque = REQUEST_DEQUE_THREAD_LOCAL.get();
        val request = deque.pollLast();
        if (deque.isEmpty()) {
            REQUEST_DEQUE_THREAD_LOCAL.remove();
        }
        val requestLog = null == request ? null : REQUEST_LOG_MAP.remove(request);
        return dealResponse(ioe, requestLog, elapsedTime, this::dealErrorLog);
    }

    /**
     * 记录请求到本线程栈，供 logIOException 兜底取回。
     * <p>入栈前清理本线程已完成的残留引用（其响应已从 REQUEST_LOG_MAP 移除），
     * 避免异步返回时栈残留导致线程池复用时累积</p>
     *
     * @param request feign 请求
     */
    private void pushRequest(Request request) {
        val deque = REQUEST_DEQUE_THREAD_LOCAL.get();
        while (!deque.isEmpty() && !REQUEST_LOG_MAP.containsKey(deque.peekLast())) {
            deque.pollLast();
        }
        deque.addLast(request);
    }

    /**
     * 请求正常完成后从本线程栈移除（仅当位于栈顶时按引用比较移除，避免同内容请求误删）
     *
     * @param request feign 请求
     */
    private void removeFromRequestDeque(Request request) {
        val deque = REQUEST_DEQUE_THREAD_LOCAL.get();
        if (deque.isEmpty()) {
            return;
        }
        if (deque.peekLast() == request) {
            deque.pollLast();
        }
        if (deque.isEmpty()) {
            REQUEST_DEQUE_THREAD_LOCAL.remove();
        }
    }

    @Override
    protected void log(String configKey, String format, Object... args) {
        log.warn("Don't call this log method");
    }

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
     * 记录请求信息到统一模型 CRequestLog：method、path、请求头、请求体，拼接在打印时执行
     *
     * @param request feign 原始请求
     */
    private void setRequestLog(Request request) {
        val requestLog = new CRequestLog();
        requestLog.setMethod(request.httpMethod().name());
        setPathAndParams(requestLog, request);
        if (BooleanUtil.isTrue(config.getEnableHeader())) {
            // feign 请求头本身为 Map<String, Collection<String>>，可直接保存多值
            requestLog.setHeaders(request.headers());
        }
        requestLog.setRequestBody(getBodyText(request.body(), request.headers()));
        REQUEST_LOG_MAP.put(request, requestLog);
    }

    /**
     * 设置日志 path（相对路径）与 query 参数：
     * <p>feign 的 @RequestParam 无论 GET/POST 都在 URL query string 中，统一放入 params 对象，
     * 由拼接层负责输出（GET 拼到 URL，非 GET 输出为 form 段），避免拼接层仅对 GET 拼接 params 导致丢失</p>
     *
     * @param requestLog 请求日志模型
     * @param request    feign 原始请求
     */
    @SneakyThrows
    private void setPathAndParams(CRequestLog requestLog, Request request) {
        val url = new URL(request.url());
        requestLog.setPath(url.getPath());
        requestLog.setParams(getParamsMap(request.requestTemplate().queries()));
    }

    /**
     * feign 的 query 参数（Map&lt;String, Collection&lt;String&gt;&gt;）转换为日志模型需要的 Map&lt;String, String[]&gt;
     *
     * @param queries feign 展开后的 query 参数
     * @return 日志模型参数，无参数返回 null
     */
    private Map<String, String[]> getParamsMap(Map<String, Collection<String>> queries) {
        if (MapUtil.isEmpty(queries)) {
            return null;
        }
        val paramMap = new LinkedHashMap<String, String[]>();
        queries.forEach((key, values) -> paramMap.put(key, values.toArray(new String[0])));
        return paramMap;
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
            return new String(bodyBytes, StandardCharsets.UTF_8);
        }
        return "[not text body]";
    }

    /**
     * 统一处理响应/异常日志：设置属性（耗时、响应体、异常）后走公共出口打印 http 格式
     *
     * @param t           原始对象（响应或异常）
     * @param requestLog  对应的请求日志，null 表示未命中（未记录请求或已被取走）
     * @param elapsedTime 耗时（毫秒）
     * @param function    设置响应体/异常的具体逻辑
     * @param <T>         原始对象类型
     * @return 原始对象（响应会被重新缓冲）
     */
    private <T> T dealResponse(T t, CRequestLog requestLog, long elapsedTime, CBiFunction<T, CRequestLog, T> function) {

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
                // 设置属性：耗时
                requestLog.setRt(elapsedTime);
                // 统一出口打印 http 格式
                CRequestLogUtils.logWrite(requestLog);
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
        requestLog.setThrowableMessage(ioException.getMessage());
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
