package com.c332030.ctool4j.core.test.cache.impl;

import com.c332030.ctool4j.core.cache.impl.CRefBiClassValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CRefBiClassValueTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「初始值 / 设置取值 / 组合独立」三个维度组织。</li>
 *   <li>初始值验证 of 计算；设置取值验证 set 覆盖 get；组合独立验证不同组合互不影响。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对双类组合初始计算与 set 覆盖的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：初始值；set 后 get 返回覆盖值；不同组合独立。</li>
 *   <li>未覆盖：无（覆盖了核心语义）。</li>
 * </ul>
 * <h2>初始值</h2>
 * <ul>
 *   <li>1.1 getInitial：of 计算双类组合初始值（getInitial）</li>
 * </ul>
 * <h2>设置与取值</h2>
 * <ul>
 *   <li>2.1 setAndGet：set 覆盖后 get 返回覆盖值（setAndGet）</li>
 * </ul>
 * <h2>组合独立</h2>
 * <ul>
 *   <li>3.1 independentByPair：不同组合缓存互不影响（independentByPair）</li>
 * </ul>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class CRefBiClassValueTests {

    /**
     * 对应测试用例 1.1：of 计算双类组合初始值
     */
    @Test
    public void getInitial() {

        CRefBiClassValue<String> cv = CRefBiClassValue.of((t1, t2) -> t1.getSimpleName() + "-" + t2.getSimpleName());
        Assertions.assertEquals("String-Integer", cv.get(String.class, Integer.class));

    }

    /**
     * 对应测试用例 2.1：set 覆盖后 get 返回覆盖值
     */
    @Test
    public void setAndGet() {

        CRefBiClassValue<String> cv = CRefBiClassValue.of((t1, t2) -> t1.getSimpleName() + "-" + t2.getSimpleName());
        cv.set(String.class, Integer.class, "overwritten");
        Assertions.assertEquals("overwritten", cv.get(String.class, Integer.class));

    }

    /**
     * 对应测试用例 3.1：不同组合缓存互不影响
     */
    @Test
    public void independentByPair() {

        CRefBiClassValue<String> cv = CRefBiClassValue.of((t1, t2) -> t1.getSimpleName() + "-" + t2.getSimpleName());
        cv.set(String.class, Integer.class, "str");
        Assertions.assertEquals("str", cv.get(String.class, Integer.class));
        Assertions.assertEquals("String-Long", cv.get(String.class, Long.class));

    }

}
