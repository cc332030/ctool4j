package com.c332030.ctool4j.csv.test.util;

import com.c332030.ctool4j.csv.util.CCsvUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CCsvUtilsTests
 * </p>
 *
 * @author c332030
 * @since 2026/8/14
 */
class CCsvUtilsTests {

    private static final String BACKSPACE = "\b";

    /**
     * 正常路径：普通字符串去除首尾空白
     */
    @Test
    void trim_normal() {
        Assertions.assertEquals("abc", CCsvUtils.trim("  abc  "));
    }

    /**
     * 正常路径：字符串内部空格保留
     */
    @Test
    void trim_innerSpaceKept() {
        Assertions.assertEquals("a b c", CCsvUtils.trim(" a b c "));
    }

    /**
     * 正常路径：含退格字符时移除退格
     */
    @Test
    void trim_backspaceRemoved() {
        Assertions.assertEquals("abc", CCsvUtils.trim("ab" + BACKSPACE + "c"));
    }

    /**
     * 边界：null 返回 null
     */
    @Test
    void trim_null() {
        Assertions.assertNull(CCsvUtils.trim(null));
    }

    /**
     * 边界：空串返回 null
     */
    @Test
    void trim_empty() {
        Assertions.assertNull(CCsvUtils.trim(""));
    }

    /**
     * 边界：纯空白串返回 null
     */
    @Test
    void trim_blank() {
        Assertions.assertNull(CCsvUtils.trim("   "));
    }

    /**
     * 边界：仅退格字符的串被 isBlank 判定为空白，返回 null
     */
    @Test
    void trim_onlyBackspace() {
        Assertions.assertNull(CCsvUtils.trim(BACKSPACE));
    }

    /**
     * 边界：超长字符串正常处理
     */
    @Test
    void trim_superLong() {
        String longStr = repeat("x", 10000);
        Assertions.assertEquals(longStr, CCsvUtils.trim(longStr));
    }

    private String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder(count * s.length());
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}
