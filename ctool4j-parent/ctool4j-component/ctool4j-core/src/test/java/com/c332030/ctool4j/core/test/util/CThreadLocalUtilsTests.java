package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CThreadLocalUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * <p>
 * Description: CThreadLocalUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「取后移除 / 默认值」两个维度组织。</li>
 *   <li>取后移除：有值取后 remove，再 get 为 null；空 ThreadLocal 取后为 null。</li>
 *   <li>默认值：直默认值重载（有值用值、无值用默认值）；supplier 重载验证惰性（有值不调用 supplier）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对取后移除与默认值、supplier 惰性的约定。</li>
 *   <li>依据测试方法（等价类/边界值/惰性验证）：有值/无值、remove 副作用、supplier 调用次数。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：getThenRemove 有值取后移除、空 ThreadLocal；getOrDefault 有值/无值；getOrDefault(supplier)</li>
 *   <li>有值不调用 supplier、无值调用 supplier。</li>
 *   <li>未覆盖：无（覆盖了全部入口与分支）。</li>
 * </ul>
 * <h2>取后移除（getThenRemove）</h2>
 * <ul>
 *   <li>1.1 有值：取到值且 remove 后再次 get 为 null（getThenRemove）</li>
 *   <li>1.2 空：空 ThreadLocal 返回 null 且 remove 无副作用（getThenRemoveEmpty）</li>
 * </ul>
 * <h2>默认值（getOrDefault）</h2>
 * <ul>
 *   <li>2.1 直默认值：有值用值、无值用默认值（getOrDefault）</li>
 *   <li>2.2 supplier：有值不调用 supplier、无值调用 supplier（getOrDefaultWithSupplier）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CThreadLocalUtilsTests {

    /**
     * 对应测试用例 1.1：有值：取到值且 remove 后再次 get 为 null
     */
    @Test
    public void getThenRemove() {

        ThreadLocal<String> tl = new ThreadLocal<>();
        tl.set("value");

        Assertions.assertEquals("value", CThreadLocalUtils.getThenRemove(tl));
        // remove 后再次 get 返回 null
        Assertions.assertNull(tl.get());

    }

    /**
     * 对应测试用例 1.2：空：空 ThreadLocal 返回 null 且 remove 无副作用
     */
    @Test
    public void getThenRemoveEmpty() {

        ThreadLocal<String> tl = new ThreadLocal<>();
        Assertions.assertNull(CThreadLocalUtils.getThenRemove(tl));
        Assertions.assertNull(tl.get());

    }

    /**
     * 对应测试用例 2.1：直默认值：有值用值、无值用默认值
     */
    @Test
    public void getOrDefault() {

        ThreadLocal<String> tl = new ThreadLocal<>();
        tl.set("value");
        Assertions.assertEquals("value", CThreadLocalUtils.getOrDefault(tl, "default"));

        ThreadLocal<String> empty = new ThreadLocal<>();
        Assertions.assertEquals("default", CThreadLocalUtils.getOrDefault(empty, "default"));

    }

    /**
     * 对应测试用例 2.2：有值不调用 supplier、无值调用 supplier
     */
    @Test
    public void getOrDefaultWithSupplier() {

        ThreadLocal<String> tl = new ThreadLocal<>();
        tl.set("value");

        AtomicInteger supplierCalls = new AtomicInteger();
        Assertions.assertEquals("value",
                CThreadLocalUtils.getOrDefault(tl, (Supplier<String>) () -> {
                    supplierCalls.incrementAndGet();
                    return "supplied";
                }));
        // 已有值时不应调用 supplier
        Assertions.assertEquals(0, supplierCalls.get());

        ThreadLocal<String> empty = new ThreadLocal<>();
        Assertions.assertEquals("supplied",
                CThreadLocalUtils.getOrDefault(empty, (Supplier<String>) () -> {
                    supplierCalls.incrementAndGet();
                    return "supplied";
                }));
        Assertions.assertEquals(1, supplierCalls.get());

    }

}
