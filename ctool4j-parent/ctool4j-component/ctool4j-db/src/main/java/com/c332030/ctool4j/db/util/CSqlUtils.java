package com.c332030.ctool4j.db.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CPageUtils;
import com.c332030.ctool4j.core.util.CStrUtils;
import com.c332030.ctool4j.db.enums.CSqlSeparatorEnum;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CSqlUtils
 * </p>
 *
 * @since 2025/11/5
 */
@UtilityClass
public class CSqlUtils {

    public String limitSql() {
        return limitSql(CPageUtils.DEFAULT_PAGE_SIZE);
    }

    public String limitSql(Integer size) {
        return "limit " + size;
    }

    public String limitOne() {
        return limitSql(1);
    }

    public String forUpdate() {
        return "for update";
    }


    public static final String TABLE_ALIAS_T = "t";

    public static final String TABLE_ALIAS_T1 = "t1";

    public static final String TABLE_ALIAS_T2 = "t2";

    public String getTableAliasSql(String alias) {
        return CStrUtils.convertNotBlank(alias, e -> e + ".", StrUtil.EMPTY);
    }

    /**
     * 获取数据库字段名，驼峰转下划线
     *
     * @param func 属性 lambda
     * @param <T>  泛型
     * @return 数据库字段名
     */
    public <T> String toColumnName(Func1<T, ?> func) {
        return toColumnName(LambdaUtil.getFieldName(func));
    }

    /**
     * 获取数据库字段名，驼峰转下划线
     *
     * @param fieldName 属性名
     * @return 数据库字段名
     */
    public String toColumnName(String fieldName) {
        return StrUtil.toUnderlineCase(fieldName);
    }

    private <T> String getColumnAliasName(Func1<T, ?> func, String alias) {
        return getColumnAliasName(toColumnName(func), alias);
    }

    private String getColumnAliasName(String fieldName, String alias) {
        return getAliasName(toColumnName(fieldName), alias);
    }

    private String getAliasName(String column, String alias) {

        if (StrUtil.isBlank(alias)
            || Boolean.TRUE.toString().equalsIgnoreCase(column)
            || Boolean.FALSE.toString().equalsIgnoreCase(column)
        ) {
            return column;
        }

        return alias + "." + column;
    }

    /**
     * 获取字段 sql
     *
     * @param funcList 属性 lambda 列表
     * @param alias    别名
     * @param <T>      泛型
     * @return sql
     */
    public <T> String getColumnsSql(Collection<Func1<T, ?>> funcList, String alias) {

        if (CollUtil.isEmpty(funcList)) {
            return StrUtil.EMPTY;
        }

        return funcList.stream()
            .map(LambdaUtil::getFieldName)
            .map(CSqlUtils::toColumnName)
            .map(column -> getAliasName(column, alias))
            .collect(Collectors.joining(","));
    }

    /**
     * 大于 sql
     *
     * @param func   属性 lambda
     * @param number 数值
     * @param <T>    泛型
     * @return sql
     */
    public <T> String getGreaterSql(Func1<T, ?> func, Number number) {
        return getGreaterSql(func, number, null);
    }

    /**
     * 大于 sql
     *
     * @param func   属性 lambda
     * @param number 数值
     * @param alias  别名
     * @param <T>    泛型
     * @return sql
     */
    public <T> String getGreaterSql(Func1<T, ?> func, Number number, String alias) {
        return CStrUtils.format(
            "{} > {}",
            getColumnAliasName(func, alias),
            number
        );
    }

    public <T1, T2> String getEqualsSql(Func1<T1, ?> leftFunc, Func1<T2, ?> rightFunc) {
        return getEqualsSql(leftFunc, null, rightFunc, null);
    }

    public <T1, T2> String getEqualsSql(
        Func1<T1, ?> leftFunc, String leftAlias,
        Func1<T2, ?> rightFunc, String rightAlias
    ) {
        return CStrUtils.format(
            "{} = {}",
            getColumnAliasName(leftFunc, leftAlias),
            getColumnAliasName(rightFunc, rightAlias)
        );
    }


    /**
     * 获取等于 sql
     *
     * @param pairs         属性 lambda 列表
     * @param separatorEnum 分隔符枚举
     * @param <T1>          左 泛型
     * @param <T2>          右 泛型
     * @return sql
     */
    public <T1, T2> String getEqualsSql(
        Collection<Pair<Func1<T1, ?>, Func1<T2, ?>>> pairs,
        CSqlSeparatorEnum separatorEnum
    ) {
        return getEqualsSql(pairs, null, null, separatorEnum);
    }

    /**
     * 获取等于 sql
     *
     * @param pairs         属性 lambda 列表
     * @param leftAlias     左 别名
     * @param rightAlias    右 别名
     * @param separatorEnum 分隔符枚举
     * @param <T1>          左 泛型
     * @param <T2>          右 泛型
     * @return sql
     */
    public <T1, T2> String getEqualsSql(
        Collection<Pair<Func1<T1, ?>, Func1<T2, ?>>> pairs,
        String leftAlias, String rightAlias,
        CSqlSeparatorEnum separatorEnum
    ) {
        return pairs.stream()
            .map(pair -> getEqualsSql(pair.getKey(), leftAlias, pair.getValue(), rightAlias))
            .collect(separatorEnum.getJoiningCollector());
    }

}
