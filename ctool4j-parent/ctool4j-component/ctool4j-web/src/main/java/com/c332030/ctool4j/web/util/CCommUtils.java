package com.c332030.ctool4j.web.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.interfaces.ICRequestHeader;
import com.c332030.ctool4j.core.interfaces.IHttpLogInfo;
import com.c332030.ctool4j.core.log.CLogUtils;
import com.c332030.ctool4j.core.util.*;
import com.c332030.ctool4j.web.enums.CRequestHeaderEnum;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CCommUtils
 * </p>
 *
 * @since 2025/3/15
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
     * 拼接完整 HTTP 报文（请求行、请求头、请求体、响应体）
     *
     * @param method       请求方法
     * @param url          请求地址
     * @param headers      请求头
     * @param requestBody  请求体
     * @param responseBody 响应体
     * @return HTTP 报文
     */
    public String getFullHttp(
            HttpMethod method,
            String url, Map<String, Collection<String>> headers,
            Object requestBody, Object responseBody
    ) {

        val headerStr = getFullHeaderStr(headers);
        return method + " " + url
                + (StrUtil.isEmpty(headerStr) ? "" : "\n" + headerStr)
                + (Objects.isNull(requestBody) ? "" : "\n\n" + requestBody)
                + (Objects.isNull(responseBody) ? "" : "\n\n" + responseBody)
                ;
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
    public void appendRequestUrl(StringBuilder sb, IHttpLogInfo info) {

        val method = info.getMethod();
        sb.append(method);
        sb.append(" ");
        appendUrl(sb, info);
    }

    /**
     * 拼接请求行：METHOD URL
     *
     * @param sb     日志拼接器
     * @param method HTTP 方法
     * @param url    URL
     */
    public void appendRequestLine(StringBuilder sb, String method, String url) {
        sb.append(method);
        sb.append(" ");
        sb.append(url);
    }

    /**
     * 拼接 URL 路径 + Query 参数
     *
     * @param sb   日志拼接器
     * @param info 请求日志信息
     */
    public void appendUrl(StringBuilder sb, IHttpLogInfo info) {

        sb.append(info.getPath());
        val params = info.getParams();
        if (!isQueryMethod(info.getMethod())
            || MapUtil.isEmpty(params)) {
            return;
        }

        sb.append("?");
        boolean first = true;
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
     * 拼接单个 header 行：Key: Value
     *
     * @param sb            日志拼接器
     * @param requestHeader 请求头枚举
     * @param value         值
     */
    public void appendHeaderLine(StringBuilder sb, ICRequestHeader requestHeader, String value) {
        appendHeaderLine(sb, requestHeader.getHeaderName(), value);
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
     * 拼接完整的 header 块
     *
     * @param sb      日志拼接器
     * @param headers 请求头 map
     */
    public void appendHeaderBlock(StringBuilder sb, Map<String, Collection<String>> headers) {
        val headerStr = getFullHeaderStr(headers);
        if (StrUtil.isNotEmpty(headerStr)) {
            sb.append("\n");
            sb.append(headerStr);
        }
    }

    /**
     * 拼接 body（byte[]，根据 Content-Type 判断是否文本），无 body 时不打印
     *
     * @param sb        日志拼接器
     * @param bodyBytes body 字节数组
     * @param headers   请求头 map（用于判断 Content-Type）
     */
    public void appendBody(
            StringBuilder sb,
            byte[] bodyBytes,
            Map<String, Collection<String>> headers
    ) {
        if (ArrayUtil.isEmpty(bodyBytes)) {
            return;
        }
        sb.append("\n\n");
        if (isTextBody(headers)) {
            sb.append(new String(bodyBytes, getCharsetOrDefault(headers)));
        } else {
            sb.append("[not text body]");
        }
    }

    /**
     * 拼接 body（Object，直接 toString），无 body 时不打印
     */
    private void appendBodyObject(StringBuilder sb, Object body) {
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
     * IP、rt、error、业务数据等日志元信息统一置于末尾（不参与 HTTP 报文结构，禁止插入请求行与请求头之间）</p>
     * <p>全部数据直接取自 {@link IHttpLogInfo} 属性，派生逻辑（请求体取 req、响应体可打印转换、
     * 耗时计算、业务数据组装）均在本方法内处理，调用方无需传入任何派生参数</p>
     *
     * @param sb   日志拼接器
     * @param info 请求日志信息（请求行、请求头、params、请求体、响应体、业务数据等）
     */
    public void appendHttpLog(StringBuilder sb, IHttpLogInfo info) {

        // 请求行
        appendRequestUrl(sb, info);

        // 请求头：同一 header 多个值时逐行输出
        val headers = info.getHeaders();
        if (MapUtil.isNotEmpty(headers)) {
            headers.forEach((key, values) -> {
                if (null == values) {
                    return;
                }
                values.forEach(value -> appendHeaderLine(sb, key, value));
            });
        }

        // 请求体：POST 且无 body 但 params 有值时，将 params 作为 form-urlencoded body
        appendRequestBody(sb, info, info.getReq());

        // 响应体：可打印处理后输出，未设置时输出占位符
        val responseBody = null == info.getRsp() ? null : CLogUtils.getPrintAble(info.getRsp());
        if (null == responseBody) {
            sb.append("\n\n[no response body]");
        } else {
            appendBodyObject(sb, responseBody);
        }

        appendError(sb, info.getErrorMessage());

        // 业务数据区（IP、耗时、token 等应用业务数据）：统一以空行与正文分隔，无数据时不追加
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
     * 组装业务数据（非 HTTP 请求头，仅用于日志末尾展示）：token、traceId、tenantId、userId、ip、耗时；
     * 有值才放入，耗时以起止时间能否计算判定：未测量不输出（避免无意义的 rt: 0ms 噪音），
     * 真实测量为 0ms 的快速请求仍会输出
     *
     * @param info 请求日志信息
     * @return 业务数据 map
     */
    private Map<String, String> getBusinessData(IHttpLogInfo info) {

        val businessDataMap = new LinkedHashMap<String, String>();
        CMapUtils.put(businessDataMap, HttpHeaders.AUTHORIZATION, info.getToken());
        CMapUtils.put(businessDataMap, CRequestHeaderEnum.X_TRACE_ID.getHeaderName(), info.getTraceId());
        CMapUtils.put(businessDataMap, CRequestHeaderEnum.X_TENANT_ID.getHeaderName(), info.getTenantId());
        CMapUtils.put(businessDataMap, CRequestHeaderEnum.X_USER_ID.getHeaderName(), info.getUserId());
        CMapUtils.put(businessDataMap, "ip", info.getIp());
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
    private Long getRt(IHttpLogInfo info) {
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
     * @param requestBody 请求体（已可打印处理）
     */
    private void appendRequestBody(StringBuilder sb, IHttpLogInfo info, Object requestBody) {
        if (isFormBodyMethod(info.getMethod())) {
            val params = info.getParams();
            if (MapUtil.isNotEmpty(params)) {
                sb.append("\n\n");
                appendFormBody(sb, params);
            }
        }
        appendBodyObject(sb, requestBody);
    }

    /**
     * 拼接 form-urlencoded body
     */
    private void appendFormBody(StringBuilder sb, Map<String, Collection<String>> params) {
        boolean first = true;
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
     * 是否为 form body 类型方法（POST/PUT/PATCH，参数在 body 中）
     */
    private boolean isFormBodyMethod(String method) {
        if (StrUtil.isEmpty(method)) {
            return false;
        }
        return !"GET".equalsIgnoreCase(method)
            && !"DELETE".equalsIgnoreCase(method);
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
