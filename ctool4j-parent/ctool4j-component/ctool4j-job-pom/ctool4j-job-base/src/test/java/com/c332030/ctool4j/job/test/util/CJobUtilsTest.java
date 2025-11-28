package com.c332030.ctool4j.job.test.util;

import cn.hutool.core.date.DateUtil;
import com.c332030.ctool4j.job.util.CJobUtils;
import lombok.CustomLog;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * <p>
 * Description: CJobUtilsTest
 * </p>
 *
 * @since 2025/10/31
 */
@CustomLog
public class CJobUtilsTest {

    @Test
    public void dayJobTime() {

        val initTimeStr = "2025-10-31";

        CJobUtils.dayJobTime(initTimeStr, 3, (startTime, endTime) -> {

            val startTimeStr = DateUtil.formatDateTime(Date.from(startTime));
            val endTimeStr = DateUtil.formatDateTime(Date.from(endTime));

            Assertions.assertEquals("2025-10-28 23:00:00", startTimeStr);
            Assertions.assertEquals("2025-11-01 00:00:00", endTimeStr);

        });

    }

}
