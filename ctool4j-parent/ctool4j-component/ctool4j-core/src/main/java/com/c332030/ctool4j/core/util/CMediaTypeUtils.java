package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.definition.enums.CMimeTypeEnum;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.MediaType;

import java.util.*;

/**
 * <p>
 * Description: CMediaTypeUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CMediaTypeUtils} 为媒体类型（MediaType）工具类，提供：</p>
 * <ul>
 *   <li>{@code getSetWithJson5(Collection)} / {@code getListWithJson5(Collection)}：在媒体类型集合/列表中加入 JSON5</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>isText 空白/null</td>
 *     <td>返回 false</td>
 *   </tr>
 *   <tr>
 *     <td>getSetWithJson5 入参为空集合</td>
 *     <td>返回仅含 JSON5 的集合</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>判断请求/响应媒体类型是否为可读文本（日志脱敏、展示等）。</li>
 *   <li>需要支持 JSON5 媒体类型的接口配置（在既有媒体类型基础上追加）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>isText 基于关键字段匹配，不解析完整 MIME 语法；对带 charset 参数的类型通过子串含 {@code /key} 或</li>
 *   <li>{@code key/} 匹配（如 {@code text/html; charset=utf-8} 命中 {@code text/} 前缀段）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>关键字匹配为启发式（段匹配），无法覆盖所有文本类型，但覆盖常见文本媒体类型；非文本类型</li>
 *   <li>（如 {@code image/*}、{@code application/pdf}）不会被误判为文本。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>JSON5 追加</b></p>
 * <ul>
 *   <li>{@code getSetWithJson5} 以 {@code LinkedHashSet} 保持原有顺序并追加 {@code CMimeTypeEnum.JSON5} 的 MimeType。</li>
 *   <li>{@code getListWithJson5} 基于 set 结果转为 List（JSON5 位于末尾）。</li>
 * </ul>
 *
 * @since 2025/9/21
 * @version 1.0
 */
@UtilityClass
public class CMediaTypeUtils {

    /**
     * 文本类型关键字集合
     */
    public static final Set<String> TEXT_KEYS = CSet.of(
            "text",
            "plain",
            "html",
            "json",
            "xml",
            "form"
    );

    /**
     * 判断媒体类型是否为文本类型
     *
     * <h2>isText 判断规则</h2>
     * <ul>
     *   <li>空白/null 入参返回 false。</li>
     *   <li>入参转小写后，与关键字集合 {@code TEXT_KEYS}（text/plain/html/json/xml/form）按段匹配：</li>
     *   <li>完整匹配、以 {@code key/} 开头、含 {@code /key}、含 {@code +key}（结构化后缀语法）、含 {@code -key}。</li>
     *   <li>段匹配避免子串误匹配（如 {@code uniform} 含 {@code form} 但非文本类型）——关键字必须被 {@code /}、{@code +}、{@code -}</li>
     *   <li>分隔才是有效段。</li>
     * </ul>
     * <ul>
     *   <li>{@code isText(String)}：判断媒体类型字符串是否为文本类型</li>
     * </ul>
     *
     * @param mediaType 媒体类型字符串
     * @return 是否为文本类型，mediaType 为空白时为 false*/
    public boolean isText(String mediaType) {
        if (StrUtil.isBlank(mediaType)) {
            return false;
        }
        val lower = mediaType.toLowerCase();
        for (val key : TEXT_KEYS) {
            // 完整匹配或按 / + - 分隔的段匹配，避免子串误匹配（如 uniform 含 form）
            if (lower.equals(key)
                || lower.startsWith(key + "/")
                || lower.contains("/" + key)
                || lower.contains("+" + key)
                || lower.contains("-" + key)
            ) {
                return true;
            }
        }
        return false;
    }

    /**
     * 在媒体类型集合中加入 JSON5
     *
     * @param mediaTypes 媒体类型集合
     * @return 加入 JSON5 后的有序集合
     */
    public Set<MediaType> getSetWithJson5(Collection<MediaType> mediaTypes) {

        val set = new LinkedHashSet<>(mediaTypes);
        set.add(CMimeTypeEnum.JSON5.getMimeType());
        return set;
    }

    /**
     * 在媒体类型集合中加入 JSON5，返回列表
     *
     * @param mediaTypes 媒体类型集合
     * @return 加入 JSON5 后的列表
     */
    public List<MediaType> getListWithJson5(Collection<MediaType> mediaTypes) {
        return new ArrayList<>(getSetWithJson5(mediaTypes));
    }

}
