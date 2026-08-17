package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.classes.CBeanUtils;
import com.c332030.ctool4j.core.util.CEntityUtils;
import com.c332030.ctool4j.definition.entity.base.CBaseEntity;
import com.c332030.ctool4j.definition.entity.base.CBaseTimeEntity;
import com.c332030.ctool4j.definition.entity.base.CLongId;
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
     */
    @Test
    public void clearNone() {

        CEntityUtils.clear(new Object());

    }

    /**
     * 测试无自身 clear 的子类，通过遍历父类/父接口链命中最近的清除方法
     * <p>验证按继承距离查找"最近" clear，而非依赖方法声明顺序</p>
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
     * 仅用于测试的继承子类（无自身 clear 方法）
     */
    private static class XBaseTimeEntity extends CBaseTimeEntity<Long> {
    }

}
