package com.c332030.ctool4j.core.test.validation;

import com.c332030.ctool4j.core.validation.CValidateUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Description: CValidateUtilsTests
 * </p>
 *
 * @since 2025/12/12
 */
public class CValidateUtilsTests {

    // ---- isNull / isNotNull ----

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void isNull() {

        Assertions.assertTrue(CValidateUtils.isNull(null));
        Assertions.assertFalse(CValidateUtils.isNull("x"));

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void isNotNull() {

        Assertions.assertTrue(CValidateUtils.isNotNull("x"));
        Assertions.assertFalse(CValidateUtils.isNotNull(null));

    }

    // ---- isEmpty(CharSequence) ----

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void isEmptyCharSequence() {

        Assertions.assertTrue(CValidateUtils.isEmpty((CharSequence) null));
        Assertions.assertTrue(CValidateUtils.isEmpty(""));
        Assertions.assertFalse(CValidateUtils.isEmpty("a"));

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void isNotEmptyCharSequence() {

        Assertions.assertFalse(CValidateUtils.isNotEmpty((CharSequence) null));
        Assertions.assertFalse(CValidateUtils.isNotEmpty(""));
        Assertions.assertTrue(CValidateUtils.isNotEmpty("a"));

    }

    // ---- isBlank ----

    /**
     * 对应测试用例 2.3
     */
    @Test
    public void isBlank() {

        Assertions.assertTrue(CValidateUtils.isBlank(null));
        Assertions.assertTrue(CValidateUtils.isBlank(""));
        Assertions.assertTrue(CValidateUtils.isBlank("   "));
        Assertions.assertFalse(CValidateUtils.isBlank("a"));

    }

    /**
     * 对应测试用例 2.4
     */
    @Test
    public void isNotBlank() {

        Assertions.assertFalse(CValidateUtils.isNotBlank(null));
        Assertions.assertFalse(CValidateUtils.isNotBlank("   "));
        Assertions.assertTrue(CValidateUtils.isNotBlank("a"));

    }

    // ---- isEmpty(Iterable / Collection) ----

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void isEmptyCollection() {

        Assertions.assertTrue(CValidateUtils.isEmpty((Collection<Object>) null));
        Assertions.assertTrue(CValidateUtils.isEmpty(Collections.emptyList()));
        Assertions.assertFalse(CValidateUtils.isEmpty(Collections.singletonList("a")));

    }

    /**
     * 对应测试用例 3.2
     */
    @Test
    public void isNotEmptyCollection() {

        Assertions.assertFalse(CValidateUtils.isNotEmpty((Collection<Object>) null));
        Assertions.assertTrue(CValidateUtils.isNotEmpty(Collections.singletonList("a")));

    }

    /**
     * 对应测试用例 3.3
     */
    @Test
    public void isEmptyIterable() {

        Assertions.assertTrue(CValidateUtils.isEmpty((Iterable<Object>) null));
        Assertions.assertTrue(CValidateUtils.isEmpty(Collections.emptyList()));
        Assertions.assertFalse(CValidateUtils.isEmpty(Collections.singletonList("a")));

    }

    /**
     * 对应测试用例 3.4
     */
    @Test
    public void isNotEmptyIterable() {

        Assertions.assertFalse(CValidateUtils.isNotEmpty((Iterable<Object>) null));
        Assertions.assertTrue(CValidateUtils.isNotEmpty(Collections.singletonList("a")));

    }

    // ---- isEmpty(Map) ----

    /**
     * 对应测试用例 4.1
     */
    @Test
    public void isEmptyMap() {

        Assertions.assertTrue(CValidateUtils.isEmpty((Map<Object, Object>) null));
        Assertions.assertTrue(CValidateUtils.isEmpty(new HashMap<>()));

        Map<String, String> map = new HashMap<>();
        map.put("a", "1");
        Assertions.assertFalse(CValidateUtils.isEmpty(map));

    }

    /**
     * 对应测试用例 4.2
     */
    @Test
    public void isNotEmptyMap() {

        Assertions.assertFalse(CValidateUtils.isNotEmpty((Map<Object, Object>) null));
        Map<String, String> map = new HashMap<>();
        map.put("a", "1");
        Assertions.assertTrue(CValidateUtils.isNotEmpty(map));

    }

    // ---- isEmpty(数组) ----

    /**
     * 对应测试用例 5.1
     */
    @Test
    public void isEmptyByteArray() {

        Assertions.assertTrue(CValidateUtils.isEmpty((byte[]) null));
        Assertions.assertTrue(CValidateUtils.isEmpty(new byte[0]));
        Assertions.assertFalse(CValidateUtils.isEmpty(new byte[] {1}));

    }

    /**
     * 对应测试用例 5.1
     */
    @Test
    public void isNotEmptyByteArray() {

        Assertions.assertFalse(CValidateUtils.isNotEmpty((byte[]) null));
        Assertions.assertTrue(CValidateUtils.isNotEmpty(new byte[] {1}));

    }

    /**
     * 对应测试用例 5.2
     */
    @Test
    public void isEmptyShortArray() {

        Assertions.assertTrue(CValidateUtils.isEmpty((short[]) null));
        Assertions.assertTrue(CValidateUtils.isEmpty(new short[0]));
        Assertions.assertFalse(CValidateUtils.isEmpty(new short[] {1}));

    }

    /**
     * 对应测试用例 5.2
     */
    @Test
    public void isNotEmptyShortArray() {

        Assertions.assertFalse(CValidateUtils.isNotEmpty((short[]) null));
        Assertions.assertTrue(CValidateUtils.isNotEmpty(new short[] {1}));

    }

    /**
     * 对应测试用例 5.3
     */
    @Test
    public void isEmptyCharArray() {

        Assertions.assertTrue(CValidateUtils.isEmpty((char[]) null));
        Assertions.assertTrue(CValidateUtils.isEmpty(new char[0]));
        Assertions.assertFalse(CValidateUtils.isEmpty(new char[] {'a'}));

    }

    /**
     * 对应测试用例 5.3
     */
    @Test
    public void isNotEmptyCharArray() {

        Assertions.assertFalse(CValidateUtils.isNotEmpty((char[]) null));
        Assertions.assertTrue(CValidateUtils.isNotEmpty(new char[] {'a'}));

    }

    /**
     * 对应测试用例 5.4
     */
    @Test
    public void isEmptyIntArray() {

        Assertions.assertTrue(CValidateUtils.isEmpty((int[]) null));
        Assertions.assertTrue(CValidateUtils.isEmpty(new int[0]));
        Assertions.assertFalse(CValidateUtils.isEmpty(new int[] {1}));

    }

    /**
     * 对应测试用例 5.4
     */
    @Test
    public void isNotEmptyIntArray() {

        Assertions.assertFalse(CValidateUtils.isNotEmpty((int[]) null));
        Assertions.assertTrue(CValidateUtils.isNotEmpty(new int[] {1}));

    }

    /**
     * 对应测试用例 5.5
     */
    @Test
    public void isEmptyLongArray() {

        Assertions.assertTrue(CValidateUtils.isEmpty((long[]) null));
        Assertions.assertTrue(CValidateUtils.isEmpty(new long[0]));
        Assertions.assertFalse(CValidateUtils.isEmpty(new long[] {1L}));

    }

    /**
     * 对应测试用例 5.5
     */
    @Test
    public void isNotEmptyLongArray() {

        Assertions.assertFalse(CValidateUtils.isNotEmpty((long[]) null));
        Assertions.assertTrue(CValidateUtils.isNotEmpty(new long[] {1L}));

    }

    /**
     * 对应测试用例 5.6
     */
    @Test
    public void isEmptyObjectArray() {

        Assertions.assertTrue(CValidateUtils.isEmpty((Object[]) null));
        Assertions.assertTrue(CValidateUtils.isEmpty(new Object[0]));
        Assertions.assertFalse(CValidateUtils.isEmpty(new Object[] {"a"}));

    }

    /**
     * 对应测试用例 5.6
     */
    @Test
    public void isNotEmptyObjectArray() {

        Assertions.assertFalse(CValidateUtils.isNotEmpty((Object[]) null));
        Assertions.assertTrue(CValidateUtils.isNotEmpty(new Object[] {"a"}));

    }

}
