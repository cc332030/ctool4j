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
 * 是 {@link CSqlUtils} 的测试用例（对应测试文档
 * <code>doc/design/db/CSqlUtilsTests.adoc</code>）。
 * </p>
 *
 * @since 2026/8/14
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

    /** 对应测试用例 1.1：limit 默认/指定大小 */
    @Test
    public void limitSql() {
        Assertions.assertEquals("limit 10", CSqlUtils.limitSql());
        Assertions.assertEquals("limit 5", CSqlUtils.limitSql(5));
        Assertions.assertEquals("limit 0", CSqlUtils.limitSql(0));
        Assertions.assertEquals("limit 1", CSqlUtils.limitSql(1));
        Assertions.assertEquals("limit 1", CSqlUtils.LIMIT_ONE);
    }

    /** 对应测试用例 1.2：limitSql(null) 抛异常 */
    @Test
    public void limitSqlNull() {
        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CSqlUtils.limitSql(null)
        );
    }

    /** 对应测试用例 2.1：行锁语句 */
    @Test
    public void forUpdate() {
        Assertions.assertEquals("for update", CSqlUtils.forUpdate());
    }

    /** 对应测试用例 4.1：lambda 字段名转列名 */
    @Test
    public void toColumnNameFunc() {
        Assertions.assertEquals("id", CSqlUtils.toColumnName(TestUser::getId));
        Assertions.assertEquals("user_name", CSqlUtils.toColumnName(TestUser::getUserName));
        Assertions.assertEquals("user_first_name", CSqlUtils.toColumnName(TestUser::getUserFirstName));
    }

    /** 对应测试用例 3.1：非空白别名 */
    @Test
    public void getTableAliasSql() {
        Assertions.assertEquals("t.", CSqlUtils.getTableAliasSql("t"));
        Assertions.assertEquals("t1.", CSqlUtils.getTableAliasSql("t1"));
        Assertions.assertEquals("t2.", CSqlUtils.getTableAliasSql("t2"));
    }

    /** 对应测试用例 3.2：空/空白别名 */
    @Test
    public void getTableAliasSqlBlank() {
        Assertions.assertEquals("", CSqlUtils.getTableAliasSql(""));
        Assertions.assertEquals("", CSqlUtils.getTableAliasSql(null));
        Assertions.assertEquals("", CSqlUtils.getTableAliasSql("  "));
    }

    /** 对应测试用例 5.1：多列拼接 */
    @Test
    public void getColumnsSql() {
        List<cn.hutool.core.lang.func.Func1<TestUser, ?>> funcList = Arrays.asList(
            TestUser::getId,
            TestUser::getUserName
        );
        Assertions.assertEquals("id,user_name", CSqlUtils.getColumnsSql(funcList, null));
        Assertions.assertEquals("t.id,t.user_name", CSqlUtils.getColumnsSql(funcList, "t"));
    }

    /** 对应测试用例 5.2：布尔字段带别名 */
    @Test
    public void getColumnsSqlBooleanFieldAlias() {
        // 普通 boolean 字段带别名时会加别名前缀
        List<cn.hutool.core.lang.func.Func1<TestUser, ?>> funcList = Arrays.asList(
            TestUser::getDeleted,
            TestUser::getUserName
        );
        Assertions.assertEquals("t.deleted,t.user_name", CSqlUtils.getColumnsSql(funcList, "t"));
    }

    /** 对应测试用例 5.3：空/null 集合 */
    @Test
    public void getColumnsSqlEmpty() {
        Assertions.assertEquals("", CSqlUtils.getColumnsSql(null, "t"));
        Assertions.assertEquals("", CSqlUtils.getColumnsSql(Collections.emptyList(), "t"));
    }

    /** 对应测试用例 6.1：大于条件 */
    @Test
    public void getGreaterSql() {
        Assertions.assertEquals("age > 18", CSqlUtils.getGreaterSql(TestUser::getAge, 18));
        Assertions.assertEquals("age > 0", CSqlUtils.getGreaterSql(TestUser::getAge, 0));
        Assertions.assertEquals("t.age > 18", CSqlUtils.getGreaterSql(TestUser::getAge, 18, "t"));
        Assertions.assertEquals("age > 18", CSqlUtils.getGreaterSql(TestUser::getAge, 18, null));
    }

    /** 对应测试用例 7.1：单对等值条件 */
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

    /** 对应测试用例 7.2：多对按分隔符拼接 */
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

    /** 对应测试用例 7.3：多对带左右别名 */
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

    /** 对应测试用例 7.4：空集合 */
    @Test
    public void getEqualsSqlPairsEmpty() {
        Assertions.assertEquals(
            "",
            CSqlUtils.getEqualsSql(Collections.emptyList(), CSqlSeparatorEnum.AND)
        );
    }

}
