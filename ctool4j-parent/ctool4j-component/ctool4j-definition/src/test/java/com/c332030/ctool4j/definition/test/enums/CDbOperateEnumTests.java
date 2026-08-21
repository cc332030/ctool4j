package com.c332030.ctool4j.definition.test.enums;

import com.c332030.ctool4j.definition.enums.CDbOperateEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CDbOperateEnumTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CDbOperateEnumTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void values() {

        Assertions.assertEquals(4, CDbOperateEnum.values().length);

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void insert() {

        Assertions.assertEquals("插入", CDbOperateEnum.INSERT.getText());
        Assertions.assertEquals("INSERT", CDbOperateEnum.INSERT.name());
        Assertions.assertEquals("INSERT", CDbOperateEnum.INSERT.getName());

    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void select() {

        Assertions.assertEquals("查询", CDbOperateEnum.SELECT.getText());
        Assertions.assertEquals("SELECT", CDbOperateEnum.SELECT.getName());

    }

    /**
     * 对应测试用例 1.4
     */
    @Test
    public void update() {

        Assertions.assertEquals("更新", CDbOperateEnum.UPDATE.getText());
        Assertions.assertEquals("UPDATE", CDbOperateEnum.UPDATE.getName());

    }

    /**
     * 对应测试用例 1.5
     */
    @Test
    public void delete() {

        Assertions.assertEquals("删除", CDbOperateEnum.DELETE.getText());
        Assertions.assertEquals("DELETE", CDbOperateEnum.DELETE.getName());

    }

    /**
     * 对应测试用例 1.6
     */
    @Test
    public void valueOfUnknown() {

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CDbOperateEnum.valueOf("UNKNOWN")
        );

    }

}
