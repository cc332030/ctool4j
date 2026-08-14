package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CArrUtils;
import com.c332030.ctool4j.definition.function.CFunction;
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

    @Test
    public void filter() {

        List<Integer> result = CArrUtils.filter(new Integer[]{1, 2, 3, 4}, e -> e % 2 == 0);
        Assertions.assertEquals(Arrays.asList(2, 4), result);

        Assertions.assertEquals(0, CArrUtils.filter(new Integer[]{1, 2, 3, 4}, e -> e > 10).size());
        Assertions.assertEquals(0, CArrUtils.filter(null, e -> true).size());
        Assertions.assertEquals(0, CArrUtils.filter(new Integer[0], e -> true).size());

    }

    @Test
    public void filterNull() {

        List<String> result = CArrUtils.filterNull(new String[]{"a", null, "b", null});
        Assertions.assertEquals(Arrays.asList("a", "b"), result);

        Assertions.assertEquals(0, CArrUtils.filterNull(null).size());

    }

    @Test
    public void filterString() {

        List<String> result = CArrUtils.filterString(new String[]{" a ", "", null, " ", "b"});
        Assertions.assertEquals(Arrays.asList(" a ", "b"), result);

        Assertions.assertEquals(0, CArrUtils.filterString(null).size());

    }

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

    @Test
    public void getNegativeIndexOutOfRangeThrows() {

        String[] arr = {"a", "b", "c"};

        Assertions.assertThrowsExactly(ArrayIndexOutOfBoundsException.class, () -> CArrUtils.get(arr, -4));

    }

    @Test
    public void convert() {

        Object[] result = CArrUtils.convert(new Integer[]{1, 2, 3}, String::valueOf);
        Assertions.assertArrayEquals(new Object[]{"1", "2", "3"}, result);

        Assertions.assertNull(CArrUtils.convert(null, (CFunction<Integer, String>) String::valueOf));

    }

    @Test
    public void convertToTypedArrayThrows() {

        Assertions.assertThrowsExactly(ClassCastException.class,
                () -> {
                    String[] result = CArrUtils.convert(new Integer[]{1, 2, 3}, String::valueOf);
                });

    }

    @Test
    public void getArr() {

        Integer[] arr = CArrUtils.getArr(1, 2, 3);
        Assertions.assertArrayEquals(new Integer[]{1, 2, 3}, arr);

        Assertions.assertEquals(0, CArrUtils.getArr().length);

    }

    @Test
    public void toStrArr() {

        String[] result = CArrUtils.toStrArr(Arrays.asList("a", "b"));
        Assertions.assertArrayEquals(new String[]{"a", "b"}, result);

        Assertions.assertEquals(0, CArrUtils.toStrArr(null).length);
        Assertions.assertEquals(0, CArrUtils.toStrArr(Collections.emptyList()).length);

    }

    @Test
    public void first() {

        Assertions.assertEquals("a", CArrUtils.first(new String[]{"a", "b"}));
        Assertions.assertNull(CArrUtils.first(null));
        Assertions.assertNull(CArrUtils.first(new String[0]));

    }

}
