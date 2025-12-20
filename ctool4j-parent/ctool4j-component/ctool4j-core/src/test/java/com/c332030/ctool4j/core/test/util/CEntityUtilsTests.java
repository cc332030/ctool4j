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

    @Test
    public void clearNone() {

        CEntityUtils.clear(new Object());

    }

}
