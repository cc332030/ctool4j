package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.exception.CBusinessExceptionProvider;
import com.c332030.ctool4j.core.exception.ICBusinessExceptionProvider;
import com.c332030.ctool4j.core.util.CSpiUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

/**
 * <p>
 * Description: CSpiUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>以 {@code ICBusinessExceptionProvider}（有 {@code CBusinessExceptionProvider} 实现）与 {@code String}（无实现）两类</li>
 *   <li>接口，覆盖"有实现 / 无实现"两条路径。</li>
 *   <li>覆盖 getImpls（有/无实现）、getFirstImpl（有实现/无实现抛异常）、getFirstCustomImplOrDefault、</li>
 *   <li>getImplsSorted 各入口。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对无实现抛异常、自定义优先的约定。</li>
 *   <li>依据测试方法（等价类/边界值/异常路径）：有/无实现、异常、排序。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：getImpls 有实现（1 个且类型正确）与无实现（空）；getFirstImpl 命中与无实现抛</li>
 *   <li>IllegalStateException；getFirstCustomImplOrDefault 无自定义时返回默认实现；getImplsSorted</li>
 *   <li>排序后结果。</li>
 *   <li>未覆盖：存在多个实现（含自定义）时自定义优先、Comparable 自然序排序（当前测试环境仅 1 个实现，</li>
 *   <li>属环境依赖的可选扩展）。</li>
 * </ul>
 * <h2>获取全部实现（getImpls）</h2>
 * <ul>
 *   <li>1.1 有实现：返回 1 个 {@code CBusinessExceptionProvider}（getImpls）</li>
 *   <li>1.2 无实现：{@code String} 返回空列表（getImplsNoProvider）</li>
 * </ul>
 * <h2>获取首实现（getFirstImpl）</h2>
 * <ul>
 *   <li>2.1 有实现：返回 {@code CBusinessExceptionProvider}（getFirstImpl）</li>
 *   <li>2.2 无实现：抛 IllegalStateException（getFirstImplNoProviderThrows）</li>
 * </ul>
 * <h2>自定义优先（getFirstCustomImplOrDefault）</h2>
 * <ul>
 *   <li>3.1 无自定义：返回默认实现 {@code CBusinessExceptionProvider}（getFirstCustomImplOrDefault）</li>
 * </ul>
 * <h2>排序（getImplsSorted）</h2>
 * <ul>
 *   <li>4.1 排序：返回 1 个 {@code CBusinessExceptionProvider}（getImplsSorted）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CSpiUtilsTests {

    /**
     * 对应测试用例 1.1：有实现：返回 1 个 {@code CBusinessExceptionProvider}
     */
    @Test
    public void getImpls() {

        List<ICBusinessExceptionProvider> impls = CSpiUtils.getImpls(ICBusinessExceptionProvider.class);

        Assertions.assertEquals(1, impls.size());
        Assertions.assertInstanceOf(CBusinessExceptionProvider.class, impls.get(0));

    }

    /**
     * 对应测试用例 1.2：无实现：{@code String} 返回空列表
     */
    @Test
    public void getImplsNoProvider() {

        Assertions.assertTrue(CSpiUtils.getImpls(String.class).isEmpty());

    }

    /**
     * 对应测试用例 2.1：有实现：返回 {@code CBusinessExceptionProvider}
     */
    @Test
    public void getFirstImpl() {

        ICBusinessExceptionProvider impl = CSpiUtils.getFirstImpl(ICBusinessExceptionProvider.class);
        Assertions.assertInstanceOf(CBusinessExceptionProvider.class, impl);

    }

    /**
     * 对应测试用例 2.2：无实现：抛 IllegalStateException
     */
    @Test
    public void getFirstImplNoProviderThrows() {

        Assertions.assertThrowsExactly(IllegalStateException.class,
                () -> CSpiUtils.getFirstImpl(String.class));

    }

    /**
     * 对应测试用例 3.1：无自定义：返回默认实现 {@code CBusinessExceptionProvider}
     */
    @Test
    public void getFirstCustomImplOrDefault() {

        ICBusinessExceptionProvider impl = CSpiUtils.getFirstCustomImplOrDefault(
                ICBusinessExceptionProvider.class, CBusinessExceptionProvider.class);

        Assertions.assertInstanceOf(CBusinessExceptionProvider.class, impl);

    }

    /**
     * 对应测试用例 4.1：排序：返回 1 个 {@code CBusinessExceptionProvider}
     */
    @Test
    public void getImplsSorted() {

        List<ICBusinessExceptionProvider> impls = CSpiUtils.getImplsSorted(
                ICBusinessExceptionProvider.class,
                Comparator.comparingInt(o -> 0));

        Assertions.assertEquals(1, impls.size());
        Assertions.assertInstanceOf(CBusinessExceptionProvider.class, impls.get(0));

    }

}
