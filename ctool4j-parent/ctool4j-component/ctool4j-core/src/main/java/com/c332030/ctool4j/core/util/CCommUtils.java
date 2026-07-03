package com.c332030.ctool4j.core.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.interfaces.ICRequestHeader;
import com.c332030.ctool4j.core.interfaces.IHttpLogInfo;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CommUtils
 * </p>
 *
 * @since 2025/3/15
 */
@UtilityClass
public class CCommUtils {

    public void contextTypeForm(HttpHeaders headers) {
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    }

    public void contextTypeJson(HttpHeaders headers) {
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    public void acceptJson(HttpHeaders headers) {
        headers.setAccept(CList.of(MediaType.APPLICATION_JSON));
    }

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

    public String getFullHeaderStr(Map<String, Collection<String>> headers) {

        if(MapUtil.isEmpty(headers)) {
            return null;
        }

        return getFullHeaderStr(headers, entry -> true);
    }

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

    public boolean isTextBody(Map<String, Collection<String>> headers) {

        val contentTypes = CCollUtils.defaultEmpty(headers.get(HttpHeaders.CONTENT_TYPE));
        return contentTypes.stream().anyMatch(CMediaTypeUtils::isText);
    }

    /**
     * 拼接请求行：METHOD path[?params]，仅 GET 请求拼接查询参数，
     * POST/PUT 等请求的参数在 body 中，不拼到 URL
     */
    public void appendRequestUrl(StringBuilder sb, IHttpLogInfo info) {

        val method = info.getMethod();
        sb.append("\n");
        sb.append(method);
        sb.append(" ");
        appendUrl(sb, info);
    }

    /**
     * 拼接请求行：METHOD URL
     */
    public void appendRequestLine(StringBuilder sb, String method, String url) {
        sb.append("\n");
        sb.append(method);
        sb.append(" ");
        sb.append(url);
    }

    /**
     * 拼接 URL 路径 + Query 参数
     */
    public void appendUrl(StringBuilder sb, IHttpLogInfo info) {

        sb.append(info.getPath());
        val params = info.getParams();
        if (!HttpMethod.GET.name().equalsIgnoreCase(info.getMethod())
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
     */
    public void appendHeaderLine(StringBuilder sb, ICRequestHeader requestHeader, String value) {
        appendHeaderLine(sb, requestHeader.getHeaderName(), value);
    }

    /**
     * 拼接单个 header 行：Key: Value
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
     */
    public void appendHeaderBlock(StringBuilder sb, Map<String, Collection<String>> headers) {
        val headerStr = getFullHeaderStr(headers);
        if (StrUtil.isNotEmpty(headerStr)) {
            sb.append("\n");
            sb.append(headerStr);
        }
    }

    /**
     * 拼接 body（byte[]，根据 Content-Type 判断是否文本）
     */
    public void appendBody(
            StringBuilder sb,
            byte[] bodyBytes,
            Map<String, Collection<String>> headers,
            String type
    ) {
        sb.append("\n\n");
        if (ArrayUtil.isEmpty(bodyBytes)) {
            sb.append("[no ");
            sb.append(type);
            sb.append(" body]");
        } else if (isTextBody(headers)) {
            sb.append(new String(bodyBytes, StandardCharsets.UTF_8));
        } else {
            sb.append("[not ");
            sb.append(type);
            sb.append(" text body]");
        }
    }

    /**
     * 拼接 body（Object，直接 toString）
     */
    public void appendBody(StringBuilder sb, Object body, String type) {
        sb.append("\n\n");
        if (null == body) {
            sb.append("[no ");
            sb.append(type);
            sb.append(" body]");
        } else {
            sb.append(body);
        }
    }

    /**
     * 统一拼接完整 HTTP 请求+响应日志（请求行、请求头、请求体、响应体、耗时、异常）
     */
    public void appendHttpLog(StringBuilder sb, IHttpLogInfo info) {

        // 请求行
        appendRequestUrl(sb, info);

        // 请求头
        val headers = info.getHeaders();
        if (MapUtil.isNotEmpty(headers)) {
            headers.forEach((key, value) -> appendHeaderLine(sb, key, value));
        }

        appendBody(sb, info.getRequestBody(), "request");
        appendBody(sb, info.getResponseBody(), "response");

        appendError(sb, info.getErrorMessage());

        // 耗时
        val rt = info.getRt();
        if (null != rt) {
            sb.append("\n\n");
            sb.append("rt: ");
            sb.append(rt);
            sb.append("ms");
        }
    }

    /**
     * 统一拼接异常信息：\n\nerror: message
     */
    public void appendError(StringBuilder sb, String errorMessage) {
        if (StrUtil.isNotEmpty(errorMessage)) {
            sb.append("\n\n");
            sb.append("error: ");
            sb.append(errorMessage);
        }
    }

}
