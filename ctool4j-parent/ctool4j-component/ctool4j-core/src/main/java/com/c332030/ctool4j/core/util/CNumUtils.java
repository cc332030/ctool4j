package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.function.CBiConsumer;
import com.c332030.ctool4j.core.function.StringFunction;
import com.c332030.ctool4j.core.function.ToStringFunction;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

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

    public boolean lessThanZero(Integer value) {
        return null != value && value < 0;
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

    @SneakyThrows
    public <T> T parse(String value, StringFunction<T> function) {
        return parse(value, function, null);
    }

    @SneakyThrows
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

}
