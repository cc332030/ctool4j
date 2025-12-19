package com.c332030.ctool4j.core.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.time.*;
import java.util.Date;

/**
 * <p>
 * Description: CDateUtils
 * </p>
 *
 * @since 2025/12/7
 */
@CustomLog
@UtilityClass
public class CDateUtils {

    public static final Instant INITIAL_INSTANT = Instant.ofEpochMilli(0);

    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();

    public Date initialDate() {
        return new Date(0);
    }

    /**
     * 格式化日期时间字符串
     * @param instant Instant
     * @param pattern 日期时间格式
     * @return 日期时间字符串
     */
    public String format(Instant instant, String pattern) {
        return DateUtil.format(toDate(instant), pattern);
    }

    /**
     * 格式化日期字符串
     * @param instant Instant
     * @return 日期字符串
     */
    public String formatDate(Instant instant) {
        return DateUtil.formatDate(toDate(instant));
    }

    /**
     * 格式化时间字符串
     * @param instant Instant
     * @return 时间字符串
     */
    public String formatTime(Instant instant) {
        return DateUtil.formatTime(toDate(instant));
    }

    /**
     * 格式化日期时间字符串
     * @param instant Instant
     * @return 日期时间字符串
     */
    public String formatDateTime(Instant instant) {
        return DateUtil.formatDateTime(toDate(instant));
    }

    /**
     * 日期时间字符串转Instant
     * @param dateStr 日期时间字符串
     * @return Instant
     */
    public Instant parseInstant(CharSequence dateStr) {
        return toInstant(DateUtil.parse(dateStr));
    }

    /**
     * 日期字符串转Instant
     * @param dateStr 日期字符串
     * @return Instant
     */
    public Instant parseInstantDate(CharSequence dateStr) {
        return toInstant(DateUtil.parseDate(dateStr));
    }

    /**
     * 时间字符串转Instant
     * @param timeStr 时间字符串
     * @return Instant
     */
    public Instant parseInstantTime(CharSequence timeStr) {
        return toInstant(DateUtil.parseTime(timeStr));
    }

    /**
     * 日期时间字符串转Instant
     * @param dateStr 日期时间字符串
     * @return Instant
     */
    public Instant parseInstantDateTime(CharSequence dateStr) {
        return toInstant(DateUtil.parseDateTime(dateStr));
    }

    /**
     * 日期时间字符串转Date，可能是字符串类型的时间戳
     * @param text 日期时间字符串
     * @return Date
     */
    public Date parseMaybeMills(String text) {

        if(StrUtil.isEmpty(text)) {
            return null;
        }

        try {
            return DateUtil.parse(text);
        } catch (Exception ex) {
            log.debug("parse text error", ex);
        }

        try {
            val mills = CNumUtils.parseLong(text);
            return CDateUtils.toDate(mills);
        } catch (Exception ex) {
            log.debug("parse long text error", ex);
        }

        return null;
    }

    /**
     * 日期时间字符串转 Instant，可能是字符串类型的时间戳
     * @param text 日期时间字符串
     * @return Instant
     */
    public Instant parseInstantMaybeMills(String text) {
        return toInstant(parseMaybeMills(text));
    }

    /**
     * 时间戳转 Date
     * @param mills 时间戳
     * @return Date
     */
    public Date toDate(Long mills) {
        if(null == mills) {
            return null;
        }
        return new Date(mills);
    }

    /**
     * Instant 转 Date
     * @param instant Instant
     * @return Date
     */
    public Date toDate(Instant instant) {
        if(null == instant) {
            return null;
        }
        return Date.from(instant);
    }

    /**
     * LocalDateTime 转 Date
     * @param localDateTime localDateTime
     * @return Date
     */
    public Date toDate(LocalDateTime localDateTime) {
        return toDate(toInstant(localDateTime));
    }

    /**
     * LocalDate 转 Date
     * @param localDate localDate
     * @return Date
     */
    public Date toDate(LocalDate localDate) {
        return toDate(toInstant(localDate));
    }

    /**
     * Date 转 Instant
     * @param date Date
     * @return Instant
     */
    public Instant toInstant(Date date) {
        if(null == date) {
            return null;
        }
        return date.toInstant();
    }

    /**
     * 时间戳转 Instant
     * @param mills 时间戳
     * @return Instant
     */
    public Instant toInstant(Long mills) {
        if(null == mills) {
            return null;
        }
        return Instant.ofEpochMilli(mills);
    }

    /**
     * LocalDateTime 转 Instant
     * @param localDateTime localDateTime
     * @return Date
     */
    public Instant toInstant(LocalDateTime localDateTime) {
        if(null == localDateTime) {
            return null;
        }
        return toZonedDateTime(localDateTime).toInstant();
    }

    /**
     * LocalDate 转 Instant
     * @param localDate localDate
     * @return Date
     */
    public Instant toInstant(LocalDate localDate) {
        if(null == localDate) {
            return null;
        }
        return toInstant(localDate.atStartOfDay());
    }

    /**
     * Date 转 ZonedDateTime
     * @param date date
     * @return ZonedDateTime
     */
    public ZonedDateTime toZonedDateTime(Date date) {
        if(null == date) {
            return null;
        }
        return toZonedDateTime(toInstant(date));
    }

    /**
     * LocalDateTime 转 ZonedDateTime
     * @param localDateTime localDateTime
     * @return ZonedDateTime
     */
    public ZonedDateTime toZonedDateTime(LocalDateTime localDateTime) {
        if(null == localDateTime) {
            return null;
        }
        return localDateTime.atZone(DEFAULT_ZONE_ID);
    }

    /**
     * Instant 转 ZonedDateTime
     * @param instant instant
     * @return ZonedDateTime
     */
    public ZonedDateTime toZonedDateTime(Instant instant) {
        if(null == instant) {
            return null;
        }
        return ZonedDateTime.ofInstant(instant, DEFAULT_ZONE_ID);
    }

    /**
     * Instant 加上指定时间
     * @param value 时间
     * @return 结果
     */
    public Instant plus(Instant value, Duration duration) {

        if(null == value) {
            return null;
        }

        val zonedDateTime = toZonedDateTime(value);
        return zonedDateTime.plus(duration)
            .toInstant();
    }

    /**
     * Date 加上指定时间
     * @param value 时间
     * @return 结果
     */
    public Date plus(Date value, Duration duration) {

        if(null == value) {
            return null;
        }

        return toDate(plus(toInstant(value), duration));
    }

}
