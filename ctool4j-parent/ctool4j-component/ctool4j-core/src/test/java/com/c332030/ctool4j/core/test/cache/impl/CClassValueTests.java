package com.c332030.ctool4j.core.test.cache.impl;

import com.c332030.ctool4j.core.cache.impl.CClassValue;
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
 *   <li>覆盖：get 返回值；同一类缓存（只计算一次）；不同类独立计算。</li>
 *   <li>未覆盖：remove 后重新计算（依赖缓存弱关联行为，未单列）。</li>
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
