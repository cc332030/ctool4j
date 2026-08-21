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
 * @since 2026/1/20
 * @see doc/design/core/CValidateUtils.adoc
 * @see doc/design/core/CValidateUtilsTests.adoc
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
