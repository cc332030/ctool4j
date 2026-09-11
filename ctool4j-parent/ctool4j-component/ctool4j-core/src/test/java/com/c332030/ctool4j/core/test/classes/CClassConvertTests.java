package com.c332030.ctool4j.core.test.classes;

import cn.hutool.core.date.DateException;
import com.c332030.ctool4j.core.classes.CClassConvert;
import com.c332030.ctool4j.core.util.CDateUtils;
import com.c332030.ctool4j.definition.interfaces.ICValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

/**
 * <p>
 * Description: CClassConvertTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「布尔 / 整数 / 长整数 / 浮点 / 大数 / 日期 / 枚举值 / 字符串」多个类型维度组织。</li>
 *   <li>数值转换覆盖正例、空/null 返回 null、非法值抛异常、溢出返回 null、原语默认值 0。</li>
 *   <li>布尔覆盖 true/false/null/大小写/数字 "1"/"0"（Q24）。</li>
 *   <li>日期覆盖 parse/format/mills/instant 互转；字符串系列覆盖 objectStr 及各类 xxxStr null 返回 null。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对各转换语义与空值/异常兜底的约定。</li>
 *   <li>依据测试方法（等价类/边界值/异常路径）：空/null、非法值、溢出、默认 0。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：toBoolean（true/false/null/大小写/数字）；toInt（正/负/null/非法/溢出/long/int）与 intValue；</li>
 *   <li>toLong 与 longValue；toFloat/floatValue、toDouble/doubleValue；toBigDecimal（string/float/double/long/</li>
 *   <li>int）与 null；toEnumIntegerValue/toEnumStringValue；parseDateTime/formatDateTime/toMills/fromMills/</li>
 *   <li>toInstant/toDate；objectStr 系列（boolean/int/long/float/double/bigDecimal）。</li>
 *   <li>未覆盖：无（覆盖了全部公开转换方法）。</li>
 * </ul>
 * <h2>布尔转换</h2>
 * <ul>
 *   <li>1.1 toBoolean：true/false/null/大小写/数字 {@code 1}/{@code 0}（Q24）（toBoolean）</li>
 * </ul>
 * <h2>整数转换</h2>
 * <ul>
 *   <li>2.1 toInt：正/负/null/空（toInt）</li>
 *   <li>2.2 toInt 非法值：抛 NumberFormatException（toInt_error）</li>
 *   <li>2.3 toInt(Long)：溢出返回 null（toInt_long）</li>
 *   <li>2.4 toInt(int)：返回自身（toInt_int）</li>
 *   <li>2.5 intValue：null 返回 0（intValue）</li>
 * </ul>
 * <h2>长整数转换</h2>
 * <ul>
 *   <li>3.1 toLong：正/null/空（toLong）</li>
 *   <li>3.2 toLong 非法值：抛 NumberFormatException（toLong_error）</li>
 *   <li>3.3 toLong(int/Integer/Long)：各形态（toLong_int）</li>
 *   <li>3.4 longValue：null 返回 0（longValue）</li>
 * </ul>
 * <h2>浮点转换</h2>
 * <ul>
 *   <li>4.1 toFloat：正例/null（toFloat）</li>
 *   <li>4.2 floatValue：null 返回 0、String/BigDecimal 形态（floatValue）</li>
 *   <li>4.3 toDouble：正例/null（toDouble）</li>
 *   <li>4.4 doubleValue：null 返回 0、String/Float/BigDecimal 形态（doubleValue）</li>
 * </ul>
 * <h2>大数转换</h2>
 * <ul>
 *   <li>5.1 toBigDecimal：String 正例/空 null/非法抛异常（toBigDecimal）</li>
 *   <li>5.2 toBigDecimal(float)：含 null（toBigDecimal_float）</li>
 *   <li>5.3 toBigDecimal(double)：含 null（toBigDecimal_double）</li>
 *   <li>5.4 toBigDecimal(long)：含 null（toBigDecimal_long）</li>
 *   <li>5.5 toBigDecimal(int)：含 null（toBigDecimal_int）</li>
 * </ul>
 * <h2>日期转换</h2>
 * <ul>
 *   <li>6.1 parseDateTime：解析后格式化精确还原；非法输入抛 DateException（parseDateTime）</li>
 *   <li>6.2 formatDateTime：格式化正确（formatDateTime）</li>
 *   <li>6.3 toMills：含 null（toMills）</li>
 *   <li>6.4 fromMills：含 null（fromMills）</li>
 *   <li>6.5 toInstant：含 null（toInstant）</li>
 *   <li>6.6 toDate：含 null（toDate）</li>
 * </ul>
 * <h2>枚举值转换</h2>
 * <ul>
 *   <li>7.1 toEnumIntegerValue：取值含 null（toEnumIntegerValue）</li>
 *   <li>7.2 toEnumStringValue：取值含 null（toEnumStringValue）</li>
 * </ul>
 * <h2>字符串转换</h2>
 * <ul>
 *   <li>8.1 objectStr：含 null（objectStr）</li>
 *   <li>8.2 booleanStr/intStr/longStr/floatStr/doubleStr/bigDecimalStr：各含 null（booleanStr/intStr/longStr/floatStr/doubleStr/bigDecimalStr）</li>
 * </ul>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class CClassConvertTests {

    // ---- toBoolean ----

    /**
     * 对应测试用例 1.1：true/false/null/大小写/数字 {@code 1}/{@code 0}（Q24）
     */
    @Test
    public void toBoolean() {

        Assertions.assertEquals(true, CClassConvert.toBoolean("true"));
        Assertions.assertEquals(true, CClassConvert.toBoolean("TRUE"));
        Assertions.assertEquals(false, CClassConvert.toBoolean(null));
        Assertions.assertEquals(false, CClassConvert.toBoolean("abc"));
        Assertions.assertEquals(false, CClassConvert.toBoolean("true "));
        Assertions.assertEquals(false, CClassConvert.toBoolean(" true"));
        // Q24 修复：兼容 "1"/"0" 数字形式
        Assertions.assertEquals(true, CClassConvert.toBoolean("1"));
        Assertions.assertEquals(false, CClassConvert.toBoolean("0"));

    }

    // ---- toInt ----

    /**
     * 对应测试用例 2.1：正/负/null/空
     */
    @Test
    public void toInt() {

        Assertions.assertEquals(0, CClassConvert.toInt("0"));
        Assertions.assertEquals(123, CClassConvert.toInt("123"));
        Assertions.assertEquals(-123, CClassConvert.toInt("-123"));
        Assertions.assertNull(CClassConvert.toInt(""));
        Assertions.assertNull(CClassConvert.toInt(" "));
        Assertions.assertNull(CClassConvert.toInt((String) null));

    }

    /**
     * 对应测试用例 2.2：toInt 非法值：抛 NumberFormatException
     */
    @Test
    public void toInt_error() {

        Assertions.assertThrowsExactly(NumberFormatException.class, () -> CClassConvert.toInt("abc"));

        // 笔误值/随意捏造值/范围外值
        Assertions.assertThrowsExactly(NumberFormatException.class, () -> CClassConvert.toInt("1.5"));
        Assertions.assertThrowsExactly(NumberFormatException.class, () -> CClassConvert.toInt("1,000"));
        Assertions.assertThrowsExactly(NumberFormatException.class, () -> CClassConvert.toInt("!@#"));
        Assertions.assertThrowsExactly(NumberFormatException.class, () -> CClassConvert.toInt("0x1F"));
        Assertions.assertThrowsExactly(NumberFormatException.class, () -> CClassConvert.toInt("99999999999"));

    }

    /**
     * 对应测试用例 2.3：toInt(Long)：溢出返回 null
     */
    @Test
    public void toInt_long() {

        Assertions.assertEquals(0, CClassConvert.toInt(0L));
        Assertions.assertEquals(123, CClassConvert.toInt(123L));
        Assertions.assertNull(CClassConvert.toInt((Long) null));
        Assertions.assertNull(CClassConvert.toInt(9223372036854775807L));

    }

    /**
     * 对应测试用例 2.4：toInt(int)：返回自身
     */
    @Test
    public void toInt_int() {

        Assertions.assertEquals(123, CClassConvert.toInt(123));

    }

    // ---- intValue ----

    /**
     * 对应测试用例 2.5：null 返回 0
     */
    @Test
    public void intValue() {

        Assertions.assertEquals(0, CClassConvert.intValue(null));
        Assertions.assertEquals(0, CClassConvert.intValue(0L));
        Assertions.assertEquals(123, CClassConvert.intValue(123L));

    }

    // ---- toLong ----

    /**
     * 对应测试用例 3.1：正/null/空
     */
    @Test
    public void toLong() {

        Assertions.assertEquals(0L, CClassConvert.toLong("0"));
        Assertions.assertEquals(123L, CClassConvert.toLong("123"));
        Assertions.assertNull(CClassConvert.toLong(""));
        Assertions.assertNull(CClassConvert.toLong((String) null));

    }

    /**
     * 对应测试用例 3.2：toLong 非法值：抛 NumberFormatException
     */
    @Test
    public void toLong_error() {

        Assertions.assertThrowsExactly(NumberFormatException.class, () -> CClassConvert.toLong("abc"));

        // 笔误值/随意捏造值/范围外值
        Assertions.assertThrowsExactly(NumberFormatException.class, () -> CClassConvert.toLong("1.5"));
        Assertions.assertThrowsExactly(NumberFormatException.class, () -> CClassConvert.toLong("1,000"));
        Assertions.assertThrowsExactly(NumberFormatException.class, () -> CClassConvert.toLong("99999999999999999999"));

    }

    /**
     * 对应测试用例 3.3：toLong(int/Integer/Long)：各形态
     */
    @Test
    public void toLong_int() {

        Assertions.assertEquals(123L, CClassConvert.toLong(123));
        Assertions.assertNull(CClassConvert.toLong((Integer) null));
        Assertions.assertEquals(123L, CClassConvert.toLong(123L));

    }

    // ---- longValue ----

    /**
     * 对应测试用例 3.4：null 返回 0
     */
    @Test
    public void longValue() {

        Assertions.assertEquals(0L, CClassConvert.longValue(null));
        Assertions.assertEquals(0L, CClassConvert.longValue(0));
        Assertions.assertEquals(123L, CClassConvert.longValue(123));

    }

    // ---- toFloat / floatValue ----

    /**
     * 对应测试用例 4.1：正例/null
     */
    @Test
    public void toFloat() {

        Assertions.assertEquals(1.5f, CClassConvert.toFloat("1.5"));
        Assertions.assertNull(CClassConvert.toFloat(null));

    }

    /**
     * 对应测试用例 4.2：null 返回 0、String/BigDecimal 形态
     */
    @Test
    public void floatValue() {

        Assertions.assertEquals(0, CClassConvert.floatValue((String) null));
        Assertions.assertEquals(1.5f, CClassConvert.floatValue("1.5"));
        Assertions.assertEquals(0, CClassConvert.floatValue((BigDecimal) null));
        Assertions.assertEquals(2.5f, CClassConvert.floatValue(new BigDecimal("2.5")));

    }

    // ---- toDouble / doubleValue ----

    /**
     * 对应测试用例 4.3：正例/null
     */
    @Test
    public void toDouble() {

        Assertions.assertEquals(1.5d, CClassConvert.toDouble("1.5"));
        Assertions.assertNull(CClassConvert.toDouble(null));

    }

    /**
     * 对应测试用例 4.4：null 返回 0、String/Float/BigDecimal 形态
     */
    @Test
    public void doubleValue() {

        Assertions.assertEquals(0, CClassConvert.doubleValue((String) null));
        Assertions.assertEquals(1.5d, CClassConvert.doubleValue("1.5"));
        Assertions.assertEquals(0, CClassConvert.doubleValue((Float) null));
        Assertions.assertEquals(2.5d, CClassConvert.doubleValue(2.5f));
        Assertions.assertEquals(0, CClassConvert.doubleValue((BigDecimal) null));
        Assertions.assertEquals(3.5d, CClassConvert.doubleValue(new BigDecimal("3.5")));

    }

    // ---- toBigDecimal ----

    /**
     * 对应测试用例 5.1：String 正例/空 null/非法抛异常
     */
    @Test
    public void toBigDecimal() {

        Assertions.assertEquals(new BigDecimal("0"), CClassConvert.toBigDecimal("0"));
        Assertions.assertEquals(new BigDecimal("123.45"), CClassConvert.toBigDecimal("123.45"));
        Assertions.assertNull(CClassConvert.toBigDecimal(""));
        Assertions.assertThrowsExactly(NumberFormatException.class, () -> CClassConvert.toBigDecimal(" "));
        Assertions.assertThrowsExactly(NumberFormatException.class, () -> CClassConvert.toBigDecimal("abc"));
        Assertions.assertThrowsExactly(NumberFormatException.class, () -> CClassConvert.toBigDecimal("1,000"));
        Assertions.assertNull(CClassConvert.toBigDecimal((String) null));

    }

    /**
     * 对应测试用例 5.2：toBigDecimal(float)：含 null
     */
    @Test
    public void toBigDecimal_float() {

        Assertions.assertEquals(new BigDecimal("0.1"), CClassConvert.toBigDecimal(0.1f));
        Assertions.assertNull(CClassConvert.toBigDecimal((Float) null));

    }

    /**
     * 对应测试用例 5.3：toBigDecimal(double)：含 null
     */
    @Test
    public void toBigDecimal_double() {

        Assertions.assertEquals(new BigDecimal("0.1"), CClassConvert.toBigDecimal(0.1d));
        Assertions.assertNull(CClassConvert.toBigDecimal((Double) null));

    }

    /**
     * 对应测试用例 5.4：toBigDecimal(long)：含 null
     */
    @Test
    public void toBigDecimal_long() {

        Assertions.assertEquals(new BigDecimal("123"), CClassConvert.toBigDecimal(123L));
        Assertions.assertNull(CClassConvert.toBigDecimal((Long) null));

    }

    /**
     * 对应测试用例 5.5：toBigDecimal(int)：含 null
     */
    @Test
    public void toBigDecimal_int() {

        Assertions.assertEquals(new BigDecimal("123"), CClassConvert.toBigDecimal(123));
        Assertions.assertNull(CClassConvert.toBigDecimal((Integer) null));

    }

    // ---- toEnumIntegerValue / toEnumStringValue ----

    /**
     * 对应测试用例 7.1：取值含 null
     */
    @Test
    public void toEnumIntegerValue() {

        ICValue<Integer> value = new ICValue<Integer>() {
            @Override
            public Integer getValue() {
                return 1;
            }
        };
        Assertions.assertEquals(1, CClassConvert.toEnumIntegerValue(value));
        Assertions.assertNull(CClassConvert.toEnumIntegerValue(null));

    }

    /**
     * 对应测试用例 7.2：取值含 null
     */
    @Test
    public void toEnumStringValue() {

        ICValue<String> value = new ICValue<String>() {
            @Override
            public String getValue() {
                return "INSERT";
            }
        };
        Assertions.assertEquals("INSERT", CClassConvert.toEnumStringValue(value));
        Assertions.assertNull(CClassConvert.toEnumStringValue(null));

    }

    // ---- Date / Instant / Mills ----

    /**
     * 对应测试用例 6.1：解析后格式化精确还原；非法输入抛 DateException
     */
    @Test
    public void parseDateTime() {

        // 解析后再格式化，验证解析结果精确还原（而非仅断言非空）
        Assertions.assertEquals("2025-03-03 08:01:03",
                CClassConvert.formatDateTime(CClassConvert.parseDateTime("2025-03-03 08:01:03")));
        // 非法日期字符串抛出 DateException
        Assertions.assertThrowsExactly(DateException.class, () -> CClassConvert.parseDateTime("not-a-date"));

    }

    /**
     * 对应测试用例 6.2：格式化正确
     */
    @Test
    public void formatDateTime() {

        Date date = CDateUtils.parseMaybeMills("2025-03-03 08:01:03");
        Assertions.assertEquals("2025-03-03 08:01:03", CClassConvert.formatDateTime(date));

    }

    /**
     * 对应测试用例 6.3：含 null
     */
    @Test
    public void toMills() {

        Date date = new Date(123L);
        Assertions.assertEquals(123L, CClassConvert.toMills(date));
        Assertions.assertNull(CClassConvert.toMills(null));

    }

    /**
     * 对应测试用例 6.4：含 null
     */
    @Test
    public void fromMills() {

        Assertions.assertEquals(new Date(123L), CClassConvert.fromMills(123L));
        Assertions.assertNull(CClassConvert.fromMills(null));

    }

    /**
     * 对应测试用例 6.5：含 null
     */
    @Test
    public void toInstant() {

        Date date = new Date(123L);
        Assertions.assertEquals(date.toInstant(), CClassConvert.toInstant(date));
        Assertions.assertNull(CClassConvert.toInstant(null));

    }

    /**
     * 对应测试用例 6.6：含 null
     */
    @Test
    public void toDate() {

        Instant instant = Instant.ofEpochMilli(123L);
        Assertions.assertEquals(Date.from(instant), CClassConvert.toDate(instant));
        Assertions.assertNull(CClassConvert.toDate(null));

    }

    // ---- objectStr 系列 ----

    /**
     * 对应测试用例 8.1：含 null
     */
    @Test
    public void objectStr() {

        Assertions.assertEquals("123", CClassConvert.objectStr(123));
        Assertions.assertEquals("1.5", CClassConvert.objectStr(1.5));
        Assertions.assertNull(CClassConvert.objectStr(null));

    }

    /**
     * 对应测试用例 8.2：booleanStr/intStr/longStr/floatStr/doubleStr/bigDecimalStr：各含 null（booleanStr/intStr/longStr/floatStr/doubleStr/bigDecimalStr）
     */
    @Test
    public void booleanStr() {

        Assertions.assertEquals("true", CClassConvert.booleanStr(true));
        Assertions.assertNull(CClassConvert.booleanStr(null));

    }

    /**
     * 对应测试用例 8.2：booleanStr/intStr/longStr/floatStr/doubleStr/bigDecimalStr：各含 null（booleanStr/intStr/longStr/floatStr/doubleStr/bigDecimalStr）
     */
    @Test
    public void intStr() {

        Assertions.assertEquals("123", CClassConvert.intStr(123));
        Assertions.assertNull(CClassConvert.intStr(null));

    }

    /**
     * 对应测试用例 8.2：booleanStr/intStr/longStr/floatStr/doubleStr/bigDecimalStr：各含 null（booleanStr/intStr/longStr/floatStr/doubleStr/bigDecimalStr）
     */
    @Test
    public void longStr() {

        Assertions.assertEquals("123", CClassConvert.longStr(123L));
        Assertions.assertNull(CClassConvert.longStr(null));

    }

    /**
     * 对应测试用例 8.2：booleanStr/intStr/longStr/floatStr/doubleStr/bigDecimalStr：各含 null（booleanStr/intStr/longStr/floatStr/doubleStr/bigDecimalStr）
     */
    @Test
    public void floatStr() {

        Assertions.assertEquals("1.5", CClassConvert.floatStr(1.5f));
        Assertions.assertNull(CClassConvert.floatStr(null));

    }

    /**
     * 对应测试用例 8.2：booleanStr/intStr/longStr/floatStr/doubleStr/bigDecimalStr：各含 null（booleanStr/intStr/longStr/floatStr/doubleStr/bigDecimalStr）
     */
    @Test
    public void doubleStr() {

        Assertions.assertEquals("1.5", CClassConvert.doubleStr(1.5d));
        Assertions.assertNull(CClassConvert.doubleStr(null));

    }

    /**
     * 对应测试用例 8.2：booleanStr/intStr/longStr/floatStr/doubleStr/bigDecimalStr：各含 null（booleanStr/intStr/longStr/floatStr/doubleStr/bigDecimalStr）
     */
    @Test
    public void bigDecimalStr() {

        Assertions.assertEquals("123", CClassConvert.bigDecimalStr(new BigDecimal("123")));
        Assertions.assertNull(CClassConvert.bigDecimalStr(null));

    }

}
