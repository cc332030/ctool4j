package com.c332030.ctool4j.job.test.util;

import com.c332030.ctool4j.definition.function.StartEndTimeConsumer;
import com.c332030.ctool4j.job.util.CJobUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * <p>
 * Description: CJobUtilsTests
 * </p>
 *
 * <p>
 * 是 {@link CJobUtils} 的测试用例（对应测试文档
 * <code>doc/design/job/CJobUtilsTests.adoc</code>）。
 * </p>
 *
 * @author c332030
 * @since 2026/8/14
 */
class CJobUtilsTests {

    private Instant start;

    private Instant end;

    private final StartEndTimeConsumer consumer = (s, e) -> {
        start = s;
        end = e;
    };

    private static Instant parseInstant(String localDateTime) {
        return LocalDateTime.parse(localDateTime)
            .atZone(ZoneId.systemDefault())
            .toInstant();
    }

    /**
     * 正常路径：param 合法时，endTime 为参数所在日次日零点，startTime 为 endTime 减 1 小时再减 days 天
 * <p>
 * 对应测试用例 1.1
 */
    @Test
    void dayJobTime_validParamDays1() {
        CJobUtils.dayJobTime("2026-08-14 10:00:00", 1, consumer);

        Instant expectedEnd = parseInstant("2026-08-15T00:00:00");
        Instant expectedStart = parseInstant("2026-08-13T23:00:00");

        Assertions.assertEquals(expectedEnd, end);
        Assertions.assertEquals(expectedStart, start);
    }

    /**
     * 正常路径：days 大于 1 时 startTime 相应前移
 * <p>
 * 对应测试用例 1.2
 */
    @Test
    void dayJobTime_validParamDays3() {
        CJobUtils.dayJobTime("2026-08-14 10:00:00", 3, consumer);

        Instant expectedEnd = parseInstant("2026-08-15T00:00:00");
        Instant expectedStart = parseInstant("2026-08-11T23:00:00");

        Assertions.assertEquals(expectedEnd, end);
        Assertions.assertEquals(expectedStart, start);
    }

    /**
     * 正常路径：param 为空时使用当前时间，仍满足 endTime 为次日零点、startTime 为 endTime 减 1 小时再减 days 天
 * <p>
 * 对应测试用例 1.3
 */
    @Test
    void dayJobTime_nullParamUsesNow() {
        // 与 CJobUtils 内部一致，按系统默认时区的日边界截断，避免 UTC 截断导致跨时区偏差
        Instant before = Instant.now()
            .atZone(ZoneId.systemDefault())
            .truncatedTo(ChronoUnit.DAYS)
            .plusDays(1)
            .toInstant();
        CJobUtils.dayJobTime(null, 1, consumer);
        Instant after = Instant.now()
            .atZone(ZoneId.systemDefault())
            .truncatedTo(ChronoUnit.DAYS)
            .plusDays(2)
            .toInstant();

        // endTime 应为参数（当前时间）所在日次日零点，落在 [次日零点, 后两日零点) 区间内
        Assertions.assertFalse(end.isBefore(before));
        Assertions.assertTrue(end.isBefore(after));
        Assertions.assertEquals(end.minus(1, ChronoUnit.HOURS).minus(1, ChronoUnit.DAYS), start);
    }

    /**
     * 边界：param 为纯空白时按当前时间处理
 * <p>
 * 对应测试用例 1.4
 */
    @Test
    void dayJobTime_blankParamUsesNow() {
        CJobUtils.dayJobTime("   ", 1, consumer);

        Assertions.assertNotNull(end);
        Assertions.assertNotNull(start);
        Assertions.assertEquals(end.minus(1, ChronoUnit.HOURS).minus(1, ChronoUnit.DAYS), start);
    }

    /**
     * 异常路径：param 格式非法时抛异常，不静默兜底
 * <p>
 * 对应测试用例 1.5
 */
    @Test
    void dayJobTime_invalidParamThrows() {
        Assertions.assertThrowsExactly(
            cn.hutool.core.date.DateException.class,
            () -> CJobUtils.dayJobTime("not-a-date", 1, consumer)
        );
    }
}
