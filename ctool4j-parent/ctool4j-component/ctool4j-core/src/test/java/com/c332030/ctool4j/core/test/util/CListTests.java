package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Description: CListTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CListTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void ofEmpty() {

        Assertions.assertTrue(CList.<String>of().isEmpty());

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void ofSingle() {

        Assertions.assertEquals(Collections.singletonList("a"), CList.of("a"));
        Assertions.assertTrue(CList.of((String) null).isEmpty());

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void ofVarargs() {

        Assertions.assertEquals(Arrays.asList("a", "b"), CList.of("a", "b"));
        Assertions.assertEquals(Collections.singletonList("b"), CList.of(null, "b", null));
        Assertions.assertTrue(CList.of((String) null, null).isEmpty());
        Assertions.assertTrue(CList.of().isEmpty());

    }

    /**
     * 对应测试用例 3.2
     */
    @Test
    public void ofVarargsUnmodifiable() {

        List<String> list = CList.of("a", "b");
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> list.add("c"));

    }

}
