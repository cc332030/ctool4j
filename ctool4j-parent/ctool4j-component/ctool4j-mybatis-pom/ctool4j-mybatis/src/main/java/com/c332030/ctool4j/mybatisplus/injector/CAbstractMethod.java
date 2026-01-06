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
public abstract class CAbstractMethod extends AbstractMethod implements ICMpMethod {

    private static final long serialVersionUID = 1L;

    protected final ICMpSqlMethod sqlMethod;

    public CAbstractMethod(ICMpSqlMethod sqlMethodEnum) {
        super(sqlMethodEnum.getMethod());
        sqlMethod = sqlMethodEnum;
    }

}
