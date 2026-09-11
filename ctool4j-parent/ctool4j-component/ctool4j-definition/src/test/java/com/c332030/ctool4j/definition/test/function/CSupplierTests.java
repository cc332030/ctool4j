package com.c332030.ctool4j.definition.test.function;

import com.c332030.ctool4j.definition.function.CSupplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * <p>
 * Description: CSupplierTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「get / 工具方法」两个维度组织。</li>
 *   <li>get 覆盖正常、null、受检异常；工具覆盖 alwaysNull、静态 get（null/正常）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 @SneakyThrows 包装与工具方法的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：get 正常/null/受检异常；alwaysNull；静态 get null/正常。</li>
 *   <li>未覆盖：无。</li>
 * </ul>
 * <h2>get</h2>
 * <ul>
 *   <li>1.1 正常：返回值正确（getNormal）</li>
 *   <li>1.2 null：返回 null（getNull）</li>
 *   <li>1.3 受检异常：抛 IOException（getSneakyThrowsCheckedException）</li>
 * </ul>
 * <h2>工具方法</h2>
 * <ul>
 *   <li>2.1 alwaysNull：恒返回 null（alwaysNull）</li>
 *   <li>2.2 静态 get null：返回 null（staticGetNullSupplier）</li>
 *   <li>2.3 静态 get 正常：返回值正确（staticGetNormal）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CSupplierTests {

    /**
     * 对应测试用例 1.1：正常：返回值正确
     */
    @Test
    public void getNormal() {

        CSupplier<String> supplier = () -> "value";

        Assertions.assertEquals("value", supplier.get());

    }

    /**
     * 对应测试用例 1.2：返回 null
     */
    @Test
    public void getNull() {

        CSupplier<String> supplier = () -> null;

        Assertions.assertNull(supplier.get());

    }

    /**
     * 对应测试用例 1.3：受检异常：抛 IOException
     */
    @Test
    public void getSneakyThrowsCheckedException() {

        CSupplier<String> supplier = () -> {
            throw new IOException("io error");
        };

        Assertions.assertThrowsExactly(
            IOException.class,
            supplier::get
        );

    }

    /**
     * 对应测试用例 2.1：恒返回 null
     */
    @Test
    public void alwaysNull() {

        CSupplier<String> supplier = CSupplier.alwaysNull();

        Assertions.assertNull(supplier.get());

    }

    /**
     * 对应测试用例 2.2：静态 get null：返回 null
     */
    @Test
    public void staticGetNullSupplier() {

        Assertions.assertNull(CSupplier.get(null));

    }

    /**
     * 对应测试用例 2.3：静态 get 正常：返回值正确
     */
    @Test
    public void staticGetNormal() {

        Supplier<String> supplier = () -> "value";

        Assertions.assertEquals("value", CSupplier.get(supplier));

    }

}
