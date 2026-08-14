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

        String json = CJacksonUtils.OBJECT_MAPPER.writeValueAsString(new BlobBean("long-content"));
        Assertions.assertTrue(json.contains("\"content\":\"<BLOB>\""));
        Assertions.assertFalse(json.contains("long-content"));

    }

    @Test
    public void normalFieldNotAffected() throws Exception {

        String json = CJacksonUtils.OBJECT_MAPPER.writeValueAsString(new BlobBean("long-content"));
        Assertions.assertTrue(json.contains("\"name\":\"tom\""));

    }

    @Test
    public void noBlobBeanNormal() throws Exception {

        String json = CJacksonUtils.OBJECT_MAPPER.writeValueAsString(new PlainBean("raw-data"));
        Assertions.assertTrue(json.contains("\"data\":\"raw-data\""));

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
