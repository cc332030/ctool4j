package com.c332030.ctool4j.db.test.util;

import com.c332030.ctool4j.db.util.CSqlUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CSqlUtilsTest
 * </p>
 *
 * <p>
 * 是 {@link CSqlUtils} 的测试用例（对应测试文档
 * <code>doc/design/db/CSqlUtilsTests.adoc</code>）。
 * </p>
 *
 * @since 2025/11/5
 */
public class CSqlUtilsTest {

    /**
     * 测试驼峰命名转下划线列名
     *
     * <p>对应测试用例 4.2：字符串转列名</p>
     */
    @Test
    public void toColumnName() {

        Assertions.assertEquals("id", CSqlUtils.toColumnName("id"));
        Assertions.assertEquals("user_id", CSqlUtils.toColumnName("UserId"));
        Assertions.assertEquals("user_id", CSqlUtils.toColumnName("userId"));
        Assertions.assertEquals("user_first_name", CSqlUtils.toColumnName("userFirstName"));

    }

}
