package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.definition.function.CBiConsumer;
import com.c332030.ctool4j.definition.function.StringFunction;
import com.c332030.ctool4j.definition.function.ToStringFunction;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * <p>
 * Description: CNumUtils
 * </p>
 *
 * @since 2024/12/2
 */
@CustomLog
@UtilityClass
public class CNumUtils {

    public final String CHARTSET_62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public final char[] CHARTSET_62_ARR = CHARTSET_62.toCharArray();

    public final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    public Integer defaultZero(Integer value) {
        return ObjUtil.defaultIfNull(value, 0);
    }

    public Long defaultZero(Long value) {
        return ObjUtil.defaultIfNull(value, 0L);
    }

    public BigDecimal defaultZero(BigDecimal value) {
        return ObjUtil.defaultIfNull(value, BigDecimal.ZERO);
    }

    public boolean greaterThanZero(Integer value) {
        return null != value && value > 0;
    }

    public boolean greaterThanZero(Long value) {
        return null != value && value > 0;
    }

    public boolean greaterThanZero(BigDecimal value) {
        return null != value && value.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean lessThanZero(Integer value) {
        return null != value && value < 0;
    }

    public boolean lessThanZero(Long value) {
        return null != value && value < 0;
    }

    public boolean lessThanZero(BigDecimal value) {
        return null != value && value.compareTo(BigDecimal.ZERO) < 0;
    }

    public Integer sum(Integer... values) {

        Integer result = null;
        for (Integer value : values) {
            if(null != value) {
                result = ObjUtil.defaultIfNull(result, 0) + value;
            }
        }

        return result;
    }

    public Long sum(Long... values) {

        Long result = null;
        for (Long value : values) {
            if(null != value) {
                result = ObjUtil.defaultIfNull(result, 0L) + value;
            }
        }

        return result;
    }

    public BigDecimal sum(BigDecimal... values) {

        BigDecimal result = null;
        for (BigDecimal value : values) {
            if(null != value) {
                result = ObjUtil.defaultIfNull(result, BigDecimal.ZERO).add(value);
            }
        }

        return result;
    }

    public BigDecimal divide(BigDecimal value1, BigDecimal value2, int scale) {

        if(null == value1 || null == value2 ) {
            return null;
        }

        if(value1.compareTo(BigDecimal.ZERO) == 0) {
            return scale(BigDecimal.ZERO, scale);
        }

        if(value2.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("除数不能为0");
        }

        return value1.divide(value2, scale, RoundingMode.HALF_UP);
    }

    /**
     * 设置小数位数
     * @param value 值
     * @param scale 小数位数
     * @return 值
     */
    public BigDecimal scale(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * 设置小数位数
     * @param value 值
     * @return 值
     */
    public BigDecimal scale2(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public int compare(Integer v1, Integer v2) {
        if(null == v2) {
            return -1;
        }
        if(null == v1) {
            return 1;
        }
        return Integer.compare(v1, v2);
    }

    public int compare(Long v1, Long v2) {
        if(null == v2) {
            return -1;
        }
        if(null == v1) {
            return 1;
        }
        return Long.compare(v1, v2);
    }

    public int compare(BigDecimal v1, BigDecimal v2) {
        if(null == v2) {
            return -1;
        }
        if(null == v1) {
            return 1;
        }
        return v1.compareTo(v2);
    }

    public Integer toStringThenParseInt(Object object) {
        return parseInt(StrUtil.toStringOrNull(object));
    }

    public <T> Integer toStringThenParseInt(T t, ToStringFunction<T> function) {
        return parseInt(CStrUtils.toString(t, function));
    }

    public Integer parseInt(String value) {
        return parse(value, Integer::parseInt, null);
    }

    public Integer parseIntDefaultNull(String value) {
        return parse(value, Integer::parseInt, CBiConsumer.empty());
    }

    public Integer parseInt(String value, CBiConsumer<String, Throwable> fallback) {
        return parse(value, Integer::parseInt, fallback);
    }

    public Long toStringThenParseLong(Object object) {
        return parseLong(CStrUtils.toString(object));
    }

    public <T> Long toStringThenParseLong(T t, ToStringFunction<T> function) {
        return parseLong(CStrUtils.toString(t, function));
    }

    public Long parseLong(String value) {
        return parseLong(value, null);
    }

    public Long parseLongDefaultNull(String value) {
        return parseLong(value, CBiConsumer.empty());
    }

    public Long parseLong(String value, CBiConsumer<String, Throwable> fallback) {
        return parse(value, Long::parseLong, fallback);
    }

    public <T> T parse(String value, StringFunction<T> function) {
        return parse(value, function, null);
    }

    public <T> T parse(String value, StringFunction<T> function, CBiConsumer<String, Throwable> fallback) {

        try {
            return CStrUtils.convertAvailable(value, function);
        } catch (Throwable e) {

            log.debug("parse error: {}", value, e);
            if(null != fallback) {
                fallback.accept(value, e);
            } else {
                throw e;
            }
        }

        return null;
    }

    public boolean checkOverflow(long value) {
        return (int)value == value;
    }

    public void assertOverflow(long value) {
        if(!checkOverflow(value)) {
            throw new ArithmeticException("值溢出：" + value);
        }
    }

    public boolean checkOverflow(double value) {
        return value <= Float.MAX_VALUE && value >= Float.MIN_VALUE;
    }

    public void assertOverflow(double value) {
        if(!checkOverflow(value)) {
            throw new ArithmeticException("值溢出：" + value);
        }
    }

    /**
     * 是否为空或0
     * @param value 值
     * @return boolean
     */
    public boolean isNullOrZero(Integer value) {
        return value == null || value == 0;
    }

    /**
     * 是否为空或0
     * @param value 值
     * @return boolean
     */
    public boolean isNullOrZero(Long value) {
        return value == null || value == 0;
    }

    /**
     * Long 转换为 Integer
     * @param value 值
     * @return Integer
     */
    public Integer toInt(Long value) {
        if(null == value) {
            return null;
        }
        assertOverflow(value);
        return value.intValue();
    }

    /**
     * Integer 转换为 Long
     * @param value 值
     * @return Long
     */
    public Long toLong(Integer value) {
        return CObjUtils.convert(value, Integer::longValue);
    }

    public String to62(long value) {

        val sb = new StringBuilder();
        while (value != 0) {

            val index = (int) (value % 62);
            sb.append(CHARTSET_62_ARR[index]);
            value /= 62;
        }

        return sb.reverse().toString();
    }

    public Integer max(Integer... values) {

        if(ArrayUtil.isEmpty(values)) {
            return null;
        }

        Integer result = null;
        for (val value : values) {

            if(null == value) {
                continue;
            }
            if(null == result) {
                result = Integer.MIN_VALUE;
            }

            result = Math.max(result, value);
        }

        return result;
    }

    public Integer min(Integer... values) {

        if(ArrayUtil.isEmpty(values)) {
            return null;
        }

        Integer result = null;
        for (val value : values) {

            if(null == value) {
                continue;
            }
            if(null == result) {
                result = Integer.MAX_VALUE;
            }

            result = Math.min(result, value);
        }

        return result;
    }

    public BigDecimal toBigDecimal(Integer value) {
        if(null == value) {
            return null;
        }
        return new BigDecimal(value);
    }

    public BigDecimal toBigDecimal(Long value) {
        if(null == value) {
            return null;
        }
        return new BigDecimal(value);
    }

}
