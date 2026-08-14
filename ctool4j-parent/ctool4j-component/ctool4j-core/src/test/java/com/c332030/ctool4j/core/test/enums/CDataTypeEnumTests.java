package com.c332030.ctool4j.core.test.enums;

import com.c332030.ctool4j.core.enums.CDataTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CDataTypeEnumTests
 * </p>
 *
 * @since 2025/12/12
 */
public class CDataTypeEnumTests {

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
