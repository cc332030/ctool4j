package com.c332030.ctool4j.mybatisplus.injector;

/**
 * <p>
 * Description: ICMpMethod
 * </p>
 *
 * @since 2026/1/6
 * @see "doc/design/mybatisplus/ICMpMethod.adoc"
 */
public interface ICMpMethod {

    /**
     * 获取 SQL 方法名
     * @param sqlMethod SQL 方法
     * @return 方法名
     */
    default String getMethod(ICMpSqlMethod sqlMethod) {
        return sqlMethod.getMethod();
    }

}
