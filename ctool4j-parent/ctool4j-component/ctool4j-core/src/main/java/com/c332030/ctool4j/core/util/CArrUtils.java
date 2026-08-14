package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.validation.CValidateUtils;
import com.c332030.ctool4j.definition.function.CFunction;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
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

    public final String[] EMPAY_STR_ARR = new String[0];

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
     * <p>返回 Object[]，适用于不关心元素具体类型或统一按 Object 处理的场景；
     * 需要类型化数组时改用三参重载，由调用方传入数组创建器</p>
     * @param oArr 原数组
     * @param converter 转换
     * @return 转换后的数组
     * @param <O> 原数组元素类型
     */
    public <O> Object[] convert(O[] oArr, CFunction<O, Object> converter) {
        return convert(oArr, Object[]::new, converter);
    }

    /**
     * 转换
     * <p>由调用方传入数组创建器 arrCreator（如 String[]::new）创建目标数组：
     * Java 泛型擦除后运行时无 R 类型信息，方法内部无法创建 R[]。
     * 原实现构造 Object[] 后强转为 R[]（R 由调用方按返回值接收），
     * 运行时数组类型仍是 Object[]，调用方解引用即抛 ClassCastException，
     * 故改为由调用方提供数组创建器，从源头消除强转</p>
     * @param oArr 原数组
     * @param arrCreator 目标数组创建器，如 String[]::new
     * @param converter 转换
     * @return 转换后的数组
     * @param <O> 原数组元素类型
     * @param <R> 转换后的数组元素类型
     */
    public <O, R> R[] convert(O[] oArr, IntFunction<R[]> arrCreator, CFunction<O, R> converter) {

        if(ArrayUtil.isEmpty(oArr)) {
            return null;
        }

        val length = oArr.length;

        val rArr = arrCreator.apply(length);
        for (int i = 0; i < length; i++) {
            rArr[i] = converter.apply(oArr[i]);
        }
        return rArr;
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
            return EMPAY_STR_ARR;
        }
        return collection
            .toArray(EMPAY_STR_ARR);
    }

    public <T> T first(T[] arr) {

        if(ArrayUtil.isEmpty(arr)){
            return null;
        }

        return arr[0];
    }

}
