package com.c332030.ctool4j.core.test.jackson;

import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * <p>
 * Description: CDateDeserializerTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「字符串 / 毫秒 / 整型 / 空值边界」多个维度组织。</li>
 *   <li>用 DateBean（date 字段）经 OBJECT_MAPPER 反序列化验证各输入形态。</li>
 *   <li>空值边界覆盖空字符串/null/缺失字段返回 null。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对字符串与整型毫秒反序列化的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：字符串日期；字符串毫秒；整型毫秒；空字符串返回 null；null 返回 null；缺失字段返回 null。</li>
 *   <li>未覆盖：其他 token（如浮点）回退默认实现的分支（未单列）。</li>
 * </ul>
 * <h2>反序列化</h2>
 * <ul>
 *   <li>1.1 字符串日期：{@code "2025-03-03 08:01:03"} 解析非空（stringDate）</li>
 *   <li>1.2 字符串毫秒：{@code "1731502563000"} 转 Date（millisDate）</li>
 *   <li>1.3 整型毫秒：{@code 1731502563000} 转 Date（numberIntDate）</li>
 *   <li>1.4 空字符串：返回 null（emptyString）</li>
 *   <li>1.5 null：返回 null（nullValue）</li>
 *   <li>1.6 缺失字段：返回 null（missingField）</li>
 * </ul>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class CDateDeserializerTests {

    /**
     * 对应测试用例 1.1：字符串日期：{@code "2025-03-03 08:01:03"} 解析非空
     */
    @Test
    public void stringDate() throws Exception {

        DateBean bean = CJacksonUtils.OBJECT_MAPPER.readValue("{\"date\":\"2025-03-03 08:01:03\"}", DateBean.class);
        Assertions.assertNotNull(bean.getDate());

    }

    /**
     * 对应测试用例 1.2：字符串毫秒：{@code "1731502563000"} 转 Date
     */
    @Test
    public void millisDate() throws Exception {

        DateBean bean = CJacksonUtils.OBJECT_MAPPER.readValue("{\"date\":\"1731502563000\"}", DateBean.class);
        Assertions.assertEquals(1731502563000L, bean.getDate().getTime());

    }

    /**
     * 对应测试用例 1.3：整型毫秒：{@code 1731502563000} 转 Date
     */
    @Test
    public void numberIntDate() throws Exception {

        DateBean bean = CJacksonUtils.OBJECT_MAPPER.readValue("{\"date\":1731502563000}", DateBean.class);
        Assertions.assertEquals(1731502563000L, bean.getDate().getTime());

    }

    /**
     * 对应测试用例 1.4：空字符串：返回 null
     */
    @Test
    public void emptyString() throws Exception {

        DateBean bean = CJacksonUtils.OBJECT_MAPPER.readValue("{\"date\":\"\"}", DateBean.class);
        Assertions.assertNull(bean.getDate());

    }

    /**
     * 对应测试用例 1.5：返回 null
     */
    @Test
    public void nullValue() throws Exception {

        DateBean bean = CJacksonUtils.OBJECT_MAPPER.readValue("{\"date\":null}", DateBean.class);
        Assertions.assertNull(bean.getDate());

    }

    /**
     * 对应测试用例 1.6：缺失字段：返回 null
     */
    @Test
    public void missingField() throws Exception {

        DateBean bean = CJacksonUtils.OBJECT_MAPPER.readValue("{}", DateBean.class);
        Assertions.assertNull(bean.getDate());

    }

    /**
     * Date 反序列化测试 Bean
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class DateBean {

        private Date date;

    }

}
