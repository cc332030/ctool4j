package com.c332030.ctool4j.doc.openapi2.util;

import com.c332030.ctool4j.definition.interfaces.ICText;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import springfox.documentation.service.AllowableListValues;

import java.util.Arrays;

/**
 * <p>
 * Description: CTextEnumUtils 单元测试：实现 {@code ICText} 的枚举，允许值保持可提交的「枚举名」，
 * 可读的「枚举名(text)」由 textEnumDescription 生成；非法输入按兜底返回 null
 * </p>
 *
 * <ul>
 *   <li>1.1 只识别「枚举且实现 ICText」的类型（isTextEnum_onlyTextEnum）</li>
 *   <li>1.2 允许值为可提交的枚举名（enumAllowableValues_callableEnumNames）</li>
 *   <li>1.3 非枚举 / 非 ICText / null / 空枚举 → null（enumAllowableValues_invalidInputReturnsNull）</li>
 *   <li>2.1 text 说明为「枚举名(text)」、以「、」连接（textEnumDescription_enumNameWithText）</li>
 *   <li>2.2 text 为空时拼「枚举名()」（textEnumDescription_blankTextKeepsEmptyParens）</li>
 *   <li>2.3 非枚举 / null / 空枚举 → null（textEnumDescription_invalidInputReturnsNull）</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/16
 * @version 1.0
 */
class CTextEnumUtilsTests {

    /**
     * 实现 ICText、text 非空的测试枚举
     */
    private enum TextEnum implements ICText {

        FIRST("甲"),
        SECOND("乙"),
        ;

        private final String text;

        TextEnum(String text) {
            this.text = text;
        }

        @Override
        public String getText() {
            return text;
        }
    }

    /**
     * 实现 ICText、text 为 null 的测试枚举
     */
    private enum NullTextEnum implements ICText {

        ONLY(null),
        ;

        private final String text;

        NullTextEnum(String text) {
            this.text = text;
        }

        @Override
        public String getText() {
            return text;
        }
    }

    /**
     * 未实现 ICText 的测试枚举
     */
    private enum PlainEnum {

        ONLY
    }

    /**
     * 空枚举（无常量）但实现 ICText
     */
    private enum EmptyTextEnum implements ICText {

        ;

        @Override
        public String getText() {
            return null;
        }
    }

    /**
     * 对应测试用例 1.1：只识别「枚举且实现 ICText」的类型
     */
    @Test
    void isTextEnum_onlyTextEnum() {
        Assertions.assertTrue(CTextEnumUtils.isTextEnum(TextEnum.class), "实现 ICText 的枚举应识别");
        Assertions.assertFalse(CTextEnumUtils.isTextEnum(PlainEnum.class), "未实现 ICText 的枚举不识别");
        Assertions.assertFalse(CTextEnumUtils.isTextEnum(String.class), "非枚举不识别");
        Assertions.assertFalse(CTextEnumUtils.isTextEnum(null), "null 不识别");
    }

    /**
     * 对应测试用例 1.2：允许值为可提交的枚举名（非 text）
     */
    @Test
    void enumAllowableValues_callableEnumNames() {
        AllowableListValues allowableValues = CTextEnumUtils.enumAllowableValues(TextEnum.class);

        Assertions.assertNotNull(allowableValues);
        Assertions.assertEquals(Arrays.asList("FIRST", "SECOND"), allowableValues.getValues(),
            "允许值应为可提交的枚举名，保证文档值可直接用于请求");
    }

    /**
     * 对应测试用例 1.3：非法输入（非枚举 / 非 ICText / null / 空枚举）返回 null
     */
    @Test
    void enumAllowableValues_invalidInputReturnsNull() {
        Assertions.assertNull(CTextEnumUtils.enumAllowableValues(PlainEnum.class), "非 ICText 枚举返回 null");
        Assertions.assertNull(CTextEnumUtils.enumAllowableValues(String.class), "非枚举返回 null");
        Assertions.assertNull(CTextEnumUtils.enumAllowableValues(null), "null 返回 null");
        Assertions.assertNull(CTextEnumUtils.enumAllowableValues(EmptyTextEnum.class), "空枚举返回 null");
    }

    /**
     * 对应测试用例 2.1：text 说明为「枚举名(text)」，以「、」连接
     */
    @Test
    void textEnumDescription_enumNameWithText() {
        Assertions.assertEquals("FIRST(甲)、SECOND(乙)", CTextEnumUtils.textEnumDescription(TextEnum.class));
    }

    /**
     * 对应测试用例 2.2：text 为空（null）时拼「枚举名()」，不出现 "null"
     */
    @Test
    void textEnumDescription_blankTextKeepsEmptyParens() {
        Assertions.assertEquals("ONLY()", CTextEnumUtils.textEnumDescription(NullTextEnum.class),
            "text 为空时拼「枚举名()」，不输出 null 字面量");
    }

    /**
     * 对应测试用例 2.3：非法输入（非枚举 / null / 空枚举）返回 null
     */
    @Test
    void textEnumDescription_invalidInputReturnsNull() {
        Assertions.assertNull(CTextEnumUtils.textEnumDescription(PlainEnum.class), "非 ICText 枚举返回 null");
        Assertions.assertNull(CTextEnumUtils.textEnumDescription(null), "null 返回 null");
        Assertions.assertNull(CTextEnumUtils.textEnumDescription(EmptyTextEnum.class), "空枚举返回 null");
    }

}
