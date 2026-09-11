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
 * <h2>能力目录</h2>
 * <p>{@code CArrUtils} 为数组工具类，提供数组相关的常用操作：</p>
 * <ul>
 *   <li>{@code filter} / {@code filterNull} / {@code filterString}：数组过滤，返回过滤后元素列表</li>
 *   <li>{@code get}：按索引取数组元素，支持负索引（从末尾倒数）、越界返回 null</li>
 *   <li>{@code convert}：数组元素转换（支持返回 Object[] 或调用方指定的类型化数组）</li>
 *   <li>{@code getArr}：泛型可变参数直接返回数组</li>
 *   <li>{@code toStrArr}：字符串集合转字符串数组</li>
 *   <li>{@code first}：取数组首元素</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>filter/filterNull/filterString 空数组/null</td>
 *     <td>返回空列表</td>
 *   </tr>
 *   <tr>
 *     <td>get 空数组/null / 索引越界（正负）</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>convert 空数组/null</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>toStrArr 空集合/null</td>
 *     <td>返回空数组常量 EMPTY_STR_ARR</td>
 *   </tr>
 *   <tr>
 *     <td>first 空数组/null</td>
 *     <td>返回 null</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>数组元素按条件过滤、去空（filterNull/filterString）。</li>
 *   <li>按索引取元素（含从末尾倒数取值的负索引场景）。</li>
 *   <li>数组元素类型转换（需要类型化数组时用三参 convert）。</li>
 *   <li>字符串集合转数组、取数组首元素。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>{@code convert} 空入参返回 null（而非空数组），调用方需注意判空。</li>
 *   <li>{@code get} 负索引的换算语义为"从末尾倒数"，与 Python 风格负索引一致。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>{@code filter} 返回 List 而非数组，便于调用方使用集合操作。</li>
 *   <li>{@code convert} 类型化数组依赖调用方传入创建器，避免运行期强转（{@code ClassCastException}）隐患。</li>
 *   <li>{@code get} 越界返回 null 而非抛异常，牺牲严格越界报错换取无异常取值便利。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义约定</b></p>
 * <ul>
 *   <li>空数组/null 入参的过滤类方法返回空列表（{@code CList.of()}），而非 null，调用方可安全遍历。</li>
 *   <li>{@code get} 支持负索引（{@code -1} 表示最后一个元素）；正负索引越界统一返回 null，不抛数组越界异常。</li>
 *   <li>{@code convert} 在空数组/null 入参时返回 null（与过滤类方法返回空列表的约定不同，按各方法语义独立约定）。</li>
 *   <li>{@code toStrArr} 在集合为空时返回共享空数组常量 {@code EMPTY_STR_ARR}（复用 {@code toArray}，非可变集合问题）。</li>
 * </ul>
 * <p><b>实现方式</b></p>
 * <ul>
 *   <li>{@code filter} 基于 {@code Arrays.stream} + {@code predicate} 过滤后收集为 List；空入参前置判断返回空列表。</li>
 *   <li>{@code get} 负索引先换算为正向索引（{@code length + index}），再统一越界判断；负索引越界</li>
 *   <li>（{@code index &lt; -length}）同样返回 null。</li>
 *   <li>{@code convert} 三参重载由调用方传入数组创建器 {@code IntFunction&lt;R[]&gt;} 创建目标数组——因 Java 泛型擦除</li>
 *   <li>后运行期无 {@code R} 类型信息，原实现构造 Object[] 后强转会在解引用时抛 {@code ClassCastException}，</li>
 *   <li>改为由调用方提供数组创建器从源头消除强转（javadoc 已注明该取舍）。</li>
 * </ul>
 *
 * @since 2025/9/10
 * @version 1.0
 */
@UtilityClass
public class CArrUtils {

    /**
     * 空对象数组
     */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * 空字符串数组
     */
    public final String[] EMPTY_STR_ARR = new String[0];

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

        // 负索引越界（index < -length）同样视为无值返回 null
        if(newIndex < 0 || newIndex >= length) {
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

    /**
     * 字符串集合转字符串数组
     *
     * @param collection 字符串集合
     * @return 字符串数组，集合为空时返回空数组
     */
    public String[] toStrArr(Collection<String> collection) {

        if(CValidateUtils.isEmpty(collection)) {
            return EMPTY_STR_ARR;
        }
        return collection
            .toArray(EMPTY_STR_ARR);
    }

    /**
     * 获取数组第一个元素
     *
     * @param arr 数组
     * @param <T> 元素类型
     * @return 第一个元素，数组为空时返回 null
     */
    public <T> T first(T[] arr) {

        if(ArrayUtil.isEmpty(arr)){
            return null;
        }

        return arr[0];
    }

}
