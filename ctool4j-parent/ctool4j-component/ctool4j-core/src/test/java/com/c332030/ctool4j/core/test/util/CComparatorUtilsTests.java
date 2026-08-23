package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CComparatorUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Description: CComparatorUtilsTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CComparatorUtilsTests {

    /**
     * 对应测试用例 1.1, 1.2
     */
    @Test
    public void minCollection() {

        Assertions.assertEquals(1, CComparatorUtils.min(Arrays.asList(3, 1, 2)));
        Assertions.assertEquals(1, CComparatorUtils.min(Arrays.asList(3, null, 1, 2)));
        Assertions.assertNull(CComparatorUtils.min(Collections.<Integer>emptyList()));
        Assertions.assertNull(CComparatorUtils.min(Arrays.<Integer>asList(null, null)));

    }

    /**
     * 对应测试用例 1.3, 1.4
     */
    @Test
    public void minVarargs() {

        Assertions.assertEquals(1, CComparatorUtils.min(3, 1, 2));
        Assertions.assertEquals(1, CComparatorUtils.min(3, null, 1, 2));
        Assertions.assertNull(CComparatorUtils.min());
        Assertions.assertNull(CComparatorUtils.min((Integer) null));

    }

    /**
     * 对应测试用例 2.1, 2.2
     */
    @Test
    public void maxCollection() {

        Assertions.assertEquals(3, CComparatorUtils.max(Arrays.asList(3, 1, 2)));
        Assertions.assertEquals(3, CComparatorUtils.max(Arrays.asList(3, null, 1, 2)));
        Assertions.assertNull(CComparatorUtils.max(Collections.<Integer>emptyList()));
        Assertions.assertNull(CComparatorUtils.max(Arrays.<Integer>asList(null, null)));

    }

    /**
     * 对应测试用例 2.3, 2.4
     */
    @Test
    public void maxVarargs() {

        Assertions.assertEquals(3, CComparatorUtils.max(3, 1, 2));
        Assertions.assertEquals(3, CComparatorUtils.max(3, null, 1, 2));
        Assertions.assertNull(CComparatorUtils.max());

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void compareCollectionWithComparator() {

        List<String> list = Arrays.asList("ccc", "a", "bb");
        Assertions.assertEquals("a", CComparatorUtils.compareCollection(list, Comparator.naturalOrder()));
        Assertions.assertEquals("ccc", CComparatorUtils.compareCollection(list, Comparator.reverseOrder()));
        Assertions.assertNull(CComparatorUtils.compareCollection(Collections.<String>emptyList(), Comparator.naturalOrder()));

    }

    /**
     * 对应测试用例 4.1
     */
    @Test
    public void minByFunction() {

        List<String> list = Arrays.asList("ccc", "a", "bb");
        Assertions.assertEquals("a", CComparatorUtils.min(list, String::length));
        Assertions.assertNull(CComparatorUtils.min(Collections.emptyList(), String::length));

    }

    /**
     * 对应测试用例 4.2
     */
    @Test
    public void maxByFunction() {

        List<String> list = Arrays.asList("ccc", "a", "bb");
        Assertions.assertEquals("ccc", CComparatorUtils.max(list, String::length));
        Assertions.assertNull(CComparatorUtils.max(Collections.emptyList(), String::length));

    }

    /**
     * 对应测试用例 5.1
     */
    @Test
    public void compareWithComparator() {

        Assertions.assertEquals(0, CComparatorUtils.compare("a", "a", Comparator.naturalOrder()));
        Assertions.assertEquals(-1, CComparatorUtils.compare("a", "b", Comparator.naturalOrder()));

    }

    /**
     * 对应测试用例 5.2
     */
    @Test
    public void compareNullHandling() {

        Assertions.assertEquals(0, CComparatorUtils.compare(null, null, Comparator.naturalOrder()));
        Assertions.assertEquals(1, CComparatorUtils.compare(null, "a", Comparator.naturalOrder()));
        Assertions.assertEquals(-1, CComparatorUtils.compare("a", null, Comparator.naturalOrder()));

    }

    /**
     * 对应测试用例 5.3
     */
    @Test
    public void compareNatural() {

        Assertions.assertEquals(0, CComparatorUtils.compare(1, 1));
        Assertions.assertEquals(-1, CComparatorUtils.compare(1, 2));
        Assertions.assertEquals(1, CComparatorUtils.compare(2, 1));
        Assertions.assertEquals(0, CComparatorUtils.compare(null, null));
        Assertions.assertEquals(1, CComparatorUtils.compare(null, 1));
        Assertions.assertEquals(-1, CComparatorUtils.compare(1, null));

    }

    /**
     * 对应测试用例 6.1
     */
    @Test
    public void minConsumer() {

        AtomicInteger result = new AtomicInteger();
        CComparatorUtils.minConsumer(Arrays.asList(3, 1, 2), Integer::intValue, result::set);
        Assertions.assertEquals(1, result.get());

        AtomicInteger emptyResult = new AtomicInteger(100);
        CComparatorUtils.minConsumer(Collections.emptyList(), Integer::intValue, emptyResult::set);
        Assertions.assertEquals(100, emptyResult.get());

    }

    /**
     * 对应测试用例 6.2
     */
    @Test
    public void maxConsumer() {

        AtomicInteger result = new AtomicInteger();
        CComparatorUtils.maxConsumer(Arrays.asList(3, 1, 2), Integer::intValue, result::set);
        Assertions.assertEquals(3, result.get());

        AtomicInteger emptyResult = new AtomicInteger(100);
        CComparatorUtils.maxConsumer(Collections.emptyList(), Integer::intValue, emptyResult::set);
        Assertions.assertEquals(100, emptyResult.get());

    }

}
