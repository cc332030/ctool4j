package com.c332030.ctool4j.core.test.jackson;

import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import com.c332030.ctool4j.definition.annotation.CLogBlob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CLogBlobSerializerModifierTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「日志 mapper 占位 / 全局 mapper 真实 / 深拷贝隔离 / 普通字段 / 无注解 / null 跳过」多个维度组织。</li>
 *   <li>用 BlobBean（name + @CLogBlob content）与 PlainBean 验证各场景。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对仅日志 mapper 生效、占位符输出的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：日志 mapper 输出 {@code &lt;BLOB&gt;}；全局 mapper 输出真实内容；深拷贝不影响其他 mapper；普通字段不受影响；</li>
 *   <li>无注解 bean 正常；@CLogBlob null 字段日志 mapper 不输出。</li>
 *   <li>未覆盖：无（覆盖了核心行为）。</li>
 * </ul>
 * <h2>占位符替换</h2>
 * <ul>
 *   <li>1.1 日志 mapper：@CLogBlob 字段输出 {@code &lt;BLOB&gt;}（blobFieldSerializedToPlaceholder）</li>
 *   <li>1.2 全局 mapper：@CLogBlob 字段输出真实内容（globalMapperOutputsRealContent）</li>
 *   <li>1.3 深拷贝隔离：日志 mapper 注册不影响其他 mapper（logMapperDeepCopyDoesNotAffectOthers）</li>
 *   <li>1.4 普通字段：name 正常输出（normalFieldNotAffected）</li>
 *   <li>1.5 无注解 bean：正常输出（noBlobBeanNormal）</li>
 *   <li>1.6 null 字段：日志 mapper 不输出（logMapperSkipsNullField）</li>
 * </ul>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class CLogBlobSerializerModifierTests {

    /**
     * 对应测试用例 1.1：日志 mapper：@CLogBlob 字段输出 {@code &lt;BLOB&gt;}
     */
    @Test
    public void blobFieldSerializedToPlaceholder() throws Exception {

        // 日志专用 mapper：@CLogBlob 字段输出 <BLOB> 占位符
        String json = CJacksonUtils.OBJECT_MAPPER_LOG.writeValueAsString(new BlobBean("tom", "long-content"));
        Assertions.assertTrue(json.contains("\"content\":\"<BLOB>\""));
        Assertions.assertFalse(json.contains("long-content"));

    }

    /**
     * 对应测试用例 1.2：全局 mapper：@CLogBlob 字段输出真实内容
     */
    @Test
    public void globalMapperOutputsRealContent() throws Exception {

        // 全局 mapper 不带占位符逻辑：@CLogBlob 字段输出真实内容（占位符仅用于日志打印）
        String json = CJacksonUtils.OBJECT_MAPPER.writeValueAsString(new BlobBean("tom", "long-content"));
        Assertions.assertTrue(json.contains("long-content"));
        Assertions.assertFalse(json.contains("\"content\":\"<BLOB>\""));

    }

    /**
     * 对应测试用例 1.3：深拷贝隔离：日志 mapper 注册不影响其他 mapper
     */
    @Test
    public void logMapperDeepCopyDoesNotAffectOthers() throws Exception {

        // copy() 为深拷贝：日志 mapper 与源/其他 mapper 是不同实例
        Assertions.assertNotSame(CJacksonUtils.OBJECT_MAPPER_LOG, CJacksonUtils.OBJECT_MAPPER_NON_NULL);
        Assertions.assertNotSame(CJacksonUtils.OBJECT_MAPPER_LOG, CJacksonUtils.OBJECT_MAPPER);
        Assertions.assertNotSame(CJacksonUtils.OBJECT_MAPPER_NON_NULL, CJacksonUtils.OBJECT_MAPPER);

        // 对日志 mapper 注册 CLogBlobSerializerModifier 不影响 copy 源 OBJECT_MAPPER_NON_NULL：
        // 源 mapper 仍输出 @CLogBlob 字段真实内容（无 <BLOB> 占位符）
        String nonNullJson = CJacksonUtils.OBJECT_MAPPER_NON_NULL.writeValueAsString(new BlobBean("tom", "secret-content"));
        Assertions.assertTrue(nonNullJson.contains("secret-content"));
        Assertions.assertFalse(nonNullJson.contains("<BLOB>"));

        // 默认 mapper 同样不受影响
        String defaultJson = CJacksonUtils.OBJECT_MAPPER.writeValueAsString(new BlobBean("tom", "secret-content"));
        Assertions.assertTrue(defaultJson.contains("secret-content"));
        Assertions.assertFalse(defaultJson.contains("<BLOB>"));

        // 日志 mapper 自身行为正常（正例）
        String logJson = CJacksonUtils.OBJECT_MAPPER_LOG.writeValueAsString(new BlobBean("tom", "secret-content"));
        Assertions.assertTrue(logJson.contains("\"content\":\"<BLOB>\""));
        Assertions.assertFalse(logJson.contains("secret-content"));

    }

    /**
     * 对应测试用例 1.4：普通字段：name 正常输出
     */
    @Test
    public void normalFieldNotAffected() throws Exception {

        String json = CJacksonUtils.OBJECT_MAPPER_LOG.writeValueAsString(new BlobBean("tom", "long-content"));
        Assertions.assertTrue(json.contains("\"name\":\"tom\""));

    }

    /**
     * 对应测试用例 1.5：无注解 bean：正常输出
     */
    @Test
    public void noBlobBeanNormal() throws Exception {

        String json = CJacksonUtils.OBJECT_MAPPER_LOG.writeValueAsString(new PlainBean("raw-data"));
        Assertions.assertTrue(json.contains("\"data\":\"raw-data\""));

    }

    /**
     * 对应测试用例 1.6：null 字段：日志 mapper 不输出
     */
    @Test
    public void logMapperSkipsNullField() throws Exception {

        // 日志专用 mapper 默认非 null：@CLogBlob 字段为 null 时不输出
        String json = CJacksonUtils.OBJECT_MAPPER_LOG.writeValueAsString(new BlobBean("tom", null));
        Assertions.assertFalse(json.contains("\"content\""));

    }

    /**
     * 含 @CLogBlob 字段的 Bean
     */
    @Getter
    @AllArgsConstructor
    static class BlobBean {

        private final String name;

        @CLogBlob
        private final String content;

    }

    /**
     * 无 @CLogBlob 的普通 Bean
     */
    @Getter
    @RequiredArgsConstructor
    static class PlainBean {

        private final String data;

    }

}
