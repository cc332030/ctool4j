package com.c332030.ctool4j.mybatis.test.util;

import com.c332030.ctool4j.definition.annotation.CBizId;
import com.c332030.ctool4j.mybatis.util.CBizIdUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CBizIdUtilsTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CBizIdUtilsTests {

    /**
     * 带 @CBizId 字段的实体
     */
    public static class BizEntity {

        @CBizId
        private String bizId;

        private String other;

        public String getBizId() {
            return bizId;
        }

        public void setBizId(String bizId) {
            this.bizId = bizId;
        }

        public String getOther() {
            return other;
        }
    }

    /**
     * 无 @CBizId 字段的实体
     */
    public static class NoBizEntity {

        private String id;

        public String getId() {
            return id;
        }
    }

    @Test
    public void getBizIdFromEntity() {
        BizEntity entity = new BizEntity();
        entity.setBizId("BIZ-001");
        Assertions.assertEquals("BIZ-001", CBizIdUtils.getBizId(entity));
    }

    @Test
    public void getBizIdFromEntityNullField() {
        // @CBizId 字段为 null 时返回 null
        BizEntity entity = new BizEntity();
        Assertions.assertNull(CBizIdUtils.getBizId(entity));
    }

    @Test
    public void getBizIdFromEntityNoAnnotation() {
        // 实体无 @CBizId 字段时返回 null
        NoBizEntity entity = new NoBizEntity();
        Assertions.assertNull(CBizIdUtils.getBizId(entity));
    }

    @Test
    public void getBizIdFromClass() {
        // 根据类生成业务 ID，前缀取自类名大写字母 + 雪花 ID
        String bizId = CBizIdUtils.getBizId(BizEntity.class);
        Assertions.assertNotNull(bizId);
        Assertions.assertFalse(bizId.isEmpty());
        Assertions.assertTrue(bizId.startsWith("BE"));
    }

    @Test
    public void getBizIdFromClassWithLength() {
        // 指定前缀长度
        String bizId = CBizIdUtils.getBizId(BizEntity.class, 1);
        Assertions.assertNotNull(bizId);
        Assertions.assertFalse(bizId.isEmpty());
        Assertions.assertTrue(bizId.startsWith("B"));
    }

}
