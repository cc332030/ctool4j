package com.c332030.ctool4j.core.validation;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * Description: CValidateUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CValidateUtils} 为值判断工具类，提供各类型的空值判断：</p>
 * <ul>
 *   <li>null：{@code isNull} / {@code isNotNull}</li>
 *   <li>字符串：{@code isEmpty} / {@code isNotEmpty} / {@code isBlank} / {@code isNotBlank}</li>
 *   <li>可迭代/集合：{@code isEmpty} / {@code isNotEmpty}</li>
 *   <li>Map：{@code isEmpty} / {@code isNotEmpty}</li>
 *   <li>数组（byte/short/char/int/long/Object）：{@code isEmpty} / {@code isNotEmpty}</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>字符串 null/空串</td>
 *     <td>isEmpty 为 true</td>
 *   </tr>
 *   <tr>
 *     <td>字符串 null/空串/纯空白</td>
 *     <td>isBlank 为 true</td>
 *   </tr>
 *   <tr>
 *     <td>集合/Map/数组 null/空</td>
 *     <td>isEmpty 为 true</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>对值做空值判断的既有代码；新代码建议使用 {@code CValidUtils}。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>本类已废弃，新开发优先使用 {@code CValidUtils}。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>本类标记 {@code @Deprecated}，其按类型判断有效性（isValid）的语义已迁移至 {@code CValidUtils}；</li>
 *   <li>保留本类以兼容既有使用（不删除），但标记废弃引导迁移至 {@code CValidUtils}。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>判断语义</b></p>
 * <ul>
 *   <li>字符串空值：{@code isEmpty} 用 {@code StrUtil.isEmpty}（null 或空串）；{@code isBlank} 用 {@code StrUtil.isBlank}</li>
 *   <li>（null、空串、纯空白）。</li>
 *   <li>集合/Map/数组：{@code isEmpty} 用 {@code CollUtil.isEmpty} / {@code MapUtil.isEmpty} / {@code ArrayUtil.isEmpty}。</li>
 * </ul>
 *
 * @since 2026/1/20
 * @version 1.0
 */
@Deprecated
@UtilityClass
public class CValidateUtils {

    /**
     * 判断是否为 null
     *
     * @param value 值
     * @return 是否为 null
     */
    public boolean isNull(Object value) {
        return Objects.isNull(value);
    }

    /**
     * 判断是否不为 null
     *
     * @param value 值
     * @return 是否不为 null
     */
    public boolean isNotNull(Object value) {
        return Objects.nonNull(value);
    }

    /**
     * 判断字符串是否为空
     *
     * @param value 字符串
     * @return 是否为空
     */
    public boolean isEmpty(CharSequence value) {
        return StrUtil.isEmpty(value);
    }

    /**
     * 判断字符串是否不为空
     *
     * @param value 字符串
     * @return 是否不为空
     */
    public boolean isNotEmpty(CharSequence value) {
        return StrUtil.isNotEmpty(value);
    }

    /**
     * 判断字符串是否为空白
     *
     * @param value 字符串
     * @return 是否为空白
     */
    public boolean isBlank(CharSequence value) {
        return StrUtil.isBlank(value);
    }

    /**
     * 判断字符串是否不为空白
     *
     * @param value 字符串
     * @return 是否不为空白
     */
    public boolean isNotBlank(CharSequence value) {
        return StrUtil.isNotBlank(value);
    }

    /**
     * 判断可迭代对象是否为空
     *
     * @param value 可迭代对象
     * @return 是否为空
     */
    public boolean isEmpty(Iterable<?> value) {
        return CollUtil.isEmpty(value);
    }

    /**
     * 判断可迭代对象是否不为空
     *
     * @param value 可迭代对象
     * @return 是否不为空
     */
    public boolean isNotEmpty(Iterable<?> value) {
        return CollUtil.isNotEmpty(value);
    }

    /**
     * 判断集合是否为空
     *
     * @param value 集合
     * @return 是否为空
     */
    public boolean isEmpty(Collection<?> value) {
        return CollUtil.isEmpty(value);
    }

    /**
     * 判断集合是否不为空
     *
     * @param value 集合
     * @return 是否不为空
     */
    public boolean isNotEmpty(Collection<?> value) {
        return CollUtil.isNotEmpty(value);
    }

    /**
     * 判断 Map 是否为空
     *
     * @param value Map
     * @return 是否为空
     */
    public boolean isEmpty(Map<?, ?> value) {
        return MapUtil.isEmpty(value);
    }

    /**
     * 判断 Map 是否不为空
     *
     * @param value Map
     * @return 是否不为空
     */
    public boolean isNotEmpty(Map<?, ?> value) {
        return MapUtil.isNotEmpty(value);
    }

    /**
     * 判断字节数组是否为空
     *
     * @param value 字节数组
     * @return 是否为空
     */
    public boolean isEmpty(byte[] value) {
        return ArrayUtil.isEmpty(value);
    }

    /**
     * 判断字节数组是否不为空
     *
     * @param value 字节数组
     * @return 是否不为空
     */
    public boolean isNotEmpty(byte[] value) {
        return ArrayUtil.isNotEmpty(value);
    }

    /**
     * 判断 short 数组是否为空
     *
     * @param value short 数组
     * @return 是否为空
     */
    public boolean isEmpty(short[] value) {
        return ArrayUtil.isEmpty(value);
    }

    /**
     * 判断 short 数组是否不为空
     *
     * @param value short 数组
     * @return 是否不为空
     */
    public boolean isNotEmpty(short[] value) {
        return ArrayUtil.isNotEmpty(value);
    }

    /**
     * 判断 char 数组是否为空
     *
     * @param value char 数组
     * @return 是否为空
     */
    public boolean isEmpty(char[] value) {
        return ArrayUtil.isEmpty(value);
    }

    /**
     * 判断 char 数组是否不为空
     *
     * @param value char 数组
     * @return 是否不为空
     */
    public boolean isNotEmpty(char[] value) {
        return ArrayUtil.isNotEmpty(value);
    }

    /**
     * 判断 int 数组是否为空
     *
     * @param value int 数组
     * @return 是否为空
     */
    public boolean isEmpty(int[] value) {
        return ArrayUtil.isEmpty(value);
    }

    /**
     * 判断 int 数组是否不为空
     *
     * @param value int 数组
     * @return 是否不为空
     */
    public boolean isNotEmpty(int[] value) {
        return ArrayUtil.isNotEmpty(value);
    }

    /**
     * 判断 long 数组是否为空
     *
     * @param value long 数组
     * @return 是否为空
     */
    public boolean isEmpty(long[] value) {
        return ArrayUtil.isEmpty(value);
    }

    /**
     * 判断 long 数组是否不为空
     *
     * @param value long 数组
     * @return 是否不为空
     */
    public boolean isNotEmpty(long[] value) {
        return ArrayUtil.isNotEmpty(value);
    }

    /**
     * 判断对象数组是否为空
     *
     * @param value 对象数组
     * @return 是否为空
     */
    public boolean isEmpty(Object[] value) {
        return ArrayUtil.isEmpty(value);
    }

    /**
     * 判断对象数组是否不为空
     *
     * @param value 对象数组
     * @return 是否不为空
     */
    public boolean isNotEmpty(Object[] value) {
        return ArrayUtil.isNotEmpty(value);
    }

}
