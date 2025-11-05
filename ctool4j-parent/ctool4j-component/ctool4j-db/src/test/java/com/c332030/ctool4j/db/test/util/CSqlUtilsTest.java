package com.c332030.ctool4j.db.test.util;

import com.c332030.ctool4j.db.util.CSqlUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CSqlUtilsTest
 * </p>
 *
 * @since 2025/11/5
 */
public class CSqlUtilsTest {

    @Test
    public void toColumnName() {

        Assertions.assertEquals("id", CSqlUtils.toColumnName("id"));
        Assertions.assertEquals("user_id", CSqlUtils.toColumnName("UserId"));
        Assertions.assertEquals("user_id", CSqlUtils.toColumnName("userId"));
        Assertions.assertEquals("user_first_name", CSqlUtils.toColumnName("userFirstName"));

    }

}
