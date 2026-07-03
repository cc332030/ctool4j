package com.c332030.ctool4j.core.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
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
    public void appendUrl(StringBuilder sb, String path, Map<String, String[]> params) {
        sb.append(path);
        if (null == params || params.isEmpty()) {
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

}
