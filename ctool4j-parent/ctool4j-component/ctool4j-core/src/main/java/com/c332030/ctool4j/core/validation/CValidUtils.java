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
 * Description: CValidUtils：按类型判断值是否有效（isValid）/无效（isNotValid）
 * </p>
 *
 * <p>
 * 基于 {@link CValidateUtils} 支持的数据类型，按类型选择校验逻辑：
 * 字符串按 blank（isNotBlank）、集合/Map/数组按 notEmpty、其他对象按非 null。
 * 调用时传入具体类型变量，由重载自动匹配；传 {@code null} 字面量会因多重载产生歧义，应传入具体类型变量。
 * </p>
 *
 * @since 2026/8/20
 */
@UtilityClass
public class CValidUtils {

    /**
     * 判断对象是否有效（非 null）
     *
     * @param value 对象
     * @return 是否有效
     */
    public boolean isValid(Object value) {
        return Objects.nonNull(value);
    }

    /**
     * 判断对象是否无效（null）
     *
     * @param value 对象
     * @return 是否无效
     */
    public boolean isNotValid(Object value) {
        return Objects.isNull(value);
    }

    /**
     * 判断字符串是否有效（非空且非空白）
     *
     * @param value 字符串
     * @return 是否有效
     */
    public boolean isValid(CharSequence value) {
        return StrUtil.isNotBlank(value);
    }

    /**
     * 判断字符串是否无效（null、空或空白）
     *
     * @param value 字符串
     * @return 是否无效
     */
    public boolean isNotValid(CharSequence value) {
        return StrUtil.isBlank(value);
    }

    /**
     * 判断可迭代对象是否有效（非空）
     *
     * @param value 可迭代对象
     * @return 是否有效
     */
    public boolean isValid(Iterable<?> value) {
        return CollUtil.isNotEmpty(value);
    }

    /**
     * 判断可迭代对象是否无效（null 或空）
     *
     * @param value 可迭代对象
     * @return 是否无效
     */
    public boolean isNotValid(Iterable<?> value) {
        return CollUtil.isEmpty(value);
    }

    /**
     * 判断集合是否有效（非空）
     *
     * @param value 集合
     * @return 是否有效
     */
    public boolean isValid(Collection<?> value) {
        return CollUtil.isNotEmpty(value);
    }

    /**
     * 判断集合是否无效（null 或空）
     *
     * @param value 集合
     * @return 是否无效
     */
    public boolean isNotValid(Collection<?> value) {
        return CollUtil.isEmpty(value);
    }

    /**
     * 判断 Map 是否有效（非空）
     *
     * @param value Map
     * @return 是否有效
     */
    public boolean isValid(Map<?, ?> value) {
        return MapUtil.isNotEmpty(value);
    }

    /**
     * 判断 Map 是否无效（null 或空）
     *
     * @param value Map
     * @return 是否无效
     */
    public boolean isNotValid(Map<?, ?> value) {
        return MapUtil.isEmpty(value);
    }

    /**
     * 判断字节数组是否有效（非空）
     *
     * @param value 字节数组
     * @return 是否有效
     */
    public boolean isValid(byte[] value) {
        return ArrayUtil.isNotEmpty(value);
    }

    /**
     * 判断字节数组是否无效（null 或空）
     *
     * @param value 字节数组
     * @return 是否无效
     */
    public boolean isNotValid(byte[] value) {
        return ArrayUtil.isEmpty(value);
    }

    /**
     * 判断 short 数组是否有效（非空）
     *
     * @param value short 数组
     * @return 是否有效
     */
    public boolean isValid(short[] value) {
        return ArrayUtil.isNotEmpty(value);
    }

    /**
     * 判断 short 数组是否无效（null 或空）
     *
     * @param value short 数组
     * @return 是否无效
     */
    public boolean isNotValid(short[] value) {
        return ArrayUtil.isEmpty(value);
    }

    /**
     * 判断 char 数组是否有效（非空）
     *
     * @param value char 数组
     * @return 是否有效
     */
    public boolean isValid(char[] value) {
        return ArrayUtil.isNotEmpty(value);
    }

    /**
     * 判断 char 数组是否无效（null 或空）
     *
     * @param value char 数组
     * @return 是否无效
     */
    public boolean isNotValid(char[] value) {
        return ArrayUtil.isEmpty(value);
    }

    /**
     * 判断 int 数组是否有效（非空）
     *
     * @param value int 数组
     * @return 是否有效
     */
    public boolean isValid(int[] value) {
        return ArrayUtil.isNotEmpty(value);
    }

    /**
     * 判断 int 数组是否无效（null 或空）
     *
     * @param value int 数组
     * @return 是否无效
     */
    public boolean isNotValid(int[] value) {
        return ArrayUtil.isEmpty(value);
    }

    /**
     * 判断 long 数组是否有效（非空）
     *
     * @param value long 数组
     * @return 是否有效
     */
    public boolean isValid(long[] value) {
        return ArrayUtil.isNotEmpty(value);
    }

    /**
     * 判断 long 数组是否无效（null 或空）
     *
     * @param value long 数组
     * @return 是否无效
     */
    public boolean isNotValid(long[] value) {
        return ArrayUtil.isEmpty(value);
    }

    /**
     * 判断 float 数组是否有效（非空）
     *
     * @param value float 数组
     * @return 是否有效
     */
    public boolean isValid(float[] value) {
        return ArrayUtil.isNotEmpty(value);
    }

    /**
     * 判断 float 数组是否无效（null 或空）
     *
     * @param value float 数组
     * @return 是否无效
     */
    public boolean isNotValid(float[] value) {
        return ArrayUtil.isEmpty(value);
    }

    /**
     * 判断 double 数组是否有效（非空）
     *
     * @param value double 数组
     * @return 是否有效
     */
    public boolean isValid(double[] value) {
        return ArrayUtil.isNotEmpty(value);
    }

    /**
     * 判断 double 数组是否无效（null 或空）
     *
     * @param value double 数组
     * @return 是否无效
     */
    public boolean isNotValid(double[] value) {
        return ArrayUtil.isEmpty(value);
    }

    /**
     * 判断 boolean 数组是否有效（非空）
     *
     * @param value boolean 数组
     * @return 是否有效
     */
    public boolean isValid(boolean[] value) {
        return ArrayUtil.isNotEmpty(value);
    }

    /**
     * 判断 boolean 数组是否无效（null 或空）
     *
     * @param value boolean 数组
     * @return 是否无效
     */
    public boolean isNotValid(boolean[] value) {
        return ArrayUtil.isEmpty(value);
    }

    /**
     * 判断对象数组是否有效（非空）
     *
     * @param value 对象数组
     * @return 是否有效
     */
    public boolean isValid(Object[] value) {
        return ArrayUtil.isNotEmpty(value);
    }

    /**
     * 判断对象数组是否无效（null 或空）
     *
     * @param value 对象数组
     * @return 是否无效
     */
    public boolean isNotValid(Object[] value) {
        return ArrayUtil.isEmpty(value);
    }

}
