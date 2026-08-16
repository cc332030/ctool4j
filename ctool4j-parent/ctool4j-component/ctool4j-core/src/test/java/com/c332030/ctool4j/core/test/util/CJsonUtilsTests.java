package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CJsonUtils;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.core.util.CMapUtils;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Description: CJsonUtilsTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CJsonUtilsTests {

    /**
     * 测试用 Bean，字段名用于验证驼峰/下划线互转
     */
    public static class TestBean {

        private Long id;

        private String userName;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

    }

    private static TestBean newBean(Long id, String userName) {

        val bean = new TestBean();
        bean.setId(id);
        bean.setUserName(userName);
        return bean;

    }

    @Test
    public void toJson() {

        // null 返回 null
        Assertions.assertNull(CJsonUtils.toJson(null));

        Assertions.assertEquals("{\"a\":1}", CJsonUtils.toJson(CMap.of("a", 1)));

        // Long 序列化为 String，避免前端溢出（易错点）
        Assertions.assertEquals("{\"id\":\"1\"}", CJsonUtils.toJson(CMap.of("id", 1L)));

        val json = CJsonUtils.toJson(newBean(1L, "a"));
        Assertions.assertEquals("{\"id\":\"1\",\"userName\":\"a\"}", json);

    }

    @Test
    public void toJsonSnakeCase() {

        // 驼峰转下划线
        val json = CJsonUtils.toJsonSnakeCase(newBean(1L, "a"));
        Assertions.assertEquals("{\"id\":\"1\",\"user_name\":\"a\"}", json);

    }

    @Test
    public void toJsonNonNull() {

        // null 字段不输出
        val json = CJsonUtils.toJsonNonNull(newBean(null, "a"));
        Assertions.assertEquals("{\"userName\":\"a\"}", json);

        Assertions.assertNull(CJsonUtils.toJsonNonNull(null));

    }

    @Test
    public void fromJson() {

        // null / 空 / 空白 返回 null
        Assertions.assertNull(CJsonUtils.fromJson(null, TestBean.class));
        Assertions.assertNull(CJsonUtils.fromJson("", TestBean.class));
        Assertions.assertNull(CJsonUtils.fromJson(" ", TestBean.class));

        // 非法 json 抛异常（未知字段/JSON5 宽松语法不抛，此处用结构不匹配的输入）
        Assertions.assertThrowsExactly(MismatchedInputException.class,
                () -> CJsonUtils.fromJson("[1,2]", TestBean.class));
        // 未闭合 JSON 抛 JsonEOFException（JsonParseException 子类，需用精确类型断言）
        Assertions.assertThrowsExactly(JsonEOFException.class,
                () -> CJsonUtils.fromJson("{\"id\":\"1\"", TestBean.class));

        // 合法 json 反序列化
        val bean = CJsonUtils.fromJson("{\"id\":\"1\",\"userName\":\"a\"}", TestBean.class);
        Assertions.assertEquals(1L, bean.getId());
        Assertions.assertEquals("a", bean.getUserName());

    }

    @Test
    public void fromJsonTypeReference() {

        // List[Map[String, Object]]
        List<Map<String, Object>> list = CJsonUtils.fromJson(
                "[{\"a\":1},{\"a\":2}]", CMapUtils.LIST_MAP_STRING_OBJECT_TYPE_REFERENCE);
        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals(1, list.get(0).get("a"));

        // Map[String, Object]
        Map<String, Object> map = CJsonUtils.fromJson(
                "{\"a\":1}", CMapUtils.MAP_STRING_OBJECT_TYPE_REFERENCE);
        Assertions.assertEquals(1, map.get("a"));

        // Map[String, String]
        Map<String, String> stringMap = CJsonUtils.fromJson(
                "{\"a\":\"1\"}", CMapUtils.MAP_STRING_STRING_TYPE_REFERENCE);
        Assertions.assertEquals("1", stringMap.get("a"));

    }

    @Test
    public void fromJsonSnakeCase() {

        val bean = CJsonUtils.fromJsonSnakeCase("{\"user_name\":\"a\"}", TestBean.class);
        Assertions.assertEquals("a", bean.getUserName());

        List<Map<String, Object>> list = CJsonUtils.fromJsonSnakeCase(
                "[{\"user_name\":\"a\"}]", CMapUtils.LIST_MAP_STRING_OBJECT_TYPE_REFERENCE);
        Assertions.assertEquals("a", list.get(0).get("user_name"));

    }

    @Test
    public void convert() {

        // 对象转换（bean -> map）
        Map<String, Object> map = CJsonUtils.convert(
                newBean(1L, "a"), CMapUtils.MAP_STRING_OBJECT_TYPE_REFERENCE);
        Assertions.assertEquals("a", map.get("userName"));

        // 对象转换（bean -> bean）
        TestBean bean = CJsonUtils.convert(newBean(1L, "a"), TestBean.class);
        Assertions.assertEquals(1L, bean.getId());
        Assertions.assertEquals("a", bean.getUserName());

    }

    @Test
    public void convertSnakeCase() {

        Map<String, Object> map = CJsonUtils.convertSnakeCase(
                newBean(1L, "a"), CMapUtils.MAP_STRING_OBJECT_TYPE_REFERENCE);
        Assertions.assertEquals("a", map.get("user_name"));

    }

    @Test
    public void toMap() {

        val map = CJsonUtils.toMap(newBean(1L, "a"));
        Assertions.assertEquals("a", map.get("userName"));
        // Q11 修复：Long 保留数值类型，不再经 JSON 中转变 String
        Assertions.assertEquals(1L, map.get("id"));

        // null 对象返回 null
        Assertions.assertNull(CJsonUtils.toMap(null));

    }

    @Test
    public void toMapSnakeCase() {

        val map = CJsonUtils.toMapSnakeCase(newBean(1L, "a"));
        Assertions.assertEquals("a", map.get("user_name"));

    }

    @Test
    public void fromJsonList() {

        val list = CJsonUtils.fromJsonList("[{\"a\":1}]");
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals(1, list.get(0).get("a"));

    }

    @Test
    public void fromJsonStringValue() {

        val map = CJsonUtils.fromJsonStringValue("{\"a\":\"1\"}");
        Assertions.assertEquals("1", map.get("a"));

    }

    @Test
    public void toMapStringValue() {

        val map = CJsonUtils.toMapStringValue(newBean(1L, "a"));
        Assertions.assertEquals("1", map.get("id"));
        Assertions.assertEquals("a", map.get("userName"));

    }

    @Test
    public void toMapStringValueSnakeCase() {

        val map = CJsonUtils.toMapStringValueSnakeCase(newBean(1L, "a"));
        Assertions.assertEquals("1", map.get("id"));
        Assertions.assertEquals("a", map.get("user_name"));

    }

}
