package com.c332030.ctool4j.core.test.jackson;

import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import com.c332030.ctool4j.core.jackson.deserializer.CEnumDeserializer;
import com.c332030.ctool4j.definition.enums.CDbOperateEnum;
import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void deserializeEnum() throws Exception {

        EnumBean bean = CJacksonUtils.OBJECT_MAPPER.readValue(
            "{\"operate\":\"INSERT\"}", EnumBean.class);
        Assertions.assertEquals(CDbOperateEnum.INSERT, bean.getOperate());

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void deserializeTrim() throws Exception {

        EnumBean bean = CJacksonUtils.OBJECT_MAPPER.readValue(
            "{\"operate\":\" INSERT \"}", EnumBean.class);
        Assertions.assertEquals(CDbOperateEnum.INSERT, bean.getOperate());

    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void deserializeBlank() throws Exception {

        EnumBean bean = CJacksonUtils.OBJECT_MAPPER.readValue(
            "{\"operate\":\"  \"}", EnumBean.class);
        Assertions.assertNull(bean.getOperate());

    }

    /**
     * 对应测试用例 1.4
     */
    @Test
    public void deserializeEmptyString() throws Exception {

        EnumBean bean = CJacksonUtils.OBJECT_MAPPER.readValue(
            "{\"operate\":\"\"}", EnumBean.class);
        Assertions.assertNull(bean.getOperate());

    }

    /**
     * 对应测试用例 1.5
     */
    @Test
    public void deserializeUnknown_throws() {

        // CEnumUtils 抛 IllegalArgumentException，Jackson 反序列化框架包装为 JsonMappingException 抛出
        Assertions.assertThrowsExactly(JsonMappingException.class,
            () -> CJacksonUtils.OBJECT_MAPPER.readValue("{\"operate\":\"XXX\"}", EnumBean.class));

    }

    /**
     * 对应测试用例 1.6
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getEnumClass() {

        CEnumDeserializer d = new CEnumDeserializer((Class<Enum<?>>) (Class<?>) CDbOperateEnum.class);
        Assertions.assertEquals(CDbOperateEnum.class, d.getEnumClass());

    }

    /**
     * 对应测试用例 1.7
     */
    @Test
    public void emptyInstanceNotNull() {

        Assertions.assertNotNull(CEnumDeserializer.EMPTY_INSTANCE);

    }

    /**
     * 枚举反序列化测试 Bean
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class EnumBean {

        private CDbOperateEnum operate;

    }

}
