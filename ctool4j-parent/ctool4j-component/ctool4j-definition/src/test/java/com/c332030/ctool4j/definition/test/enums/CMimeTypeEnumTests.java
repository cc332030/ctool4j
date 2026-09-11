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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证枚举数量、各枚举的 mimeTypeStr/text/mimeType，以及未知名抛异常。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对媒体类型字段的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：枚举数量与 valueOf；JSON5/XLS/XLSX 的 mimeTypeStr/text/mimeType；未知名抛异常。</li>
 *   <li>未覆盖：无。</li>
 * </ul>
 * <h2>枚举</h2>
 * <ul>
 *   <li>1.1 values：3 个枚举且可 valueOf（values）</li>
 *   <li>1.2 json5：mimeTypeStr/text/mimeType（json5）</li>
 *   <li>1.3 xls：mimeTypeStr/text/mimeType（xls）</li>
 *   <li>1.4 xlsx：mimeTypeStr/text/mimeType（xlsx）</li>
 *   <li>1.5 未知名抛异常（valueOfUnknown）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CMimeTypeEnumTests {

    /**
     * 对应测试用例 1.1：3 个枚举且可 valueOf
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
     * 对应测试用例 1.2：mimeTypeStr/text/mimeType
     */
    @Test
    public void json5() {

        Assertions.assertEquals("application/json5", CMimeTypeEnum.JSON5.getMimeTypeStr());
        Assertions.assertEquals("json5", CMimeTypeEnum.JSON5.getText());
        Assertions.assertEquals(MediaType.parseMediaType("application/json5"), CMimeTypeEnum.JSON5.getMimeType());
        Assertions.assertEquals("application/json5", CMimeTypeEnum.JSON5.getMimeType().toString());

    }

    /**
     * 对应测试用例 1.3：mimeTypeStr/text/mimeType
     */
    @Test
    public void xls() {

        Assertions.assertEquals("application/vnd.ms-excel", CMimeTypeEnum.XLS.getMimeTypeStr());
        Assertions.assertEquals("Excel xls", CMimeTypeEnum.XLS.getText());
        Assertions.assertEquals("application/vnd.ms-excel", CMimeTypeEnum.XLS.getMimeType().toString());

    }

    /**
     * 对应测试用例 1.4：mimeTypeStr/text/mimeType
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
     * 对应测试用例 1.5：未知名抛异常
     */
    @Test
    public void valueOfUnknown() {

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CMimeTypeEnum.valueOf("UNKNOWN")
        );

    }

}
