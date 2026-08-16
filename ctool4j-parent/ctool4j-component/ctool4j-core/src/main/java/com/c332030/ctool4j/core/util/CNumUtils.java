package com.c332030.ctool4j.core.util;

import cn.hutool.core.lang.Assert;
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

    /**
     * 62 进制字符集
     */
    public final String CHARTSET_62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * 62 进制字符集数组
     */
    public final char[] CHARTSET_62_ARR = CHARTSET_62.toCharArray();

    /**
     * 常量 100
     */
    public final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    /**
     * 值为 null 时返回 0
     *
     * @param value 值
     * @return 值或 0
     */
    public Integer defaultZero(Integer value) {
        return ObjUtil.defaultIfNull(value, 0);
    }

    /**
     * 值为 null 时返回 0
     *
     * @param value 值
     * @return 值或 0
     */
    public Long defaultZero(Long value) {
        return ObjUtil.defaultIfNull(value, 0L);
    }

    /**
     * 值为 null 时返回 0
     *
     * @param value 值
     * @return 值或 0
     */
    public BigDecimal defaultZero(BigDecimal value) {
        return ObjUtil.defaultIfNull(value, BigDecimal.ZERO);
    }

    /**
     * 判断值是否大于 0
     *
     * @param value 值
     * @return 是否大于 0，null 返回 false
     */
    public boolean greaterThanZero(Integer value) {
        return null != value && value > 0;
    }

    /**
     * 判断值是否大于 0
     *
     * @param value 值
     * @return 是否大于 0，null 返回 false
     */
    public boolean greaterThanZero(Long value) {
        return null != value && value > 0;
    }

    /**
     * 判断值是否大于 0
     *
     * @param value 值
     * @return 是否大于 0，null 返回 false
     */
    public boolean greaterThanZero(BigDecimal value) {
        return null != value && value.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 判断值是否小于 0
     *
     * @param value 值
     * @return 是否小于 0，null 返回 false
     */
    public boolean lessThanZero(Integer value) {
        return null != value && value < 0;
    }

    /**
     * 判断值是否小于 0
     *
     * @param value 值
     * @return 是否小于 0，null 返回 false
     */
    public boolean lessThanZero(Long value) {
        return null != value && value < 0;
    }

    /**
     * 判断值是否小于 0
     *
     * @param value 值
     * @return 是否小于 0，null 返回 false
     */
    public boolean lessThanZero(BigDecimal value) {
        return null != value && value.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 求和（跳过 null 值）
     *
     * @param values 值数组
     * @return 和，全为 null 时返回 null
     */
    public Integer sum(Integer... values) {

        if(null == values) {
            return null;
        }

        Integer result = null;
        for (Integer value : values) {
            if(null != value) {
                result = ObjUtil.defaultIfNull(result, 0) + value;
            }
        }

        return result;
    }

    /**
     * 求和（跳过 null 值）
     *
     * @param values 值数组
     * @return 和，全为 null 时返回 null
     */
    public Long sum(Long... values) {

        if(null == values) {
            return null;
        }

        Long result = null;
        for (Long value : values) {
            if(null != value) {
                result = ObjUtil.defaultIfNull(result, 0L) + value;
            }
        }

        return result;
    }

    /**
     * 求和（跳过 null 值）
     *
     * @param values 值数组
     * @return 和，全为 null 时返回 null
     */
    public BigDecimal sum(BigDecimal... values) {

        if(null == values) {
            return null;
        }

        BigDecimal result = null;
        for (BigDecimal value : values) {
            if(null != value) {
                result = ObjUtil.defaultIfNull(result, BigDecimal.ZERO).add(value);
            }
        }

        return result;
    }

    /**
     * 除法（四舍五入保留指定小数位）
     *
     * @param value1 被除数
     * @param value2 除数
     * @param scale  小数位数
     * @return 商，任一值为 null 时返回 null
     * @throws IllegalArgumentException 除数为 0 时抛出
     */
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

    /**
     * 比较两个值（null 视为最大）
     *
     * @param v1 第一个值
     * @param v2 第二个值
     * @return 比较结果，v1 大于 v2 返回正数，相等返回 0，否则返回负数
     */
    public int compare(Integer v1, Integer v2) {
        if(null == v1 && null == v2) {
            return 0;
        }
        if(null == v2) {
            return -1;
        }
        if(null == v1) {
            return 1;
        }
        return Integer.compare(v1, v2);
    }

    /**
     * 比较两个值（null 视为最大）
     *
     * @param v1 第一个值
     * @param v2 第二个值
     * @return 比较结果，v1 大于 v2 返回正数，相等返回 0，否则返回负数
     */
    public int compare(Long v1, Long v2) {
        if(null == v1 && null == v2) {
            return 0;
        }
        if(null == v2) {
            return -1;
        }
        if(null == v1) {
            return 1;
        }
        return Long.compare(v1, v2);
    }

    /**
     * 比较两个值（null 视为最大）
     *
     * @param v1 第一个值
     * @param v2 第二个值
     * @return 比较结果，v1 大于 v2 返回正数，相等返回 0，否则返回负数
     */
    public int compare(BigDecimal v1, BigDecimal v2) {
        if(null == v1 && null == v2) {
            return 0;
        }
        if(null == v2) {
            return -1;
        }
        if(null == v1) {
            return 1;
        }
        return v1.compareTo(v2);
    }

    /**
     * 对象转字符串后解析为 Integer
     *
     * @param object 对象
     * @return Integer，解析失败时返回 null
     */
    public Integer toStringThenParseInt(Object object) {
        return parseInt(StrUtil.toStringOrNull(object));
    }

    /**
     * 对象经函数转字符串后解析为 Integer
     *
     * @param t        对象
     * @param function 转字符串函数
     * @param <T>      对象类型
     * @return Integer，解析失败时返回 null
     */
    public <T> Integer toStringThenParseInt(T t, ToStringFunction<T> function) {
        return parseInt(CStrUtils.toString(t, function));
    }

    /**
     * 字符串解析为 Integer，解析失败时抛出异常
     *
     * @param value 字符串
     * @return Integer
     */
    public Integer parseInt(String value) {
        return parse(value, Integer::parseInt, null);
    }

    /**
     * 字符串解析为 Integer，解析失败时返回 null
     *
     * @param value 字符串
     * @return Integer，解析失败时返回 null
     */
    public Integer parseIntDefaultNull(String value) {
        return parse(value, Integer::parseInt, CBiConsumer.empty());
    }

    /**
     * 字符串解析为 Integer，解析失败时执行回退
     *
     * @param value    字符串
     * @param fallback 失败回退
     * @return Integer，解析失败时返回 null
     */
    public Integer parseInt(String value, CBiConsumer<String, Throwable> fallback) {
        return parse(value, Integer::parseInt, fallback);
    }

    /**
     * 对象转字符串后解析为 Long
     *
     * @param object 对象
     * @return Long，解析失败时返回 null
     */
    public Long toStringThenParseLong(Object object) {
        return parseLong(CStrUtils.toString(object));
    }

    /**
     * 对象经函数转字符串后解析为 Long
     *
     * @param t        对象
     * @param function 转字符串函数
     * @param <T>      对象类型
     * @return Long，解析失败时返回 null
     */
    public <T> Long toStringThenParseLong(T t, ToStringFunction<T> function) {
        return parseLong(CStrUtils.toString(t, function));
    }

    /**
     * 字符串解析为 Long，解析失败时抛出异常
     *
     * @param value 字符串
     * @return Long
     */
    public Long parseLong(String value) {
        return parseLong(value, null);
    }

    /**
     * 字符串解析为 Long，解析失败时返回 null
     *
     * @param value 字符串
     * @return Long，解析失败时返回 null
     */
    public Long parseLongDefaultNull(String value) {
        return parseLong(value, CBiConsumer.empty());
    }

    /**
     * 字符串解析为 Long，解析失败时执行回退
     *
     * @param value    字符串
     * @param fallback 失败回退
     * @return Long，解析失败时返回 null
     */
    public Long parseLong(String value, CBiConsumer<String, Throwable> fallback) {
        return parse(value, Long::parseLong, fallback);
    }

    /**
     * 字符串解析，解析失败时抛出异常
     *
     * @param value    字符串
     * @param function 解析函数
     * @param <T>      结果类型
     * @return 解析结果
     */
    public <T> T parse(String value, StringFunction<T> function) {
        return parse(value, function, null);
    }

    /**
     * 字符串解析，解析失败时执行回退
     *
     * @param value    字符串
     * @param function 解析函数
     * @param fallback 失败回退
     * @param <T>      结果类型
     * @return 解析结果，失败时返回 null
     */
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

    /**
     * 判断 long 值是否超出 int 范围
     *
     * @param value long 值
     * @return 是否溢出
     */
    public boolean isOverflow(long value) {
        return (int)value != value;
    }

    /**
     * 断言 long 值未超出 int 范围
     *
     * @param value long 值
     * @throws ArithmeticException 值溢出时抛出
     */
    public void assertOverflow(long value) {
        if(isOverflow(value)) {
            throw new ArithmeticException("值溢出：" + value);
        }
    }

    /**
     * 判断 double 值是否超出 float 范围
     *
     * @param value double 值
     * @return 是否溢出
     */
    public boolean isOverflow(double value) {
        return value > Float.MAX_VALUE || value < -Float.MAX_VALUE;
    }

    /**
     * 断言 double 值未超出 float 范围
     *
     * @param value double 值
     * @throws ArithmeticException 值溢出时抛出
     */
    public void assertOverflow(double value) {
        if(isOverflow(value)) {
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
    public Integer toInt(long value) {
        if(isOverflow(value)) {
            log.debug("try to convert overflow long {} to int", value);
            return null;
        }
        return (int)value;
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
        return toInt(value.longValue());
    }

    /**
     * Integer 转换为 Long
     * @param value 值
     * @return Long
     */
    public Long toLong(Integer value) {
        return CObjUtils.convert(value, Integer::longValue);
    }

    /**
     * long 值转 62 进制字符串
     * <p>不支持负数输入，0 的 62 进制表示为 "0"。</p>
     *
     * @param value 非负 long 值
     * @return 62 进制字符串
     * @throws IllegalArgumentException 输入为负数时抛出
     */
    public String to62(long value) {

        Assert.isTrue(value >= 0, "to62 not support negative value: " + value);

        if (value == 0) {
            return "0";
        }

        val sb = new StringBuilder();
        while (value != 0) {

            val index = (int) (value % 62);
            sb.append(CHARTSET_62_ARR[index]);
            value /= 62;
        }

        return sb.reverse().toString();
    }

    /**
     * 取最大值（跳过 null 值）
     *
     * @param values 值数组
     * @return 最大值，数组为空或全为 null 时返回 null
     */
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

    /**
     * 取最小值（跳过 null 值）
     *
     * @param values 值数组
     * @return 最小值，数组为空或全为 null 时返回 null
     */
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
     * 计算占比
     * @param value 值
     * @param total 总数
     * @return 占比
     */
    public BigDecimal percent(Integer value, Integer total) {
        return percent(value, total, 0);
    }

    /**
     * 计算占比
     * @param value 值
     * @param total 总数
     * @param scale 百分比小数位数
     * @return 占比
     */
    public BigDecimal percent(Integer value, Integer total, int scale) {
        if(null == value || null == total) {
            return null;
        }
        return percent(new BigDecimal(value), new BigDecimal(total), scale);
    }

    /**
     * 计算占比
     * @param value 值
     * @param total 总数
     * @return 占比
     */
    public BigDecimal percent(Long value, Long total) {
        return percent(value, total, 0);
    }

    /**
     * 计算占比
     * @param value 值
     * @param total 总数
     * @param scale 百分比小数位数
     * @return 占比
     */
    public BigDecimal percent(Long value, Long total, int scale) {
        if(null == value || null == total) {
            return null;
        }
        return percent(new BigDecimal(value), new BigDecimal(total), scale);
    }

    /**
     * 计算占比
     * @param value 值
     * @param total 总数
     * @return 占比
     */
    public BigDecimal percent(BigDecimal value, BigDecimal total) {
        return percent(value, total, 0);
    }

    /**
     * 计算占比
     * @param value 值
     * @param total 总数
     * @param scale 百分比小数位数
     * @return 占比
     */
    public BigDecimal percent(BigDecimal value, BigDecimal total, int scale) {
        if(null == value || null == total) {
            return null;
        }
        // 总数为 0 无法计算占比，返回 null（不抛 divide 的 IllegalArgumentException）
        if(BigDecimal.ZERO.compareTo(total) == 0) {
            return null;
        }
        return divide(value.multiply(ONE_HUNDRED), total, scale);
    }

}
