package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CStreamUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * Description: CStreamUtilsTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CStreamUtilsTests {

    @Test
    public void distinctByKey() {

        List<String> result = Stream.of("a", "b", "a", "c", "b")
                .filter(CStreamUtils.distinctByKey(s -> s))
                .collect(Collectors.toList());

        Assertions.assertEquals(Arrays.asList("a", "b", "c"), result);

    }

    @Test
    public void distinctByKeyNullKeyExcluded() {

        List<String> result = Stream.of("a", "b")
                .filter(CStreamUtils.distinctByKey(s -> null))
                .collect(Collectors.toList());

        Assertions.assertTrue(result.isEmpty());

    }

    @Test
    public void distinctByKeySingleElement() {

        List<String> result = Stream.of("only")
                .filter(CStreamUtils.distinctByKey(s -> s))
                .collect(Collectors.toList());

        Assertions.assertEquals(Collections.singletonList("only"), result);

    }

}
