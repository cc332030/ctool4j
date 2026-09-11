package com.c332030.ctool4j.definition.test.enums;

import com.c332030.ctool4j.definition.enums.CDbOperateEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CDbOperateEnumTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证枚举数量、各枚举的描述/名称，以及未知枚举名抛异常。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对枚举元素与描述的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：枚举数量；四枚举 text/name；未知名抛异常。</li>
 *   <li>未覆盖：无。</li>
 * </ul>
 * <h2>枚举</h2>
 * <ul>
 *   <li>1.1 values：4 个枚举（values）</li>
 *   <li>1.2 insert：{@code 插入}/INSERT（insert）</li>
 *   <li>1.3 select：{@code 查询}/SELECT（select）</li>
 *   <li>1.4 update：{@code 更新}/UPDATE（update）</li>
 *   <li>1.5 delete：{@code 删除}/DELETE（delete）</li>
 *   <li>1.6 未知名抛异常（valueOfUnknown）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CDbOperateEnumTests {

    /**
     * 对应测试用例 1.1：4 个枚举
     */
    @Test
    public void values() {

        Assertions.assertEquals(4, CDbOperateEnum.values().length);

    }

    /**
     * 对应测试用例 1.2：{@code 插入}/INSERT
     */
    @Test
    public void insert() {

        Assertions.assertEquals("插入", CDbOperateEnum.INSERT.getText());
        Assertions.assertEquals("INSERT", CDbOperateEnum.INSERT.name());
        Assertions.assertEquals("INSERT", CDbOperateEnum.INSERT.getName());

    }

    /**
     * 对应测试用例 1.3：{@code 查询}/SELECT
     */
    @Test
    public void select() {

        Assertions.assertEquals("查询", CDbOperateEnum.SELECT.getText());
        Assertions.assertEquals("SELECT", CDbOperateEnum.SELECT.getName());

    }

    /**
     * 对应测试用例 1.4：{@code 更新}/UPDATE
     */
    @Test
    public void update() {

        Assertions.assertEquals("更新", CDbOperateEnum.UPDATE.getText());
        Assertions.assertEquals("UPDATE", CDbOperateEnum.UPDATE.getName());

    }

    /**
     * 对应测试用例 1.5：{@code 删除}/DELETE
     */
    @Test
    public void delete() {

        Assertions.assertEquals("删除", CDbOperateEnum.DELETE.getText());
        Assertions.assertEquals("DELETE", CDbOperateEnum.DELETE.getName());

    }

    /**
     * 对应测试用例 1.6：未知名抛异常
     */
    @Test
    public void valueOfUnknown() {

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CDbOperateEnum.valueOf("UNKNOWN")
        );

    }

}
