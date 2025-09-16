package com.c332030.ctool.core.lang;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool.core.exception.CExceptionUtils;
import lombok.experimental.UtilityClass;

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

    public void notEmpty(String value, Supplier<String> messageSupplier) {
        if(StrUtil.isEmpty(value)) {
            CExceptionUtils.newBusinessException(messageSupplier.get());
        }
    }

    public void notEmpty(String value, String message) {
        if(StrUtil.isEmpty(value)) {
            CExceptionUtils.newBusinessException(message);
        }
    }

    public void notBlank(String value, Supplier<String> messageSupplier) {
        if(StrUtil.isBlank(value)) {
            CExceptionUtils.newBusinessException(messageSupplier.get());
        }
    }

    public void notBlank(String value, String message) {
        if(StrUtil.isBlank(value)) {
            CExceptionUtils.newBusinessException(message);
        }
    }

}
