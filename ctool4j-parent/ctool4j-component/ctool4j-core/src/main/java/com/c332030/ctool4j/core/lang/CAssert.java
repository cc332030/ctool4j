package com.c332030.ctool4j.core.lang;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.exception.CExceptionUtils;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * <p>
 * Description: CAssert
 * </p>
 *
 * @since 2025/9/14
 */
@UtilityClass
public class CAssert {

    /**
     * 断言
     * @param value 断言条件
     * @param messageSupplier 错误信息提供者
     */
    public void isTrue(boolean value, Supplier<String> messageSupplier) {
        if(value) {
            CExceptionUtils.throwBusinessException(messageSupplier);
        }
    }

    /**
     * 断言
     * @param value 断言条件
     * @param message 错误信息
     */
    public void isTrue(boolean value, String message) {
        if(value) {
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

}
