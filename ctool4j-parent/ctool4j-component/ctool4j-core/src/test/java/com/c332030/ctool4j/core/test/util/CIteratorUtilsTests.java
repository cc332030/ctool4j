package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CIteratorUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Description: CIteratorUtilsTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CIteratorUtilsTests {

    @Test
    public void forEachIgnoreExceptionByIterable() {

        List<String> collected = new ArrayList<>();
        CIteratorUtils.forEachIgnoreException(Arrays.asList("a", "b", "c"), collected::add);

        Assertions.assertEquals(Arrays.asList("a", "b", "c"), collected);

    }

    @Test
    public void forEachIgnoreExceptionNullIterable() {

        CIteratorUtils.forEachIgnoreException((Iterable<String>) null, s -> {
            throw new AssertionError("不应执行");
        });
        // 不抛异常即通过

    }

    @Test
    public void forEachIgnoreExceptionNullValueSkipped() {

        List<String> collected = new ArrayList<>();
        CIteratorUtils.forEachIgnoreException(Arrays.asList("a", null, "b"), collected::add);

        Assertions.assertEquals(Arrays.asList("a", "b"), collected);

    }

    @Test
    public void forEachIgnoreExceptionConsumerExceptionSwallowed() {

        List<String> collected = new ArrayList<>();
        CIteratorUtils.forEachIgnoreException(Arrays.asList("a", "b", "c"), s -> {
            if ("b".equals(s)) {
                throw new IllegalStateException("boom");
            }
            collected.add(s);
        });

        // b 抛异常被忽略，a/c 继续处理
        Assertions.assertEquals(Arrays.asList("a", "c"), collected);

    }

    @Test
    public void forEachIgnoreExceptionEmpty() {

        List<String> collected = new ArrayList<>();
        CIteratorUtils.forEachIgnoreException(Collections.<String>emptyList(), collected::add);

        Assertions.assertTrue(collected.isEmpty());

    }

}
