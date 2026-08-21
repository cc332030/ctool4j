package com.c332030.ctool4j.definition.test.enums;

import com.c332030.ctool4j.definition.enums.CMimeTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/**
 * <p>
 * Description: CMimeTypeEnumTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CMimeTypeEnumTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void values() {

        CMimeTypeEnum[] values = CMimeTypeEnum.values();

        Assertions.assertEquals(3, values.length);
        Assertions.assertNotNull(CMimeTypeEnum.valueOf("JSON5"));
        Assertions.assertNotNull(CMimeTypeEnum.valueOf("XLS"));
        Assertions.assertNotNull(CMimeTypeEnum.valueOf("XLSX"));

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void json5() {

        Assertions.assertEquals("application/json5", CMimeTypeEnum.JSON5.getMimeTypeStr());
        Assertions.assertEquals("json5", CMimeTypeEnum.JSON5.getText());
        Assertions.assertEquals(MediaType.parseMediaType("application/json5"), CMimeTypeEnum.JSON5.getMimeType());
        Assertions.assertEquals("application/json5", CMimeTypeEnum.JSON5.getMimeType().toString());

    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void xls() {

        Assertions.assertEquals("application/vnd.ms-excel", CMimeTypeEnum.XLS.getMimeTypeStr());
        Assertions.assertEquals("Excel xls", CMimeTypeEnum.XLS.getText());
        Assertions.assertEquals("application/vnd.ms-excel", CMimeTypeEnum.XLS.getMimeType().toString());

    }

    /**
     * 对应测试用例 1.4
     */
    @Test
    public void xlsx() {

        Assertions.assertEquals(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            CMimeTypeEnum.XLSX.getMimeTypeStr()
        );
        Assertions.assertEquals("Excel xlsx", CMimeTypeEnum.XLSX.getText());
        Assertions.assertEquals(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            CMimeTypeEnum.XLSX.getMimeType().toString()
        );

    }

    /**
     * 对应测试用例 1.5
     */
    @Test
    public void valueOfUnknown() {

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CMimeTypeEnum.valueOf("UNKNOWN")
        );

    }

}
