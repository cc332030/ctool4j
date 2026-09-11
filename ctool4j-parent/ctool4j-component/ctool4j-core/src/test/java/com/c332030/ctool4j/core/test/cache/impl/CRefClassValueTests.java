package com.c332030.ctool4j.core.test.cache.impl;

import com.c332030.ctool4j.core.cache.impl.CRefClassValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CRefClassValueTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「初始值 / 设置取值 / 类独立」三个维度组织。</li>
 *   <li>初始值验证 of 时计算；设置取值验证 set 覆盖 get；类独立验证不同类互不影响。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对初始计算与 set 覆盖的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：初始值；set 后 get 返回覆盖值；不同类独立。</li>
 *   <li>未覆盖：无（覆盖了核心语义）。</li>
 * </ul>
 * <h2>初始值</h2>
 * <ul>
 *   <li>1.1 getInitial：of 计算初始值（getInitial）</li>
 * </ul>
 * <h2>设置与取值</h2>
 * <ul>
 *   <li>2.1 setAndGet：set 覆盖后 get 返回覆盖值（setAndGet）</li>
 * </ul>
 * <h2>类独立</h2>
 * <ul>
 *   <li>3.1 independentByClass：不同类缓存互不影响（independentByClass）</li>
 * </ul>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class CRefClassValueTests {

    /**
     * 对应测试用例 1.1：of 计算初始值
     */
    @Test
    public void getInitial() {

        CRefClassValue<String> cv = CRefClassValue.of(clazz -> clazz.getSimpleName());
        Assertions.assertEquals("String", cv.get(String.class));

    }

    /**
     * 对应测试用例 2.1：set 覆盖后 get 返回覆盖值
     */
    @Test
    public void setAndGet() {

        CRefClassValue<String> cv = CRefClassValue.of(clazz -> clazz.getSimpleName());
        cv.set(String.class, "overwritten");
        Assertions.assertEquals("overwritten", cv.get(String.class));

    }

    /**
     * 对应测试用例 3.1：不同类缓存互不影响
     */
    @Test
    public void independentByClass() {

        CRefClassValue<String> cv = CRefClassValue.of(clazz -> clazz.getSimpleName());
        cv.set(String.class, "str");
        Assertions.assertEquals("str", cv.get(String.class));
        Assertions.assertEquals("Integer", cv.get(Integer.class));

    }

}
