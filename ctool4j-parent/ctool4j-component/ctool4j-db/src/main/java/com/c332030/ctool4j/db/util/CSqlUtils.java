package com.c332030.ctool4j.db.util;

import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CStrUtils;
import lombok.experimental.UtilityClass;

/**
 * <p>
 * Description: CSqlUtils
 * </p>
 *
 * @since 2025/11/5
 */
@UtilityClass
public class CSqlUtils {

    public String getTableAliasSql(String alias) {
        return CStrUtils.convertNotBlank(alias, e -> e + ".", StrUtil.EMPTY);
    }

    /**
     * 获取数据库字段名，驼峰转下划线
     * @param func 属性 lambda
     * @return 数据库字段名
     * @param <T> 泛型
     */
    public <T> String toColumnName(Func1<T, ?> func) {
        return toColumnName(LambdaUtil.getFieldName(func));
    }

    /**
     * 获取数据库字段名，驼峰转下划线
     * @param fieldName 属性名
     * @return 数据库字段名
     */
    public String toColumnName(String fieldName) {
        return StrUtil.toUnderlineCase(fieldName);
    }

    /**
     * 大于 sql
     * @param func 属性 lambda
     * @param number 数值
     * @return sql
     * @param <T> 泛型
     */
    public <T> String getGreaterSql(Func1<T, ?> func, Number number) {
        return getGreaterSql(func, number, null);
    }

    /**
     * 大于 sql
     * @param func 属性 lambda
     * @param number 数值
     * @param alias 别名
     * @return sql
     * @param <T> 泛型
     */
    public <T> String getGreaterSql(Func1<T, ?> func, Number number, String alias) {
        return CStrUtils.format(
                "{}{} > {}",
                getTableAliasSql(alias),
                toColumnName(func),
                number
        );
    }

}
