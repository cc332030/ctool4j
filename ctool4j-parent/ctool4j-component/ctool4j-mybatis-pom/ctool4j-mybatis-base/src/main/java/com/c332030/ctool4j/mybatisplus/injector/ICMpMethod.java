package com.c332030.ctool4j.mybatisplus.injector;

/**
 * <p>
 * Description: ICMpMethod
 * </p>
 *
 * @since 2026/1/6
 */
public interface ICMpMethod {

    default String getMethod(ICMpSqlMethod sqlMethod) {
        return sqlMethod.getMethod();
    }

}
