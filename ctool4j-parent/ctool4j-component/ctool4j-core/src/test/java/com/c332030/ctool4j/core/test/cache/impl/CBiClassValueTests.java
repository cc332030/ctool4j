package com.c332030.ctool4j.core.test.cache.impl;

import com.c332030.ctool4j.core.cache.impl.CBiClassValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Description: CBiClassValueTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「取值 / 缓存 / 独立计算」三个维度组织。</li>
 *   <li>取值验证组合结果；缓存用计数验证同一组合只计算一次；独立计算验证不同组合分别计算。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对双类组合惰性计算与缓存的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：get 组合值；同一组合缓存；不同组合独立计算。</li>
 *   <li>未覆盖：remove 后重新计算（未单列）。</li>
 * </ul>
 * <h2>取值</h2>
 * <ul>
 *   <li>1.1 get：返回双类名组合（get）</li>
 * </ul>
 * <h2>缓存</h2>
 * <ul>
 *   <li>2.1 同一组合缓存：多次 get 只计算一次（cached）</li>
 * </ul>
 * <h2>独立计算</h2>
 * <ul>
 *   <li>3.1 不同组合：各自独立计算（differentPairComputeSeparately）</li>
 * </ul>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class CBiClassValueTests {

    /**
     * 对应测试用例 1.1：返回双类名组合
     */
    @Test
    public void get() {

        CBiClassValue<String> cv = CBiClassValue.of((t1, t2) -> t1.getSimpleName() + "-" + t2.getSimpleName());
        Assertions.assertEquals("String-Integer", cv.get(String.class, Integer.class));

    }

    /**
     * 对应测试用例 2.1：同一组合缓存：多次 get 只计算一次
     */
    @Test
    public void cached() {

        AtomicInteger counter = new AtomicInteger();
        CBiClassValue<String> cv = CBiClassValue.of((t1, t2) -> {
            counter.incrementAndGet();
            return t1.getSimpleName() + "-" + t2.getSimpleName();
        });

        cv.get(String.class, Integer.class);
        cv.get(String.class, Integer.class);
        Assertions.assertEquals(1, counter.get());

    }

    /**
     * 对应测试用例 3.1：不同组合：各自独立计算
     */
    @Test
    public void differentPairComputeSeparately() {

        AtomicInteger counter = new AtomicInteger();
        CBiClassValue<String> cv = CBiClassValue.of((t1, t2) -> {
            counter.incrementAndGet();
            return t1.getSimpleName() + "-" + t2.getSimpleName();
        });

        cv.get(String.class, Integer.class);
        cv.get(String.class, Long.class);
        Assertions.assertEquals(2, counter.get());

    }

}
