package com.c332030.core.mapstruct;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.core.interfaces.IValue;
import com.c332030.core.util.CNumUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * Description: CMapStructConvert
 * </p>
 *
 * @since 2025/4/17
 */
@UtilityClass
public class CMapStructConvert {

    public Date parseDateTime(String date) {
        return DateUtil.parse(date);
    }

    public String formatDateTime(Date date) {
        return DateUtil.formatDateTime(date);
    }

    public Long toMills(Date date) {
        if(null == date) {
            return null;
        }
        return date.getTime();
    }

    public Date fromMills(Long mills) {
        if(null == mills) {
            return null;
        }
        return new Date(mills);
    }

    public Boolean toBoolean(String str) {
        return Boolean.parseBoolean(str);
    }

    public String fromBoolean(Boolean value) {
        return StrUtil.toStringOrNull(value);
    }

    public Integer toInt(String str) {
        return CNumUtils.parseInt(str);
    }

    public Integer toInt(int value) {
        return value;
    }

    public Integer toInt(long value) {
        CNumUtils.assertOverflow(value);
        return (int)value;
    }

    public int intValue(Long value) {

        if(null == value) {
            return 0;
        }

        CNumUtils.assertOverflow(value);
        return value.intValue();
    }

    public Long toLong(String str) {
        return CNumUtils.parseLong(str);
    }

    public Long toLong(int value) {
        return (long) value;
    }

    public Long toLong(long value) {
        return value;
    }

    public long longValue(Integer value) {

        if(null == value) {
            return 0;
        }
        return value.longValue();
    }

    public Float toFloat(String value) {
        if(null == value) {
            return null;
        }
        return CNumUtils.parse(value, Float::parseFloat);
    }

    public float floatValue(String value) {
        val valueNullable = toFloat(value);
        if(null == valueNullable) {
            return 0;
        }
        return valueNullable;
    }

    public float floatValue(Double value) {
        if(null == value) {
            return 0;
        }
        CNumUtils.assertOverflow(value);
        return value.floatValue();
    }

    public float floatValue(BigDecimal value) {
        if(null == value) {
            return 0;
        }
        return value.floatValue();
    }

    public Double toDouble(String value) {
        if(null == value) {
            return null;
        }
        return CNumUtils.parse(value, Double::parseDouble);
    }

    public double doubleValue(String value) {
        val valueNullable = toDouble(value);
        if(null == valueNullable) {
            return 0;
        }
        return valueNullable;
    }
    public double doubleValue(Float value) {
        if(null == value) {
            return 0;
        }
        return value;
    }

    public double doubleValue(BigDecimal value) {
        if(null == value) {
            return 0;
        }
        return value.doubleValue();
    }

    public BigDecimal toBigDecimal(int value) {
        return new BigDecimal(value);
    }

    public BigDecimal toBigDecimal(Integer value) {
        if(null == value) {
            return null;
        }
        return new BigDecimal(value);
    }

    public BigDecimal toBigDecimal(long value) {
        return new BigDecimal(value);
    }

    public BigDecimal toBigDecimal(Long value) {
        if(null == value) {
            return null;
        }
        return new BigDecimal(value);
    }

    public BigDecimal toBigDecimal(float value) {
        return toBigDecimal(String.valueOf(value));
    }

    public BigDecimal toBigDecimal(Float value) {
        if(null == value) {
            return null;
        }
        return toBigDecimal(value.floatValue());
    }

    public BigDecimal toBigDecimal(double value) {
        return toBigDecimal(String.valueOf(value));
    }

    public BigDecimal toBigDecimal(Double value) {
        if(null == value) {
            return null;
        }
        return toBigDecimal(value.doubleValue());
    }

    public BigDecimal toBigDecimal(String value) {
        if(StrUtil.isEmpty(value)) {
            return null;
        }
        return new BigDecimal(value);
    }

    @Named("toEnumIntegerValue")
    public Integer toEnumIntegerValue(IValue<Integer> value) {
        if(null == value) {
            return null;
        }
        return value.getValue();
    }

    @Named("toEnumStringValue")
    public String toEnumStringValue(IValue<String> value) {
        if(null == value) {
            return null;
        }
        return value.getValue();
    }

//    public Long toCent(BigDecimal value) {
//        return null;
//    }
//
//    public BigDecimal toYuan(Integer value) {
//        return null;
//    }
//
//    public BigDecimal toYuan(Long value) {
//        return null;
//    }

}
