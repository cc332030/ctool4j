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
