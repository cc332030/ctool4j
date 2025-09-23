package com.c332030.ctool4j.core.test.log;

import com.c332030.ctool4j.core.log.CLogUtils;
import com.c332030.ctool4j.core.model.CResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

/**
 * <p>
 * Description: CLogUtilsTest
 * </p>
 *
 * @since 2025/9/14
 */
public class CLogUtilsTest {

    @Test
    public void isJsonLog() {

        Assertions.assertTrue(CLogUtils.isJsonLog(CResult.class));

        Assertions.assertFalse(CLogUtils.isJsonLog(String.class));
        Assertions.assertFalse(CLogUtils.isJsonLog(DataSource.class));

    }

}
