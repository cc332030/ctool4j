package com.c332030.ctool4j.core.test.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.c332030.ctool4j.core.util.CDateUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

/**
 * <p>
 * Description: CDateUtilsTests
 * </p>
 *
 * @since 2025/12/7
 */
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

    }

    @Test
    public void toInstant() {

        Assertions.assertNull(CDateUtils.toInstant((Long) null));
        Assertions.assertNull(CDateUtils.toInstant((Date) null));
        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(CDateUtils.toInstant(MILLS)));
        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(CDateUtils.toInstant(DATE)));

    }

}
