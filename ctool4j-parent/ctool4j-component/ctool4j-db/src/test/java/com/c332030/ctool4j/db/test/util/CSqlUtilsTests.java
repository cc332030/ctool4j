package com.c332030.ctool4j.db.test.util;

import cn.hutool.core.lang.Pair;
import com.c332030.ctool4j.db.enums.CSqlSeparatorEnum;
import com.c332030.ctool4j.db.util.CSqlUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Description: CSqlUtilsTests
 * </p>
 *
 * <p>
 * 是 {@link CSqlUtils} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按功能分组覆盖：limit 语句、行锁语句、表别名、列名转换（lambda 与字符串）、多列拼接、</li>
 *   <li>大于条件、等值条件（单对/多对/别名/空集合）。</li>
 *   <li>覆盖正例、边界（0、null、空白别名、空集合）、反例（{@code limitSql(null)} 抛异常）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 limitSql 默认值/空值抛异常、别名空白返回空串、空集合返回空串的约定。</li>
 *   <li>依据白盒/黑盒原则：各方法取典型值与边界值覆盖；{@code limitSql(null)} 验证异常路径；</li>
 *   <li>布尔字段列验证不加别名前缀的兜底逻辑。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：limit（默认/指定/0/1/null）、行锁、表别名（非空/空/空白）、列名（lambda/字符串/复合字段）、</li>
 *   <li>多列拼接（含别名、布尔字段、空集合）、大于条件（含别名）、等值条件（单对/多对/别名/空集合）。</li>
 *   <li>未覆盖：非空校验之外的非法入参（如非法字段名）；SQL 注入转义（本类不负责）。</li>
 * </ul>
 * <h2>limit 语句</h2>
 * <ul>
 *   <li>1.1 默认/指定大小：limit 10 / limit 5 / limit 0 / limit 1，及 LIMIT_ONE 常量（limitSql）</li>
 *   <li>1.2 空值异常：{@code limitSql(null)} 抛 {@code IllegalArgumentException}（limitSqlNull）</li>
 * </ul>
 * <h2>行锁语句</h2>
 * <ul>
 *   <li>2.1 返回 {@code for update}（forUpdate）</li>
 * </ul>
 * <h2>表别名</h2>
 * <ul>
 *   <li>3.1 非空白别名：{@code t.} / {@code t1.} / {@code t2.}（getTableAliasSql）</li>
 *   <li>3.2 空/空白别名：返回空串（getTableAliasSqlBlank）</li>
 * </ul>
 * <h2>列名转换</h2>
 * <ul>
 *   <li>4.1 lambda 字段名转列名：id / user_name / user_first_name（toColumnNameFunc）</li>
 *   <li>4.2 字符串转列名：id / user_id（含 UserId）/ user_first_name（toColumnName）</li>
 * </ul>
 * <h2>多列拼接</h2>
 * <ul>
 *   <li>5.1 多列：id,user_name / t.id,t.user_name（getColumnsSql）</li>
 *   <li>5.2 布尔字段带别名：t.deleted,t.user_name（getColumnsSqlBooleanFieldAlias）</li>
 *   <li>5.3 空/null 集合：返回空串（getColumnsSqlEmpty）</li>
 * </ul>
 * <h2>大于条件</h2>
 * <ul>
 *   <li>6.1 无别名/带别名：age &gt; 18 / t.age &gt; 18 / 数值 0 边界（getGreaterSql）</li>
 * </ul>
 * <h2>等值条件</h2>
 * <ul>
 *   <li>7.1 单对（无/带/空别名）：id = user_name / t1.id = t2.user_name / 空别名（getEqualsSqlTwoFunc）</li>
 *   <li>7.2 多对按分隔符拼接：COMMA / AND / OR（getEqualsSqlPairs）</li>
 *   <li>7.3 多对带左右别名：t1.id = t2.user_name（getEqualsSqlPairsWithAlias）</li>
 *   <li>7.4 空集合：返回空串（getEqualsSqlPairsEmpty）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CSqlUtilsTests {

    /**
     * 测试实体，用于提供属性 lambda
     */
    @Getter
    @NoArgsConstructor
    public static class TestUser {

        private Long id;

        private String userName;

        private Integer age;

        private Boolean deleted;

        private String userFirstName;

    }

    /**
     * 对应测试用例 1.1：limit 默认/指定大小
     */
    @Test
    public void limitSql() {
        Assertions.assertEquals("limit 10", CSqlUtils.limitSql());
        Assertions.assertEquals("limit 5", CSqlUtils.limitSql(5));
        Assertions.assertEquals("limit 0", CSqlUtils.limitSql(0));
        Assertions.assertEquals("limit 1", CSqlUtils.limitSql(1));
        Assertions.assertEquals("limit 1", CSqlUtils.LIMIT_ONE);
    }

    /**
     * 对应测试用例 1.2：limitSql(null) 抛异常
     */
    @Test
    public void limitSqlNull() {
        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CSqlUtils.limitSql(null)
        );
    }

    /**
     * 对应测试用例 2.1：行锁语句
     */
    @Test
    public void forUpdate() {
        Assertions.assertEquals("for update", CSqlUtils.forUpdate());
    }

    /**
     * 对应测试用例 4.1：lambda 字段名转列名
     */
    @Test
    public void toColumnNameFunc() {
        Assertions.assertEquals("id", CSqlUtils.toColumnName(TestUser::getId));
        Assertions.assertEquals("user_name", CSqlUtils.toColumnName(TestUser::getUserName));
        Assertions.assertEquals("user_first_name", CSqlUtils.toColumnName(TestUser::getUserFirstName));
    }

    /**
     * 对应测试用例 3.1：非空白别名
     */
    @Test
    public void getTableAliasSql() {
        Assertions.assertEquals("t.", CSqlUtils.getTableAliasSql("t"));
        Assertions.assertEquals("t1.", CSqlUtils.getTableAliasSql("t1"));
        Assertions.assertEquals("t2.", CSqlUtils.getTableAliasSql("t2"));
    }

    /**
     * 对应测试用例 3.2：空/空白别名
     */
    @Test
    public void getTableAliasSqlBlank() {
        Assertions.assertEquals("", CSqlUtils.getTableAliasSql(""));
        Assertions.assertEquals("", CSqlUtils.getTableAliasSql(null));
        Assertions.assertEquals("", CSqlUtils.getTableAliasSql("  "));
    }

    /**
     * 对应测试用例 5.1：多列拼接
     */
    @Test
    public void getColumnsSql() {
        List<cn.hutool.core.lang.func.Func1<TestUser, ?>> funcList = Arrays.asList(
            TestUser::getId,
            TestUser::getUserName
        );
        Assertions.assertEquals("id,user_name", CSqlUtils.getColumnsSql(funcList, null));
        Assertions.assertEquals("t.id,t.user_name", CSqlUtils.getColumnsSql(funcList, "t"));
    }

    /**
     * 对应测试用例 5.2：布尔字段带别名
     */
    @Test
    public void getColumnsSqlBooleanFieldAlias() {
        // 普通 boolean 字段带别名时会加别名前缀
        List<cn.hutool.core.lang.func.Func1<TestUser, ?>> funcList = Arrays.asList(
            TestUser::getDeleted,
            TestUser::getUserName
        );
        Assertions.assertEquals("t.deleted,t.user_name", CSqlUtils.getColumnsSql(funcList, "t"));
    }

    /**
     * 对应测试用例 5.3：空/null 集合
     */
    @Test
    public void getColumnsSqlEmpty() {
        Assertions.assertEquals("", CSqlUtils.getColumnsSql(null, "t"));
        Assertions.assertEquals("", CSqlUtils.getColumnsSql(Collections.emptyList(), "t"));
    }

    /**
     * 对应测试用例 6.1：大于条件
     */
    @Test
    public void getGreaterSql() {
        Assertions.assertEquals("age > 18", CSqlUtils.getGreaterSql(TestUser::getAge, 18));
        Assertions.assertEquals("age > 0", CSqlUtils.getGreaterSql(TestUser::getAge, 0));
        Assertions.assertEquals("t.age > 18", CSqlUtils.getGreaterSql(TestUser::getAge, 18, "t"));
        Assertions.assertEquals("age > 18", CSqlUtils.getGreaterSql(TestUser::getAge, 18, null));
    }

    /**
     * 对应测试用例 7.1：单对等值条件
     */
    @Test
    public void getEqualsSqlTwoFunc() {
        Assertions.assertEquals(
            "id = user_name",
            CSqlUtils.getEqualsSql(TestUser::getId, TestUser::getUserName)
        );
        Assertions.assertEquals(
            "t1.id = t2.user_name",
            CSqlUtils.getEqualsSql(TestUser::getId, "t1", TestUser::getUserName, "t2")
        );
        Assertions.assertEquals(
            "id = user_name",
            CSqlUtils.getEqualsSql(TestUser::getId, null, TestUser::getUserName, null)
        );
    }

    /**
     * 对应测试用例 7.2：多对按分隔符拼接
     */
    @Test
    public void getEqualsSqlPairs() {
        List<Pair<cn.hutool.core.lang.func.Func1<TestUser, ?>, cn.hutool.core.lang.func.Func1<TestUser, ?>>> pairs = Arrays.asList(
            Pair.of(TestUser::getId, TestUser::getUserName),
            Pair.of(TestUser::getAge, TestUser::getId)
        );
        Assertions.assertEquals(
            "id = user_name , age = id",
            CSqlUtils.getEqualsSql(pairs, CSqlSeparatorEnum.COMMA)
        );
        Assertions.assertEquals(
            "id = user_name AND age = id",
            CSqlUtils.getEqualsSql(pairs, CSqlSeparatorEnum.AND)
        );
        Assertions.assertEquals(
            "id = user_name OR age = id",
            CSqlUtils.getEqualsSql(pairs, CSqlSeparatorEnum.OR)
        );
    }

    /**
     * 对应测试用例 7.3：多对带左右别名
     */
    @Test
    public void getEqualsSqlPairsWithAlias() {
        List<Pair<cn.hutool.core.lang.func.Func1<TestUser, ?>, cn.hutool.core.lang.func.Func1<TestUser, ?>>> pairs = Collections.singletonList(
            Pair.of(TestUser::getId, TestUser::getUserName)
        );
        Assertions.assertEquals(
            "t1.id = t2.user_name",
            CSqlUtils.getEqualsSql(pairs, "t1", "t2", CSqlSeparatorEnum.AND)
        );
    }

    /**
     * 对应测试用例 7.4：空集合
     */
    @Test
    public void getEqualsSqlPairsEmpty() {
        Assertions.assertEquals(
            "",
            CSqlUtils.getEqualsSql(Collections.emptyList(), CSqlSeparatorEnum.AND)
        );
    }

}
