package com.c332030.ctool4j.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.definition.function.CBiFunction;
import com.c332030.ctool4j.definition.function.CFunction;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.Collection;
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

    /**
     * 初始 Instant（epoch 0）
     */
    public static final Instant INITIAL_INSTANT = Instant.ofEpochMilli(0);

    /**
     * 默认时区（系统时区）
     */
    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();

    /**
     * 获取初始日期（epoch 0）
     *
     * @return 初始日期
     */
    public Date initialDate() {
        return new Date(0);
    }

    /**
     * 格式化日期时间字符串
     *
     * @param instant Instant
     * @param pattern 日期时间格式
     * @return 日期时间字符串
     */
    public String format(Instant instant, String pattern) {
        return DateUtil.format(toDate(instant), pattern);
    }

    /**
     * 格式化日期字符串
     *
     * @param instant Instant
     * @return 日期字符串
     */
    public String formatDate(Instant instant) {
        return DateUtil.formatDate(toDate(instant));
    }

    /**
     * 格式化时间字符串
     *
     * @param instant Instant
     * @return 时间字符串
     */
    public String formatTime(Instant instant) {
        return DateUtil.formatTime(toDate(instant));
    }

    /**
     * 格式化日期时间字符串
     *
     * @param instant Instant
     * @return 日期时间字符串
     */
    public String formatDateTime(Instant instant) {
        return DateUtil.formatDateTime(toDate(instant));
    }

    /**
     * 格式化纯日期字符串
     *
     * @param instant Instant
     * @return 纯日期字符串
     */
    public String formatPureDate(Instant instant) {
        return DateUtil.format(toLocalDateTime(instant), DatePattern.PURE_DATE_PATTERN);
    }

    /**
     * 格式化纯时间字符串
     *
     * @param instant Instant
     * @return 纯时间字符串
     */
    public String formatPureTime(Instant instant) {
        return DateUtil.format(toLocalDateTime(instant), DatePattern.PURE_TIME_PATTERN);
    }

    /**
     * 格式化纯日期时间字符串
     *
     * @param instant Instant
     * @return 纯日期时间字符串
     */
    public String formatPureDateTime(Instant instant) {
        return DateUtil.format(toLocalDateTime(instant), DatePattern.PURE_DATETIME_PATTERN);
    }



    /**
     * 日期时间字符串转Instant
     *
     * @param dateStr 日期时间字符串
     * @return Instant
     */
    public Instant parseInstant(CharSequence dateStr) {
        return toInstant(DateUtil.parse(dateStr));
    }

    /**
     * 日期字符串转Instant
     *
     * @param dateStr 日期字符串
     * @return Instant
     */
    public Instant parseInstantDate(CharSequence dateStr) {
        return toInstant(DateUtil.parseDate(dateStr));
    }

    /**
     * 时间字符串转Instant
     *
     * @param timeStr 时间字符串
     * @return Instant
     */
    public Instant parseInstantTime(CharSequence timeStr) {
        return toInstant(DateUtil.parseTime(timeStr));
    }

    /**
     * 日期时间字符串转Instant
     *
     * @param dateStr 日期时间字符串
     * @return Instant
     */
    public Instant parseInstantDateTime(CharSequence dateStr) {
        return toInstant(DateUtil.parseDateTime(dateStr));
    }

    /**
     * 最小的时间戳（毫秒），判断时间戳是秒还是毫秒
     */
    public final Long MIN_MILLS = 10000000000L;

    /**
     * 日期时间字符串转Date，可能是字符串类型的时间戳
     *
     * @param text 日期时间字符串
     * @return Date
     */
    public Date parseMaybeMills(String text) {

        if (StrUtil.isEmpty(text)) {
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
     *
     * @param text 日期时间字符串
     * @return Instant
     */
    public Instant parseInstantMaybeMills(String text) {
        return toInstant(parseMaybeMills(text));
    }

    /**
     * 时间戳转 Date（秒/毫秒自动识别，见 {@link #toMillis(Long)}）
     *
     * @param mills 时间戳
     * @return Date
     */
    public Date toDate(Long mills) {
        if (null == mills) {
            return null;
        }
        val millis = toMillis(mills);
        return null == millis ? null : new Date(millis);
    }

    /**
     * 时间戳统一归一化为毫秒：负数视为非法输入返回 null；小于等于 {@link #MIN_MILLS} 的值按秒处理乘 1000，否则视为毫秒。
     * <p>秒/毫秒自动识别为启发式，存在固有歧义：{@link #MIN_MILLS}（1e10）同时是 2286-11-20 的秒值与 1973-03-03 的毫秒值，
     * 两者在 0 ~ 1e10 区间重叠，本方法统一按秒解释该区间；实际影响仅限 1973-03-03 之前的毫秒时间戳
     * （现实几乎不存在）被按秒解释、2286-11-20 之后的秒值被按毫秒解释。
     * 需要精确单位时建议调用方显式换算（秒：value*1000；毫秒：value）。
     * toDate/toInstant/parseMaybeMills 共用此判定，保证同一时间戳语义一致。</p>
     *
     * @param value 时间戳
     * @return 毫秒时间戳；负数返回 null
     */
    private Long toMillis(Long value) {
        // 时间戳不存在负数，负数视为非法输入返回 null
        if (value < 0) {
            return null;
        }
        if (value <= MIN_MILLS) {
            return value * 1000;
        }
        return value;
    }

    /**
     * Instant 转 Date
     *
     * @param instant Instant
     * @return Date
     */
    public Date toDate(Instant instant) {
        if (null == instant) {
            return null;
        }
        return Date.from(instant);
    }

    /**
     * LocalDateTime 转 Date
     *
     * @param localDateTime localDateTime
     * @return Date
     */
    public Date toDate(LocalDateTime localDateTime) {
        return toDate(toInstant(localDateTime));
    }

    /**
     * LocalDate 转 Date
     *
     * @param localDate localDate
     * @return Date
     */
    public Date toDate(LocalDate localDate) {
        return toDate(toInstant(localDate));
    }

    /**
     * Date 转 Instant
     *
     * @param date Date
     * @return Instant
     */
    public Instant toInstant(Date date) {
        if (null == date) {
            return null;
        }
        return date.toInstant();
    }

    /**
     * 时间戳转 Instant（秒/毫秒自动识别，与 {@link #toDate(Long)} 语义一致，见 {@link #toMillis(Long)}）
     *
     * @param mills 时间戳
     * @return Instant
     */
    public Instant toInstant(Long mills) {
        if (null == mills) {
            return null;
        }
        val millis = toMillis(mills);
        return null == millis ? null : Instant.ofEpochMilli(millis);
    }

    /**
     * LocalDateTime 转 Instant
     *
     * @param localDateTime localDateTime
     * @return Date
     */
    public Instant toInstant(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }
        return toZonedDateTime(localDateTime).toInstant();
    }

    /**
     * LocalDate 转 Instant
     *
     * @param localDate localDate
     * @return Date
     */
    public Instant toInstant(LocalDate localDate) {
        if (null == localDate) {
            return null;
        }
        return toInstant(localDate.atStartOfDay());
    }

    /**
     * Date 转 LocalDate
     *
     * @param instant date
     * @return LocalDate
     */
    public LocalDate toLocalDate(Instant instant) {
        if (null == instant) {
            return null;
        }
        return toZonedDateTime(instant)
            .toLocalDate()
            ;
    }

    /**
     * Date 转 LocalTime
     *
     * @param instant date
     * @return LocalTime
     */
    public LocalTime toLocalTime(Instant instant) {
        if (null == instant) {
            return null;
        }
        return toZonedDateTime(instant)
            .toLocalTime()
            ;
    }

    /**
     * Date 转 LocalDateTime
     *
     * @param instant date
     * @return LocalDateTime
     */
    public LocalDateTime toLocalDateTime(Instant instant) {
        if (null == instant) {
            return null;
        }
        return toZonedDateTime(instant)
            .toLocalDateTime()
            ;
    }

    /**
     * Date 转 ZonedDateTime
     *
     * @param date date
     * @return ZonedDateTime
     */
    public ZonedDateTime toZonedDateTime(Date date) {
        if (null == date) {
            return null;
        }
        return toZonedDateTime(toInstant(date));
    }

    /**
     * LocalDateTime 转 ZonedDateTime
     *
     * @param localDateTime localDateTime
     * @return ZonedDateTime
     */
    public ZonedDateTime toZonedDateTime(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }
        return localDateTime.atZone(DEFAULT_ZONE_ID);
    }

    /**
     * Instant 转 ZonedDateTime
     *
     * @param instant instant
     * @return ZonedDateTime
     */
    public ZonedDateTime toZonedDateTime(Instant instant) {
        if (null == instant) {
            return null;
        }
        return ZonedDateTime.ofInstant(instant, DEFAULT_ZONE_ID);
    }

    /**
     * Instant 计算
     *
     * @param value    时间
     * @param function 函数
     * @return 结果
     */
    public Instant calc(Instant value, CFunction<ZonedDateTime, ZonedDateTime> function) {

        if (null == value) {
            return null;
        }

        val zonedDateTime = toZonedDateTime(value);
        val result = function.apply(zonedDateTime);
        return result.toInstant();
    }

    /**
     * Instant 计算
     *
     * @param value    时间
     * @param function 函数
     * @return 结果
     */
    public Date calc(Date value, CFunction<ZonedDateTime, ZonedDateTime> function) {
        return toDate(calc(toInstant(value), function));
    }

    /**
     * Instant 计算
     *
     * @param value    时间
     * @param collection 集合
     * @param function 函数
     * @param <T> 泛型
     * @return 结果
     */
    public <T> Instant calc(
        Instant value,
        Collection<T> collection,
        CBiFunction<ZonedDateTime, T, ZonedDateTime> function
    ) {
        if(CollUtil.isEmpty(collection)) {
            return value;
        }
        return calc(value, zonedDateTime -> {

            var zonedDateTimeNew = zonedDateTime;
            for (T t : collection) {
                zonedDateTimeNew = function.apply(zonedDateTimeNew, t);
            }
            return zonedDateTimeNew;
        });
    }

    /**
     * Instant 计算
     *
     * @param value    时间
     * @param collection 集合
     * @param function 函数
     * @param <T> 泛型
     * @return 结果
     */
    public <T> Date calc(
        Date value,
        Collection<T> collection,
        CBiFunction<ZonedDateTime, T, ZonedDateTime> function
    ) {
        if(CollUtil.isEmpty(collection)) {
            return value;
        }
        return toDate(calc(toInstant(value), collection, function));
    }

    /**
     * Instant 计算
     *
     * @param value    时间
     * @param arr 数组
     * @param function 函数
     * @param <T> 泛型
     * @return 结果
     */
    public <T> Instant calc(
        Instant value,
        T[] arr,
        CBiFunction<ZonedDateTime, T, ZonedDateTime> function
    ) {
        if(ArrayUtil.isEmpty(arr)) {
            return value;
        }
        return calc(value, Arrays.asList(arr), function);
    }

    /**
     * Instant 计算
     *
     * @param value    时间
     * @param arr 数组
     * @param function 函数
     * @param <T> 泛型
     * @return 结果
     */
    public <T> Date calc(
        Date value,
        T[] arr,
        CBiFunction<ZonedDateTime, T, ZonedDateTime> function
    ) {
        if(ArrayUtil.isEmpty(arr)) {
            return value;
        }
        return calc(value, Arrays.asList(arr), function);
    }

    /**
     * Instant 加上指定时间
     *
     * @param value  时间
     * @param amount 数量
     * @param unit   单位
     * @return 结果
     */
    public Instant plus(Instant value, long amount, TemporalUnit unit) {
        return calc(value, zonedDateTime ->
            zonedDateTime.plus(amount, unit));
    }

    /**
     * Instant 减去指定时间
     *
     * @param value  时间
     * @param amount 数量
     * @param unit   单位
     * @return 结果
     */
    public Instant minus(Instant value, long amount, TemporalUnit unit) {
        return calc(value, zonedDateTime ->
            zonedDateTime.minus(amount, unit));
    }

    /**
     * Instant 加上指定时间
     *
     * @param value  时间
     * @param pairs 数量、单位集合
     * @return 结果
     */
    public Instant plus(Instant value, Collection<Pair<Long, TemporalUnit>> pairs) {
        return calc(value, pairs, (zonedDateTime, pair) ->
            zonedDateTime.plus(pair.getKey(), pair.getValue()));
    }

    /**
     * Instant 减去指定时间
     *
     * @param value  时间
     * @param pairs 数量、单位集合
     * @return 结果
     */
    public Instant minus(Instant value, Collection<Pair<Long, TemporalUnit>> pairs) {
        return calc(value, pairs, (zonedDateTime, pair) ->
            zonedDateTime.minus(pair.getKey(), pair.getValue()));
    }

    /**
     * Instant 加上指定时间
     *
     * @param value     时间
     * @param durations 指定时间段
     * @return 结果
     */
    public Instant plus(Instant value, Duration... durations) {
        return calc(value, durations, ZonedDateTime::plus);
    }

    /**
     * Instant 减去指定时间
     *
     * @param value     时间
     * @param durations 指定时间段
     * @return 结果
     */
    public Instant minus(Instant value, Duration... durations) {
        return calc(value, durations, ZonedDateTime::minus);
    }

    /**
     * Date 加上指定时间
     *
     * @param value  时间
     * @param amount 数量
     * @param unit   单位
     * @return 结果
     */
    public Date plus(Date value, long amount, TemporalUnit unit) {
        return calc(value, zonedDateTime ->
            zonedDateTime.plus(amount, unit));
    }

    /**
     * Date 减去指定时间
     *
     * @param value  时间
     * @param amount 数量
     * @param unit   单位
     * @return 结果
     */
    public Date minus(Date value, long amount, TemporalUnit unit) {
        return calc(value, zonedDateTime ->
            zonedDateTime.minus(amount, unit));
    }

    /**
     * Date 加上指定时间
     *
     * @param value 时间
     * @param pairs 数量、单位集合
     * @return 结果
     */
    public Date plus(Date value, Collection<Pair<Long, TemporalUnit>> pairs) {
        return calc(value, pairs, (zonedDateTime, pair) ->
            zonedDateTime.plus(pair.getKey(), pair.getValue()));
    }

    /**
     * Date 减去指定时间
     *
     * @param value 时间
     * @param pairs 数量、单位集合
     * @return 结果
     */
    public Date minus(Date value, Collection<Pair<Long, TemporalUnit>> pairs) {
        return calc(value, pairs, (zonedDateTime, pair) ->
            zonedDateTime.minus(pair.getKey(), pair.getValue()));
    }

    /**
     * Date 加上指定时间
     *
     * @param value     时间
     * @param durations 指定时间段
     * @return 结果
     */
    public Date plus(Date value, Duration... durations) {
        return calc(value, durations, ZonedDateTime::plus);
    }

    /**
     * Date 减去指定时间
     *
     * @param value     时间
     * @param durations 指定时间段
     * @return 结果
     */
    public Date minus(Date value, Duration... durations) {
        return calc(value, durations, ZonedDateTime::minus);
    }

    /**
     * 获取指定日期这一天的开始
     * @param instant 指定日期
     * @return 指定日期这一天的开始
     */
    public Instant getDayBegin(Instant instant) {

        if(null == instant) {
            return null;
        }

        return instant
            .atZone(ZoneId.systemDefault())
            .truncatedTo(ChronoUnit.DAYS)
            .toInstant();
    }

}
