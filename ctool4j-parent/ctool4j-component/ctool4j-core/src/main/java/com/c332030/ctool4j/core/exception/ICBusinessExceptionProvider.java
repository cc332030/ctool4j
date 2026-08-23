package com.c332030.ctool4j.core.exception;

import com.c332030.ctool4j.core.util.CResUtils;
import com.c332030.ctool4j.definition.function.CBiFunction;
import com.c332030.ctool4j.definition.function.CTriFunction;
import com.c332030.ctool4j.definition.interfaces.ICRes;
import lombok.val;

/**
 * <p>
 * Description: ICBusinessExceptionProvider
 * </p>
 *
 * @since 2025/9/14
 * @see "doc/design/core/ICBusinessExceptionProvider.adoc"
 * @see "doc/design/core/ICBusinessExceptionProviderTests.adoc"
 */
public interface ICBusinessExceptionProvider<T extends Throwable> {

    default CTriFunction<ICRes<?>, String, Throwable, T> getExceptionFunction() {
        return (error, errorExtend, cause) -> {

            val message = CResUtils.formatResMessage(error, errorExtend);
            return getMessageExceptionFunction().apply(message, cause);
        };
    }

    /**
     * 获取异常生成函数（默认实现抛出 UnsupportedOperationException）
     * @return 异常生成函数
     */
    default CBiFunction<String, Throwable, T> getMessageExceptionFunction() {
        return (message, cause) -> {
            throw new UnsupportedOperationException("No impl");
        };
    }

}
