package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.validation.CValidateUtils;
import com.c332030.ctool4j.definition.function.CFunction;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CArrUtils
 * </p>
 *
 * @since 2025/9/10
 */
@UtilityClass
public class CArrUtils {

    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    @SuppressWarnings("unchecked")
    public <T> T[] emptyArray() {
        return (T[]) EMPTY_OBJECT_ARRAY;
    }

    /**
     * 过滤
     * @param array 数组
     * @param predicate 断言
     * @return 过滤后的数组
     * @param <T> 泛型
     */
    public <T> List<T> filter(T[] array, Predicate<T> predicate) {

        if(ArrayUtil.isEmpty(array)) {
            return CList.of();
        }

        return Arrays.stream(array)
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * 获取非空的数组元素
     * @param array 数组
     * @return 非空的数组元素
     * @param <T> 泛型
     */
    public <T> List<T> filterNull(T[] array) {
        return filter(array, Objects::nonNull);
    }

    /**
     * 获取非空的数组元素
     * @param array 数组
     * @return 非空的数组元素
     */
    public List<String> filterString(String[] array) {
        return filter(array, StrUtil::isNotBlank);
    }

    /**
     * 获取数组元素
     * @param arr 数组
     * @param index 索引
     * @return 数组元素
     * @param <T> 泛型
     */
    public <T> T get(T[] arr, int index) {

        if(ArrayUtil.isEmpty(arr)) {
            return null;
        }

        var newIndex = index;
        val length = arr.length;
        if(index < 0) {
            newIndex = length + index;
        }

        if(newIndex >= length) {
            return null;
        }

        return arr[newIndex];
    }

    /**
     * 转换
     * @param oArr 原数组
     * @param converter 转换
     * @return 转换后的数组
     * @param <O> 原数组元素类型
     * @param <R> 转换后的数组元素类型
     */
    public <O, R> R[] convert(O[] oArr, CFunction<O, R> converter) {

        if(ArrayUtil.isEmpty(oArr)) {
            return null;
        }

        val length = oArr.length;

        val rArr = new Object[length];
        for (int i = 0; i < length; i++) {
            rArr[i] = converter.apply(oArr[i]);
        }

        return CObjUtils.anyType(rArr);
    }

    /**
     * 获取泛型数组
     * @param arr 数据
     * @return 泛型数组
     * @param <T> 泛型
     */
    @SafeVarargs
    public <T> T[] getArr(T... arr) {
        return arr;
    }

    public String[] toStrArr(Collection<String> collection) {

        if(CValidateUtils.isEmpty(collection)) {
            return emptyArray();
        }

        return collection
            .toArray(new String[0]);
    }

}
