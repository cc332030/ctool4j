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

    /**
     * 获取数据库字段名，驼峰转下划线
     * @param func 属性 lambda
     * @return 数据库字段名
     */
    public String toColumnName(Func1<?, ?> func) {
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
     */
    public String getGreaterSql(Func1<?, ?> func, Number number) {
        return CStrUtils.format(
                "{} > {}",
                toColumnName(func),
                number
        );
    }

}
