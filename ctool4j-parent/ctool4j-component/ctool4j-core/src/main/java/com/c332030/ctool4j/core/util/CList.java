package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.ArrayUtil;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Description: CList，{@code List.of}（JDK 9+）在低版本 JDK 的替代构造工具。
 * </p>
 *
 * <p><b>JDK 版本兼容：</b>当运行环境 JDK ≥ 9 时，优先使用 JDK 自带的 {@code List.of}；
 * 仅当 JDK 不支持（如 JDK 8 目标）时才使用本类。语义差异：{@code List.of} 不允许 null 元素
 * （抛 {@link NullPointerException}），本类自动过滤 null 元素。</p>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>of() 空参</td>
 *     <td>返回空 List</td>
 *   </tr>
 *   <tr>
 *     <td>of(t) 单元素为 null</td>
 *     <td>返回空 List</td>
 *   </tr>
 *   <tr>
 *     <td>of(T...) 全部为 null / 空参</td>
 *     <td>返回空 List</td>
 *   </tr>
 *   <tr>
 *     <td>of(T...) 含 null 元素</td>
 *     <td>过滤 null，保留非空元素</td>
 *   </tr>
 *   <tr>
 *     <td>返回 List 被修改</td>
 *     <td>抛 UnsupportedOperationException</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要不可变 List、且自动过滤 null 元素的集合构造。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>返回不可变 List，需要后续增删时用原生 {@code new ArrayList}。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>自动过滤 null，牺牲"保留 null"能力换取安全的集合构造。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义约定</b></p>
 * <ul>
 *   <li>单元素重载：元素为 null 时返回空 List（{@code Collections.emptyList()}）。</li>
 *   <li>可变参数重载：过滤 null 元素；全部为 null/空参时返回空 List。</li>
 *   <li>返回不可变 List（{@code Collections.unmodifiableList} / {@code Collections.emptyList} /</li>
 *   <li>{@code Collections.singletonList}），调用方 add 抛 UnsupportedOperationException。</li>
 * </ul>
 * <p><b>实现方式</b></p>
 * <ul>
 *   <li>可变参数先经 {@code CArrUtils.filterNull(ts)} 去空，{@code ArrayUtil.isEmpty} 时空返回空 List，否则包不可变。</li>
 * </ul>
 *
 * @since 2024/11/12
 * @version 1.0
 */
@UtilityClass
public class CList {

    /**
     * 获取空 List
     *
     * @param <T> 元素类型
     * @return 空 List
     */
    public <T> List<T> of() {
        return Collections.emptyList();
    }

    /**
     * 获取单元素 List
     *
     * @param t   元素
     * @param <T> 元素类型
     * @return 单元素 List，元素为 null 时返回空 List
     */
    public <T> List<T> of(T t) {

        if (t == null) {
            return of();
        }
        return Collections.singletonList(t);
    }

    /**
     * 获取元素 List（过滤 null 元素）
     *
     * @param ts  元素
     * @param <T> 元素类型
     * @return 不可变 List
     */
    @SafeVarargs
    public <T> List<T> of(T... ts) {

        val tsNew = CArrUtils.filterNull(ts);
        if (ArrayUtil.isEmpty(tsNew)) {
            return of();
        }

        return Collections.unmodifiableList(tsNew);
    }

}
