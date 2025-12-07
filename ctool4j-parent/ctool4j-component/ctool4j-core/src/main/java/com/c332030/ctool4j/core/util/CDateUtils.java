package com.c332030.ctool4j.core.util;

import cn.hutool.core.date.DateUtil;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.util.Date;

/**
 * <p>
 * Description: CDateUtils
 * </p>
 *
 * @since 2025/12/7
 */
@UtilityClass
public class CDateUtils {

    /**
     * 日期字符串转Instant
     * @param dateStr 日期字符串
     * @return Instant
     */
    public Instant parseDateInstant(CharSequence dateStr) {
        return toInstant(DateUtil.parseDate(dateStr));
    }

    /**
     * 时间字符串转Instant
     * @param timeStr 时间字符串
     * @return Instant
     */
    public Instant parseTimeInstant(CharSequence timeStr) {
        return toInstant(DateUtil.parseTime(timeStr));
    }

    /**
     * 日期时间字符串转Instant
     * @param dateStr 日期时间字符串
     * @return Instant
     */
    public Instant parseDateTimeInstant(CharSequence dateStr) {
        return toInstant(DateUtil.parseDateTime(dateStr));
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

}
