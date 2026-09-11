package com.c332030.ctool4j.core.validation;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
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
 *   <li>{@code notEmpty}：字符串/byte/int/long/Object 数组/Collection/Map 非空</li>
 *   <li>{@code notBlank}：字符串非空白</li>
 * </ul>
 * <p>每个断言均有「String message」与「Supplier&lt;String&gt;」两个重载。</p>
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
 *     <td>notEmpty/notBlank 空值</td>
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
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>统一抛业务异常，与业务校验语义一致，调用方可按业务异常统一处理。</li>
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
 * <p><b>空值语义</b></p>
 * <ul>
 *   <li>{@code notEmpty}（字符串）用 {@code StrUtil.isEmpty}（null 或空串视为空）。</li>
 *   <li>{@code notBlank} 用 {@code StrUtil.isBlank}（null、空串、纯空白视为空）。</li>
 *   <li>{@code notEmpty}（数组/集合/Map）分别用 {@code ArrayUtil.isEmpty} / {@code CollUtil.isEmpty} / {@code MapUtil.isEmpty}。</li>
 * </ul>
 *
 * @since 2025/9/14
 * @version 1.0
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
     * 不为空断言
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notEmpty(String value, Supplier<String> messageSupplier) {
        if(StrUtil.isEmpty(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 不为空断言
     * @param value 校验值
     * @param message 错误信息
     */
    public void notEmpty(String value, String message) {
        if(StrUtil.isEmpty(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 不为空且不为空白断言
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notBlank(String value, Supplier<String> messageSupplier) {
        if(StrUtil.isBlank(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 不为空且不为空白断言
     * @param value 校验值
     * @param message 错误信息
     */
    public void notBlank(String value, String message) {
        if(StrUtil.isBlank(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 不为空断言
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notEmpty(byte[] value, Supplier<String> messageSupplier) {
        if(ArrayUtil.isEmpty(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 不为空断言
     * @param value 校验值
     * @param message 错误信息
     */
    public void notEmpty(byte[] value, String message) {
        if(ArrayUtil.isEmpty(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 不为空断言
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notEmpty(int[] value, Supplier<String> messageSupplier) {
        if(ArrayUtil.isEmpty(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 不为空断言
     * @param value 校验值
     * @param message 错误信息
     */
    public void notEmpty(int[] value, String message) {
        if(ArrayUtil.isEmpty(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 不为空断言
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notEmpty(long[] value, Supplier<String> messageSupplier) {
        if(ArrayUtil.isEmpty(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 不为空断言
     * @param value 校验值
     * @param message 错误信息
     */
    public void notEmpty(long[] value, String message) {
        if(ArrayUtil.isEmpty(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 不为空断言
     * @param value 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notEmpty(Object[] value, Supplier<String> messageSupplier) {
        if(ArrayUtil.isEmpty(value)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 不为空断言
     * @param value 校验值
     * @param message 错误信息
     */
    public void notEmpty(Object[] value, String message) {
        if(ArrayUtil.isEmpty(value)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 不为空断言
     * @param collection 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notEmpty(Collection<?> collection, Supplier<String> messageSupplier) {
        if(CollUtil.isEmpty(collection)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 不为空断言
     * @param collection 校验值
     * @param message 错误信息
     */
    public void notEmpty(Collection<?> collection, String message) {
        if(CollUtil.isEmpty(collection)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

    /**
     * 不为空断言
     * @param map 校验值
     * @param messageSupplier 错误信息提供者
     */
    public void notEmpty(Map<?, ?> map, Supplier<String> messageSupplier) {
        if(MapUtil.isEmpty(map)) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 不为空断言
     * @param map 校验值
     * @param message 错误信息
     */
    public void notEmpty(Map<?, ?> map, String message) {
        if(MapUtil.isEmpty(map)) {
            CExceptionUtils.throwBusinessException(message);
        }
    }

}
