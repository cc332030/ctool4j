package com.c332030.ctool4j.mybatisplus.injector;

/**
 * <p>
 * Description: ICMpSqlMethod
 * </p>
 *
 * @author c332030
 * @since 2024/5/7
 * @see doc/design/mybatisplus/ICMpSqlMethod.adoc
 */
public interface ICMpSqlMethod {

    /**
     * 获取方法名
     * @return 方法名
     */
    String getMethod();

    /**
     * 获取方法描述
     * @return 方法描述
     */
    String getDesc();

    /**
     * 获取 SQL 语句
     * @return SQL 语句
     */
    String getSql();

}
