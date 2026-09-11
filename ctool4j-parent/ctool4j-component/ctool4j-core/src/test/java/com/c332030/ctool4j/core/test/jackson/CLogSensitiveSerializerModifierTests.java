package com.c332030.ctool4j.core.test.jackson;

import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import com.c332030.ctool4j.definition.annotation.CLogSensitive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CLogSensitiveSerializerModifierTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「日志 mapper 脱敏 / 全局 mapper 真实 / 自定义保留 / 普通字段 / null 跳过 / 与 BLOB 共存 / 深拷贝隔离」多个维度组织。</li>
 *   <li>用 SensitiveBean（name + @CLogSensitive phone）、CustomKeepBean、BlobSensitiveBean 验证各场景。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对仅日志 mapper 脱敏、自定义保留位数的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：日志 mapper 脱敏；全局 mapper 真实；自定义保留位数；普通字段不受影响；@CLogSensitive null 不输出；</li>
 *   <li>@CLogBlob 与 @CLogSensitive 共存；深拷贝隔离。</li>
 *   <li>未覆盖：无（覆盖了核心行为）。</li>
 * </ul>
 * <h2>脱敏替换</h2>
 * <ul>
 *   <li>1.1 日志 mapper：@CLogSensitive 字段脱敏（sensitiveFieldMaskedInLogMapper）</li>
 *   <li>1.2 全局 mapper：@CLogSensitive 字段输出真实内容（globalMapperOutputsRealContent）</li>
 *   <li>1.3 自定义保留：前 1 后 2（customKeepApplied）</li>
 *   <li>1.4 普通字段：name 正常输出（nonSensitiveFieldNotAffected）</li>
 *   <li>1.5 null 字段：日志 mapper 不输出（logMapperSkipsNullSensitiveField）</li>
 *   <li>1.6 与 BLOB 共存：占位符与脱敏互不影响（blobAndSensitiveCoexist）</li>
 *   <li>1.7 深拷贝隔离：不影响其他 mapper（logMapperDeepCopyDoesNotAffectOthers）</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.0
 */
public class CLogSensitiveSerializerModifierTests {

    /**
     * 对应测试用例 1.1：日志 mapper：@CLogSensitive 字段脱敏
     */
    @Test
    public void sensitiveFieldMaskedInLogMapper() throws Exception {

        // 日志专用 mapper：@CLogSensitive 字段脱敏输出（默认保留前三后四）
        String json = CJacksonUtils.OBJECT_MAPPER_LOG.writeValueAsString(new SensitiveBean("tom", "13812345678"));
        Assertions.assertTrue(json.contains("\"phone\":\"138****5678\""));
        Assertions.assertFalse(json.contains("13812345678"));

    }

    /**
     * 对应测试用例 1.2：全局 mapper：@CLogSensitive 字段输出真实内容
     */
    @Test
    public void globalMapperOutputsRealContent() throws Exception {

        // 全局 mapper 不带脱敏逻辑：@CLogSensitive 字段输出真实内容（脱敏仅用于日志打印）
        String json = CJacksonUtils.OBJECT_MAPPER.writeValueAsString(new SensitiveBean("tom", "13812345678"));
        Assertions.assertTrue(json.contains("13812345678"));
        Assertions.assertFalse(json.contains("138****5678"));

    }

    /**
     * 对应测试用例 1.3：自定义保留：前 1 后 2
     */
    @Test
    public void customKeepApplied() throws Exception {

        // 自定义保留位数：前 1 后 2
        String json = CJacksonUtils.OBJECT_MAPPER_LOG.writeValueAsString(new CustomKeepBean("1234567"));
        Assertions.assertTrue(json.contains("\"code\":\"1****67\""));
        Assertions.assertFalse(json.contains("1234567"));

    }

    /**
     * 对应测试用例 1.4：普通字段：name 正常输出
     */
    @Test
    public void nonSensitiveFieldNotAffected() throws Exception {

        String json = CJacksonUtils.OBJECT_MAPPER_LOG.writeValueAsString(new SensitiveBean("tom", "13812345678"));
        Assertions.assertTrue(json.contains("\"name\":\"tom\""));

    }

    /**
     * 对应测试用例 1.5：null 字段：日志 mapper 不输出
     */
    @Test
    public void logMapperSkipsNullSensitiveField() throws Exception {

        // 日志专用 mapper 默认不序列化 null：@CLogSensitive 字段为 null 时不输出
        String json = CJacksonUtils.OBJECT_MAPPER_LOG.writeValueAsString(new SensitiveBean("tom", null));
        Assertions.assertFalse(json.contains("\"phone\""));

    }

    /**
     * 对应测试用例 1.6：与 BLOB 共存：占位符与脱敏互不影响
     */
    @Test
    public void blobAndSensitiveCoexist() throws Exception {

        // @CLogBlob 输出占位符、@CLogSensitive 脱敏，同一 bean 两者互不影响
        String json = CJacksonUtils.OBJECT_MAPPER_LOG.writeValueAsString(new BlobSensitiveBean("long-content", "13812345678"));
        Assertions.assertTrue(json.contains("\"content\":\"<BLOB>\""));
        Assertions.assertTrue(json.contains("\"phone\":\"138****5678\""));
        Assertions.assertFalse(json.contains("long-content"));
        Assertions.assertFalse(json.contains("13812345678"));

    }

    /**
     * 对应测试用例 1.7：深拷贝隔离：不影响其他 mapper
     */
    @Test
    public void logMapperDeepCopyDoesNotAffectOthers() throws Exception {

        // 日志 mapper 与源/其他 mapper 是不同实例
        Assertions.assertNotSame(CJacksonUtils.OBJECT_MAPPER_LOG, CJacksonUtils.OBJECT_MAPPER_NON_NULL);
        Assertions.assertNotSame(CJacksonUtils.OBJECT_MAPPER_LOG, CJacksonUtils.OBJECT_MAPPER);

        // 源 mapper 仍输出 @CLogSensitive 字段真实内容（无脱敏）
        String nonNullJson = CJacksonUtils.OBJECT_MAPPER_NON_NULL.writeValueAsString(new SensitiveBean("tom", "13812345678"));
        Assertions.assertTrue(nonNullJson.contains("13812345678"));
        Assertions.assertFalse(nonNullJson.contains("138****5678"));

        // 日志 mapper 自身行为正常（正例）
        String logJson = CJacksonUtils.OBJECT_MAPPER_LOG.writeValueAsString(new SensitiveBean("tom", "13812345678"));
        Assertions.assertTrue(logJson.contains("\"phone\":\"138****5678\""));

    }

    /**
     * 含 @CLogSensitive 字段的 Bean
     */
    @Getter
    @AllArgsConstructor
    static class SensitiveBean {

        private final String name;

        @CLogSensitive
        private final String phone;

    }

    /**
     * 自定义保留位数的 Bean
     */
    @Getter
    @RequiredArgsConstructor
    static class CustomKeepBean {

        @CLogSensitive(prefixKeep = 1, suffixKeep = 2)
        private final String code;

    }

    /**
     * 同时含 @CLogBlob 与 @CLogSensitive 字段的 Bean
     */
    @Getter
    @RequiredArgsConstructor
    static class BlobSensitiveBean {

        @com.c332030.ctool4j.definition.annotation.CLogBlob
        private final String content;

        @CLogSensitive
        private final String phone;

    }

}
