package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CPageUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Description: CPageUtilsTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CPageUtilsTests {

    @Test
    public void defaultPageSizes() {

        Assertions.assertEquals(10, CPageUtils.DEFAULT_PAGE_SIZE);
        Assertions.assertEquals(100, CPageUtils.DEFAULT_JOB_PAGE_SIZE);
        Assertions.assertEquals(1000, CPageUtils.DEFAULT_EXPORT_PAGE_SIZE);

    }

    @Test
    public void pageThenDoStopsWhenResultNull() {

        List<Integer> pages = new ArrayList<>();
        CPageUtils.pageThenDo(
                page -> {
                    pages.add(page);
                    return page < 3 ? "data" : null;
                },
                data -> true);

        Assertions.assertEquals(Arrays.asList(1, 2, 3), pages);

    }

    @Test
    public void pageThenDoStopsWhenDoSthFalse() {

        List<Integer> pages = new ArrayList<>();
        CPageUtils.pageThenDo(
                page -> {
                    pages.add(page);
                    return page == 2 ? "stop" : "go";
                },
                data -> !"stop".equals(data));

        Assertions.assertEquals(Arrays.asList(1, 2), pages);

    }

    @Test
    public void pageThenEach() {

        List<String> consumed = new ArrayList<>();
        List<Integer> pages = new ArrayList<>();
        CPageUtils.pageThenEach(
                page -> {
                    pages.add(page);
                    if (page == 1) {
                        return Arrays.asList("a", "b");
                    }
                    if (page == 2) {
                        return Collections.singletonList("c");
                    }
                    return Collections.emptyList();
                },
                consumed::add);

        Assertions.assertEquals(Arrays.asList("a", "b", "c"), consumed);
        Assertions.assertEquals(Arrays.asList(1, 2, 3), pages);

    }

    @Test
    public void pageThenEachFirstPageEmpty() {

        List<String> consumed = new ArrayList<>();
        CPageUtils.pageThenEach(
                page -> Collections.<String>emptyList(),
                consumed::add);

        Assertions.assertTrue(consumed.isEmpty());

    }

}
