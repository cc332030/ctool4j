package com.c332030.ctool4j.core.test.jackson;

import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import com.c332030.ctool4j.definition.annotation.CLogBlob;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CLogBlobSerializerModifierTests
 * </p>
 *
 * @since 2025/12/12
 */
public class CLogBlobSerializerModifierTests {

    @Test
    public void blobFieldSerializedToPlaceholder() throws Exception {

        // 日志专用 mapper：@CLogBlob 字段输出 <BLOB> 占位符
        String json = CJacksonUtils.OBJECT_MAPPER_LOG.writeValueAsString(new BlobBean("long-content"));
        Assertions.assertTrue(json.contains("\"content\":\"<BLOB>\""));
        Assertions.assertFalse(json.contains("long-content"));

    }

    @Test
    public void globalMapperOutputsRealContent() throws Exception {

        // 全局 mapper 不带占位符逻辑：@CLogBlob 字段输出真实内容（占位符仅用于日志打印）
        String json = CJacksonUtils.OBJECT_MAPPER.writeValueAsString(new BlobBean("long-content"));
        Assertions.assertTrue(json.contains("long-content"));
        Assertions.assertFalse(json.contains("\"content\":\"<BLOB>\""));

    }

    @Test
    public void logMapperDeepCopyDoesNotAffectOthers() throws Exception {

        // copy() 为深拷贝：日志 mapper 与源/其他 mapper 是不同实例
        Assertions.assertNotSame(CJacksonUtils.OBJECT_MAPPER_LOG, CJacksonUtils.OBJECT_MAPPER_NON_NULL);
        Assertions.assertNotSame(CJacksonUtils.OBJECT_MAPPER_LOG, CJacksonUtils.OBJECT_MAPPER);
        Assertions.assertNotSame(CJacksonUtils.OBJECT_MAPPER_NON_NULL, CJacksonUtils.OBJECT_MAPPER);

        // 对日志 mapper 注册 CLogBlobSerializerModifier 不影响 copy 源 OBJECT_MAPPER_NON_NULL：
        // 源 mapper 仍输出 @CLogBlob 字段真实内容（无 <BLOB> 占位符）
        String nonNullJson = CJacksonUtils.OBJECT_MAPPER_NON_NULL.writeValueAsString(new BlobBean("secret-content"));
        Assertions.assertTrue(nonNullJson.contains("secret-content"));
        Assertions.assertFalse(nonNullJson.contains("<BLOB>"));

        // 默认 mapper 同样不受影响
        String defaultJson = CJacksonUtils.OBJECT_MAPPER.writeValueAsString(new BlobBean("secret-content"));
        Assertions.assertTrue(defaultJson.contains("secret-content"));
        Assertions.assertFalse(defaultJson.contains("<BLOB>"));

        // 日志 mapper 自身行为正常（正例）
        String logJson = CJacksonUtils.OBJECT_MAPPER_LOG.writeValueAsString(new BlobBean("secret-content"));
        Assertions.assertTrue(logJson.contains("\"content\":\"<BLOB>\""));
        Assertions.assertFalse(logJson.contains("secret-content"));

    }

    @Test
    public void normalFieldNotAffected() throws Exception {

        String json = CJacksonUtils.OBJECT_MAPPER_LOG.writeValueAsString(new BlobBean("long-content"));
        Assertions.assertTrue(json.contains("\"name\":\"tom\""));

    }

    @Test
    public void noBlobBeanNormal() throws Exception {

        String json = CJacksonUtils.OBJECT_MAPPER_LOG.writeValueAsString(new PlainBean("raw-data"));
        Assertions.assertTrue(json.contains("\"data\":\"raw-data\""));

    }

    @Test
    public void logMapperSkipsNullField() throws Exception {

        // 日志专用 mapper 默认非 null：@CLogBlob 字段为 null 时不输出
        String json = CJacksonUtils.OBJECT_MAPPER_LOG.writeValueAsString(new BlobBean(null));
        Assertions.assertFalse(json.contains("\"content\""));

    }

    /**
     * 含 @CLogBlob 字段的 Bean
     */
    static class BlobBean {

        private final String name;

        @CLogBlob
        private final String content;

        BlobBean(String content) {
            this.name = "tom";
            this.content = content;
        }

        public String getName() {
            return name;
        }

        public String getContent() {
            return content;
        }

    }

    /**
     * 无 @CLogBlob 的普通 Bean
     */
    static class PlainBean {

        private final String data;

        PlainBean(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }

    }

}
