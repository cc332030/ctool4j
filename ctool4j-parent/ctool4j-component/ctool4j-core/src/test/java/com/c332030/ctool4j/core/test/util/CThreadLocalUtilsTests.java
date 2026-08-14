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
 * @since 2026/8/14
 */
public class CThreadLocalUtilsTests {

    @Test
    public void getThenRemove() {

        ThreadLocal<String> tl = new ThreadLocal<>();
        tl.set("value");

        Assertions.assertEquals("value", CThreadLocalUtils.getThenRemove(tl));
        // remove 后再次 get 返回 null
        Assertions.assertNull(tl.get());

    }

    @Test
    public void getThenRemoveEmpty() {

        ThreadLocal<String> tl = new ThreadLocal<>();
        Assertions.assertNull(CThreadLocalUtils.getThenRemove(tl));
        Assertions.assertNull(tl.get());

    }

    @Test
    public void getOrDefault() {

        ThreadLocal<String> tl = new ThreadLocal<>();
        tl.set("value");
        Assertions.assertEquals("value", CThreadLocalUtils.getOrDefault(tl, "default"));

        ThreadLocal<String> empty = new ThreadLocal<>();
        Assertions.assertEquals("default", CThreadLocalUtils.getOrDefault(empty, "default"));

    }

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
