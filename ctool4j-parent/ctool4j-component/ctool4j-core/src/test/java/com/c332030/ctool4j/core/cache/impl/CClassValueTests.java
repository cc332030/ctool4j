package com.c332030.ctool4j.core.cache.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Description: CClassValueTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「取值 / 缓存 / 独立计算」三个维度组织。</li>
 *   <li>取值验证返回类名；缓存用 AtomicInteger 计数验证同一类只计算一次；独立计算验证不同类分别计算。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对惰性计算与按类缓存的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：get 返回值；同一类缓存（只计算一次）；不同类独立计算；remove / clear 后重新计算。</li>
 *   <li>未覆盖：类卸载后条目被运行时回收（依赖 GC 行为，无法在单测内稳定复现）。</li>
 * </ul>
 * <h2>取值</h2>
 * <ul>
 *   <li>1.1 get：返回类简单名（get）</li>
 * </ul>
 * <h2>缓存</h2>
 * <ul>
 *   <li>2.1 同一类缓存：多次 get 值函数只执行一次（cached）</li>
 * </ul>
 * <h2>独立计算</h2>
 * <ul>
 *   <li>3.1 不同类：各自计算（differentClassComputeSeparately）</li>
 * </ul>
 * <h2>清理</h2>
 * <ul>
 *   <li>4.1 remove：移除后重新计算（removedThenRecompute）</li>
 *   <li>4.2 clear：清空后每个已缓存类都重新计算（clearedThenRecompute）</li>
 *   <li>4.3 clear：只清本实例的键，别的实例不受影响（clearOnlyTouchesOwnInstance）</li>
 *   <li>4.4 clear：未 get 过的类无副作用、清空后 get 正常计算（clearOnUntouchedInstance）</li>
 *   <li>4.5 clear：多键一次清空，四个键都被清掉（clearManyKeys_removesAll）</li>
 * </ul>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class CClassValueTests {

    /**
     * 对应测试用例 1.1：返回类简单名
     */
    @Test
    public void get() {

        CClassValue<String> cv = CClassValue.of(clazz -> clazz.getSimpleName());
        Assertions.assertEquals("String", cv.get(String.class));
        Assertions.assertEquals("Integer", cv.get(Integer.class));

    }

    /**
     * 对应测试用例 2.1：同一类缓存：多次 get 值函数只执行一次
     */
    @Test
    public void cached() {

        AtomicInteger counter = new AtomicInteger();
        CClassValue<String> cv = CClassValue.of(clazz -> {
            counter.incrementAndGet();
            return clazz.getSimpleName();
        });

        cv.get(String.class);
        cv.get(String.class);
        Assertions.assertEquals(1, counter.get());

    }

    /**
     * 对应测试用例 4.1：移除后重新计算
     */
    @Test
    public void removedThenRecompute() {

        AtomicInteger counter = new AtomicInteger();
        CClassValue<String> cv = CClassValue.of(clazz -> {
            counter.incrementAndGet();
            return clazz.getSimpleName();
        });

        cv.get(String.class);
        cv.remove(String.class);

        Assertions.assertEquals("String", cv.get(String.class));
        Assertions.assertEquals(2, counter.get());

    }

    /**
     * 对应测试用例 4.2：清空后每个已缓存类都重新计算
     */
    @Test
    public void clearedThenRecompute() {

        AtomicInteger counter = new AtomicInteger();
        CClassValue<String> cv = CClassValue.of(clazz -> {
            counter.incrementAndGet();
            return clazz.getSimpleName();
        });

        cv.get(String.class);
        cv.get(Integer.class);
        cv.clear();

        Assertions.assertEquals("String", cv.get(String.class));
        Assertions.assertEquals("Integer", cv.get(Integer.class));
        Assertions.assertEquals(4, counter.get());

    }

    /**
     * 对应测试用例 4.3：清空只作用本实例，别的实例不受影响
     */
    @Test
    public void clearOnlyTouchesOwnInstance() {

        AtomicInteger otherCounter = new AtomicInteger();
        CClassValue<String> cleared = CClassValue.of(Class::getSimpleName);
        CClassValue<String> other = CClassValue.of(clazz -> {
            otherCounter.incrementAndGet();
            return clazz.getSimpleName();
        });

        cleared.get(String.class);
        other.get(String.class);

        cleared.clear();

        other.get(String.class);
        Assertions.assertEquals(1, otherCounter.get(), "别的实例的缓存不应被清");

    }

    /**
     * 对应测试用例 4.4：未 get 过就清空无副作用，之后 get 正常计算
     */
    @Test
    public void clearOnUntouchedInstance() {

        AtomicInteger counter = new AtomicInteger();
        CClassValue<String> cv = CClassValue.of(clazz -> {
            counter.incrementAndGet();
            return clazz.getSimpleName();
        });

        cv.clear();

        Assertions.assertEquals("String", cv.get(String.class));
        Assertions.assertEquals(1, counter.get());

    }

    /**
     * 对应测试用例 4.5：一次清空多个键——每个键都被清掉
     * <p>历史用例都只有一个键，一个键的遍历「删完即结束」，覆盖不到「多个键」这一最常见形态；
     * 本用例用四个键固化 {@code clear()} 的清空完整性（键记录不随 {@code remove} 缩减，
     * 故 {@code clear} 的枚举面与「已缓存过的键」始终一致）。</p>
     */
    @Test
    public void clearManyKeys_removesAll() {

        AtomicInteger counter = new AtomicInteger();
        CClassValue<String> cv = CClassValue.of(clazz -> {
            counter.incrementAndGet();
            return clazz.getSimpleName();
        });

        cv.get(String.class);
        cv.get(Integer.class);
        cv.get(Long.class);
        cv.get(Thread.class);
        Assertions.assertEquals(4, counter.get());

        // 值与键记录分离：remove 只移除值、键仍留在 CLASS_KEYS 里
        cv.remove(String.class);
        cv.remove(Integer.class);

        cv.clear();

        Assertions.assertEquals("String", cv.get(String.class));
        Assertions.assertEquals("Integer", cv.get(Integer.class));
        Assertions.assertEquals("Long", cv.get(Long.class));
        Assertions.assertEquals("Thread", cv.get(Thread.class));
        Assertions.assertEquals(8, counter.get(), "四个键都须被清掉后重新计算");

    }

    /**
     * 对应测试用例 3.1：不同类：各自计算
     */
    @Test
    public void differentClassComputeSeparately() {

        AtomicInteger counter = new AtomicInteger();
        CClassValue<String> cv = CClassValue.of(clazz -> {
            counter.incrementAndGet();
            return clazz.getSimpleName();
        });

        cv.get(String.class);
        cv.get(Integer.class);
        cv.get(String.class);
        Assertions.assertEquals(2, counter.get());

    }

}
