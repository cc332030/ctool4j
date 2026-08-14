package com.c332030.ctool4j.mybatis.test.injector;

import com.c332030.ctool4j.mybatisplus.injector.CMpSqlMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CMpSqlMethodTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CMpSqlMethodTests {

    @Test
    public void values() {
        Assertions.assertEquals(2, CMpSqlMethod.values().length);
    }

    @Test
    public void getMethod() {
        // 枚举名转驼峰
        Assertions.assertEquals("insertIgnore", CMpSqlMethod.INSERT_IGNORE.getMethod());
        Assertions.assertEquals("updateAllById", CMpSqlMethod.UPDATE_ALL_BY_ID.getMethod());
    }

    @Test
    public void getDesc() {
        Assertions.assertEquals("插入一条数据（如果存在则忽略）", CMpSqlMethod.INSERT_IGNORE.getDesc());
        Assertions.assertEquals("根据ID 选择修改数据，数据为空则设置为空", CMpSqlMethod.UPDATE_ALL_BY_ID.getDesc());
    }

    @Test
    public void insertIgnoreSql() {
        // INSERT_IGNORE 基于 MyBatis-Plus INSERT_ONE 的 SQL，将 INSERT 替换为 INSERT IGNORE
        String sql = CMpSqlMethod.INSERT_IGNORE.getSql();
        Assertions.assertNotNull(sql);
        Assertions.assertTrue(sql.contains("INSERT IGNORE"));
        // 不应保留单独的 INSERT 前缀
        Assertions.assertFalse(sql.startsWith("INSERT "));
    }

    @Test
    public void updateAllByIdSql() {
        // UPDATE_ALL_BY_ID 未提供 sql
        Assertions.assertNull(CMpSqlMethod.UPDATE_ALL_BY_ID.getSql());
    }

}
