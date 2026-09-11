package com.c332030.ctool4j.mybatis.test.injector;

import com.c332030.ctool4j.mybatisplus.injector.CMpSqlMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CMpSqlMethodTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证 SQL 方法枚举的方法名/描述/SQL 语句定义。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对枚举值的方法名/描述/SQL 的约定。</li>
 *   <li>依据测试方法（枚举值验证）：values、getMethod、getDesc、insertIgnoreSql、updateAllByIdSql。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：枚举值、方法名、描述、插入忽略 SQL、按ID更新 SQL。</li>
 *   <li>未覆盖：真实 SQL 注入执行。</li>
 * </ul>
 * <h2>SQL 方法枚举</h2>
 * <ul>
 *   <li>1.1 枚举值（{@code values}）</li>
 *   <li>1.2 方法名（{@code getMethod}）</li>
 *   <li>1.3 描述（{@code getDesc}）</li>
 *   <li>1.4 插入忽略 SQL（{@code insertIgnoreSql}）</li>
 *   <li>1.5 按ID更新 SQL（{@code updateAllByIdSql}）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CMpSqlMethodTests {

        /**
         * 对应测试用例 1.1：枚举值（{@code values}）
         */
    @Test
    public void values() {
        Assertions.assertEquals(2, CMpSqlMethod.values().length);
    }

        /**
         * 对应测试用例 1.2：方法名（{@code getMethod}）
         */
    @Test
    public void getMethod() {
        // 枚举名转驼峰
        Assertions.assertEquals("insertIgnore", CMpSqlMethod.INSERT_IGNORE.getMethod());
        Assertions.assertEquals("updateAllById", CMpSqlMethod.UPDATE_ALL_BY_ID.getMethod());
    }

        /**
         * 对应测试用例 1.3：描述（{@code getDesc}）
         */
    @Test
    public void getDesc() {
        Assertions.assertEquals("插入一条数据（如果存在则忽略）", CMpSqlMethod.INSERT_IGNORE.getDesc());
        Assertions.assertEquals("根据ID 选择修改数据，数据为空则设置为空", CMpSqlMethod.UPDATE_ALL_BY_ID.getDesc());
    }

        /**
         * 对应测试用例 1.4：插入忽略 SQL（{@code insertIgnoreSql}）
         */
    @Test
    public void insertIgnoreSql() {
        // INSERT_IGNORE 基于 MyBatis-Plus INSERT_ONE 的 SQL，将 INSERT 替换为 INSERT IGNORE
        String sql = CMpSqlMethod.INSERT_IGNORE.getSql();
        Assertions.assertNotNull(sql);
        Assertions.assertTrue(sql.contains("INSERT IGNORE"));
        // 不应保留单独的 INSERT 前缀
        Assertions.assertFalse(sql.startsWith("INSERT "));
    }

        /**
         * 对应测试用例 1.5：按ID更新 SQL（{@code updateAllByIdSql}）
         */
    @Test
    public void updateAllByIdSql() {
        // UPDATE_ALL_BY_ID 未提供 sql
        Assertions.assertNull(CMpSqlMethod.UPDATE_ALL_BY_ID.getSql());
    }

}
