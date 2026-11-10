package com.c332030.ctool4j.core.validation;

import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Map;

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
 *   <li>保留本类以兼容既有使用（不删除），但标记废弃引导迁移至 {@code CValidUtils}；</li>
 *   <li>除字符串 {@code isEmpty}/{@code isNotEmpty}（「null 或空串」）外，全部方法均<b>委托</b> {@code CValidUtils}，
 *   不再持有第二份判定实现——修判定语义只需改一处。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>判断语义</b></p>
 * <ul>
 *   <li>字符串：{@code isEmpty}/{@code isNotEmpty} 用 {@code StrUtil}（null 或空串，纯空白串不算空，
 *   与 {@code CValidUtils} 的 blank 口径不同，故保留独立实现）；{@code isBlank}/{@code isNotBlank}
 *   委托 {@code CValidUtils.isNotValid} / {@code isValid}（null、空串、纯空白）。</li>
 *   <li>集合/Map/数组：{@code isEmpty}/{@code isNotEmpty} 委托 {@code CValidUtils.isNotValid} / {@code isValid}。</li>
 *   <li>null 判定：{@code isNull}/{@code isNotNull} 委托 {@code CValidUtils.isNotValid} / {@code isValid}。</li>
 * </ul>
 *
 * @since 2026/1/20
 * @version 1.1
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
        return CValidUtils.isNotValid(value);
    }

    /**
     * 判断是否不为 null
     *
     * @param value 值
     * @return 是否不为 null
     */
    public boolean isNotNull(Object value) {
        return CValidUtils.isValid(value);
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
        return CValidUtils.isNotValid(value);
    }

    /**
     * 判断字符串是否不为空白
     *
     * @param value 字符串
     * @return 是否不为空白
     */
    public boolean isNotBlank(CharSequence value) {
        return CValidUtils.isValid(value);
    }

    /**
     * 判断可迭代对象是否为空
     *
     * @param value 可迭代对象
     * @return 是否为空
     */
    public boolean isEmpty(Iterable<?> value) {
        return CValidUtils.isNotValid(value);
    }

    /**
     * 判断可迭代对象是否不为空
     *
     * @param value 可迭代对象
     * @return 是否不为空
     */
    public boolean isNotEmpty(Iterable<?> value) {
        return CValidUtils.isValid(value);
    }

    /**
     * 判断集合是否为空
     *
     * @param value 集合
     * @return 是否为空
     */
    public boolean isEmpty(Collection<?> value) {
        return CValidUtils.isNotValid(value);
    }

    /**
     * 判断集合是否不为空
     *
     * @param value 集合
     * @return 是否不为空
     */
    public boolean isNotEmpty(Collection<?> value) {
        return CValidUtils.isValid(value);
    }

    /**
     * 判断 Map 是否为空
     *
     * @param value Map
     * @return 是否为空
     */
    public boolean isEmpty(Map<?, ?> value) {
        return CValidUtils.isNotValid(value);
    }

    /**
     * 判断 Map 是否不为空
     *
     * @param value Map
     * @return 是否不为空
     */
    public boolean isNotEmpty(Map<?, ?> value) {
        return CValidUtils.isValid(value);
    }

    /**
     * 判断字节数组是否为空
     *
     * @param value 字节数组
     * @return 是否为空
     */
    public boolean isEmpty(byte[] value) {
        return CValidUtils.isNotValid(value);
    }

    /**
     * 判断字节数组是否不为空
     *
     * @param value 字节数组
     * @return 是否不为空
     */
    public boolean isNotEmpty(byte[] value) {
        return CValidUtils.isValid(value);
    }

    /**
     * 判断 short 数组是否为空
     *
     * @param value short 数组
     * @return 是否为空
     */
    public boolean isEmpty(short[] value) {
        return CValidUtils.isNotValid(value);
    }

    /**
     * 判断 short 数组是否不为空
     *
     * @param value short 数组
     * @return 是否不为空
     */
    public boolean isNotEmpty(short[] value) {
        return CValidUtils.isValid(value);
    }

    /**
     * 判断 char 数组是否为空
     *
     * @param value char 数组
     * @return 是否为空
     */
    public boolean isEmpty(char[] value) {
        return CValidUtils.isNotValid(value);
    }

    /**
     * 判断 char 数组是否不为空
     *
     * @param value char 数组
     * @return 是否不为空
     */
    public boolean isNotEmpty(char[] value) {
        return CValidUtils.isValid(value);
    }

    /**
     * 判断 int 数组是否为空
     *
     * @param value int 数组
     * @return 是否为空
     */
    public boolean isEmpty(int[] value) {
        return CValidUtils.isNotValid(value);
    }

    /**
     * 判断 int 数组是否不为空
     *
     * @param value int 数组
     * @return 是否不为空
     */
    public boolean isNotEmpty(int[] value) {
        return CValidUtils.isValid(value);
    }

    /**
     * 判断 long 数组是否为空
     *
     * @param value long 数组
     * @return 是否为空
     */
    public boolean isEmpty(long[] value) {
        return CValidUtils.isNotValid(value);
    }

    /**
     * 判断 long 数组是否不为空
     *
     * @param value long 数组
     * @return 是否不为空
     */
    public boolean isNotEmpty(long[] value) {
        return CValidUtils.isValid(value);
    }

    /**
     * 判断对象数组是否为空
     *
     * @param value 对象数组
     * @return 是否为空
     */
    public boolean isEmpty(Object[] value) {
        return CValidUtils.isNotValid(value);
    }

    /**
     * 判断对象数组是否不为空
     *
     * @param value 对象数组
     * @return 是否不为空
     */
    public boolean isNotEmpty(Object[] value) {
        return CValidUtils.isValid(value);
    }

}
