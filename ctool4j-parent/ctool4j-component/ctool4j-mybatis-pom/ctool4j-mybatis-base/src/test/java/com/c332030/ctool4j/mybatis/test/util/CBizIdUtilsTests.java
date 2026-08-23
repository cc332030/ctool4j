package com.c332030.ctool4j.mybatis.test.util;

import com.c332030.ctool4j.definition.annotation.CBizId;
import com.c332030.ctool4j.mybatis.util.CBizIdUtils;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    @Data
    @NoArgsConstructor
    public static class BizEntity {

        @CBizId
        private String bizId;

        private String other;

    }

    /**
     * 无 @CBizId 字段的实体
     */
    @Getter
    @NoArgsConstructor
    public static class NoBizEntity {

        private String id;

    }

        /**
     * 对应测试用例 1.1
     */
    @Test
    public void getBizIdFromEntity() {
        BizEntity entity = new BizEntity();
        entity.setBizId("BIZ-001");
        Assertions.assertEquals("BIZ-001", CBizIdUtils.getBizId(entity));
    }

        /**
     * 对应测试用例 1.2
     */
    @Test
    public void getBizIdFromEntityNullField() {
        // @CBizId 字段为 null 时返回 null
        BizEntity entity = new BizEntity();
        Assertions.assertNull(CBizIdUtils.getBizId(entity));
    }

        /**
     * 对应测试用例 1.3
     */
    @Test
    public void getBizIdFromEntityNoAnnotation() {
        // 实体无 @CBizId 字段时返回 null
        NoBizEntity entity = new NoBizEntity();
        Assertions.assertNull(CBizIdUtils.getBizId(entity));
    }

        /**
     * 对应测试用例 1.4
     */
    @Test
    public void getBizIdFromClass() {
        // 根据类生成业务 ID，前缀取自类名大写字母 + 雪花 ID
        String bizId = CBizIdUtils.getBizId(BizEntity.class);
        Assertions.assertNotNull(bizId);
        Assertions.assertFalse(bizId.isEmpty());
        Assertions.assertTrue(bizId.startsWith("BE"));
    }

        /**
     * 对应测试用例 1.5
     */
    @Test
    public void getBizIdFromClassWithLength() {
        // 指定前缀长度
        String bizId = CBizIdUtils.getBizId(BizEntity.class, 1);
        Assertions.assertNotNull(bizId);
        Assertions.assertFalse(bizId.isEmpty());
        Assertions.assertTrue(bizId.startsWith("B"));
    }

}
