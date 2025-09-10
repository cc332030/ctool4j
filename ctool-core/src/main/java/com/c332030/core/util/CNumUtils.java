package com.c332030.core.util;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.core.function.StringFunction;
import com.c332030.core.function.ToStringFunction;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

/**
 * <p>
 * Description: CNumUtils
 * </p>
 *
 * @since 2024/12/2
 */
@UtilityClass
public class CNumUtils {

    public static Integer defaultZero(Integer value) {
        return ObjUtil.defaultIfNull(value, 0);
    }

    public static Long defaultZero(Long value) {
        return ObjUtil.defaultIfNull(value, 0L);
    }

    public static BigDecimal defaultZero(BigDecimal value) {
        return ObjUtil.defaultIfNull(value, BigDecimal.ZERO);
    }

    public boolean greaterThanZero(Integer value) {
        return null != value && value > 0;
    }

    public boolean lessThanZero(Integer value) {
        return null != value && value < 0;
    }

    public static Integer sum(Integer... values) {

        Integer result = null;
        for (Integer value : values) {
            if(null != value) {
                result = ObjUtil.defaultIfNull(result, 0) + value;
            }
        }

        return result;
    }

    public static Long sum(Long... values) {

        Long result = null;
        for (Long value : values) {
            if(null != value) {
                result = ObjUtil.defaultIfNull(result, 0L) + value;
            }
        }

        return result;
    }

    public static BigDecimal sum(BigDecimal... values) {

        BigDecimal result = null;
        for (BigDecimal value : values) {
            if(null != value) {
                result = ObjUtil.defaultIfNull(result, BigDecimal.ZERO).add(value);
            }
        }

        return result;
    }

    public static int compare(Integer v1, Integer v2) {
        if(null == v2) {
            return -1;
        }
        if(null == v1) {
            return 1;
        }
        return Integer.compare(v1, v2);
    }

    public static int compare(Long v1, Long v2) {
        if(null == v2) {
            return -1;
        }
        if(null == v1) {
            return 1;
        }
        return Long.compare(v1, v2);
    }

    public static int compare(BigDecimal v1, BigDecimal v2) {
        if(null == v2) {
            return -1;
        }
        if(null == v1) {
            return 1;
        }
        return v1.compareTo(v2);
    }

    public static Integer toStringThenParseInt(Object object) {
        return parseInt(StrUtil.toStringOrNull(object));
    }

    public static <T> Integer toStringThenParseInt(T t, ToStringFunction<T> function) {
        return parseInt(CStrUtils.toString(t, function));
    }

    public static Integer parseInt(String value) {
        return parse(value, Integer::parseInt);
    }

    public static Long toStringThenParseLong(Object object) {
        return parseLong(CStrUtils.toString(object));
    }

    public static <T> Long toStringThenParseLong(T t, ToStringFunction<T> function) {
        return parseLong(CStrUtils.toString(t, function));
    }

    public static Long parseLong(String value) {
        return parse(value, Long::parseLong);
    }

    public static <T> T parse(String value, StringFunction<T> function) {
        return CStrUtils.convertAvailable(value, function);
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
