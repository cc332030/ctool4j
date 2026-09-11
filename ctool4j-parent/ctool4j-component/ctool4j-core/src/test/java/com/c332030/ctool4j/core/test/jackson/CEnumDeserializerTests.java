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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「枚举反序列化 / 空白边界 / 未知枚举异常 / 实例」多个维度组织。</li>
 *   <li>用 EnumBean（CDbOperateEnum.operate）验证按名反序列化、trim、空白/null/空返回 null、未知抛异常。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对按名反序列化、空白返回 null、未知抛异常的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：按名反序列化；trim 容忍空格；空白/null/空返回 null；未知名抛 JsonMappingException；getEnumClass；</li>
 *   <li>EMPTY_INSTANCE 非空。</li>
 *   <li>未覆盖：createContextual 非枚举字段校验分支（未单列）。</li>
 * </ul>
 * <h2>枚举反序列化</h2>
 * <ul>
 *   <li>1.1 按名：{@code "INSERT"} → INSERT（deserializeEnum）</li>
 *   <li>1.2 trim：{@code " INSERT "} → INSERT（deserializeTrim）</li>
 *   <li>1.3 空白：{@code "  "} → null（deserializeBlank）</li>
 *   <li>1.4 空字符串：{@code ""} → null（deserializeEmptyString）</li>
 *   <li>1.5 未知枚举：{@code "XXX"} 抛 JsonMappingException（deserializeUnknown_throws）</li>
 *   <li>1.6 getEnumClass：返回绑定枚举类型（getEnumClass）</li>
 *   <li>1.7 EMPTY_INSTANCE 非空（emptyInstanceNotNull）</li>
 * </ul>
 *
 * <p>被测依赖类（异常 / 序列化器 / 日志 / 服务 / 切面 / 拦截器等）无 builder，测试按常规直接 new 构造——属规范允许的取舍，依据与边界在此记录。</p>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class CEnumDeserializerTests {

    /**
     * 对应测试用例 1.1：按名：{@code "INSERT"} → INSERT
     */
    @Test
    public void deserializeEnum() throws Exception {

        EnumBean bean = CJacksonUtils.OBJECT_MAPPER.readValue(
            "{\"operate\":\"INSERT\"}", EnumBean.class);
        Assertions.assertEquals(CDbOperateEnum.INSERT, bean.getOperate());

    }

    /**
     * 对应测试用例 1.2：{@code " INSERT "} → INSERT
     */
    @Test
    public void deserializeTrim() throws Exception {

        EnumBean bean = CJacksonUtils.OBJECT_MAPPER.readValue(
            "{\"operate\":\" INSERT \"}", EnumBean.class);
        Assertions.assertEquals(CDbOperateEnum.INSERT, bean.getOperate());

    }

    /**
     * 对应测试用例 1.3：空白：{@code "  "} → null
     */
    @Test
    public void deserializeBlank() throws Exception {

        EnumBean bean = CJacksonUtils.OBJECT_MAPPER.readValue(
            "{\"operate\":\"  \"}", EnumBean.class);
        Assertions.assertNull(bean.getOperate());

    }

    /**
     * 对应测试用例 1.4：空字符串：{@code ""} → null
     */
    @Test
    public void deserializeEmptyString() throws Exception {

        EnumBean bean = CJacksonUtils.OBJECT_MAPPER.readValue(
            "{\"operate\":\"\"}", EnumBean.class);
        Assertions.assertNull(bean.getOperate());

    }

    /**
     * 对应测试用例 1.5：未知枚举：{@code "XXX"} 抛 JsonMappingException
     */
    @Test
    public void deserializeUnknown_throws() {

        // CEnumUtils 抛 IllegalArgumentException，Jackson 反序列化框架包装为 JsonMappingException 抛出
        Assertions.assertThrowsExactly(JsonMappingException.class,
            () -> CJacksonUtils.OBJECT_MAPPER.readValue("{\"operate\":\"XXX\"}", EnumBean.class));

    }

    /**
     * 对应测试用例 1.6：返回绑定枚举类型
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getEnumClass() {

        CEnumDeserializer d = new CEnumDeserializer((Class<Enum<?>>) (Class<?>) CDbOperateEnum.class);
        Assertions.assertEquals(CDbOperateEnum.class, d.getEnumClass());

    }

    /**
     * 对应测试用例 1.7：EMPTY_INSTANCE 非空
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
