package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.ArrayUtil;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.*;

/**
 * <p>
 * Description: CSet，{@code Set.of}（JDK 9+）在低版本 JDK 的替代构造工具。
 * </p>
 *
 * <p><b>JDK 版本兼容：</b>当运行环境 JDK ≥ 9 时，优先使用 JDK 自带的 {@code Set.of}；
 * 仅当 JDK 不支持（如 JDK 8 目标）时才使用本类。语义差异：{@code Set.of} 不允许 null 元素
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
 *     <td>返回空 Set</td>
 *   </tr>
 *   <tr>
 *     <td>of(T...) 全部为 null / 空参</td>
 *     <td>返回空 Set</td>
 *   </tr>
 *   <tr>
 *     <td>of(T...) 含 null 元素</td>
 *     <td>过滤 null，保留非空元素</td>
 *   </tr>
 *   <tr>
 *     <td>返回 Set 被修改</td>
 *     <td>抛 UnsupportedOperationException</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要不可变 Set、且自动过滤 null 元素的集合构造。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>返回不可变 Set，需要后续增删时用原生 {@code new HashSet}。</li>
 *   <li>普通元素重载基于 HashSet，不保证顺序；需顺序用 LinkedHashSet 自行构造。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>自动过滤 null，牺牲"保留 null"能力换取安全的集合构造。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义约定</b></p>
 * <ul>
 *   <li>元素重载过滤 null 元素；全部为 null/空参时返回空 Set（{@code Collections.emptySet()}）。</li>
 *   <li>返回不可变 Set（{@code Collections.unmodifiableSet}），调用方 add 抛 UnsupportedOperationException。</li>
 *   <li>枚举重载基于 {@code EnumSet.copyOf} 构造（去重、枚举特性），同样不可变。</li>
 * </ul>
 * <p><b>实现方式</b></p>
 * <ul>
 *   <li>元素重载先经 {@code CArrUtils.filterNull(ts)} 去空，{@code ArrayUtil.isEmpty} 时空返回空 Set，否则</li>
 *   <li>{@code HashSet.addAll} 后包 {@code Collections.unmodifiableSet}。</li>
 *   <li>枚举重载经 filterNull 后 {@code EnumSet.copyOf}，再包不可变。</li>
 * </ul>
 *
 * @since 2024/11/12
 * @version 1.0
 */
@UtilityClass
public class CSet {

    /**
     * 获取空 Set
     *
     * @param <T> 元素类型
     * @return 空 Set
     */
    public <T> Set<T> of() {
        return Collections.emptySet();
    }

    /**
     * 获取元素 Set（过滤 null 元素）
     *
     * @param ts  元素
     * @param <T> 元素类型
     * @return 不可变 Set
     */
    @SafeVarargs
    public <T> Set<T> of(T... ts) {

        val tsNew = CArrUtils.filterNull(ts);
        if(ArrayUtil.isEmpty(tsNew)) {
            return of();
        }

        val set = new HashSet<T>(tsNew.size());
        set.addAll(tsNew);
        return Collections.unmodifiableSet(set);
    }

    /**
     * 获取枚举 Set（过滤 null 元素）
     *
     * @param ts  枚举元素
     * @param <T> 枚举类型
     * @return 不可变枚举 Set
     */
    @SafeVarargs
    public <T extends Enum<T>> Set<T> of(T... ts) {

        val tsNew = CArrUtils.filterNull(ts);
        if(ArrayUtil.isEmpty(tsNew)) {
            return of();
        }

        val set = EnumSet.copyOf(tsNew);
        return Collections.unmodifiableSet(set);
    }

}
