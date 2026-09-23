package com.c332030.ctool4j.core.cache.impl;

import com.c332030.ctool4j.core.cache.ICClassValue;
import com.c332030.ctool4j.definition.function.CFunction;
import lombok.NonNull;
import lombok.val;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * <p>
 * Description: CClassValue
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CClassValue&lt;T&gt;} 为按类缓存值实现，实现 {@code ICClassValue&lt;T&gt;}，提供：</p>
 * <ul>
 *   <li>{@code get(Class&lt;?&gt;)}：获取指定类缓存值（首次经值函数计算并缓存）</li>
 *   <li>{@code remove(Class&lt;?&gt;)}：移除单个类的缓存值</li>
 *   <li>{@code clear()}：清空本实例已缓存过的全部键，此后每次 get 重新计算</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>同一类多次 get</td>
 *     <td>值函数只执行一次（缓存）</td>
 *   </tr>
 *   <tr>
 *     <td>remove / clear 后 get</td>
 *     <td>重新计算</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>按类缓存计算结果（如反射元数据、类相关配置），避免重复计算。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要运行期覆盖缓存值时用 {@code CRefClassValue}。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>基于 ClassValue 弱关联，类卸载时缓存自动释放、无内存泄漏。</li>
 *   <li><b>弱关联不等于不需要清理</b>：条目只在「该类曾被本实例成功 {@code get} 过」时驻留，
 *   且键的类由长期存活的类加载器承载时不会自行释放——需要交还内存时由调用方 {@code remove} 或 {@code clear} 显式清理。</li>
 *   <li>按键清空只作用于 {@code CLASS_KEYS} 记录下来的键（即本实例读取过的类）；该记录同样是弱引用、不阻止类卸载。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>基于 ClassValue</b></p>
 * <ul>
 *   <li>内部委托 {@code java.lang.ClassValue}：线程安全、按类弱关联（类可被 GC），{@code computeValue} 经值函数计算。</li>
 * </ul>
 * <p><b>惰性计算与缓存</b></p>
 * <ul>
 *   <li>首次 get 经 {@code computeValue} 计算并缓存，后续 get 直接返回缓存值（同一类只计算一次）。</li>
 *   <li>不同类各自独立计算缓存。</li>
 * </ul>
 *
 * @since 2025/11/20
 * @version 1.1
 * @see "doc/design/core/class-value.adoc"
 */
public class CClassValue<T> implements ICClassValue<T> {

    /**
     * 已被本实例读取过的类：仅供 {@link #clear()} 枚举键用
     * <p>{@code WeakHashMap} 的键为弱引用，条目随类卸载自动消失、不阻止类卸载。</p>
     */
    private final Set<Class<?>> CLASS_KEYS = Collections.newSetFromMap(new WeakHashMap<>());

    private final ClassValue<T> classValue;

    private CClassValue(CFunction<Class<?>, T> function) {
        classValue = new ClassValue<T>() {
            /**
             * 通过值函数计算指定类的值并缓存
             *
             * @param type 类
             * @return 计算得到的值
             */
            @Override
            protected T computeValue(@NonNull Class<?> type) {
                CLASS_KEYS.add(type);
                return function.apply(type);
            }
        };
    }

    /**
     * 获取值
     * <ul>
     *   <li>{@code get(Class&lt;?&gt;)}：获取指定类缓存值（首次经值函数计算并缓存）</li>
     * </ul>
     *
     * @param clazz 类
     * @return 值
     */
    @Override
    public T get(Class<?> clazz) {
        return classValue.get(clazz);
    }

    /**
     * 移除缓存值，下次 get 时重新计算
     * <ul>
     *   <li>{@code remove(Class&lt;?&gt;)}：移除缓存值，下次 get 重新计算</li>
     * </ul>
     *
     * @param clazz 类
     */
    public void remove(Class<?> clazz) {
        classValue.remove(clazz);
    }

    /**
     * 清空本实例读取过的全部键，此后每次 get 重新计算
     *
     * <p><b>适用范围</b>：缓存值是「启动期一次性写入、启动后不再需要」的进程级静态数据时，
     * 由应用启动完成回调调用一次，把内存交还给运行时——{@code java.lang.ClassValue} 只在<b>键的类被卸载</b>时
     * 释放条目，长期存活的类加载器（应用自身的类型、容器加载的第三方类型）承载的条目不会自行释放。</p>
     *
     * <p><b>作用面</b>：清的是 {@code CLASS_KEYS} 记录的键，即<b>本实例读取过</b>的那些类——
     * 未读取过的类本就没有条目，无副作用。</p>
     *
     * <p><b>代价</b>：清空后同进程内再次 {@code get} 会重新计算（值函数被再执行一次），
     * 故只应在「缓存值不再被读取」的时点调用。清空按实例切分，同一值函数被多处共用时不波及别的调用方。</p>
     */
    public void clear() {

        // 先取快照再删：CLASS_KEYS 是 WeakHashMap 的键集视图，其迭代器不允许在遍历中做删除
        // （与 HashMap 不同，下一次 next 会抛 ConcurrentModificationException），快照隔离掉遍历与修改
        val keys = new ArrayList<Class<?>>(CLASS_KEYS);
        keys.forEach(classValue::remove);

    }

    /**
     * 创建 CClassValue
     * <ul>
     *   <li>{@code of(CFunction&lt;Class&lt;?&gt;,T&gt;)}：创建，值函数按类惰性计算</li>
     * </ul>
     *
     * @param function 值函数
     * @return CClassValue
     * @param <T> 值泛型
     */
    public static <T> CClassValue<T> of(CFunction<Class<?>, T> function) {
        return new CClassValue<>(function);
    }

}
