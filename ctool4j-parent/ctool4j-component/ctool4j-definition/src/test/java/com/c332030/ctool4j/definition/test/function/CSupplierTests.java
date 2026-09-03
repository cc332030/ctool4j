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
 * @since 2026/8/14
 */
public class CSupplierTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void getNormal() {

        CSupplier<String> supplier = () -> "value";

        Assertions.assertEquals("value", supplier.get());

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void getNull() {

        CSupplier<String> supplier = () -> null;

        Assertions.assertNull(supplier.get());

    }

    /**
     * 对应测试用例 1.3
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
     * 对应测试用例 2.1
     */
    @Test
    public void alwaysNull() {

        CSupplier<String> supplier = CSupplier.alwaysNull();

        Assertions.assertNull(supplier.get());

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void staticGetNullSupplier() {

        Assertions.assertNull(CSupplier.get(null));

    }

    /**
     * 对应测试用例 2.3
     */
    @Test
    public void staticGetNormal() {

        Supplier<String> supplier = () -> "value";

        Assertions.assertEquals("value", CSupplier.get(supplier));

    }

}
