package com.c332030.ctool4j.web.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.log.CLogUtils;
import com.c332030.ctool4j.core.util.*;
import com.c332030.ctool4j.web.enums.CRequestHeaderEnum;
import com.c332030.ctool4j.web.model.CRequestLog;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CCommUtils
 * </p>
 * <p>HTTP 请求日志拼接工具类，将请求日志信息拼接为类似 HTTP 请求+响应的完整报文 dump，功能设计与用例设计见设计文档。</p>
 *
 * @since 2025/3/15
 * @see "doc/design/web/CCommUtils.adoc"
 * @see "doc/design/web/CCommUtilsTests.adoc"
 */
@UtilityClass
public class CCommUtils {

    /**
     * 设置表单提交 Content-Type
     *
     * @param headers 请求头
     */
    public void contextTypeForm(HttpHeaders headers) {
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    }

    /**
     * 设置 JSON Content-Type
     *
     * @param headers 请求头
     */
    public void contextTypeJson(HttpHeaders headers) {
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    /**
     * 设置 Accept 为 JSON
     *
     * @param headers 请求头
     */
    public void acceptJson(HttpHeaders headers) {
        headers.setAccept(CList.of(MediaType.APPLICATION_JSON));
    }

    /**
     * 请求头拼为字符串（每个 header 一行）
     *
     * @param headers 请求头
     * @return 请求头字符串，为空时返回 null
     */
    public String getFullHeaderStr(Map<String, Collection<String>> headers) {

        if(MapUtil.isEmpty(headers)) {
            return null;
        }

        return getFullHeaderStr(headers, entry -> true);
    }

    /**
     * 请求头拼为字符串（按条件过滤，每个 header 一行）
     *
     * @param headers   请求头
     * @param predicate 过滤条件
     * @return 请求头字符串，为空时返回 null
     */
    public String getFullHeaderStr(
            Map<String, Collection<String>> headers,
            Predicate<Map.Entry<String, Collection<String>>> predicate
    ) {

        if(MapUtil.isEmpty(headers)) {
            return null;
        }

        return headers.entrySet().stream()
                .filter(predicate)
                .map(entry -> entry.getKey()
                        + ": "
                        +  StrUtil.join(",", entry.getValue())
                ).collect(Collectors.joining("\n"));
    }

    /**
     * 判断请求体是否为文本类型
     *
     * @param headers 请求头
     * @return 是否为文本类型
     */
    public boolean isTextBody(Map<String, Collection<String>> headers) {

        if(MapUtil.isEmpty(headers)) {
            return false;
        }

        val contentTypes = CCollUtils.defaultEmpty(headers.get(HttpHeaders.CONTENT_TYPE));
        return contentTypes.stream().anyMatch(CMediaTypeUtils::isText);
    }

    /**
     * 从 Content-Type 解析字符集，未声明时默认 UTF-8；无法解析的 Content-Type 忽略
     * @param headers 请求/响应头
     * @return 字符集
     */
    public Charset getCharsetOrDefault(Map<String, Collection<String>> headers) {

        if(MapUtil.isEmpty(headers)) {
            return CCharsets.UTF_8;
        }

        val contentTypes = CCollUtils.defaultEmpty(headers.get(HttpHeaders.CONTENT_TYPE));
        for (val contentType : contentTypes) {
            try {
                val charset = MediaType.parseMediaType(contentType).getCharset();
                if (null != charset) {
                    return charset;
                }
            } catch (Exception e) {
                // 无法解析的 Content-Type 忽略，继续尝试下一个
            }
        }
        return CCharsets.UTF_8;
    }

    /**
     * 拼接请求行：METHOD path[?params]，仅 GET 请求拼接查询参数，
     * POST/PUT 等请求的参数在 body 中，不拼到 URL。
     * <p>请求行是完整 HTTP 报文的第一行，本方法不自带头部换行</p>
     *
     * @param sb   日志拼接器
     * @param info 请求日志信息
     */
    public void appendRequestUrl(StringBuilder sb, CRequestLog info) {

        val method = info.getMethod();
        sb.append(method);
        sb.append(" ");
        appendUrl(sb, info);
    }

    /**
     * 拼接 URL 路径 + Query 参数
     *
     * @param sb   日志拼接器
     * @param info 请求日志信息
     */
    public void appendUrl(StringBuilder sb, CRequestLog info) {

        sb.append(info.getPath());
        val params = info.getParams();
        if (!isQueryMethod(info.getMethod())
            || MapUtil.isEmpty(params)) {
            return;
        }

        sb.append("?");
        appendParams(sb, params);
    }

    /**
     * 拼接单个 header 行：Key: Value
     *
     * @param sb    日志拼接器
     * @param key   请求头名
     * @param value 值
     */
    public void appendHeaderLine(StringBuilder sb, String key, String value) {
        if (StrUtil.isNotEmpty(value)) {
            sb.append("\n");
            sb.append(key);
            sb.append(": ");
            sb.append(value);
        }
    }

    /**
     * 非文本 body 的输出占位符
     */
    public static final String NOT_TEXT_BODY = "[not text body]";

    /**
     * body 字节数组转可打印文本的统一出口：
     * <p>所有请求方式（feign、resttemplate、httpclient 等）统一调用，不再各自实现 body 转换逻辑：
     * 空 body 返回 emptyPlaceholder，文本 body 按 Content-Type 字符集解码，非文本 body 输出占位符</p>
     *
     * @param bodyBytes       body 字节数组
     * @param headers         请求/响应头（用于判断 Content-Type 与字符集）
     * @param emptyPlaceholder 空 body 时返回的占位符（如 EMPTY_REQ/EMPTY_RSP），无需占位可传 null
     * @return 可打印文本；空 body 且 emptyPlaceholder 为 null 时返回 null
     */
    public String getBodyText(
            byte[] bodyBytes,
            Map<String, Collection<String>> headers,
            String emptyPlaceholder
    ) {
        if (ArrayUtil.isEmpty(bodyBytes)) {
            return emptyPlaceholder;
        }
        if (isTextBody(headers)) {
            return new String(bodyBytes, getCharsetOrDefault(headers));
        }
        return NOT_TEXT_BODY;
    }

    /**
     * 拼接 body（Object，经 getPrintAbleString 输出）；null 时不输出
     */
    private void appendBodyObject(StringBuilder sb, Object body) {
        // null 请求体/响应体不输出（避免出现 [null] 占位）；服务端 MVC 无 body 时由 EMPTY_REQ/EMPTY_RSP 占位（非 null）
        if (null == body) {
            return;
        }
        sb.append("\n\n");
        sb.append(CLogUtils.getPrintAbleString(body));
    }

    /**
     * 统一拼接完整 HTTP 请求+响应日志（请求行、请求头、请求体、响应体、耗时、异常、业务数据）。
     * <p>格式要求：请求行、请求头、请求体必须保持标准 HTTP 报文结构连续输出
     * （请求行后紧跟请求头，中间不得插入非 HTTP 内容，否则无法作为 HTTP 客户端/回放格式使用）；
     * 来源标识置于整个日志最前面（独立一行，满足"来源前缀"诉求）；
     * IP、rt、error、业务数据等日志元信息统一置于末尾（不参与 HTTP 报文结构，禁止插入请求行与请求头之间）</p>
     * <p>全部数据直接取自 {@link CRequestLog} 属性，派生逻辑（请求体取 req、响应体可打印转换、
     * 耗时计算、业务数据组装）均在本方法内处理，调用方无需传入任何派生参数</p>
     *
     * @param sb           日志拼接器
     * @param info         请求日志信息（请求行、请求头、params、请求体、响应体、业务数据等）
     * @param enableHeader 是否打印请求头/响应头（打印层开关，采集层总是采集存储）
     */
    public void appendHttpLog(
            StringBuilder sb,
            CRequestLog info,
            boolean enableHeader
    ) {

        // 日志来源标识置于整个日志最前面（独立一行）；请求行等 HTTP 报文结构紧随其后
        val source = info.getSource();
        if (null != source) {
            sb.append('[').append(source.getText()).append("]\n");
        }

        // 请求行
        appendRequestUrl(sb, info);

        // 请求头：开关开启时输出（同一 header 多个值逐行输出）
        if (enableHeader) {
            appendHeaderMap(sb, info.getRequestHeaders());
        }

        // 请求体：POST 且无 body 但 params 有值时，将 params 作为 form-urlencoded body
        appendRequestBody(sb, info);

        // 响应报文头：状态行 + 响应头（与请求侧对称），空行分隔由 appendBodyObject 统一处理
        appendResponseBlock(sb, info, enableHeader);

        // 响应体：可打印处理后输出，未设置时输出占位符
        appendBodyObject(sb, info.getRsp());

        appendError(sb, info.getErrorMessage());

        // 业务数据区（traceId、tenantId、userId、耗时等应用业务数据）：统一以空行与正文分隔，无数据时不追加
        val businessData = getBusinessData(info);
        if (MapUtil.isNotEmpty(businessData)) {
            sb.append("\n\n");
            businessData.forEach((key, value) -> {
                sb.append(key);
                sb.append(": ");
                sb.append(value);
                sb.append("\n");
            });
        }
    }

    /**
     * 组装业务数据（非 HTTP 请求头，仅用于日志末尾展示）：traceId、tenantId、userId、耗时；
     * 有值才放入，耗时以起止时间能否计算判定：未测量不输出（避免无意义的 rt: 0ms 噪音），
     * 真实测量为 0ms 的快速请求仍会输出
     *
     * @param info 请求日志信息
     * @return 业务数据 map
     */
    private Map<String, String> getBusinessData(CRequestLog info) {

        val businessDataMap = new LinkedHashMap<String, String>();
        CMapUtils.put(businessDataMap, CRequestHeaderEnum.X_TRACE_ID.getHeaderName(), info.getTraceId());
        CMapUtils.put(businessDataMap, CRequestHeaderEnum.X_TENANT_ID.getHeaderName(), info.getTenantId());
        CMapUtils.put(businessDataMap, CRequestHeaderEnum.X_USER_ID.getHeaderName(), info.getUserId());
        val rt = getRt(info);
        if (null != rt) {
            businessDataMap.put("rt", rt + "ms");
        }
        return businessDataMap;
    }

    /**
     * 耗时（毫秒）：由起止时间计算；未设置或不可用（end 早于 begin）时返回 null
     *
     * @param info 请求日志信息
     * @return 耗时（毫秒），无法计算时返回 null
     */
    private Long getRt(CRequestLog info) {
        if (info.getBeginTimeMillis() > 0
            && info.getEndTimeMillis() >= info.getBeginTimeMillis()) {
            return info.getEndTimeMillis() - info.getBeginTimeMillis();
        }
        return null;
    }

    /**
     * 拼接请求体：form 方法（POST/PUT/PATCH）时，params 有值先输出 form-urlencoded 段，
     * 再输出请求体（feign 的 @RequestParam 放 params 对象后，即使有 body 也不会丢失）
     *
     * @param sb          日志拼接器
     * @param info        请求基础数据（请求行、请求头、params 等）
     */
    private void appendRequestBody(StringBuilder sb, CRequestLog info) {

        val params = info.getParams();
        if (MapUtil.isNotEmpty(params)) {
            // form body 前补空行，与请求行/请求头分隔，符合 HTTP 报文"headers 与 body 间空行"格式要求；
            // form body 后不加，由 appendBodyObject 统一处理与 req body 间的分隔
            sb.append("\n\n");
            appendParams(sb, params);
        }

        appendBodyObject(sb, info.getReq());
    }

    /**
     * 拼接响应报文头：状态行 + 响应头（与请求侧对称），与响应体间的空行由 appendBodyObject 统一处理。
     * <p>状态码未采集（null）时不输出状态行；响应头为空时不输出响应头，均向后兼容</p>
     *
     * @param sb           日志拼接器
     * @param info         请求日志信息（响应状态码、响应头）
     * @param enableHeader 是否输出响应头（状态行不受开关控制，总是输出）
     */
    private void appendResponseBlock(
            StringBuilder sb,
            CRequestLog info,
            boolean enableHeader
    ) {

        val status = info.getResponseStatus();
        val responseHeaders = info.getResponseHeaders();
        val showStatus = null != status;
        val showHeaders = enableHeader && MapUtil.isNotEmpty(responseHeaders);
        if (!showStatus && !showHeaders) {
            // 无可输出的响应报文头（状态行 + 开关控制的响应头），不输出
            return;
        }
        // 响应报文头与请求体间空行分隔（对称于请求侧 appendBodyObject 的 \n\n）
        sb.append("\n\n");
        if (showStatus) {
            // 状态行：code 描述（如 200 OK）；未知状态码仅输出数字，不输出描述
            sb.append(status);
            val httpStatus = HttpStatus.resolve(status);
            if (null != httpStatus) {
                sb.append(" ").append(httpStatus.getReasonPhrase());
            }
        }
        if (showHeaders) {
            // 响应头首行以状态行后的换行开始，后续行由 appendHeaderLine 前置换行
            appendHeaderMap(sb, responseHeaders);
        }
    }

    /**
     * 拼接 header map：同一 header 多个值逐行输出（请求头与响应头共用）
     *
     * @param sb      日志拼接器
     * @param headers header map（headerName → 一个或多个 headerValue），为空时不输出
     */
    private void appendHeaderMap(StringBuilder sb, Map<String, Collection<String>> headers) {
        if (MapUtil.isEmpty(headers)) {
            return;
        }
        headers.forEach((key, values) -> {
            if (null == values) {
                return;
            }
            values.forEach(value -> appendHeaderLine(sb, key, value));
        });
    }

    /**
     * 拼接 key=value 参数：多个参数用 &amp; 连接，同一 key 多个 value 时重复输出 key（URL query 与 form body 共用）
     *
     * @param sb     日志拼接器
     * @param params 参数 map（参数名 → 一个或多个参数值）
     */
    private void appendParams(StringBuilder sb, Map<String, Collection<String>> params) {
        var first = true;
        for (val entry : params.entrySet()) {
            for (val value : entry.getValue()) {
                if (!first) {
                    sb.append("&");
                }
                sb.append(entry.getKey());
                sb.append("=");
                sb.append(value);
                first = false;
            }
        }
    }

    /**
     * 是否为 query 参数类型方法（GET/DELETE，参数拼到 URL 的 query string）
     */
    private boolean isQueryMethod(String method) {
        if (StrUtil.isEmpty(method)) {
            return false;
        }
        return "GET".equalsIgnoreCase(method)
            || "DELETE".equalsIgnoreCase(method);
    }

    /**
     * 统一拼接异常信息：\n\nerror: message
     *
     * @param sb           日志拼接器
     * @param errorMessage 异常信息
     */
    public void appendError(StringBuilder sb, String errorMessage) {
        if (StrUtil.isNotEmpty(errorMessage)) {
            sb.append("\n\n");
            sb.append("error: ");
            sb.append(errorMessage);
        }
    }

}
