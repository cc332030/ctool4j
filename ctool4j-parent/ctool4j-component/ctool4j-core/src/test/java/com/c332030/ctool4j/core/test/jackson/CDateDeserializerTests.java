package com.c332030.ctool4j.core.test.jackson;

import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * <p>
 * Description: CDateDeserializerTests
 * </p>
 *
 * @since 2025/12/12
 */
public class CDateDeserializerTests {

    @Test
    public void stringDate() throws Exception {

        DateBean bean = CJacksonUtils.OBJECT_MAPPER.readValue("{\"date\":\"2025-03-03 08:01:03\"}", DateBean.class);
        Assertions.assertNotNull(bean.getDate());

    }

    @Test
    public void millisDate() throws Exception {

        DateBean bean = CJacksonUtils.OBJECT_MAPPER.readValue("{\"date\":\"1731502563000\"}", DateBean.class);
        Assertions.assertEquals(1731502563000L, bean.getDate().getTime());

    }

    @Test
    public void numberIntDate() throws Exception {

        DateBean bean = CJacksonUtils.OBJECT_MAPPER.readValue("{\"date\":1731502563000}", DateBean.class);
        Assertions.assertEquals(1731502563000L, bean.getDate().getTime());

    }

    @Test
    public void emptyString() throws Exception {

        DateBean bean = CJacksonUtils.OBJECT_MAPPER.readValue("{\"date\":\"\"}", DateBean.class);
        Assertions.assertNull(bean.getDate());

    }

    @Test
    public void nullValue() throws Exception {

        DateBean bean = CJacksonUtils.OBJECT_MAPPER.readValue("{\"date\":null}", DateBean.class);
        Assertions.assertNull(bean.getDate());

    }

    @Test
    public void missingField() throws Exception {

        DateBean bean = CJacksonUtils.OBJECT_MAPPER.readValue("{}", DateBean.class);
        Assertions.assertNull(bean.getDate());

    }

    /**
     * Date 反序列化测试 Bean
     */
    static class DateBean {

        private Date date;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

    }

}
