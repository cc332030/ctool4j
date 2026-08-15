package com.c332030.ctool4j.core.test.jackson;

import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import com.c332030.ctool4j.core.jackson.deserializer.CEnumDeserializer;
import com.c332030.ctool4j.definition.enums.CDbOperateEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CEnumDeserializerTests
 * </p>
 *
 * @since 2025/12/12
 */
public class CEnumDeserializerTests {

    @Test
    public void deserializeEnum() throws Exception {

        EnumBean bean = CJacksonUtils.OBJECT_MAPPER.readValue(
            "{\"operate\":\"INSERT\"}", EnumBean.class);
        Assertions.assertEquals(CDbOperateEnum.INSERT, bean.getOperate());

    }

    @Test
    public void deserializeTrim() throws Exception {

        EnumBean bean = CJacksonUtils.OBJECT_MAPPER.readValue(
            "{\"operate\":\" INSERT \"}", EnumBean.class);
        Assertions.assertEquals(CDbOperateEnum.INSERT, bean.getOperate());

    }

    @Test
    public void deserializeBlank() throws Exception {

        EnumBean bean = CJacksonUtils.OBJECT_MAPPER.readValue(
            "{\"operate\":\"  \"}", EnumBean.class);
        Assertions.assertNull(bean.getOperate());

    }

    @Test
    public void deserializeEmptyString() throws Exception {

        EnumBean bean = CJacksonUtils.OBJECT_MAPPER.readValue(
            "{\"operate\":\"\"}", EnumBean.class);
        Assertions.assertNull(bean.getOperate());

    }

    @Test
    public void deserializeUnknown_throws() {

        Assertions.assertThrows(com.fasterxml.jackson.databind.JsonMappingException.class,
            () -> CJacksonUtils.OBJECT_MAPPER.readValue("{\"operate\":\"XXX\"}", EnumBean.class));

    }

    @Test
    @SuppressWarnings("unchecked")
    public void getEnumClass() {

        CEnumDeserializer d = new CEnumDeserializer((Class<Enum<?>>) (Class<?>) CDbOperateEnum.class);
        Assertions.assertEquals(CDbOperateEnum.class, d.getEnumClass());

    }

    @Test
    public void emptyInstanceNotNull() {

        Assertions.assertNotNull(CEnumDeserializer.EMPTY_INSTANCE);

    }

    /**
     * 枚举反序列化测试 Bean
     */
    static class EnumBean {

        private CDbOperateEnum operate;

        public CDbOperateEnum getOperate() {
            return operate;
        }

        public void setOperate(CDbOperateEnum operate) {
            this.operate = operate;
        }

    }

}
