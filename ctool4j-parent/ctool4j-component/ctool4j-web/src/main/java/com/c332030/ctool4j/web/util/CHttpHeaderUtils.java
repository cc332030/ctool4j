package com.c332030.ctool4j.web.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CCharsets;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CMediaTypeUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CHttpHeaderUtils，HTTP 请求头与内容协商工具。
 * </p>
 *
 * <p>
 * 从 {@link CCommUtils} 拆出的头处理能力：Content-Type / Accept 设置、请求头按行渲染、body 是否文本、
 * Content-Type 字符集解析。与原实现逐字节等价，{@code CCommUtils} 保留同名方法作兼容委托。
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>Content-Type / Accept：{@code contextTypeForm}、{@code contextTypeJson}、{@code acceptJson}。</li>
 *   <li>请求头渲染：{@code getFullHeaderStr}（全量 / 按谓词过滤），每个 header 一行、多值逗号连接。</li>
 *   <li>内容协商：{@code isTextBody}（是否文本 body）、{@code getCharsetOrDefault}（charset 解析，缺省 UTF-8）。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>headers 为 null / 空</td>
 *     <td>{@code getFullHeaderStr} 返回 null；{@code isTextBody} 返回 false；{@code getCharsetOrDefault} 返回 UTF-8</td>
 *   </tr>
 *   <tr>
 *     <td>Content-Type 无法解析</td>
 *     <td>忽略该值，继续尝试下一个；全部失败返回 UTF-8</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要成对设置请求头（JSON / 表单）、渲染请求头、按 Content-Type 判定 body 可打印性的场景（web / feign）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不做 body 解码：字节 → 文本的转换与占位符策略在 {@link CCommUtils#getBodyText}。</li>
 *   <li>不做请求头发送：只构造/渲染，发送由具体客户端负责。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>{@code getFullHeaderStr} 返回的字符串以 {@code \n} 连接、不加首尾换行，调用方自行决定前后分隔。</li>
 *   <li>按谓词过滤的版本不过滤值：谓词只作用于 header 条目（名称 + 值集合）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>无状态</b>：全部方法为纯函数式工具，不持有配置，可被任意客户端复用。</li>
 *   <li><b>可打印性判定单点</b>：{@code isTextBody} 是"是否文本 body"的唯一判定点，避免各客户端各写一套。</li>
 * </ul>
 *
 * @since 2026/11/10
 * @version 1.0
 */
@UtilityClass
public class CHttpHeaderUtils {

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
}
