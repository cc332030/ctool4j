package com.c332030.ctool4j.core.test.validation;

import com.c332030.ctool4j.core.exception.CBusinessException;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.core.validation.CAssert;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Description: CAssertTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CAssertTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void isTrue() {

        // 通过
        CAssert.isTrue(true, "msg");
        CAssert.isTrue(true, () -> "msg");

        // 失败抛业务异常，且使用指定的错误信息
        val e1 = Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.isTrue(false, "error"));
        Assertions.assertEquals("error", e1.getMessage());

        val e2 = Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.isTrue(false, () -> "error2"));
        Assertions.assertEquals("error2", e2.getMessage());

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void equals() {

        CAssert.equals(1, 1, "msg");
        CAssert.equals(1, 1, () -> "msg");

        // 不相等抛异常
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.equals(1, 2, "msg"));

        // null 与值不相等（Objects.equals 语义）
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.equals(null, 1, "msg"));
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.equals(1, null, "msg"));

        // 都为 null 视为相等
        CAssert.equals(null, null, "msg");

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void isNull() {

        CAssert.isNull(null, "msg");
        CAssert.isNull(null, () -> "msg");

        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.isNull(1, "msg"));

    }

    /**
     * 对应测试用例 3.2
     */
    @Test
    public void notNull() {

        CAssert.notNull(1, "msg");
        CAssert.notNull(1, () -> "msg");

        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notNull(null, "msg"));

    }

    /**
     * 对应测试用例 4.1
     */
    @Test
    public void notEmptyString() {

        CAssert.notEmpty("a", "msg");
        CAssert.notEmpty("a", () -> "msg");

        // 空字符串抛异常
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty("", "msg"));

        // null 抛异常
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty((String) null, "msg"));

    }

    /**
     * 对应测试用例 4.2
     */
    @Test
    public void notBlank() {

        CAssert.notBlank(" a ", "msg");
        CAssert.notBlank(" a ", () -> "msg");

        // 纯空白抛异常
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notBlank(" ", "msg"));

        // 空字符串 / null 抛异常
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notBlank("", "msg"));
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notBlank(null, "msg"));

    }

    /**
     * 对应测试用例 4.3
     */
    @Test
    public void notEmptyByteArray() {

        CAssert.notEmpty(new byte[] {1}, "msg");

        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty(new byte[0], "msg"));
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty((byte[]) null, "msg"));

    }

    /**
     * 对应测试用例 4.4
     */
    @Test
    public void notEmptyIntArray() {

        CAssert.notEmpty(new int[] {1}, "msg");

        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty(new int[0], "msg"));
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty((int[]) null, "msg"));

    }

    /**
     * 对应测试用例 4.5
     */
    @Test
    public void notEmptyLongArray() {

        CAssert.notEmpty(new long[] {1L}, "msg");

        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty(new long[0], "msg"));
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty((long[]) null, "msg"));

    }

    /**
     * 对应测试用例 4.6
     */
    @Test
    public void notEmptyObjectArray() {

        CAssert.notEmpty(new Object[] {1}, "msg");

        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty(new Object[0], "msg"));
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty((Object[]) null, "msg"));

    }

    /**
     * 对应测试用例 4.7
     */
    @Test
    public void notEmptyCollection() {

        CAssert.notEmpty(CList.of(1), "msg");

        // 空集合 / null 抛异常
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty(CList.of(), "msg"));
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty((Collection<?>) null, "msg"));

    }

    /**
     * 对应测试用例 4.8
     */
    @Test
    public void notEmptyMap() {

        CAssert.notEmpty(CMap.of("a", 1), "msg");

        // 空 map / null 抛异常
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty(new HashMap<>(), "msg"));
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CAssert.notEmpty((Map<?, ?>) null, "msg"));

    }

}
