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

    @Test
    public void getNormal() {

        CSupplier<String> supplier = () -> "value";

        Assertions.assertEquals("value", supplier.get());

    }

    @Test
    public void getNull() {

        CSupplier<String> supplier = () -> null;

        Assertions.assertNull(supplier.get());

    }

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

    @Test
    public void alwaysNull() {

        CSupplier<String> supplier = CSupplier.alwaysNull();

        Assertions.assertNull(supplier.get());

    }

    @Test
    public void staticGetNullSupplier() {

        Assertions.assertNull(CSupplier.get(null));

    }

    @Test
    public void staticGetNormal() {

        Supplier<String> supplier = () -> "value";

        Assertions.assertEquals("value", CSupplier.get(supplier));

    }

    @Test
    public void convert() {

        CSupplier<String> cSupplier = () -> "converted";

        Supplier<String> supplier = CSupplier.convert(cSupplier);

        Assertions.assertEquals("converted", supplier.get());

    }

    @Test
    public void convertNullCSupplier() {

        Supplier<String> supplier = CSupplier.convert(null);

        Assertions.assertNull(supplier.get());

    }

}
