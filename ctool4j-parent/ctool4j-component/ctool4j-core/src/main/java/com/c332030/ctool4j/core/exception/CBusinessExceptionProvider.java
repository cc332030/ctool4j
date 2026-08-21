package com.c332030.ctool4j.core.exception;

import com.c332030.ctool4j.definition.function.CTriFunction;
import com.c332030.ctool4j.definition.interfaces.ICRes;

/**
 * <p>
 * Description: CBusinessExceptionProvider
 * </p>
 *
 * @since 2025/9/14
 * @see doc/design/core/CBusinessExceptionProvider.adoc
 * @see doc/design/core/CBusinessExceptionProviderTests.adoc
 */
public class CBusinessExceptionProvider implements ICBusinessExceptionProvider<CBusinessException> {

    /**
     * 获取创建业务异常的函数（默认即 CBusinessException 构造引用）
     *
     * @return 业务异常创建函数
     */
    @Override
    public CTriFunction<ICRes<?>, String, Throwable, CBusinessException> getExceptionFunction() {
        return CBusinessException::new;
    }

}
