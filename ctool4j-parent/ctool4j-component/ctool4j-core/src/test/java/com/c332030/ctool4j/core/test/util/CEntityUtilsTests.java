package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.classes.CBeanUtils;
import com.c332030.ctool4j.core.util.CEntityUtils;
import com.c332030.ctool4j.definition.entity.base.*;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * <p>
 * Description: CEntityUtilsTests
 * </p>
 *
 * @author c332030
 * @since 2025/12/20
 */
public class CEntityUtilsTests {

    /**
     * 测试清空 CBaseEntity 的公共字段
     * 对应测试用例 1.1
     */
    @Test
    public void clearCBaseEntity() {

        val entity = CBaseEntity.builder()
                .id(1L)
                .createById(33L)
                .createBy("c332030")
                .createTime(new Date())
                .updateById(44L)
                .updateBy("cc332030")
                .updateTime(new Date())
                .build();
        val entity2 = CBeanUtils.copy(entity, CBaseEntity.class);

        CEntityUtils.clear((Object) entity);
        Assertions.assertNull(entity.getId());
        Assertions.assertNull(entity.getCreateById());
        Assertions.assertNull(entity.getCreateBy());
        Assertions.assertNull(entity.getCreateTime());
        Assertions.assertNull(entity.getUpdateById());
        Assertions.assertNull(entity.getUpdateBy());
        Assertions.assertNull(entity.getUpdateTime());

        CEntityUtils.clear(entity2);
        Assertions.assertNull(entity2.getId());
        Assertions.assertNull(entity2.getCreateById());
        Assertions.assertNull(entity2.getCreateBy());
        Assertions.assertNull(entity2.getCreateTime());
        Assertions.assertNull(entity2.getUpdateById());
        Assertions.assertNull(entity2.getUpdateBy());
        Assertions.assertNull(entity2.getUpdateTime());


    }

    /**
     * 测试清空 CBaseTimeEntity 的公共字段
     * 对应测试用例 1.2
     */
    @Test
    public void clearBaseTimeEntity() {

        val entity = CBaseTimeEntity.builder()
                .id(1L)
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        val entity2 = CBeanUtils.copy(entity, CBaseTimeEntity.class);

        CEntityUtils.clear((Object) entity);
        Assertions.assertNull(entity.getId());
        Assertions.assertNull(entity.getCreateTime());
        Assertions.assertNull(entity.getUpdateTime());

        CEntityUtils.clear(entity2);
        Assertions.assertNull(entity2.getId());
        Assertions.assertNull(entity2.getCreateTime());
        Assertions.assertNull(entity2.getUpdateTime());

    }

    /**
     * 测试清空 CLongId 的 id 字段
     * 对应测试用例 1.3
     */
    @Test
    public void clearLongId() {

        val entity = CLongId.builder()
                .id(1L)
                .build();
        val entity2 = CBeanUtils.copy(entity, CLongId.class);

        CEntityUtils.clear((Object) entity);
        Assertions.assertNull(entity.getId());

        CEntityUtils.clear(entity2);
        Assertions.assertNull(entity2.getId());

    }

    /**
     * 测试清空无公共字段的普通对象
     * 对应测试用例 1.4
     */
    @Test
    public void clearNone() {

        CEntityUtils.clear(new Object());

    }

    /**
     * 测试无自身 clear 的子类，通过遍历父类/父接口链命中最近的清除方法
     * <p>验证按继承距离查找"最近" clear，而非依赖方法声明顺序</p>
     * 对应测试用例 2.1
     */
    @Test
    public void clearSubClass() {

        val entity = new XBaseTimeEntity();
        entity.setId(1L);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());

        // 走 Object 重载，运行时动态查找最近的 clear
        CEntityUtils.clear((Object) entity);

        Assertions.assertNull(entity.getId());
        Assertions.assertNull(entity.getCreateTime());
        Assertions.assertNull(entity.getUpdateTime());

    }

    /**
     * 测试接口按继承顺序查找"最近" clear：子类实现接口优先于父类实现接口
     * <p>XChild 类链（XChild、XParent）均无类级 clear，只能走接口；getInterfaces 按类继承
     * 由近及远取直接接口（子类接口在前），应命中子类接口 {@code ICCreateUpdateBy}（只清 by），
     * 而非父类接口 {@code ICCreateUpdateTime}（清 time）</p>
     * 对应测试用例 2.2
     */
    @Test
    public void clearChildInterfaceFirst() {

        val entity = new XChild();
        entity.setCreateById(1L);
        entity.setCreateBy("c332030");
        entity.setUpdateById(2L);
        entity.setUpdateBy("cc332030");
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());

        CEntityUtils.clear((Object) entity);

        // 命中子类接口 ICCreateUpdateBy：by 字段全清
        Assertions.assertNull(entity.getCreateById());
        Assertions.assertNull(entity.getCreateBy());
        Assertions.assertNull(entity.getUpdateById());
        Assertions.assertNull(entity.getUpdateBy());
        // 未命中父类接口 ICCreateUpdateTime：time 字段保留
        Assertions.assertNotNull(entity.getCreateTime());
        Assertions.assertNotNull(entity.getUpdateTime());

    }

    /**
     * 测试直接实现深层接口的类命中该接口的 clear，清空其全部字段
     * <p>XByAndTime 类链仅自身、无类级 clear，走接口命中 {@code ICCreateUpdateByAndTime}，
     * 该 clear 级联清 by 与 time 全字段</p>
     * 对应测试用例 2.3
     */
    @Test
    public void clearByAndTimeInterface() {

        val entity = new XByAndTime();
        entity.setCreateById(1L);
        entity.setCreateBy("c332030");
        entity.setUpdateById(2L);
        entity.setUpdateBy("cc332030");
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());

        CEntityUtils.clear((Object) entity);

        Assertions.assertNull(entity.getCreateById());
        Assertions.assertNull(entity.getCreateBy());
        Assertions.assertNull(entity.getUpdateById());
        Assertions.assertNull(entity.getUpdateBy());
        Assertions.assertNull(entity.getCreateTime());
        Assertions.assertNull(entity.getUpdateTime());

    }

    /**
     * 仅用于测试的继承子类（无自身 clear 方法）
     */
    private static class XBaseTimeEntity extends CBaseTimeEntity<Long> {
    }

    /**
     * 仅用于测试的父类：实现 ICCreateUpdateTime（有 clear，清 time）
     */
    @Data
    private static class XParent implements ICCreateUpdateTime {

        private Date createTime;
        private Date updateTime;

    }

    /**
     * 仅用于测试的子类：继承 XParent 并实现 ICCreateUpdateBy（有 clear，清 by）
     */
    @Data
    private static class XChild extends XParent implements ICCreateUpdateBy {

        private Long createById;
        private String createBy;
        private Long updateById;
        private String updateBy;

    }

    /**
     * 仅用于测试的类：直接实现 ICCreateUpdateByAndTime（有 clear，清 by 与 time）
     */
    @Data
    private static class XByAndTime implements ICCreateUpdateByAndTime {

        private Long createById;
        private String createBy;
        private Long updateById;
        private String updateBy;
        private Date createTime;
        private Date updateTime;

    }

}
