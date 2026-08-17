package com.c332030.ctool4j.core.classes;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CDateUtils;
import com.c332030.ctool4j.core.util.CNumUtils;
import com.c332030.ctool4j.definition.interfaces.ICValue;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

/**
 * <p>
 * Description: CClassConvert
 * </p>
 *
 * @since 2025/4/17
 */
@CustomLog
@UtilityClass
public class CClassConvert {

    /**
     * 解析日期时间字符串为日期
     *
     * @param date 日期时间字符串
     * @return 解析后的日期
     */
    public Date parseDateTime(String date) {
        return DateUtil.parse(date);
    }

    /**
     * 格式化日期为日期时间字符串
     *
     * @param date 日期
     * @return 日期时间字符串
     */
    public String formatDateTime(Date date) {
        return DateUtil.formatDateTime(date);
    }

    /**
     * 日期转毫秒
     *
     * @param date 日期
     * @return 毫秒值，日期为 null 时返回 null
     */
    public Long toMills(Date date) {
        if(null == date) {
            return null;
        }
        return date.getTime();
    }

    /**
     * 毫秒转日期
     *
     * @param mills 毫秒值
     * @return 日期，毫秒为 null 时返回 null
     */
    public Date fromMills(Long mills) {
        if(null == mills) {
            return null;
        }
        return new Date(mills);
    }

    /**
     * Instant 转日期
     *
     * @param instant Instant
     * @return 日期
     */
    public Date toDate(Instant instant) {
        return CDateUtils.toDate(instant);
    }

    /**
     * 日期转 Instant
     *
     * @param date 日期
     * @return Instant
     */
    public Instant toInstant(Date date) {
        return CDateUtils.toInstant(date);
    }

    /**
     * 字符串转布尔值
     *
     * @param str 字符串
     * @return 布尔值
     */
    public Boolean toBoolean(String str) {
        // Boolean.parseBoolean 仅识别 "true"/"false"，此处额外兼容数字 "1" 表示 true
        return "1".equals(str) || Boolean.parseBoolean(str);
    }

    /**
     * 布尔值转字符串
     *
     * @param value 布尔值
     * @return 字符串
     */
    public String booleanStr(Boolean value) {
        return objectStr(value);
    }

    /**
     * 字符串转整数
     *
     * @param str 字符串
     * @return 整数
     */
    public Integer toInt(String str) {
        return CNumUtils.parseInt(str);
    }

    /**
     * int 转整数
     *
     * @param value int 值
     * @return 整数
     */
    public Integer toInt(int value) {
        return value;
    }

    /**
     * long 转整数
     *
     * @param value long 值
     * @return 整数
     */
    public Integer toInt(long value) {
        return CNumUtils.toInt(value);
    }

    /**
     * Long 转整数
     *
     * @param value Long 值
     * @return 整数
     */
    public Integer toInt(Long value) {
        return CNumUtils.toInt(value);
    }

    /**
     * 整数转字符串
     *
     * @param value 整数
     * @return 字符串
     */
    public String intStr(Integer value) {
        return objectStr(value);
    }

    /**
     * Long 转 int
     *
     * @param value Long 值
     * @return int 值
     */
    public int intValue(Long value) {
        val intValue = CNumUtils.toInt(value);
        if(null == intValue) {
            return 0;
        }
        return intValue;
    }

    /**
     * 字符串转长整数
     *
     * @param str 字符串
     * @return 长整数
     */
    public Long toLong(String str) {
        return CNumUtils.parseLong(str);
    }

    /**
     * int 转长整数
     *
     * @param value int 值
     * @return 长整数
     */
    public Long toLong(int value) {
        return (long) value;
    }

    /**
     * Integer 转长整数
     *
     * @param value Integer 值
     * @return 长整数
     */
    public Long toLong(Integer value) {
        return CNumUtils.toLong(value);
    }

    /**
     * long 转长整数
     *
     * @param value long 值
     * @return 长整数
     */
    public Long toLong(long value) {
        return value;
    }

    /**
     * Integer 转 long
     *
     * @param value Integer 值
     * @return long 值
     */
    public long longValue(Integer value) {

        if(null == value) {
            return 0;
        }
        return value.longValue();
    }

    /**
     * 长整数转字符串
     *
     * @param value 长整数
     * @return 字符串
     */
    public String longStr(Long value) {
        return objectStr(value);
    }

    /**
     * 字符串转浮点数
     *
     * @param value 字符串
     * @return 浮点数
     */
    public Float toFloat(String value) {
        if(null == value) {
            return null;
        }
        return CNumUtils.parse(value, Float::parseFloat);
    }

    /**
     * 字符串转 float
     *
     * @param value 字符串
     * @return float 值
     */
    public float floatValue(String value) {
        val valueNullable = toFloat(value);
        if(null == valueNullable) {
            return 0;
        }
        return valueNullable;
    }

    /**
     * BigDecimal 转 float
     *
     * @param value BigDecimal
     * @return float 值
     */
    public float floatValue(BigDecimal value) {
        if(null == value) {
            return 0;
        }
        return value.floatValue();
    }

    /**
     * 浮点数转字符串
     *
     * @param value 浮点数
     * @return 字符串
     */
    public String floatStr(Float value) {
        return objectStr(value);
    }

    /**
     * 字符串转双精度浮点数
     *
     * @param value 字符串
     * @return 双精度浮点数
     */
    public Double toDouble(String value) {
        if(null == value) {
            return null;
        }
        return CNumUtils.parse(value, Double::parseDouble);
    }

    /**
     * 字符串转 double
     *
     * @param value 字符串
     * @return double 值
     */
    public double doubleValue(String value) {
        val valueNullable = toDouble(value);
        if(null == valueNullable) {
            return 0;
        }
        return valueNullable;
    }
    /**
     * Float 转 double
     *
     * @param value Float 值
     * @return double 值
     */
    public double doubleValue(Float value) {
        if(null == value) {
            return 0;
        }
        return value;
    }

    /**
     * BigDecimal 转 double
     *
     * @param value BigDecimal
     * @return double 值
     */
    public double doubleValue(BigDecimal value) {
        if(null == value) {
            return 0;
        }
        return value.doubleValue();
    }

    /**
     * 双精度浮点数转字符串
     *
     * @param value 双精度浮点数
     * @return 字符串
     */
    public String doubleStr(Double value) {
        return objectStr(value);
    }

    /**
     * int 转 BigDecimal
     *
     * @param value int 值
     * @return BigDecimal
     */
    public BigDecimal toBigDecimal(int value) {
        return new BigDecimal(value);
    }

    /**
     * Integer 转 BigDecimal
     *
     * @param value Integer 值
     * @return BigDecimal，值为 null 时返回 null
     */
    public BigDecimal toBigDecimal(Integer value) {
        if(null == value) {
            return null;
        }
        return new BigDecimal(value);
    }

    /**
     * long 转 BigDecimal
     *
     * @param value long 值
     * @return BigDecimal
     */
    public BigDecimal toBigDecimal(long value) {
        return new BigDecimal(value);
    }

    /**
     * Long 转 BigDecimal
     *
     * @param value Long 值
     * @return BigDecimal，值为 null 时返回 null
     */
    public BigDecimal toBigDecimal(Long value) {
        if(null == value) {
            return null;
        }
        return new BigDecimal(value);
    }

    /**
     * float 转 BigDecimal
     *
     * @param value float 值
     * @return BigDecimal
     */
    public BigDecimal toBigDecimal(float value) {
        return toBigDecimal(String.valueOf(value));
    }

    /**
     * Float 转 BigDecimal
     *
     * @param value Float 值
     * @return BigDecimal，值为 null 时返回 null
     */
    public BigDecimal toBigDecimal(Float value) {
        if(null == value) {
            return null;
        }
        return toBigDecimal(value.floatValue());
    }

    /**
     * double 转 BigDecimal
     *
     * @param value double 值
     * @return BigDecimal
     */
    public BigDecimal toBigDecimal(double value) {
        return toBigDecimal(String.valueOf(value));
    }

    /**
     * Double 转 BigDecimal
     *
     * @param value Double 值
     * @return BigDecimal，值为 null 时返回 null
     */
    public BigDecimal toBigDecimal(Double value) {
        if(null == value) {
            return null;
        }
        return toBigDecimal(value.doubleValue());
    }

    /**
     * 字符串转 BigDecimal
     *
     * @param value 字符串
     * @return BigDecimal，字符串为空时返回 null
     */
    public BigDecimal toBigDecimal(String value) {
        if(StrUtil.isEmpty(value)) {
            return null;
        }
        return new BigDecimal(value);
    }

    /**
     * BigDecimal 转字符串
     *
     * @param value BigDecimal
     * @return 字符串
     */
    public String bigDecimalStr(BigDecimal value) {
        return objectStr(value);
    }

    /**
     * 枚举转其整数值
     *
     * @param value 枚举
     * @return 枚举的整数值，为 null 时返回 null
     */
    @Named("toEnumIntegerValue")
    public Integer toEnumIntegerValue(ICValue<Integer> value) {
        if(null == value) {
            return null;
        }
        return value.getValue();
    }

    /**
     * 枚举转其字符串值
     *
     * @param value 枚举
     * @return 枚举的字符串值，为 null 时返回 null
     */
    @Named("toEnumStringValue")
    public String toEnumStringValue(ICValue<String> value) {
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

    /**
     * 对象转字符串
     *
     * @param value 对象
     * @return 字符串，为 null 时返回 null
     */
    public String objectStr(Object value) {
        return StrUtil.toStringOrNull(value);
    }

}
