package com.c332030.ctool4j.core.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

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
                + (StringUtils.isEmpty(headerStr) ? "" : "\n" + headerStr)
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

}
