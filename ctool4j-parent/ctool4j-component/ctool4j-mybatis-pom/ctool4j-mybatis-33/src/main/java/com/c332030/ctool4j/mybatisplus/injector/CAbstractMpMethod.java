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

    protected final ICMpSqlMethod sqlMethod;

    public CAbstractMpMethod(ICMpSqlMethod sqlMethodEnum) {
        sqlMethod = sqlMethodEnum;
    }

}
