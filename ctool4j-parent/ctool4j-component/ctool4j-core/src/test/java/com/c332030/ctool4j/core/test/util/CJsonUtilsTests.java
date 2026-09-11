package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CJsonUtils;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.core.util.CMapUtils;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「序列化 / 反序列化 / 对象转换 / 转 Map」多个维度组织，覆盖驼峰-下划线、非空、Long 安全等核心语义。</li>
 *   <li>用 {@code TestBean}（id/userName）验证驼峰/下划线互转与 Long 序列化。</li>
 *   <li>异常路径用 {@code assertThrowsExactly} 精确匹配（MismatchedInputException、JsonEOFException）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 Long 转 String、null 返回 null、驼峰-下划线的约定。</li>
 *   <li>依据测试方法（等价类/边界值/异常路径）：null/空/空白、Long、非法结构、驼峰下划线。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：toJson（null/Long/bean/Map）；toJsonSnakeCase；toJsonNonNull；fromJson（null/非法/合法）；</li>
 *   <li>fromJsonTypeReference（List/Map）；fromJsonSnakeCase；convert（bean→map/bean）与 convertSnakeCase；</li>
 *   <li>toMap/toMapSnakeCase；fromJsonList；fromJsonStringValue；toMapStringValue(toMapStringValueSnakeCase)。</li>
 *   <li>未覆盖：{@code toJsonLog}（日志脱敏路径，属扩展入口，未单列）。</li>
 * </ul>
 * <h2>序列化</h2>
 * <ul>
 *   <li>1.1 toJson：null→null；{@code {a:1}}→{@code {"a":1}}；Long→{@code "1"}；bean→驼峰（toJson）</li>
 *   <li>1.2 toJsonSnakeCase：驼峰转下划线（toJsonSnakeCase）</li>
 *   <li>1.3 toJsonNonNull：null 字段不输出；null 入参→null（toJsonNonNull）</li>
 * </ul>
 * <h2>反序列化</h2>
 * <ul>
 *   <li>2.1 fromJson：null/空/空白→null；非法结构抛 MismatchedInputException；未闭合抛 JsonEOFException；</li>
 *   <li>合法反序列化（fromJson）</li>
 *   <li>2.2 fromJsonTypeReference：List[Map]/Map[String,Object]/Map[String,String]（fromJsonTypeReference）</li>
 *   <li>2.3 fromJsonSnakeCase：下划线转驼峰（fromJsonSnakeCase）</li>
 * </ul>
 * <h2>对象转换</h2>
 * <ul>
 *   <li>3.1 convert：bean→Map（值 userName）、bean→bean（convert）</li>
 *   <li>3.2 convertSnakeCase：bean→Map 键转下划线（convertSnakeCase）</li>
 * </ul>
 * <h2>转 Map</h2>
 * <ul>
 *   <li>4.1 toMap：值 userName、id 保留 Long 数值类型（Q11）；null→null（toMap）</li>
 *   <li>4.2 toMapSnakeCase：键转下划线（toMapSnakeCase）</li>
 *   <li>4.3 fromJsonList：List[Map] 解析（fromJsonList）</li>
 *   <li>4.4 fromJsonStringValue：值 String 的 Map（fromJsonStringValue）</li>
 *   <li>4.5 toMapStringValue：值统一转 String（id→{@code "1"}）（toMapStringValue）</li>
 *   <li>4.6 toMapStringValueSnakeCase：值 String 且键转下划线（toMapStringValueSnakeCase）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CJsonUtilsTests {

    /**
     * 测试用 Bean，字段名用于验证驼峰/下划线互转
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestBean {

        private Long id;

        private String userName;

    }

    private static TestBean newBean(Long id, String userName) {

        val bean = new TestBean();
        bean.setId(id);
        bean.setUserName(userName);
        return bean;

    }

    /**
     * 对应测试用例 1.1：null→null；{@code {a:1}}→{@code {"a":1}}；Long→{@code "1"}；bean→驼峰
     */
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

    /**
     * 对应测试用例 1.2：驼峰转下划线
     */
    @Test
    public void toJsonSnakeCase() {

        // 驼峰转下划线
        val json = CJsonUtils.toJsonSnakeCase(newBean(1L, "a"));
        Assertions.assertEquals("{\"id\":\"1\",\"user_name\":\"a\"}", json);

    }

    /**
     * 对应测试用例 1.3：null 字段不输出；null 入参→null
     */
    @Test
    public void toJsonNonNull() {

        // null 字段不输出
        val json = CJsonUtils.toJsonNonNull(newBean(null, "a"));
        Assertions.assertEquals("{\"userName\":\"a\"}", json);

        Assertions.assertNull(CJsonUtils.toJsonNonNull(null));

    }

    /**
     * 对应测试用例 2.1：null/空/空白→null；非法结构抛 MismatchedInputException；未闭合抛 JsonEOFException；
     */
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

    /**
     * 对应测试用例 2.2：List[Map]/Map[String,Object]/Map[String,String]
     */
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

    /**
     * 对应测试用例 2.3：下划线转驼峰
     */
    @Test
    public void fromJsonSnakeCase() {

        val bean = CJsonUtils.fromJsonSnakeCase("{\"user_name\":\"a\"}", TestBean.class);
        Assertions.assertEquals("a", bean.getUserName());

        List<Map<String, Object>> list = CJsonUtils.fromJsonSnakeCase(
                "[{\"user_name\":\"a\"}]", CMapUtils.LIST_MAP_STRING_OBJECT_TYPE_REFERENCE);
        Assertions.assertEquals("a", list.get(0).get("user_name"));

    }

    /**
     * 对应测试用例 3.1：bean→Map（值 userName）、bean→bean
     */
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

    /**
     * 对应测试用例 3.2：bean→Map 键转下划线
     */
    @Test
    public void convertSnakeCase() {

        Map<String, Object> map = CJsonUtils.convertSnakeCase(
                newBean(1L, "a"), CMapUtils.MAP_STRING_OBJECT_TYPE_REFERENCE);
        Assertions.assertEquals("a", map.get("user_name"));

    }

    /**
     * 对应测试用例 4.1：值 userName、id 保留 Long 数值类型（Q11）；null→null
     */
    @Test
    public void toMap() {

        val map = CJsonUtils.toMap(newBean(1L, "a"));
        Assertions.assertEquals("a", map.get("userName"));
        // Q11 修复：Long 保留数值类型，不再经 JSON 中转变 String
        Assertions.assertEquals(1L, map.get("id"));

        // null 对象返回 null
        Assertions.assertNull(CJsonUtils.toMap(null));

    }

    /**
     * 对应测试用例 4.2：键转下划线
     */
    @Test
    public void toMapSnakeCase() {

        val map = CJsonUtils.toMapSnakeCase(newBean(1L, "a"));
        Assertions.assertEquals("a", map.get("user_name"));

    }

    /**
     * 对应测试用例 4.3：List[Map] 解析
     */
    @Test
    public void fromJsonList() {

        val list = CJsonUtils.fromJsonList("[{\"a\":1}]");
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals(1, list.get(0).get("a"));

    }

    /**
     * 对应测试用例 4.4：值 String 的 Map
     */
    @Test
    public void fromJsonStringValue() {

        val map = CJsonUtils.fromJsonStringValue("{\"a\":\"1\"}");
        Assertions.assertEquals("1", map.get("a"));

    }

    /**
     * 对应测试用例 4.5：值统一转 String（id→{@code "1"}）
     */
    @Test
    public void toMapStringValue() {

        val map = CJsonUtils.toMapStringValue(newBean(1L, "a"));
        Assertions.assertEquals("1", map.get("id"));
        Assertions.assertEquals("a", map.get("userName"));

    }

    /**
     * 对应测试用例 4.6：值 String 且键转下划线
     */
    @Test
    public void toMapStringValueSnakeCase() {

        val map = CJsonUtils.toMapStringValueSnakeCase(newBean(1L, "a"));
        Assertions.assertEquals("1", map.get("id"));
        Assertions.assertEquals("a", map.get("user_name"));

    }

}
