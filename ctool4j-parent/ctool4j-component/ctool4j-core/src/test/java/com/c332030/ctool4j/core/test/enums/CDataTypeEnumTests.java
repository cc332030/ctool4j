package com.c332030.ctool4j.core.test.enums;

import com.c332030.ctool4j.core.enums.CDataTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CDataTypeEnumTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「枚举值与描述 / 小写名 / 日期类型集合」三个维度组织。</li>
 *   <li>枚举值逐一断言数量与各枚举的中文描述。</li>
 *   <li>小写名覆盖避开含字母 I 的枚举（规避 Turkish locale 陷阱）。</li>
 *   <li>日期类型集合断言大小、包含日期相关类型、不包含 INT。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 13 种类型、描述、DATE_TYPES 集合的约定。</li>
 *   <li>依据测试方法（等价类/边界值）：全部枚举、小写名、集合成员判断。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：13 个枚举值数量与描述；getLowerCase 小写名；DATE_TYPES 大小与成员（含/不含）。</li>
 *   <li>未覆盖：含字母 I 的枚举（INT/TIMESTAMP 等）的 getLowerCase（因 locale 陷阱有意避开，未断言）。</li>
 * </ul>
 * <h2>枚举值与描述</h2>
 * <ul>
 *   <li>1.1 values：13 个枚举，各枚举中文描述正确（values）</li>
 * </ul>
 * <h2>小写名</h2>
 * <ul>
 *   <li>2.1 getLowerCase：非 I 字母枚举的小写名正确（getLowerCase）</li>
 * </ul>
 * <h2>日期类型集合</h2>
 * <ul>
 *   <li>3.1 DATE_TYPES：大小 4，含 DATE/TIME/DATETIME/TIMESTAMP，不含 INT（dateTypes）</li>
 * </ul>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class CDataTypeEnumTests {

    /**
     * 对应测试用例 1.1：13 个枚举，各枚举中文描述正确
     */
    @Test
    public void values() {

        Assertions.assertEquals(13, CDataTypeEnum.values().length);

        Assertions.assertEquals("整形", CDataTypeEnum.INT.getText());
        Assertions.assertEquals("长整形", CDataTypeEnum.LONG.getText());
        Assertions.assertEquals("浮点型", CDataTypeEnum.FLOAT.getText());
        Assertions.assertEquals("双精度浮点型", CDataTypeEnum.DOUBLE.getText());
        Assertions.assertEquals("布尔", CDataTypeEnum.BOOLEAN.getText());
        Assertions.assertEquals("字符串", CDataTypeEnum.STRING.getText());
        Assertions.assertEquals("日期", CDataTypeEnum.DATE.getText());
        Assertions.assertEquals("时间", CDataTypeEnum.TIME.getText());
        Assertions.assertEquals("日期时间", CDataTypeEnum.DATETIME.getText());
        Assertions.assertEquals("时间戳", CDataTypeEnum.TIMESTAMP.getText());
        Assertions.assertEquals("枚举", CDataTypeEnum.ENUM.getText());
        Assertions.assertEquals("选项", CDataTypeEnum.OPTION.getText());
        Assertions.assertEquals("选项-多选", CDataTypeEnum.MULTI_OPTION.getText());

    }

    /**
     * 对应测试用例 2.1：非 I 字母枚举的小写名正确
     */
    @Test
    public void getLowerCase() {

        // 避开含字母 I 的枚举名（toLowerCase 无 Locale，规避 Turkish locale 陷阱）
        Assertions.assertEquals("long", CDataTypeEnum.LONG.getLowerCase());
        Assertions.assertEquals("float", CDataTypeEnum.FLOAT.getLowerCase());
        Assertions.assertEquals("double", CDataTypeEnum.DOUBLE.getLowerCase());
        Assertions.assertEquals("boolean", CDataTypeEnum.BOOLEAN.getLowerCase());
        Assertions.assertEquals("string", CDataTypeEnum.STRING.getLowerCase());
        Assertions.assertEquals("date", CDataTypeEnum.DATE.getLowerCase());
        Assertions.assertEquals("time", CDataTypeEnum.TIME.getLowerCase());
        Assertions.assertEquals("enum", CDataTypeEnum.ENUM.getLowerCase());
        Assertions.assertEquals("option", CDataTypeEnum.OPTION.getLowerCase());

    }

    /**
     * 对应测试用例 3.1：大小 4，含 DATE/TIME/DATETIME/TIMESTAMP，不含 INT
     */
    @Test
    public void dateTypes() {

        Assertions.assertEquals(4, CDataTypeEnum.DATE_TYPES.size());
        Assertions.assertTrue(CDataTypeEnum.DATE_TYPES.contains(CDataTypeEnum.DATE));
        Assertions.assertTrue(CDataTypeEnum.DATE_TYPES.contains(CDataTypeEnum.TIME));
        Assertions.assertTrue(CDataTypeEnum.DATE_TYPES.contains(CDataTypeEnum.DATETIME));
        Assertions.assertTrue(CDataTypeEnum.DATE_TYPES.contains(CDataTypeEnum.TIMESTAMP));
        Assertions.assertFalse(CDataTypeEnum.DATE_TYPES.contains(CDataTypeEnum.INT));

    }

}
