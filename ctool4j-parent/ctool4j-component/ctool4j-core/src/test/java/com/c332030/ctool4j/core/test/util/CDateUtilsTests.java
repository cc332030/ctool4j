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
 * <p>`com.c332030.ctool4j.core.util.CDateUtils`（CDateUtils）的测试用例</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「格式化 / 解析 / 转换 / 加减运算 / 时间戳解析」多个维度组织。</li>
 *   <li>以固定日期 {@code 2025-03-03 08:01:03} 为基准，验证格式化与解析往返一致。</li>
 *   <li>加减运算覆盖秒/分/时/天/月/年六个时间单位的 plus/minus（含 0 单位边界），以及 Duration、单位数组版本。</li>
 *   <li>时间戳解析覆盖毫秒时间戳与秒级时间戳（9999999999）两形态。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对格式化模式、时间戳智能解析、加减运算的约定。</li>
 *   <li>依据测试方法（等价类/边界值）：六个单位、0 单位边界、时间戳长短、null 入参。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：formatDate/Time/DateTime/format；parse（parseMaybeMills/parseInstantMaybeMills 日期/时间/日期时间/</li>
 *   <li>毫秒字符串）；toDate（null/mills/instant/边界时间戳）；toInstant（null/mills/date）；plus/minus 六单位</li>
 *   <li>（Instant/Date 两入口、0 单位边界）；plusArr/minusArr 单位数组；parseMaybeMills 毫秒与秒级时间戳。</li>
 *   <li>未覆盖：{@code calc}/{@code getDayBegin}/{@code toLocalDate}/{@code toLocalTime}/{@code toLocalDateTime}/{@code toZonedDateTime} 等其余入口</li>
 *   <li>（当前测试聚焦格式化/解析/转换/加减/时间戳核心路径，其余入口可后续批次补充）。</li>
 * </ul>
 * <h2>格式化</h2>
 * <ul>
 *   <li>1.1 formatDate/Time/DateTime 与自定义 pattern（format）</li>
 * </ul>
 * <h2>解析</h2>
 * <ul>
 *   <li>2.1 parseMaybeMills/parseInstantMaybeMills：日期/时间/日期时间/毫秒字符串解析（parse）</li>
 * </ul>
 * <h2>转换</h2>
 * <ul>
 *   <li>3.1 toDate：null 返回 null；mills/instant 转换；边界时间戳（9999999999）（toDate）</li>
 *   <li>3.2 toInstant：null 返回 null；mills/date 转换（toInstant）</li>
 * </ul>
 * <h2>加减运算（plus/minus）</h2>
 * <ul>
 *   <li>4.1 秒：plus/minus 1 秒（Instant/Date），0 秒不变（plusSecond / minusSecond）</li>
 *   <li>4.2 分：plus/minus 1 分（Instant/Date），0 分不变（plusMinute / minusMinute）</li>
 *   <li>4.3 时：plus/minus 1 时（Instant/Date），0 时不变（plusHour / minusHour）</li>
 *   <li>4.4 天：plus/minus 1 天（Instant/Date），0 天不变（plusDay / minusDay）</li>
 *   <li>4.5 月：plus/minus 1 月（单位版本），0 月不变（plusMonth / minusMonth）</li>
 *   <li>4.6 年：plus/minus 1 年（单位版本），0 年不变（plusYear / minusYear）</li>
 *   <li>4.7 单位数组：plusArr 六单位同时增加（plusArr）</li>
 *   <li>4.8 单位数组：minusArr 六单位同时减少（minusArr）</li>
 * </ul>
 * <h2>时间戳解析（parseMaybeMills）</h2>
 * <ul>
 *   <li>5.1 毫秒时间戳：{@code 1767492170633} → {@code 2026-01-04 10:02:50}（parseMaybeMills）</li>
 *   <li>5.2 秒级时间戳：{@code 9999999999} → {@code 2286-11-21 01:46:39}（parseMaybeMillsForSecondMills）</li>
 * </ul>
 *
 * @since 2025/12/7
 * @version 1.0
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

    /**
     * 测试日期时间格式化
     * 对应测试用例 1.1：formatDate/Time/DateTime 与自定义 pattern
     */
    @Test
    public void format() {

        Assertions.assertEquals(DATE_STR, CDateUtils.formatDate(INSTANT));
        Assertions.assertEquals(TIME_STR, CDateUtils.formatTime(INSTANT));
        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(INSTANT));
        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.format(INSTANT, DatePattern.NORM_DATETIME_PATTERN));

    }

    /**
     * 测试日期时间解析
     * 对应测试用例 2.1：parseMaybeMills/parseInstantMaybeMills：日期/时间/日期时间/毫秒字符串解析
     */
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

    /**
     * 测试转换为 Date
     * 对应测试用例 3.1：null 返回 null；mills/instant 转换；边界时间戳（9999999999）
     */
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

    /**
     * 测试转换为 Instant
     * 对应测试用例 3.2：null 返回 null；mills/date 转换
     */
    @Test
    public void toInstant() {

        Assertions.assertNull(CDateUtils.toInstant((Long) null));
        Assertions.assertNull(CDateUtils.toInstant((Date) null));
        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(CDateUtils.toInstant(MILLS)));
        Assertions.assertEquals(DATE_TIME_STR, CDateUtils.formatDateTime(CDateUtils.toInstant(DATE)));

    }

    /**
     * 测试增加秒
     * 对应测试用例 4.1：秒：plus/minus 1 秒（Instant/Date），0 秒不变（plusSecond / minusSecond）
     */
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

    /**
     * 测试减少秒
     * 对应测试用例 4.1：秒：plus/minus 1 秒（Instant/Date），0 秒不变（plusSecond / minusSecond）
     */
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

    /**
     * 测试增加分钟
     * 对应测试用例 4.2：分：plus/minus 1 分（Instant/Date），0 分不变（plusMinute / minusMinute）
     */
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

    /**
     * 测试减少分钟
     * 对应测试用例 4.2：分：plus/minus 1 分（Instant/Date），0 分不变（plusMinute / minusMinute）
     */
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

    /**
     * 测试增加小时
     * 对应测试用例 4.3：时：plus/minus 1 时（Instant/Date），0 时不变（plusHour / minusHour）
     */
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

    /**
     * 测试减少小时
     * 对应测试用例 4.3：时：plus/minus 1 时（Instant/Date），0 时不变（plusHour / minusHour）
     */
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

    /**
     * 测试增加天
     * 对应测试用例 4.4：天：plus/minus 1 天（Instant/Date），0 天不变（plusDay / minusDay）
     */
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

    /**
     * 测试减少天
     * 对应测试用例 4.4：天：plus/minus 1 天（Instant/Date），0 天不变（plusDay / minusDay）
     */
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

    /**
     * 测试增加月
     * 对应测试用例 4.5：月：plus/minus 1 月（单位版本），0 月不变（plusMonth / minusMonth）
     */
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

    /**
     * 测试减少月
     * 对应测试用例 4.5：月：plus/minus 1 月（单位版本），0 月不变（plusMonth / minusMonth）
     */
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

    /**
     * 测试增加年
     * 对应测试用例 4.6：年：plus/minus 1 年（单位版本），0 年不变（plusYear / minusYear）
     */
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

    /**
     * 测试减少年
     * 对应测试用例 4.6：年：plus/minus 1 年（单位版本），0 年不变（plusYear / minusYear）
     */
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

    /**
     * 测试按时间单位数组增加
     * 对应测试用例 4.7：单位数组：plusArr 六单位同时增加
     */
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

    /**
     * 测试按时间单位数组减少
     * 对应测试用例 4.8：单位数组：minusArr 六单位同时减少
     */
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

    /**
     * 测试毫秒字符串解析
     * 对应测试用例 5.1：毫秒时间戳：{@code 1767492170633} → {@code 2026-01-04 10:02:50}
     */
    @Test
    public void parseMaybeMills() {

        val date = CDateUtils.parseMaybeMills("1767492170633");
        Assertions.assertEquals("2026-01-04 10:02:50", DateUtil.formatDateTime(date));

    }

    /**
     * 测试秒级时间戳字符串解析
     * 对应测试用例 5.2：秒级时间戳：{@code 9999999999} → {@code 2286-11-21 01:46:39}
     */
    @Test
    public void parseMaybeMillsForSecondMills() {

        val date = CDateUtils.parseMaybeMills("9999999999");
        Assertions.assertEquals("2286-11-21 01:46:39", DateUtil.formatDateTime(date));

    }

}
