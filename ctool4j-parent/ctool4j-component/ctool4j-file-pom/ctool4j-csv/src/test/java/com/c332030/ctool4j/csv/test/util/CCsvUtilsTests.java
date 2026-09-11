package com.c332030.ctool4j.csv.test.util;

import com.c332030.ctool4j.csv.util.CCsvUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CCsvUtilsTests
 * </p>
 *
 * <p>
 * 是 {@link CCsvUtils} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>覆盖 trim 的正常路径：普通字符串去两端空白、内部空格保留、退格字符移除。</li>
 *   <li>覆盖边界：null、空串、纯空白、仅退格、超长字符串。</li>
 *   <li>依据白盒/黑盒原则与错误推测法：空白形态（空/纯空白/仅退格）、退格内嵌、超长输入均覆盖。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对"去两端空白、空白返 null、退格移除"的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：正常去空白、内部空格保留、退格移除、null/空/纯空白/仅退格返回 null、超长字符串。</li>
 *   <li>未覆盖：Unicode 特殊空白字符（如 {@code \u00A0}）——当前依赖 {@code StrUtil.isBlank}，其空白判定范围未逐项验证。</li>
 * </ul>
 * <h2>trim 去空白与退格</h2>
 * <ul>
 *   <li>1.1 普通字符串去除首尾空白（trim_normal）</li>
 *   <li>1.2 字符串内部空格保留（trim_innerSpaceKept）</li>
 *   <li>1.3 含退格字符时移除退格（trim_backspaceRemoved）</li>
 *   <li>1.4 null 返回 null（trim_null）</li>
 *   <li>1.5 空串返回 null（trim_empty）</li>
 *   <li>1.6 纯空白串返回 null（trim_blank）</li>
 *   <li>1.7 仅退格字符返回 null（trim_onlyBackspace）</li>
 *   <li>1.8 超长字符串正常处理（trim_superLong）</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/8/14
 * @version 1.0
 */
class CCsvUtilsTests {

    private static final String BACKSPACE = "\b";

    /**
     * 正常路径：普通字符串去除首尾空白
     * <p>
     * 对应测试用例 1.1：普通字符串去除首尾空白
     */
    @Test
    void trim_normal() {
        Assertions.assertEquals("abc", CCsvUtils.trim("  abc  "));
    }

    /**
     * 正常路径：字符串内部空格保留
     * <p>
     * 对应测试用例 1.2：字符串内部空格保留
     */
    @Test
    void trim_innerSpaceKept() {
        Assertions.assertEquals("a b c", CCsvUtils.trim(" a b c "));
    }

    /**
     * 正常路径：含退格字符时移除退格
     * <p>
     * 对应测试用例 1.3：含退格字符时移除退格
     */
    @Test
    void trim_backspaceRemoved() {
        Assertions.assertEquals("abc", CCsvUtils.trim("ab" + BACKSPACE + "c"));
    }

    /**
     * 边界：null 返回 null
     * <p>
     * 对应测试用例 1.4：null 返回 null
     */
    @Test
    void trim_null() {
        Assertions.assertNull(CCsvUtils.trim(null));
    }

    /**
     * 边界：空串返回 null
     * <p>
     * 对应测试用例 1.5：空串返回 null
     */
    @Test
    void trim_empty() {
        Assertions.assertNull(CCsvUtils.trim(""));
    }

    /**
     * 边界：纯空白串返回 null
     * <p>
     * 对应测试用例 1.6：纯空白串返回 null
     */
    @Test
    void trim_blank() {
        Assertions.assertNull(CCsvUtils.trim("   "));
    }

    /**
     * 边界：仅退格字符的串被 isBlank 判定为空白，返回 null
     * <p>
     * 对应测试用例 1.7：仅退格字符返回 null
     */
    @Test
    void trim_onlyBackspace() {
        Assertions.assertNull(CCsvUtils.trim(BACKSPACE));
    }

    /**
     * 边界：超长字符串正常处理
     * <p>
     * 对应测试用例 1.8：超长字符串正常处理
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
