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

    protected final ICMpSqlMethod sqlMethod;

    public CAbstractMethod(ICMpSqlMethod sqlMethodEnum) {
        sqlMethod = sqlMethodEnum;
    }

}
