package com.c332030.ctool4j.core.validation;

import com.c332030.ctool4j.core.exception.CExceptionUtils;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * <p>
 * Description: CAssert
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CAssert} 为断言工具类，提供多种断言方法，条件不满足时抛业务异常：</p>
 * <ul>
 *   <li>{@code isTrue}：条件为 true</li>
 *   <li>{@code equals}：两值相等（Objects.equals 语义）</li>
 *   <li>{@code isNull} / {@code notNull}：对象为 null / 非 null</li>
 *   <li>{@code valid} / {@code notValid}：值有效 / 无效，按数据类型选择校验语义（见 {@link CValidUtils}）</li>
 *   <li>{@code notEmpty}：字符串/byte/int/long/Object 数组/Collection/Map 非空</li>
 *   <li>{@code notBlank}：字符串非空白</li>
 * </ul>
 * <p>每个断言均有「String message」与「Supplier&lt;String&gt;」两个重载；
 * 按值类型分派语义的断言（{@code valid} / {@code notValid} / {@code notEmpty}）另有参数类型不同的重名重载。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>断言不满足</td>
 *     <td>抛 CBusinessException（携带指定信息）</td>
 *   </tr>
 *   <tr>
 *     <td>equals null/null</td>
 *     <td>视为相等，不抛异常</td>
 *   </tr>
 *   <tr>
 *     <td>valid / notValid / notEmpty / notBlank 不满足</td>
 *     <td>抛 CBusinessException</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>参数校验、前置条件断言，失败时抛出统一业务异常。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>抛的是业务异常，非断言类异常（AssertionError）；需严格断言语义时用 JDK assert 或 AssertJ。</li>
 *   <li>传 {@code null} 字面量会因参数类型不同的重名重载产生编译歧义（{@code valid} / {@code notValid} / {@code notEmpty}），
 *       须传入具体类型变量或显式强转。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>统一抛业务异常，与业务校验语义一致，调用方可按业务异常统一处理。</li>
 *   <li>{@code notEmpty} 与 {@code valid} 对字符串的语义不同（empty / blank），保留差异以兼容既有调用方。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>断言失败语义</b></p>
 * <ul>
 *   <li>断言条件不满足时经 {@code CExceptionUtils.throwBusinessException} 抛 {@code CBusinessException}，携带指定错误信息。</li>
 * </ul>
 * <p><b>相等语义</b></p>
 * <ul>
 *   <li>{@code equals} 基于 {@code Objects.equals}：两个 null 视为相等；null 与非 null 视为不相等。</li>
 * </ul>
 * <p><b>空值与有效性语义</b></p>
 * <ul>
 *   <li>{@code notEmpty}（字符串）用 {@code StrUtil.isEmpty}（null 或空串视为空）。</li>
 *   <li>{@code notBlank} 用 {@code StrUtil.isBlank}（null、空串、纯空白视为空）。</li>
 *   <li>{@code valid} / {@code notValid} 按值类型分派（见 {@link CValidUtils}）：字符串按 blank、集合/Map/数组按 empty、其他对象按 null。</li>
 * </ul>
 *
 * @since 2025/9/14
 * @version 1.1
 */
@UtilityClass
public class CAssert {

    /**
     * 断言
     * @param value 断言条件
     * @param messageSupplier 错误信息提供者
     */
    public void isTrue(boolean value, Supplier<String> messageSupplier) {
        if(!value) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 断言
     * @param value 断言条件
     * @param message 错误信息
     */
    public void isTrue(boolean value, String message) {
        if(!value) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 断言是否相等
     * @param value1 值1
     * @param value2 值2
     * @param messageSupplier 错误信息提供者
     */
    public void equals(Object value1, Object value2, Supplier<String> messageSupplier) {
        if(!Objects.equals(value1, value2)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 断言是否相等
     * @param value1 值1
     * @param value2 值2
     * @param message 错误信息
     */
    public void equals(Object value1, Object value2, String message) {
        if(!Objects.equals(value1, value2)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 为空断言
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void isNull(Object value, Supplier<String> messageSupplier) {
        if(Objects.nonNull(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 为空断言
     * @param value 校验值
     * @param message 错误信息
     */
    public void isNull(Object value, String message) {
        if(Objects.nonNull(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 不为空断言
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notNull(Object value, Supplier<String> messageSupplier) {
        if(Objects.isNull(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 不为空断言
     * @param value 校验值
     * @param message 错误信息
     */
    public void notNull(Object value, String message) {
        if(Objects.isNull(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 有效断言（对象非 null）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void valid(Object value, Supplier<String> messageSupplier) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 有效断言（对象非 null）
     * @param value 校验值
     * @param message 错误信息
     */
    public void valid(Object value, String message) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 无效断言（对象为 null）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notValid(Object value, Supplier<String> messageSupplier) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 无效断言（对象为 null）
     * @param value 校验值
     * @param message 错误信息
     */
    public void notValid(Object value, String message) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 有效断言（字符串非空且非空白）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void valid(CharSequence value, Supplier<String> messageSupplier) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 有效断言（字符串非空且非空白）
     * @param value 校验值
     * @param message 错误信息
     */
    public void valid(CharSequence value, String message) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 无效断言（字符串为 null、空或空白）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notValid(CharSequence value, Supplier<String> messageSupplier) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 无效断言（字符串为 null、空或空白）
     * @param value 校验值
     * @param message 错误信息
     */
    public void notValid(CharSequence value, String message) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 有效断言（可迭代对象非空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void valid(Iterable<?> value, Supplier<String> messageSupplier) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 有效断言（可迭代对象非空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void valid(Iterable<?> value, String message) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 无效断言（可迭代对象为 null 或空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notValid(Iterable<?> value, Supplier<String> messageSupplier) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 无效断言（可迭代对象为 null 或空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void notValid(Iterable<?> value, String message) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 有效断言（集合非空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void valid(Collection<?> value, Supplier<String> messageSupplier) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 有效断言（集合非空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void valid(Collection<?> value, String message) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 无效断言（集合为 null 或空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notValid(Collection<?> value, Supplier<String> messageSupplier) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 无效断言（集合为 null 或空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void notValid(Collection<?> value, String message) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 有效断言（Map 非空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void valid(Map<?, ?> value, Supplier<String> messageSupplier) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 有效断言（Map 非空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void valid(Map<?, ?> value, String message) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 无效断言（Map 为 null 或空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notValid(Map<?, ?> value, Supplier<String> messageSupplier) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 无效断言（Map 为 null 或空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void notValid(Map<?, ?> value, String message) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 有效断言（字节数组非空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void valid(byte[] value, Supplier<String> messageSupplier) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 有效断言（字节数组非空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void valid(byte[] value, String message) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 无效断言（字节数组为 null 或空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notValid(byte[] value, Supplier<String> messageSupplier) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 无效断言（字节数组为 null 或空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void notValid(byte[] value, String message) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 有效断言（short 数组非空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void valid(short[] value, Supplier<String> messageSupplier) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 有效断言（short 数组非空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void valid(short[] value, String message) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 无效断言（short 数组为 null 或空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notValid(short[] value, Supplier<String> messageSupplier) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 无效断言（short 数组为 null 或空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void notValid(short[] value, String message) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 有效断言（char 数组非空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void valid(char[] value, Supplier<String> messageSupplier) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 有效断言（char 数组非空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void valid(char[] value, String message) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 无效断言（char 数组为 null 或空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notValid(char[] value, Supplier<String> messageSupplier) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 无效断言（char 数组为 null 或空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void notValid(char[] value, String message) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 有效断言（int 数组非空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void valid(int[] value, Supplier<String> messageSupplier) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 有效断言（int 数组非空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void valid(int[] value, String message) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 无效断言（int 数组为 null 或空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notValid(int[] value, Supplier<String> messageSupplier) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 无效断言（int 数组为 null 或空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void notValid(int[] value, String message) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 有效断言（long 数组非空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void valid(long[] value, Supplier<String> messageSupplier) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 有效断言（long 数组非空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void valid(long[] value, String message) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 无效断言（long 数组为 null 或空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notValid(long[] value, Supplier<String> messageSupplier) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 无效断言（long 数组为 null 或空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void notValid(long[] value, String message) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 有效断言（float 数组非空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void valid(float[] value, Supplier<String> messageSupplier) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 有效断言（float 数组非空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void valid(float[] value, String message) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 无效断言（float 数组为 null 或空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notValid(float[] value, Supplier<String> messageSupplier) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 无效断言（float 数组为 null 或空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void notValid(float[] value, String message) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 有效断言（double 数组非空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void valid(double[] value, Supplier<String> messageSupplier) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 有效断言（double 数组非空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void valid(double[] value, String message) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 无效断言（double 数组为 null 或空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notValid(double[] value, Supplier<String> messageSupplier) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 无效断言（double 数组为 null 或空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void notValid(double[] value, String message) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 有效断言（boolean 数组非空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void valid(boolean[] value, Supplier<String> messageSupplier) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 有效断言（boolean 数组非空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void valid(boolean[] value, String message) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 无效断言（boolean 数组为 null 或空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notValid(boolean[] value, Supplier<String> messageSupplier) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 无效断言（boolean 数组为 null 或空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void notValid(boolean[] value, String message) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 有效断言（对象数组非空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void valid(Object[] value, Supplier<String> messageSupplier) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 有效断言（对象数组非空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void valid(Object[] value, String message) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 无效断言（对象数组为 null 或空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notValid(Object[] value, Supplier<String> messageSupplier) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 无效断言（对象数组为 null 或空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void notValid(Object[] value, String message) {
        if(CValidUtils.isValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 不为空断言（字符串非空，空串视为空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notEmpty(String value, Supplier<String> messageSupplier) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 不为空断言（字符串非空，空串视为空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void notEmpty(String value, String message) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 不为空且不为空白断言
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notBlank(String value, Supplier<String> messageSupplier) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 不为空且不为空白断言
     * @param value 校验值
     * @param message 错误信息
     */
    public void notBlank(String value, String message) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 不为空断言（字节数组非空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notEmpty(byte[] value, Supplier<String> messageSupplier) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 不为空断言（字节数组非空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void notEmpty(byte[] value, String message) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 不为空断言（int 数组非空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notEmpty(int[] value, Supplier<String> messageSupplier) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 不为空断言（int 数组非空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void notEmpty(int[] value, String message) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 不为空断言（long 数组非空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notEmpty(long[] value, Supplier<String> messageSupplier) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 不为空断言（long 数组非空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void notEmpty(long[] value, String message) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 不为空断言（对象数组非空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notEmpty(Object[] value, Supplier<String> messageSupplier) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 不为空断言（对象数组非空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void notEmpty(Object[] value, String message) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 不为空断言（集合非空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notEmpty(Collection<?> value, Supplier<String> messageSupplier) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 不为空断言（集合非空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void notEmpty(Collection<?> value, String message) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 不为空断言（Map 非空）
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notEmpty(Map<?, ?> value, Supplier<String> messageSupplier) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 不为空断言（Map 非空）
     * @param value 校验值
     * @param message 错误信息
     */
    public void notEmpty(Map<?, ?> value, String message) {
        if(CValidUtils.isNotValid(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

}
