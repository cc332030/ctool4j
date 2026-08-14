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

    @Test
    public void values() {

        Assertions.assertEquals(4, CDbOperateEnum.values().length);

    }

    @Test
    public void insert() {

        Assertions.assertEquals("插入", CDbOperateEnum.INSERT.getText());
        Assertions.assertEquals("INSERT", CDbOperateEnum.INSERT.name());
        Assertions.assertEquals("INSERT", CDbOperateEnum.INSERT.getName());

    }

    @Test
    public void select() {

        Assertions.assertEquals("查询", CDbOperateEnum.SELECT.getText());
        Assertions.assertEquals("SELECT", CDbOperateEnum.SELECT.getName());

    }

    @Test
    public void update() {

        Assertions.assertEquals("更新", CDbOperateEnum.UPDATE.getText());
        Assertions.assertEquals("UPDATE", CDbOperateEnum.UPDATE.getName());

    }

    @Test
    public void delete() {

        Assertions.assertEquals("删除", CDbOperateEnum.DELETE.getText());
        Assertions.assertEquals("DELETE", CDbOperateEnum.DELETE.getName());

    }

    @Test
    public void valueOfUnknown() {

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CDbOperateEnum.valueOf("UNKNOWN")
        );

    }

}
