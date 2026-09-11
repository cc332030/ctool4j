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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证从实体/类获取业务ID，覆盖 null 字段、无 @CBizId 注解、指定长度等场景。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 @CBizId 字段查找、String 类型校验、ID 生成与补零的约定。</li>
 *   <li>依据测试方法（等价类/边界）：实体获取、null 字段、无注解、类获取、指定长度。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：从实体/类获取业务ID；null 字段；无 @CBizId 注解；指定长度补零。</li>
 *   <li>未覆盖：非 String 类型 @CBizId 字段（校验抛异常）场景。</li>
 * </ul>
 * <h2>业务ID获取</h2>
 * <ul>
 *   <li>1.1 从实体获取（{@code getBizIdFromEntity}）</li>
 *   <li>1.2 实体字段为 null（{@code getBizIdFromEntityNullField}）</li>
 *   <li>1.3 实体无 @CBizId 注解（{@code getBizIdFromEntityNoAnnotation}）</li>
 *   <li>1.4 从类获取（{@code getBizIdFromClass}）</li>
 *   <li>1.5 从类获取并指定长度（{@code getBizIdFromClassWithLength}）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
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
         * 对应测试用例 1.1：从实体获取（{@code getBizIdFromEntity}）
         */
    @Test
    public void getBizIdFromEntity() {
        BizEntity entity = new BizEntity();
        entity.setBizId("BIZ-001");
        Assertions.assertEquals("BIZ-001", CBizIdUtils.getBizId(entity));
    }

        /**
         * 对应测试用例 1.2：实体字段为 null（{@code getBizIdFromEntityNullField}）
         */
    @Test
    public void getBizIdFromEntityNullField() {
        // @CBizId 字段为 null 时返回 null
        BizEntity entity = new BizEntity();
        Assertions.assertNull(CBizIdUtils.getBizId(entity));
    }

        /**
         * 对应测试用例 1.3：实体无 @CBizId 注解（{@code getBizIdFromEntityNoAnnotation}）
         */
    @Test
    public void getBizIdFromEntityNoAnnotation() {
        // 实体无 @CBizId 字段时返回 null
        NoBizEntity entity = new NoBizEntity();
        Assertions.assertNull(CBizIdUtils.getBizId(entity));
    }

        /**
         * 对应测试用例 1.4：从类获取（{@code getBizIdFromClass}）
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
         * 对应测试用例 1.5：从类获取并指定长度（{@code getBizIdFromClassWithLength}）
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
