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
 * <h2>能力目录</h2>
 * <p>{@code CSqlUtils}（{@code @UtilityClass}，静态工具类）提供 SQL 拼接相关的常用片段生成：</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>{@code limitSql(null)}</td>
 *     <td>抛 {@code IllegalArgumentException}</td>
 *   </tr>
 *   <tr>
 *     <td>{@code getTableAliasSql} 空白/空别名</td>
 *     <td>返回空串</td>
 *   </tr>
 *   <tr>
 *     <td>{@code getColumnsSql} 空/null 集合</td>
 *     <td>返回空串</td>
 *   </tr>
 *   <tr>
 *     <td>布尔字段列</td>
 *     <td>不加别名前缀（避免 {@code t.true} 这类非法片段）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>MyBatis/手写 SQL 场景下生成 limit、行锁、列名与等值/大于条件片段。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅生成片段字符串，不执行 SQL；不处理参数绑定与注入转义，调用方自行拼接到完整 SQL 时需注意。</li>
 *   <li>{@code toColumnName} 依赖 hutool 的下划线转换规则，对含数字/特殊字符的字段名转换结果需调用方确认。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>列名转换基于 lambda 字段名（hutool 序列化解析），复杂 lambda（如嵌套调用）可能无法正确解析字段名。</li>
 *   <li>{@code getGreaterSql}/{@code getEqualsSql} 不校验数值与别名的合法性，直接拼入字符串。</li>
 *   <li>拼接类方法为纯字符串拼接，不防 SQL 注入，条件值部分由调用方负责参数化。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义约定</b></p>
 * <ul>
 *   <li>列名统一经驼峰转下划线（{@code StrUtil.toUnderlineCase}），支持属性 lambda（hutool {@code LambdaUtil.getFieldName}）。</li>
 *   <li>别名非空白（{@code StrUtil.isBlank} 判断）时加 {@code 别名.} 前缀；布尔字面量列（{@code true}/{@code false}）不加前缀。</li>
 *   <li>{@code getColumnsSql} 空集合/null 返回空串；{@code getEqualsSql} 多对版本按分隔符枚举的 joiningCollector 拼接。</li>
 * </ul>
 * <p><b>实现方式</b></p>
 * <ul>
 *   <li>借助 hutool（{@code CollUtil}/{@code StrUtil}/{@code LambdaUtil}/{@code Pair}）与自定义 {@code CStrUtils}/{@code CPageUtils}。</li>
 *   <li>{@code getGreaterSql}/{@code getEqualsSql} 通过 {@code CStrUtils.format} 组装字符串，列名经 {@code toColumnName} 转换。</li>
 * </ul>
 *
 * @since 2025/11/5
 * @version 1.0
 */
@UtilityClass
public class CSqlUtils {

    /**
     * limit 1 语句
     */
    public final String LIMIT_ONE = limitSql(1);

    /**
     * 默认分页大小的 limit 语句
     * <ul>
     *   <li>{@code limitSql()} / {@code limitSql(Integer)}：生成 {@code limit N} 语句（默认 {@code CPageUtils.DEFAULT_PAGE_SIZE}）；</li>
     *   <li>常量 {@code LIMIT_ONE = limitSql(1)}。</li>
     *   <li>{@code limitSql(null)} 抛出 {@code IllegalArgumentException("size 不能为空")}。</li>
     * </ul>
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
     * <ul>
     *   <li>{@code forUpdate()}：返回行锁语句 {@code for update}。</li>
     * </ul>
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
     * <ul>
     *   <li>{@code getTableAliasSql(String)}：表别名非空白时返回 {@code 别名.}，否则返回空串。</li>
     * </ul>
     *
     * @param alias 别名
     * @return 表别名 sql
     */
    public String getTableAliasSql(String alias) {
        return CStrUtils.convertNotBlank(alias, e -> e + ".", StrUtil.EMPTY);
    }

    /**
     * 获取数据库字段名，驼峰转下划线
     * <ul>
     *   <li>{@code toColumnName(Func1)} / {@code toColumnName(String)}：驼峰转下划线列名（hutool {@code StrUtil.toUnderlineCase}）。</li>
     * </ul>
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
     * <ul>
     *   <li>{@code getColumnsSql(Collection&lt;Func1&gt;, String alias)}：多个属性 lambda 转列名列表并按别名前缀、逗号拼接。</li>
     * </ul>
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
     * <ul>
     *   <li>{@code getGreaterSql(...)}：生成 {@code 列 &gt; 数值}（可带别名）条件。</li>
     * </ul>
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
     * <ul>
     *   <li>{@code getEqualsSql(...)}：生成 {@code 左列 = 右列}（可带别名）条件；支持单对、多对（按分隔符枚举拼接）。</li>
     * </ul>
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
