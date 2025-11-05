package com.c332030.ctool4j.db.util;

import cn.hutool.core.lang.func.Func1;
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

    /**
     * 获取数据库字段名，驼峰转下划线
     * @param func 属性 lambda
     * @return 数据库字段名
     */
    public String getColumnName(Func1<?, ?> func) {
        return "";
//        return CStrUtils.lowerCamelToLowerUnderscore(func);
    }

    /**
     * 大于 sql
     * @param func 属性 lambda
     * @param number 数值
     * @return sql
     */
    public String getGreaterSql(Func1<?, ?> func, Number number) {
        return CStrUtils.format(
                "{} > {}",
                getColumnName(func),
                number
        );
    }

}
