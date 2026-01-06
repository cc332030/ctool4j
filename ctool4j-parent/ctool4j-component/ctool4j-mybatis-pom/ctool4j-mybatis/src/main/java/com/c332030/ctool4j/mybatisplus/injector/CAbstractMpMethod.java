package com.c332030.ctool4j.mybatisplus.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;

/**
 * <p>
 * Description: CAbstractMpMethod
 * </p>
 *
 * @author c332030
 * @since 2024/5/7
 */
public abstract class CAbstractMpMethod extends AbstractMethod implements ICMpMethod {

    private static final long serialVersionUID = 1L;

    protected final ICMpSqlMethod sqlMethod;

    public CAbstractMpMethod(ICMpSqlMethod sqlMethodEnum) {
        super(sqlMethodEnum.getMethod());
        sqlMethod = sqlMethodEnum;
    }

}
