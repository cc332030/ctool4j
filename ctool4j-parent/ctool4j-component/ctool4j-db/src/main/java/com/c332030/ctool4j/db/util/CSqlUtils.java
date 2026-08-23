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
 * @see "doc/design/db/CSqlUtils.adoc"
 * @see "doc/design/db/CSqlUtilsTests.adoc"
 * @since 2025/11/5
 */
@UtilityClass
public class CSqlUtils {

    /**
     * limit 1 语句
     */
    public final String LIMIT_ONE = limitSql(1);

    /**
     * 默认分页大小的 limit 语句
     *
     * @return limit 语句
     */
    public String limitSql() {
        return limitSql(CPageUtils.DEFAULT_PAGE_SIZE);
    }

    /**
     * 指定大小的 limit 语句
     *
     * @param size 大小
     * @return limit 语句
     */
    public String limitSql(Integer size) {
        if(null == size) {
            throw new IllegalArgumentException("size 不能为空");
        }
        return "limit " + size;
    }

    /**
     * 行锁语句
     *
     * @return 行锁语句
     */
    public String forUpdate() {
        return "for update";
    }


    /**
     * 表别名 t
     */
    public static final String TABLE_ALIAS_T = "t";

    /**
     * 表别名 t1
     */
    public static final String TABLE_ALIAS_T1 = "t1";

    /**
     * 表别名 t2
     */
    public static final String TABLE_ALIAS_T2 = "t2";

    /**
     * 获取表别名 sql（别名非空白时加 "别名."）
     *
     * @param alias 别名
     * @return 表别名 sql
     */
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

    /**
     * 获取左列等于右列 sql
     *
     * @param leftFunc  左列 lambda
     * @param rightFunc 右列 lambda
     * @param <T1>      左泛型
     * @param <T2>      右泛型
     * @return sql
     */
    public <T1, T2> String getEqualsSql(Func1<T1, ?> leftFunc, Func1<T2, ?> rightFunc) {
        return getEqualsSql(leftFunc, null, rightFunc, null);
    }

    /**
     * 获取左列等于右列 sql（带别名）
     *
     * @param leftFunc   左列 lambda
     * @param leftAlias  左别名
     * @param rightFunc  右列 lambda
     * @param rightAlias 右别名
     * @param <T1>       左泛型
     * @param <T2>       右泛型
     * @return sql
     */
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
