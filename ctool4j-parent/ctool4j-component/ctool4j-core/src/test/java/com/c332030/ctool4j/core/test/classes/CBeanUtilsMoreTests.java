package com.c332030.ctool4j.core.test.classes;

import cn.hutool.core.date.DateUtil;
import com.c332030.ctool4j.core.classes.CBeanUtils;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.test.definition.model.UserDto;
import com.c332030.ctool4j.test.definition.model.UserRsp;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.*;

/**
 * <p>
 * Description: CBeanUtils 补充测试（覆盖各复制入口与 toMap 语义一致性）
 * </p>
 * <p>
 * 关键语义：集合/Map/数组字段不复制（与转换器对这三类返回空一致）、
 * final/static 字段跳过、null 值跳过、类型不匹配无转换器时跳过、
 * toMap 过滤 null 键值并返回不可变 Map。
 * </p>
 *
 * @since 2026/8/16
 */
public class CBeanUtilsMoreTests {

    @Data
    public static class BaseBean {

        private String baseName;

    }

    @Data
    public static class FromBean extends BaseBean {

        private String name;

        private Integer age;

        private List<String> roles;

        private Map<String, String> tags;

        private int[] nums;

        private StringBuilder sb;

        private final String finalField = "from-init";

    }

    @Data
    public static class ToBean extends BaseBean {

        private String name;

        private Integer age;

        private List<String> roles;

        private Map<String, String> tags;

        private int[] nums;

        private StringBuffer sb;

        private final String finalField = "to-init";

    }

    @Data
    public static class JsonNameBean {

        @JsonProperty("json_name")
        private String jsonName;

    }

    private static FromBean newFrom() {
        val from = new FromBean();
        from.setBaseName("base");
        from.setName("name");
        from.setAge(18);
        from.setRoles(CList.of("role1", "role2"));
        from.setTags(CMap.of("k", "v"));
        from.setNums(new int[] {1, 2, 3});
        from.setSb(new StringBuilder("sb"));
        return from;
    }

    /**
     * 测试 Map 转对象基础复制
     */
    @Test
    public void copyMapToObject() {

        val map = CMap.of("name", "n", "age", 20);

        val to = CBeanUtils.copy(map, ToBean.class);

        Assertions.assertEquals("n", to.getName());
        Assertions.assertEquals(20, to.getAge());
        Assertions.assertNull(to.getRoles());
        Assertions.assertEquals("to-init", to.getFinalField());
    }

    /**
     * 测试 Map 转对象时 null 值不覆盖已有值
     */
    @Test
    public void copyMapToObjectSkipNull() {

        val to = new ToBean();
        to.setName("exist");

        CBeanUtils.copy(CMap.of("name", null), to);

        Assertions.assertEquals("exist", to.getName());
    }

    /**
     * 测试对象直接复制（直接路径）：基础字段与继承字段
     */
    @Test
    public void copyObjectToObject() {

        val to = CBeanUtils.copy(newFrom(), new ToBean());

        Assertions.assertEquals("base", to.getBaseName());
        Assertions.assertEquals("name", to.getName());
        Assertions.assertEquals(18, to.getAge());
    }

    /**
     * 测试对象直接复制跳过集合/Map/数组字段（与转换器对这三类返回空一致）
     */
    @Test
    public void copyObjectToObjectSkipCollectionAndArray() {

        val to = CBeanUtils.copy(newFrom(), new ToBean());

        Assertions.assertNull(to.getRoles());
        Assertions.assertNull(to.getTags());
        Assertions.assertNull(to.getNums());
    }

    /**
     * 测试对象直接复制时目标 final 字段不被覆盖（setter 缓存排除 final）
     */
    @Test
    public void copyObjectToObjectSkipFinal() {

        val to = CBeanUtils.copy(newFrom(), new ToBean());

        Assertions.assertEquals("to-init", to.getFinalField());
    }

    /**
     * 测试 Class 入口（copy(Object, Class)）同样跳过集合/Map/数组与 final 字段
     */
    @Test
    public void copyClassEntrySkipCollectionAndArray() {

        val to = CBeanUtils.copy(newFrom(), ToBean.class);

        Assertions.assertEquals("name", to.getName());
        Assertions.assertNull(to.getRoles());
        Assertions.assertNull(to.getTags());
        Assertions.assertNull(to.getNums());
        Assertions.assertEquals("to-init", to.getFinalField());
    }

    /**
     * 测试类型不匹配且无转换器时跳过（StringBuilder -> StringBuffer）
     */
    @Test
    public void copyNoConverterSkip() {

        val to = CBeanUtils.copy(newFrom(), new ToBean());

        Assertions.assertNull(to.getSb());
    }

    /**
     * 测试各类 null 边界
     * <p>字面量 null 经重载决议匹配更具体的 Map 重载（既有语义返回 null）；
     * 显式 Object 引用走 Object 重载返回新实例</p>
     */
    @Test
    public void copyNullEdge() {

        val to = new ToBean();
        Assertions.assertSame(to, CBeanUtils.copy(null, to));
        Assertions.assertNull(CBeanUtils.copy(newFrom(), (ToBean) null));
        // 字面量 null 匹配 Map 重载（更具体）→ null
        Assertions.assertNull(CBeanUtils.copy(null, ToBean.class));
        Assertions.assertNull(CBeanUtils.copy(null, () -> new ToBean()));
        // 显式 Object 引用走 Object 重载 → 新实例
        Assertions.assertNotNull(CBeanUtils.copy((Object) null, ToBean.class));
        Assertions.assertNotNull(CBeanUtils.copy((Object) null, () -> new ToBean()));
        // 空 map → 新实例
        Assertions.assertNotNull(CBeanUtils.copy(new HashMap<>(), ToBean.class));
    }

    /**
     * 测试 JDK 类源对象复制返回空实例（保持原语义）
     */
    @Test
    public void copyJdkClassSource() {

        val to = CBeanUtils.copy("string-source", ToBean.class);

        Assertions.assertNotNull(to);
        Assertions.assertNull(to.getName());
        Assertions.assertEquals("to-init", to.getFinalField());
    }

    /**
     * 测试真实模型 UserDto -> UserRsp：集合/Map 字段跳过、类型转换生效
     */
    @Test
    public void copyUserDtoToRsp() {

        val user = UserDto.builder()
                .userName("u")
                .sex(1)
                .amount(100)
                .roles(CList.of("r1"))
                .tags(CMap.of("k", "v"))
                .build();

        val rsp = CBeanUtils.copy(user, UserRsp.class);

        Assertions.assertEquals("u", rsp.getUserName());
        Assertions.assertEquals("1", rsp.getSex());
        Assertions.assertNotNull(rsp.getAmount());
        Assertions.assertNull(rsp.getRoles());
        Assertions.assertNull(rsp.getTags());
    }

    /**
     * 测试 copyList
     */
    @Test
    public void copyList() {

        val list = CBeanUtils.copyList(Arrays.asList(newFrom(), newFrom()), ToBean.class);

        Assertions.assertEquals(2, list.size());
        for (val to : list) {
            Assertions.assertEquals("name", to.getName());
            Assertions.assertNull(to.getRoles());
        }
    }

    /**
     * 测试 copyListFromMap
     */
    @Test
    public void copyListFromMap() {

        val mapList = CList.of(CMap.of("name", "n1"), CMap.of("name", "n2"));

        val list = CBeanUtils.copyListFromMap(mapList, ToBean.class);

        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals("n1", list.get(0).getName());
        Assertions.assertEquals("n2", list.get(1).getName());
    }

    /**
     * 测试 copyFromArr 反序遍历（后复制者覆盖先复制者）
     */
    @Test
    public void copyFromArr() {

        val first = newFrom();
        first.setName("first");
        val second = newFrom();
        second.setName("second");

        val to = CBeanUtils.copyFromArr(new Object[] {first, second}, ToBean.class);

        Assertions.assertEquals("first", to.getName());
    }

    /**
     * 测试 toMap 基础：非 null 值入 map，null 值排除，返回不可变 Map
     */
    @Test
    public void toMapBasic() {

        val from = newFrom();
        from.setName(null);
        from.setAge(null);

        val map = CBeanUtils.toMap(from);

        Assertions.assertEquals("base", map.get("baseName"));
        Assertions.assertEquals("v", ((Map<?, ?>) map.get("tags")).get("k"));
        Assertions.assertFalse(map.containsKey("name"));
        Assertions.assertFalse(map.containsKey("age"));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> map.put("x", 1));
    }

    /**
     * 测试 toMap 下划线命名
     */
    @Test
    public void toMapUnderline() {

        val map = CBeanUtils.toMapUnderlineName(newFrom());

        Assertions.assertEquals("base", map.get("base_name"));
    }

    /**
     * 测试 toMap 使用 json 属性名
     */
    @Test
    public void toMapJsonName() {

        val bean = new JsonNameBean();
        bean.setJsonName("json");

        val map = CBeanUtils.toMapJsonName(bean);

        Assertions.assertEquals("json", map.get("json_name"));
        Assertions.assertFalse(map.containsKey("jsonName"));
    }

    /**
     * 测试 toMap key 冲突时抛异常（与 merge 冲突语义一致）
     */
    @Test
    public void toMapConflict() {

        val from = newFrom();

        Assertions.assertThrows(IllegalStateException.class,
                () -> CBeanUtils.toMap(from, field -> "conflict"));
    }

    /**
     * 测试 Map 入口类型转换（Integer -> BigDecimal）
     */
    @Test
    public void copyMapToObjectTypeConvert() {

        val map = new HashMap<String, Object>();
        map.put("amount", 1);

        val rsp = CBeanUtils.copy(map, UserRsp.class);

        Assertions.assertNotNull(rsp.getAmount());
        Assertions.assertEquals(0, rsp.getAmount().compareTo(new java.math.BigDecimal("1")));
    }

    /**
     * 测试 copy(Map, To) 对不可变 Map 正常处理
     */
    @Test
    public void copyMapUnmodifiable() {

        val to = CBeanUtils.copy(CMap.of("name", "n"), new ToBean());

        Assertions.assertEquals("n", to.getName());
    }

    /**
     * 测试 toMap 空对象返回空不可变 Map
     */
    @Test
    public void toMapEmpty() {

        val map = CBeanUtils.toMap(new EmptyBean());

        Assertions.assertTrue(map.isEmpty());
        Assertions.assertThrows(UnsupportedOperationException.class, () -> map.put("x", 1));
    }

    /**
     * 全部字段为 null 的测试 Bean（toMap 空结果验证用）
     */
    @Data
    public static class EmptyBean {

        private String name;

    }

    /**
     * 测试 toMap null 入参返回空 Map
     */
    @Test
    public void toMapNull() {

        val map = CBeanUtils.toMap(null);

        Assertions.assertTrue(map.isEmpty());
    }

    @Data
    public static class ParentTypeFrom {

        private Number number;

        private Object data;

        private Object collectionData;

        private int primitive;

    }

    @Data
    public static class ParentTypeTo {

        private Integer number;

        private String data;

        private Object collectionData;

        private Integer primitive;

    }

    /**
     * 测试父类型/Object 声明字段走回退路径（计划期无法按声明类型解析转换路径，
     * 运行期按实际值类型判断，语义与旧实现一致）
     */
    @Test
    public void copyParentDeclaredTypeFallback() {

        val from = new ParentTypeFrom();
        from.setNumber(5);
        from.setData("str");
        from.setCollectionData(CList.of("x"));
        from.setPrimitive(7);

        val to = CBeanUtils.copy(from, new ParentTypeTo());

        // 声明 Number 实际 Integer，运行期值类型可赋值直接写入
        Assertions.assertEquals(5, to.getNumber());
        // 声明 Object 实际 String，可赋值直接写入
        Assertions.assertEquals("str", to.getData());
        // 已知取舍：声明 Object 实际持有集合，走回退路径按旧逻辑跳过（不写入）
        Assertions.assertNull(to.getCollectionData());
        // 声明 int 实际 Integer（装箱），运行期值类型可赋值直接写入
        Assertions.assertEquals(7, to.getPrimitive());
    }

    /**
     * 测试 toMap 计划包含 final 字段值（final 值进 map，反向 set 不包含）
     */
    @Test
    public void toMapIncludesFinal() {

        val map = CBeanUtils.toMap(newFrom());

        Assertions.assertEquals("from-init", map.get("finalField"));
    }

    /**
     * 测试 toMap 对 JDK 类返回空 Map（JDK 判断下沉计划期后语义保持）
     */
    @Test
    public void toMapJdkClass() {

        val map = CBeanUtils.toMap("string-source");

        Assertions.assertTrue(map.isEmpty());
    }

    @Data
    public static class ObjectStrFrom {

        private Object objDate;

        private Serializable serialDate;

        private Date declaredDate;

        private Object objName;

    }

    @Data
    public static class ObjectStrTo {

        private String objDate;

        private String serialDate;

        private String declaredDate;

        private String objName;

    }

    /**
     * 测试 objectStr 优先级最低：Object/父类型声明字段实际持有 Date 时，
     * 走回退路径按实际值类型命中 Date→String 格式化转换，而非 objectStr 的 toString
     */
    @Test
    public void copyObjectStrLowestPriority() {

        val date = new Date(1700000000000L);
        val expected = DateUtil.formatDateTime(date);

        val from = new ObjectStrFrom();
        from.setObjDate(date);
        from.setSerialDate(date);
        from.setDeclaredDate(date);
        from.setObjName("name");

        val to = CBeanUtils.copy(from, new ObjectStrTo());

        Assertions.assertEquals(expected, to.getObjDate());
        Assertions.assertEquals(expected, to.getSerialDate());
        Assertions.assertEquals(expected, to.getDeclaredDate());
        Assertions.assertEquals("name", to.getObjName());
    }

}
