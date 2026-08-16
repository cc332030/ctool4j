package com.c332030.ctool4j.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CUrlUtils
 * </p>
 *
 * @since 2024/12/2
 */
@UtilityClass
public class CUrlUtils {

    /**
     * http/https 协议起始匹配
     */
    private static final Pattern URL_START = Pattern.compile("https?://");

    /**
     * 字符串转 URI
     *
     * @param uri URI 字符串
     * @return URI
     */
    @SneakyThrows
    public URI getURI(String uri) {
        return new URI(uri);
    }

    /**
     * 获取 URI 字符串的协议
     *
     * @param uri URI 字符串
     * @return 协议
     */
    public String getScheme(String uri) {
        return getScheme(getURI(uri));
    }

    /**
     * 获取 URI 的协议
     *
     * @param uri URI
     * @return 协议
     */
    public String getScheme(URI uri) {
        return uri.getScheme();
    }

    /**
     * 获取 URI 字符串的主机
     *
     * @param uri URI 字符串
     * @return 主机
     */
    public String getHost(String uri) {
        return getHost(getURI(uri));
    }

    /**
     * 获取 URI 的主机
     *
     * @param uri URI
     * @return 主机
     */
    public String getHost(URI uri) {
        return uri.getHost();
    }

    /**
     * 获取 URI 字符串的端口
     *
     * @param uri URI 字符串
     * @return 端口，未指定时返回 null
     */
    public Integer getPort(String uri) {
        return getPort(getURI(uri));
    }

    /**
     * 获取 URI 的端口
     *
     * @param uri URI
     * @return 端口，未指定时返回 null
     */
    public Integer getPort(URI uri) {
        val port = uri.getPort();
        return port == -1 ? null : port;
    }

    /**
     * 获取主机及端口
     *
     * @param uriStr URI 字符串
     * @return 主机:端口，无端口时仅返回主机
     */
    public String getHostWithPort(String uriStr) {

        val uri = getURI(uriStr);

        val host = getHost(uri);
        val port = getPort(uri);
        return port == null ? host : host + ":" + port;
    }

    /**
     * 从 URL 中获取指定参数值
     *
     * @param url       URL
     * @param paramName 参数名
     * @return 参数值，不存在时返回 null
     */
    public String getParam(String url, String paramName) {
        return getParam(getParamMap(url), paramName);
    }

    /**
     * 从参数 Map 中获取参数值
     *
     * @param paramMap  参数 Map
     * @param paramName 参数名
     * @return 参数值，不存在时返回 null
     */
    public String getParam(Map<String, String> paramMap, String paramName) {
        return paramMap.getOrDefault(paramName, null);
    }

    /**
     * 从 URL 中获取多个参数值
     *
     * @param url        URL
     * @param paramNames 参数名集合
     * @return 参数名到参数值的 Map
     */
    public Map<String, String> getParams(String url, Collection<String> paramNames) {
        return getParams(getParamMap(url), paramNames);
    }

    /**
     * 从参数 Map 中获取多个参数值（过滤空白值）
     *
     * @param paramMap   参数 Map
     * @param paramNames 参数名集合
     * @return 参数名到参数值的 Map，参数 Map 为空时返回空 Map
     */
    public Map<String, String> getParams(Map<String, String> paramMap, Collection<String> paramNames) {

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

    /**
     * 解析 URL 的查询参数为 Map
     *
     * @param url URL
     * @return 参数名到参数值的不可变 Map，URL 为空或无法解析时返回空 Map
     */
    public Map<String, String> getParamMap(String url) {

        if(StrUtil.isEmpty(url)) {
            return Collections.emptyMap();
        }

        // 未解码前剥离 fragment（# 之后的部分），# 编码为 %23 不受影响
        val noFragment = url.split("#", 2)[0];

        val urlArr = noFragment.split("\\?");
        if(urlArr.length < 2) {
            return Collections.emptyMap();
        }

        val paramsArr = urlArr[1].split("&");
        val map = new LinkedHashMap<String, String>(paramsArr.length);
        for (String param : paramsArr) {

            // 按第一个 = 分割，值中含 = 时保留完整
            val paramArr = param.split("=", 2);
            if(paramArr.length < 2) {
                continue;
            }
            CMapUtils.put(map, decode(paramArr[0]), decode(paramArr[1]));
        }

        return map;
    }

    /**
     * URL 解码
     *
     * @param value 编码后的字符串
     * @return 解码后的字符串，为空时返回 null
     */
    @SneakyThrows
    public String decode(String value) {
        if(StrUtil.isEmpty(value)) {
            return null;
        }
        return URLDecoder.decode(value, CCharsets.UTF_8.name());
    }

    /**
     * 提取 URL 中 http 协议开始的部分
     *
     * @param url URL
     * @return http(s) 协议开始的 URL，为空或不含协议时返回 null
     */
    public String getUrl(String url) {
        if(StrUtil.isEmpty(url)) {
            return null;
        }
        val matcher = URL_START.matcher(url);
        if(!matcher.find()) {
            return null;
        }
        return StrUtil.sub(url, matcher.start(), url.length());
    }

    /**
     * 获取 URL 路径
     *
     * @param url URL
     * @return 路径，无法解析时返回 null
     */
    @SneakyThrows
    public String getPath(String url) {
        url = getUrl(url);
        if(StrUtil.isEmpty(url)) {
            return null;
        }
        return new URL(url).getPath();
    }

    /**
     * 将 URL 路径按斜杠拆分为路径片段
     *
     * @param urlStr URL
     * @return 路径片段 List，无路径时返回空 List
     */
    public List<String> splitToPath(String urlStr) {

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

    /**
     * 获取 URL 路径第一个片段
     *
     * @param url URL
     * @return 第一个路径片段，无路径时返回 null
     */
    public String firstPath(String url) {
        return CCollUtils.first(splitToPath(url));
    }

    /**
     * 获取 URL 路径最后一个片段
     *
     * @param url URL
     * @return 最后一个路径片段，无路径时返回 null
     */
    public String lastPath(String url) {
        return CCollUtils.last(splitToPath(url));
    }

    /**
     * 替换 URL 域名
     *
     * @param url       URL
     * @param newDomain 新域名
     * @return 替换后的 URL，URL 为空时返回 null，新域名为空时原样返回
     */
    public String replaceDomain(String url, String newDomain) {

        if(StrUtil.isEmpty(url)) {
            return null;
        }

        if(StrUtil.isEmpty(newDomain)) {
            return url;
        }

        val uri = getURI(url);
        val path = StrUtil.nullToEmpty(uri.getRawPath());
        val query = StrUtil.isNotEmpty(uri.getRawQuery()) ? "?" + uri.getRawQuery() : "";
        val fragment = StrUtil.isNotEmpty(uri.getRawFragment()) ? "#" + uri.getRawFragment() : "";
        return newDomain + path + query + fragment;
    }

}
