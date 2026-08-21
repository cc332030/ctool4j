package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CArrUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Description: CArrUtilsTests
 * </p>
 *
 * @since 2025/9/10
 */
public class CArrUtilsTests {

    /**
     * 对应测试用例 1.1 / 1.2 / 1.3 / 1.4
     */
    @Test
    public void filter() {

        List<Integer> result = CArrUtils.filter(new Integer[]{1, 2, 3, 4}, e -> e % 2 == 0);
        Assertions.assertEquals(Arrays.asList(2, 4), result);

        Assertions.assertEquals(0, CArrUtils.filter(new Integer[]{1, 2, 3, 4}, e -> e > 10).size());
        Assertions.assertEquals(0, CArrUtils.filter(null, e -> true).size());
        Assertions.assertEquals(0, CArrUtils.filter(new Integer[0], e -> true).size());

    }

    /**
     * 对应测试用例 1.5 / 1.6
     */
    @Test
    public void filterNull() {

        List<String> result = CArrUtils.filterNull(new String[]{"a", null, "b", null});
        Assertions.assertEquals(Arrays.asList("a", "b"), result);

        Assertions.assertEquals(0, CArrUtils.filterNull(null).size());

    }

    /**
     * 对应测试用例 1.7 / 1.8
     */
    @Test
    public void filterString() {

        List<String> result = CArrUtils.filterString(new String[]{" a ", "", null, " ", "b"});
        Assertions.assertEquals(Arrays.asList(" a ", "b"), result);

        Assertions.assertEquals(0, CArrUtils.filterString(null).size());

    }

    /**
     * 对应测试用例 2.1 / 2.2 / 2.3 / 2.4 / 2.5
     */
    @Test
    public void get() {

        String[] arr = {"a", "b", "c"};

        Assertions.assertEquals("a", CArrUtils.get(arr, 0));
        Assertions.assertEquals("c", CArrUtils.get(arr, 2));
        Assertions.assertEquals("c", CArrUtils.get(arr, -1));
        Assertions.assertEquals("b", CArrUtils.get(arr, -2));
        Assertions.assertNull(CArrUtils.get(arr, 3));
        Assertions.assertNull(CArrUtils.get(null, 0));
        Assertions.assertNull(CArrUtils.get(new String[0], 0));

    }

    /**
     * 对应测试用例 2.6
     */
    @Test
    public void getNegativeIndexOutOfRangeReturnsNull() {

        String[] arr = {"a", "b", "c"};

        // Q12 修复：负索引越界（index < -length）视为无值返回 null，不抛数组越界异常
        Assertions.assertNull(CArrUtils.get(arr, -4));

    }

    /**
     * 对应测试用例 3.1 / 3.2
     */
    @Test
    public void convert() {

        Object[] result = CArrUtils.convert(new Integer[]{1, 2, 3}, String::valueOf);
        Assertions.assertArrayEquals(new Object[]{"1", "2", "3"}, result);

        Assertions.assertNull(CArrUtils.convert(null, String::valueOf));

    }

    /**
     * 对应测试用例 3.3 / 3.4
     */
    @Test
    public void convertToTypedArray() {

        String[] result = CArrUtils.convert(new Integer[]{1, 2, 3}, String[]::new, String::valueOf);
        Assertions.assertArrayEquals(new String[]{"1", "2", "3"}, result);

        Assertions.assertNull(CArrUtils.convert(null, String[]::new, String::valueOf));

    }

    /**
     * 对应测试用例 4.1 / 4.2
     */
    @Test
    public void getArr() {

        Integer[] arr = CArrUtils.getArr(1, 2, 3);
        Assertions.assertArrayEquals(new Integer[]{1, 2, 3}, arr);

        Assertions.assertEquals(0, CArrUtils.getArr().length);

    }

    /**
     * 对应测试用例 5.1 / 5.2 / 5.3
     */
    @Test
    public void toStrArr() {

        String[] result = CArrUtils.toStrArr(Arrays.asList("a", "b"));
        Assertions.assertArrayEquals(new String[]{"a", "b"}, result);

        Assertions.assertEquals(0, CArrUtils.toStrArr(null).length);
        Assertions.assertEquals(0, CArrUtils.toStrArr(Collections.emptyList()).length);

    }

    /**
     * 对应测试用例 6.1 / 6.2 / 6.3
     */
    @Test
    public void first() {

        Assertions.assertEquals("a", CArrUtils.first(new String[]{"a", "b"}));
        Assertions.assertNull(CArrUtils.first(null));
        Assertions.assertNull(CArrUtils.first(new String[0]));

    }

}
