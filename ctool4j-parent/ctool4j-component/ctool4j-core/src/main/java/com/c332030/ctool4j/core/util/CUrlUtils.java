package com.c332030.ctool4j.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CUrlUtils
 * </p>
 *
 * @since 2024/12/2
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CUrlUtils {

    public static String getParam(String url, String paramName) {
        return getParam(getParamMap(url), paramName);
    }

    public static String getParam(Map<String, String> paramMap, String paramName) {
        return paramMap.getOrDefault(paramName, null);
    }

    public static Map<String, String> getParams(String url, Collection<String> paramNames) {
        return getParams(getParamMap(url), paramNames);
    }

    public static Map<String, String> getParams(Map<String, String> paramMap, Collection<String> paramNames) {

        if(MapUtil.isEmpty(paramMap)
                || CollUtil.isEmpty(paramNames = CCollUtils.filterString(paramNames))
        ) {
            return Collections.emptyMap();
        }

        val newMap = new HashMap<String, String>(paramNames.size());
        paramNames.forEach(paramName -> {

            val value = paramMap.get(paramName);
            if(StrUtil.isNotEmpty(value)) {
                newMap.put(paramName, value);
            }
        });

        return newMap;
    }

    public static Map<String, String> getParamMap(String url) {

        if(StrUtil.isEmpty(url)) {
            return Collections.emptyMap();
        }

        val urlArr = url.split("\\?");
        if(urlArr.length < 2) {
            return Collections.emptyMap();
        }

        val paramsArr = urlArr[1].split("&");
        val map = new LinkedHashMap<String, String>(paramsArr.length);
        for (String param : paramsArr) {

            val paramArr = param.split("=");
            if(paramArr.length < 2) {
                continue;
            }
            CMapUtils.put(map, decode(paramArr[0]), decode(paramArr[1]));
        }

        return map;
    }

    @SneakyThrows
    public static String decode(String value) {
        if(StrUtil.isEmpty(value)) {
            return null;
        }
        return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
    }

    public static String getUrl(String url) {
        if(StrUtil.isEmpty(url)) {
            return null;
        }
        return url.substring(url.indexOf("http"));
    }

    @SneakyThrows
    public static String getPath(String url) {
        url = getUrl(url);
        if(StrUtil.isEmpty(url)) {
            return null;
        }
        return new URL(url).getPath();
    }

    public static List<String> splitToPath(String urlStr) {

        val path = getPath(urlStr);
        if(StrUtil.isEmpty(path)) {
            return CList.of();
        }

        val urlSplit = path.split("\\?");
        val paths = urlSplit[0].split("/");

        return Arrays.stream(paths)
                .map(CStrUtils::toAvailable)
                .filter(StrUtil::isNotEmpty)
                .collect(Collectors.toList());
    }

    public static String firstPath(String url) {
        return CCollUtils.first(splitToPath(url));
    }

    public static String lastPath(String url) {
        return CCollUtils.last(splitToPath(url));
    }

}
