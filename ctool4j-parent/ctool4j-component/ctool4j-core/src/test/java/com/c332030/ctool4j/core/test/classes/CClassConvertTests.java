package com.c332030.ctool4j.core.test.classes;

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
 * @since 2025/12/12
 */
public class CClassConvertTests {

    // ---- toBoolean ----

    @Test
    public void toBoolean() {

        Assertions.assertEquals(true, CClassConvert.toBoolean("true"));
        Assertions.assertEquals(true, CClassConvert.toBoolean("TRUE"));
        Assertions.assertEquals(false, CClassConvert.toBoolean(null));
        Assertions.assertEquals(false, CClassConvert.toBoolean("abc"));
        Assertions.assertEquals(false, CClassConvert.toBoolean("true "));
        Assertions.assertEquals(false, CClassConvert.toBoolean(" true"));

    }

    // ---- toInt ----

    @Test
    public void toInt() {

        Assertions.assertEquals(0, CClassConvert.toInt("0"));
        Assertions.assertEquals(123, CClassConvert.toInt("123"));
        Assertions.assertEquals(-123, CClassConvert.toInt("-123"));
        Assertions.assertNull(CClassConvert.toInt(""));
        Assertions.assertNull(CClassConvert.toInt(" "));
        Assertions.assertNull(CClassConvert.toInt((String) null));

    }

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

    @Test
    public void toInt_long() {

        Assertions.assertEquals(0, CClassConvert.toInt(0L));
        Assertions.assertEquals(123, CClassConvert.toInt(123L));
        Assertions.assertNull(CClassConvert.toInt((Long) null));
        Assertions.assertNull(CClassConvert.toInt(9223372036854775807L));

    }

    @Test
    public void toInt_int() {

        Assertions.assertEquals(123, CClassConvert.toInt(123));

    }

    // ---- intValue ----

    @Test
    public void intValue() {

        Assertions.assertEquals(0, CClassConvert.intValue(null));
        Assertions.assertEquals(0, CClassConvert.intValue(0L));
        Assertions.assertEquals(123, CClassConvert.intValue(123L));

    }

    // ---- toLong ----

    @Test
    public void toLong() {

        Assertions.assertEquals(0L, CClassConvert.toLong("0"));
        Assertions.assertEquals(123L, CClassConvert.toLong("123"));
        Assertions.assertNull(CClassConvert.toLong(""));
        Assertions.assertNull(CClassConvert.toLong((String) null));

    }

    @Test
    public void toLong_error() {

        Assertions.assertThrowsExactly(NumberFormatException.class, () -> CClassConvert.toLong("abc"));

        // 笔误值/随意捏造值/范围外值
        Assertions.assertThrowsExactly(NumberFormatException.class, () -> CClassConvert.toLong("1.5"));
        Assertions.assertThrowsExactly(NumberFormatException.class, () -> CClassConvert.toLong("1,000"));
        Assertions.assertThrowsExactly(NumberFormatException.class, () -> CClassConvert.toLong("99999999999999999999"));

    }

    @Test
    public void toLong_int() {

        Assertions.assertEquals(123L, CClassConvert.toLong(123));
        Assertions.assertNull(CClassConvert.toLong((Integer) null));
        Assertions.assertEquals(123L, CClassConvert.toLong(123L));

    }

    // ---- longValue ----

    @Test
    public void longValue() {

        Assertions.assertEquals(0L, CClassConvert.longValue(null));
        Assertions.assertEquals(0L, CClassConvert.longValue(0));
        Assertions.assertEquals(123L, CClassConvert.longValue(123));

    }

    // ---- toFloat / floatValue ----

    @Test
    public void toFloat() {

        Assertions.assertEquals(1.5f, CClassConvert.toFloat("1.5"));
        Assertions.assertNull(CClassConvert.toFloat(null));

    }

    @Test
    public void floatValue() {

        Assertions.assertEquals(0, CClassConvert.floatValue((String) null));
        Assertions.assertEquals(1.5f, CClassConvert.floatValue("1.5"));
        Assertions.assertEquals(0, CClassConvert.floatValue((BigDecimal) null));
        Assertions.assertEquals(2.5f, CClassConvert.floatValue(new BigDecimal("2.5")));

    }

    // ---- toDouble / doubleValue ----

    @Test
    public void toDouble() {

        Assertions.assertEquals(1.5d, CClassConvert.toDouble("1.5"));
        Assertions.assertNull(CClassConvert.toDouble(null));

    }

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

    @Test
    public void toBigDecimal_float() {

        Assertions.assertEquals(new BigDecimal("0.1"), CClassConvert.toBigDecimal(0.1f));
        Assertions.assertNull(CClassConvert.toBigDecimal((Float) null));

    }

    @Test
    public void toBigDecimal_double() {

        Assertions.assertEquals(new BigDecimal("0.1"), CClassConvert.toBigDecimal(0.1d));
        Assertions.assertNull(CClassConvert.toBigDecimal((Double) null));

    }

    @Test
    public void toBigDecimal_long() {

        Assertions.assertEquals(new BigDecimal("123"), CClassConvert.toBigDecimal(123L));
        Assertions.assertNull(CClassConvert.toBigDecimal((Long) null));

    }

    @Test
    public void toBigDecimal_int() {

        Assertions.assertEquals(new BigDecimal("123"), CClassConvert.toBigDecimal(123));
        Assertions.assertNull(CClassConvert.toBigDecimal((Integer) null));

    }

    // ---- toEnumIntegerValue / toEnumStringValue ----

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

    @Test
    public void parseDateTime() {

        Assertions.assertNotNull(CClassConvert.parseDateTime("2025-03-03 08:01:03"));

    }

    @Test
    public void formatDateTime() {

        Date date = CDateUtils.parseMaybeMills("2025-03-03 08:01:03");
        Assertions.assertEquals("2025-03-03 08:01:03", CClassConvert.formatDateTime(date));

    }

    @Test
    public void toMills() {

        Date date = new Date(123L);
        Assertions.assertEquals(123L, CClassConvert.toMills(date));
        Assertions.assertNull(CClassConvert.toMills(null));

    }

    @Test
    public void fromMills() {

        Assertions.assertEquals(new Date(123L), CClassConvert.fromMills(123L));
        Assertions.assertNull(CClassConvert.fromMills(null));

    }

    @Test
    public void toInstant() {

        Date date = new Date(123L);
        Assertions.assertEquals(date.toInstant(), CClassConvert.toInstant(date));
        Assertions.assertNull(CClassConvert.toInstant(null));

    }

    @Test
    public void toDate() {

        Instant instant = Instant.ofEpochMilli(123L);
        Assertions.assertEquals(Date.from(instant), CClassConvert.toDate(instant));
        Assertions.assertNull(CClassConvert.toDate(null));

    }

    // ---- objectStr 系列 ----

    @Test
    public void objectStr() {

        Assertions.assertEquals("123", CClassConvert.objectStr(123));
        Assertions.assertEquals("1.5", CClassConvert.objectStr(1.5));
        Assertions.assertNull(CClassConvert.objectStr(null));

    }

    @Test
    public void booleanStr() {

        Assertions.assertEquals("true", CClassConvert.booleanStr(true));
        Assertions.assertNull(CClassConvert.booleanStr(null));

    }

    @Test
    public void intStr() {

        Assertions.assertEquals("123", CClassConvert.intStr(123));
        Assertions.assertNull(CClassConvert.intStr(null));

    }

    @Test
    public void longStr() {

        Assertions.assertEquals("123", CClassConvert.longStr(123L));
        Assertions.assertNull(CClassConvert.longStr(null));

    }

    @Test
    public void floatStr() {

        Assertions.assertEquals("1.5", CClassConvert.floatStr(1.5f));
        Assertions.assertNull(CClassConvert.floatStr(null));

    }

    @Test
    public void doubleStr() {

        Assertions.assertEquals("1.5", CClassConvert.doubleStr(1.5d));
        Assertions.assertNull(CClassConvert.doubleStr(null));

    }

    @Test
    public void bigDecimalStr() {

        Assertions.assertEquals("123", CClassConvert.bigDecimalStr(new BigDecimal("123")));
        Assertions.assertNull(CClassConvert.bigDecimalStr(null));

    }

}
