package com.c332030.ctool4j.mybatisplus.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;

/**
 * <p>
 * Description: CAbstractMethod
 * </p>
 *
 * @author c332030
 * @since 2024/5/7
 */
public abstract class CAbstractMethod extends AbstractMethod {

    private static final long serialVersionUID = 1L;

    protected final ISqlMethodEnum sqlMethod;

    protected final String methodName;

    public CAbstractMethod(ISqlMethodEnum sqlMethodEnum) {
        sqlMethod = sqlMethodEnum;
        methodName = sqlMethod.getMethod();
    }

}
