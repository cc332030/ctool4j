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
 */
@UtilityClass
public class CValidateUtils {

    public boolean isNull(Object value) {
        return Objects.isNull(value);
    }
    public boolean isNotNull(Object value) {
        return Objects.nonNull(value);
    }

    public boolean isEmpty(CharSequence value) {
        return StrUtil.isEmpty(value);
    }
    public boolean isNotEmpty(CharSequence value) {
        return StrUtil.isNotEmpty(value);
    }

    public boolean isBlank(CharSequence value) {
        return StrUtil.isBlank(value);
    }
    public boolean isNotBlank(CharSequence value) {
        return StrUtil.isNotBlank(value);
    }

    public boolean isEmpty(Iterable<?> value) {
        return CollUtil.isEmpty(value);
    }
    public boolean isNotEmpty(Iterable<?> value) {
        return CollUtil.isNotEmpty(value);
    }

    public boolean isEmpty(Collection<?> value) {
        return CollUtil.isEmpty(value);
    }
    public boolean isNotEmpty(Collection<?> value) {
        return CollUtil.isNotEmpty(value);
    }

    public boolean isEmpty(Map<?, ?> value) {
        return MapUtil.isEmpty(value);
    }
    public boolean isNotEmpty(Map<?, ?> value) {
        return MapUtil.isNotEmpty(value);
    }

    public boolean isEmpty(byte[] value) {
        return ArrayUtil.isEmpty(value);
    }
    public boolean isNotEmpty(byte[] value) {
        return ArrayUtil.isNotEmpty(value);
    }

    public boolean isEmpty(short[] value) {
        return ArrayUtil.isEmpty(value);
    }
    public boolean isNotEmpty(short[] value) {
        return ArrayUtil.isNotEmpty(value);
    }

    public boolean isEmpty(char[] value) {
        return ArrayUtil.isEmpty(value);
    }
    public boolean isNotEmpty(char[] value) {
        return ArrayUtil.isNotEmpty(value);
    }

    public boolean isEmpty(int[] value) {
        return ArrayUtil.isEmpty(value);
    }
    public boolean isNotEmpty(int[] value) {
        return ArrayUtil.isNotEmpty(value);
    }

    public boolean isEmpty(long[] value) {
        return ArrayUtil.isEmpty(value);
    }
    public boolean isNotEmpty(long[] value) {
        return ArrayUtil.isNotEmpty(value);
    }

    public boolean isEmpty(Object[] value) {
        return ArrayUtil.isEmpty(value);
    }
    public boolean isNotEmpty(Object[] value) {
        return ArrayUtil.isNotEmpty(value);
    }

}
