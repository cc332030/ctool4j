package com.c332030.ctool4j.core.test.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import com.c332030.ctool4j.core.util.CDateUtils;
import com.c332030.ctool4j.core.util.CList;
import lombok.CustomLog;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * <p>
 * Description: CDateUtilsTests
 * </p>
 *
 * @since 2025/12/7
 */
@CustomLog
public class CDateUtilsTests {

    private static final String DATE_STR = "2025-03-03";
    private static final String TIME_STR = "08:01:03";
    private static final String DATE_TIME_STR = DATE_STR + " " + TIME_STR;

    private static final Date DATE = DateUtil.parseDateTime(DATE_TIME_STR);
    private static final Instant INSTANT  = DATE.toInstant();

    private static final Long MILLS  = DATE.getTime();
    private static final String MILLS_STR  = MILLS.toString();

    @Test
    public void format() {

        Assertions.assertEquals(DATE_STR, CDateUtils.formatDate(INSTANT));
        Assertions.assertEquals(TIME_STR, CDateUtils.formatTime(INSTANT));
        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(INSTANT));
        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.format(INSTANT, DatePattern.NORM_DATETIME_PATTERN));

    }

    @Test
    public void parse() {

        Assertions.assertEquals(DATE_STR, CDateUtils.formatDate(INSTANT));
        Assertions.assertEquals(TIME_STR, CDateUtils.formatTime(INSTANT));
        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(INSTANT));
        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.format(INSTANT, DatePattern.NORM_DATETIME_PATTERN));

        Assertions.assertEquals(DATE_STR, DateUtil.formatDate(CDateUtils.parseMaybeMills(DATE_STR)));
        Assertions.assertEquals(TIME_STR, DateUtil.formatTime(CDateUtils.parseMaybeMills(TIME_STR)));
        Assertions.assertEquals(DATE_TIME_STR, DateUtil.formatDateTime(CDateUtils.parseMaybeMills(DATE_TIME_STR)));
        Assertions.assertEquals(DATE_TIME_STR, DateUtil.formatDateTime(CDateUtils.parseMaybeMills(MILLS_STR)));

        Assertions.assertEquals(DATE_STR, CDateUtils.formatDate(CDateUtils.parseInstantMaybeMills(DATE_STR)));
        Assertions.assertEquals(TIME_STR, CDateUtils.formatTime(CDateUtils.parseInstantMaybeMills(TIME_STR)));
        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(CDateUtils.parseInstantMaybeMills(DATE_TIME_STR)));
        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(CDateUtils.parseInstantMaybeMills(MILLS_STR)));

    }

    @Test
    public void toDate() {

        Assertions.assertNull(CDateUtils.toDate((Long) null));
        Assertions.assertNull(CDateUtils.toDate((Instant) null));
        Assertions.assertEquals(DATE_TIME_STR, DateUtil.formatDateTime(CDateUtils.toDate(MILLS)));
        Assertions.assertEquals(DATE_TIME_STR, DateUtil.formatDateTime(CDateUtils.toDate(INSTANT)));

        Assertions.assertEquals("2026-01-04 10:02:50", DateUtil.formatDateTime(
            CDateUtils.toDate(1767492170633L)));
        Assertions.assertEquals("2286-11-21 01:46:39", DateUtil.formatDateTime(
            CDateUtils.toDate(9999999999L)));

    }

    @Test
    public void toInstant() {

        Assertions.assertNull(CDateUtils.toInstant((Long) null));
        Assertions.assertNull(CDateUtils.toInstant((Date) null));
        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(CDateUtils.toInstant(MILLS)));
        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(CDateUtils.toInstant(DATE)));

    }

    @Test
    public void plusSecond() {

        val dateStr = DATE_STR + " 08:01:04";

        Assertions.assertEquals(dateStr, CDateUtils.formatDateTime(
            CDateUtils.plus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), Duration.ofSeconds(1))
        ));
        Assertions.assertEquals(dateStr, DateUtil.formatDateTime(
            CDateUtils.plus(DateUtil.parseDateTime(DATE_TIME_STR), Duration.ofSeconds(1))
        ));

        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(
            CDateUtils.plus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), Duration.ofSeconds(0))
        ));
        Assertions.assertEquals(DATE_TIME_STR, DateUtil.formatDateTime(
            CDateUtils.plus(DateUtil.parseDateTime(DATE_TIME_STR), Duration.ofSeconds(0))
        ));

    }

    @Test
    public void minusSecond() {

        val dateStr = DATE_STR + " 08:01:02";

        Assertions.assertEquals(dateStr, CDateUtils.formatDateTime(
            CDateUtils.minus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), Duration.ofSeconds(1))
        ));
        Assertions.assertEquals(dateStr, DateUtil.formatDateTime(
            CDateUtils.minus(DateUtil.parseDateTime(DATE_TIME_STR), Duration.ofSeconds(1))
        ));

        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(
            CDateUtils.minus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), Duration.ofSeconds(0))
        ));
        Assertions.assertEquals(DATE_TIME_STR, DateUtil.formatDateTime(
            CDateUtils.minus(DateUtil.parseDateTime(DATE_TIME_STR), Duration.ofSeconds(0))
        ));

    }

    @Test
    public void plusMinute() {

        val dateStr = DATE_STR + " 08:02:03";

        Assertions.assertEquals(dateStr, CDateUtils.formatDateTime(
            CDateUtils.plus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), Duration.ofMinutes(1))
        ));
        Assertions.assertEquals(dateStr, DateUtil.formatDateTime(
            CDateUtils.plus(DateUtil.parseDateTime(DATE_TIME_STR), Duration.ofMinutes(1))
        ));

        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(
            CDateUtils.plus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), Duration.ofMinutes(0))
        ));
        Assertions.assertEquals(DATE_TIME_STR, DateUtil.formatDateTime(
            CDateUtils.plus(DateUtil.parseDateTime(DATE_TIME_STR), Duration.ofMinutes(0))
        ));

    }


    @Test
    public void minusMinute() {

        val dateStr = DATE_STR + " 08:00:03";

        Assertions.assertEquals(dateStr, CDateUtils.formatDateTime(
            CDateUtils.minus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), Duration.ofMinutes(1))
        ));
        Assertions.assertEquals(dateStr, DateUtil.formatDateTime(
            CDateUtils.minus(DateUtil.parseDateTime(DATE_TIME_STR), Duration.ofMinutes(1))
        ));

        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(
            CDateUtils.minus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), Duration.ofMinutes(0))
        ));
        Assertions.assertEquals(DATE_TIME_STR, DateUtil.formatDateTime(
            CDateUtils.minus(DateUtil.parseDateTime(DATE_TIME_STR), Duration.ofMinutes(0))
        ));

    }

    @Test
    public void plusHour() {

        val dateStr = DATE_STR + " 09:01:03";

        Assertions.assertEquals(dateStr, CDateUtils.formatDateTime(
            CDateUtils.plus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), Duration.ofHours(1))
        ));
        Assertions.assertEquals(dateStr, DateUtil.formatDateTime(
            CDateUtils.plus(DateUtil.parseDateTime(DATE_TIME_STR), Duration.ofHours(1))
        ));

        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(
            CDateUtils.plus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), Duration.ofHours(0))
        ));
        Assertions.assertEquals(DATE_TIME_STR, DateUtil.formatDateTime(
            CDateUtils.plus(DateUtil.parseDateTime(DATE_TIME_STR), Duration.ofHours(0))
        ));

    }

    @Test
    public void minusHour() {

        val dateStr = DATE_STR + " 07:01:03";

        Assertions.assertEquals(dateStr, CDateUtils.formatDateTime(
            CDateUtils.minus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), Duration.ofHours(1))
        ));
        Assertions.assertEquals(dateStr, DateUtil.formatDateTime(
            CDateUtils.minus(DateUtil.parseDateTime(DATE_TIME_STR), Duration.ofHours(1))
        ));

        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(
            CDateUtils.minus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), Duration.ofHours(0))
        ));
        Assertions.assertEquals(DATE_TIME_STR, DateUtil.formatDateTime(
            CDateUtils.minus(DateUtil.parseDateTime(DATE_TIME_STR), Duration.ofHours(0))
        ));

    }

    @Test
    public void plusDay() {

        val dateStr = "2025-03-04 " + TIME_STR;

        Assertions.assertEquals(dateStr, CDateUtils.formatDateTime(
            CDateUtils.plus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), Duration.ofDays(1))
        ));
        Assertions.assertEquals(dateStr, DateUtil.formatDateTime(
            CDateUtils.plus(DateUtil.parseDateTime(DATE_TIME_STR), Duration.ofDays(1))
        ));

        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(
            CDateUtils.plus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), Duration.ofDays(0))
        ));
        Assertions.assertEquals(DATE_TIME_STR, DateUtil.formatDateTime(
            CDateUtils.plus(DateUtil.parseDateTime(DATE_TIME_STR), Duration.ofDays(0))
        ));

    }

    @Test
    public void minusDay() {

        val dateStr = "2025-03-02 " + TIME_STR;

        Assertions.assertEquals(dateStr, CDateUtils.formatDateTime(
            CDateUtils.minus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), Duration.ofDays(1))
        ));
        Assertions.assertEquals(dateStr, DateUtil.formatDateTime(
            CDateUtils.minus(DateUtil.parseDateTime(DATE_TIME_STR), Duration.ofDays(1))
        ));

        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(
            CDateUtils.minus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), Duration.ofDays(0))
        ));
        Assertions.assertEquals(DATE_TIME_STR, DateUtil.formatDateTime(
            CDateUtils.minus(DateUtil.parseDateTime(DATE_TIME_STR), Duration.ofDays(0))
        ));

    }

    @Test
    public void plusMonth() {

        val dateStr = "2025-04-03 " + TIME_STR;

        Assertions.assertEquals(dateStr, CDateUtils.formatDateTime(
            CDateUtils.plus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), 1, ChronoUnit.MONTHS)
        ));
        Assertions.assertEquals(dateStr, DateUtil.formatDateTime(
            CDateUtils.plus(DateUtil.parseDateTime(DATE_TIME_STR), 1, ChronoUnit.MONTHS)
        ));

        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(
            CDateUtils.plus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), 0, ChronoUnit.MONTHS)
        ));
        Assertions.assertEquals(DATE_TIME_STR, DateUtil.formatDateTime(
            CDateUtils.plus(DateUtil.parseDateTime(DATE_TIME_STR), 0, ChronoUnit.MONTHS)
        ));

    }


    @Test
    public void minusMonth() {

        val dateStr = "2025-02-03 " + TIME_STR;

        Assertions.assertEquals(dateStr, CDateUtils.formatDateTime(
            CDateUtils.minus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), 1, ChronoUnit.MONTHS)
        ));
        Assertions.assertEquals(dateStr, DateUtil.formatDateTime(
            CDateUtils.minus(DateUtil.parseDateTime(DATE_TIME_STR), 1, ChronoUnit.MONTHS)
        ));

        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(
            CDateUtils.minus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), 0, ChronoUnit.MONTHS)
        ));
        Assertions.assertEquals(DATE_TIME_STR, DateUtil.formatDateTime(
            CDateUtils.minus(DateUtil.parseDateTime(DATE_TIME_STR), 0, ChronoUnit.MONTHS)
        ));

    }

    @Test
    public void plusYear() {

        val dateStr = "2026-03-03 " + TIME_STR;

        Assertions.assertEquals(dateStr, CDateUtils.formatDateTime(
            CDateUtils.plus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), 1, ChronoUnit.YEARS)
        ));
        Assertions.assertEquals(dateStr, DateUtil.formatDateTime(
            CDateUtils.plus(DateUtil.parseDateTime(DATE_TIME_STR), 1, ChronoUnit.YEARS)
        ));

        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(
            CDateUtils.plus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), 0, ChronoUnit.YEARS)
        ));
        Assertions.assertEquals(DATE_TIME_STR, DateUtil.formatDateTime(
            CDateUtils.plus(DateUtil.parseDateTime(DATE_TIME_STR), 0, ChronoUnit.YEARS)
        ));

    }


    @Test
    public void minusYear() {

        val dateStr = "2024-03-03 " + TIME_STR;

        Assertions.assertEquals(dateStr, CDateUtils.formatDateTime(
            CDateUtils.minus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), 1, ChronoUnit.YEARS)
        ));
        Assertions.assertEquals(dateStr, DateUtil.formatDateTime(
            CDateUtils.minus(DateUtil.parseDateTime(DATE_TIME_STR), 1, ChronoUnit.YEARS)
        ));

        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(
            CDateUtils.minus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), 0, ChronoUnit.YEARS)
        ));
        Assertions.assertEquals(DATE_TIME_STR, DateUtil.formatDateTime(
            CDateUtils.minus(DateUtil.parseDateTime(DATE_TIME_STR), 0, ChronoUnit.YEARS)
        ));

    }

    @Test
    public void plusArr() {

        val dateStr = "2026-04-04 09:02:04";

        Assertions.assertEquals(dateStr, CDateUtils.formatDateTime(
            CDateUtils.plus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), CList.of(
                Pair.of(1L, ChronoUnit.SECONDS),
                Pair.of(1L, ChronoUnit.MINUTES),
                Pair.of(1L, ChronoUnit.HOURS),
                Pair.of(1L, ChronoUnit.DAYS),
                Pair.of(1L, ChronoUnit.MONTHS),
                Pair.of(1L, ChronoUnit.YEARS)
            ))
        ));

    }

    @Test
    public void minusArr() {

        val dateStr = "2024-02-02 07:00:02";

        Assertions.assertEquals(dateStr, CDateUtils.formatDateTime(
            CDateUtils.minus(CDateUtils.parseInstantDateTime(DATE_TIME_STR), CList.of(
                Pair.of(1L, ChronoUnit.SECONDS),
                Pair.of(1L, ChronoUnit.MINUTES),
                Pair.of(1L, ChronoUnit.HOURS),
                Pair.of(1L, ChronoUnit.DAYS),
                Pair.of(1L, ChronoUnit.MONTHS),
                Pair.of(1L, ChronoUnit.YEARS)
            ))
        ));

    }

    @Test
    public void parseMaybeMills() {

        val date = CDateUtils.parseMaybeMills("1767492170633");
        Assertions.assertEquals("2026-01-04 10:02:50", DateUtil.formatDateTime(date));

    }

    @Test
    public void parseMaybeMillsForSecondMills() {

        val date = CDateUtils.parseMaybeMills("9999999999");
        Assertions.assertEquals("2286-11-21 01:46:39", DateUtil.formatDateTime(date));

    }

}
