package com.c332030.ctool4j.core.util;

import cn.hutool.core.collection.CollUtil;
import com.c332030.ctool4j.definition.function.CConsumer;
import com.c332030.ctool4j.definition.function.CFunction;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;

import java.util.List;

/**
 * <p>
 * Description: CPageUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CPageUtils} 为分页工具类，提供：</p>
 * <ul>
 *   <li>分页大小常量：{@code DEFAULT_PAGE_SIZE=10}、{@code DEFAULT_JOB_PAGE_SIZE=100}、{@code DEFAULT_EXPORT_PAGE_SIZE=1000}</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>pageThenDo：queryFunction 返回 null</td>
 *     <td>结束循环</td>
 *   </tr>
 *   <tr>
 *     <td>pageThenDo：doSth 返回 false</td>
 *     <td>结束循环</td>
 *   </tr>
 *   <tr>
 *     <td>pageThenEach：返回空集合</td>
 *     <td>结束循环</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>分批处理大数据量（定时任务、导出等），逐页查询避免一次加载全量。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>pageThenDo 的 doSth 返回 null 时（CBoolUtils.isNotTrue(null)=true）会结束循环，业务需注意返回 Boolean 而非 null。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>使用简单页号自增，无并发/游标支持；超大结果集建议按时间游标。</li>
 *   <li>页号从 1 开始，与常见"第 1 页"语义一致。</li>
 * </ul>
 *
 * @since 2025/10/31
 * @version 1.0
 */
@UtilityClass
public class CPageUtils {

    /**
     * 默认分页大小
     */
    public static final Integer DEFAULT_PAGE_SIZE = 10;

    /**
     * 定时任务分页大小
     */
    public static final Integer DEFAULT_JOB_PAGE_SIZE = 100;

    /**
     * 导出分页大小
     */
    public static final Integer DEFAULT_EXPORT_PAGE_SIZE = 1000;

    /**
     * 分页查询并执行逻辑
     *
     * <p>终止条件：queryFunction 返回 null、返回集合为空，或 doSth 返回 false</p>
     *
     * <p>当返回自定义分页对象（非集合，如 IPage）时，无法通过 CollUtil.isEmpty 判断是否结束，
     * 需由业务在无数据时返回 null，或让 doSth 在结束场景返回 false</p>
     *
     * <h2>终止条件（pageThenDo）</h2>
     * <ul>
     *   <li>从页号 1 开始循环，{@code queryFunction.apply(page)} 返回 null，或 {@code doSth.apply(result)} 非 true</li>
     *   <li>（CBoolUtils.isNotTrue）时终止循环，页号自增。</li>
     *   <li>适用于返回自定义分页对象（非集合）的场景：无数据时业务返回 null，或让 doSth 返回 false 结束。</li>
     * </ul>
     * <ul>
     *   <li>{@code pageThenDo(CFunction&lt;Integer,T&gt; queryFunction, CFunction&lt;T,Boolean&gt; doSth)}：逐页查询并执行逻辑</li>
     * </ul>
     *
     * @param queryFunction 分页查询
     * @param doSth 执行逻辑
     * @param <T> 数据类型*/
    public <T> void pageThenDo(
            CFunction<Integer, T> queryFunction,
            CFunction<T, Boolean> doSth
    ) {

        var start = 1;
        while (true) {

            val result = queryFunction.apply(start);
            if(null == result
                || CBoolUtils.isNotTrue(doSth.apply(result))
            ) {
                break;
            }
            start++;
        }

    }

    /**
     * 分页查询并执行逻辑
     *
     * <h2>pageThenEach 语义</h2>
     * <ul>
     *   <li>委托 pageThenDo：{@code queryFunction} 返回的 list 为空（{@code CollUtil.isEmpty}）时返回 false 结束，</li>
     *   <li>否则逐元素执行 {@code doSth} 并返回 true 继续。</li>
     * </ul>
     * <ul>
     *   <li>{@code pageThenEach(CFunction&lt;Integer,List&lt;T&gt;&gt; queryFunction, CConsumer&lt;T&gt; doSth)}：逐页查询并对每元素执行逻辑</li>
     * </ul>
     *
     * @param queryFunction 分页查询
     * @param doSth 执行逻辑
     * @param <T> 数据类型*/
    public <T> void pageThenEach(
        CFunction<Integer, List<T>> queryFunction,
        CConsumer<T> doSth
    ) {
        pageThenDo(
            queryFunction,
            list -> {
                if(CollUtil.isEmpty(list)) {
                    return false;
                }
                list.forEach(doSth);
                return true;
            }
        );
    }

}
