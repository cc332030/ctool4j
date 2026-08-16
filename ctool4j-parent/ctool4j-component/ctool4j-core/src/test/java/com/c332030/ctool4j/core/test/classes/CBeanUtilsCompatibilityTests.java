package com.c332030.ctool4j.core.test.classes;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CBeanUtils;
import com.c332030.ctool4j.core.classes.CClassUtils;
import com.c332030.ctool4j.core.classes.CConvertUtils;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.definition.function.CConsumer;
import com.c332030.ctool4j.definition.function.ToStringFunction;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CBeanUtils 重构兼容性测试
 * </p>
 * <p>
 * 以重构前旧语义（copy 经 toMap 中转、运行期按实际值类型转换；toMap 含 final/集合字段、
 * null key/值过滤、冲突抛异常、结果不可变）为基线，内嵌旧语义参考实现，
 * 多场景对比新实现（计划预计算直连）与旧实现结果一致。
 * 已知差异（旧缺口修复，javadoc 已注明）：原始类型同型字段旧实现无转换器跳过、
 * 新实现计划期 SELF 直接写入；源声明集合父类型实际持有集合旧实现跳过、新实现直接写入。
 * </p>
 *
 * @since 2026/8/16
 */
public class CBeanUtilsCompatibilityTests {

    // ============================ 测试 Bean ============================

    @Data
    public static class BaseFrom {

        private String baseName;

    }

    @Data
    public static class BaseTo {

        private String baseName;

    }

    @Data
    public static class FullFrom extends BaseFrom {

        private String name;

        private Integer age;

        private Integer level;

        private Long score;

        private Double ratio;

        private Boolean active;

        private Date date;

        private Object objDate;

        private Serializable serialDate;

        private Number number;

        private List<String> roles;

        private Map<String, String> tags;

        private String[] arr;

        private StringBuilder sb;

        private String nullField;

        private final String finalField = "from-final";

    }

    @Data
    public static class FullTo extends BaseTo {

        private String name;

        private Integer age;

        private Integer level;

        private Long score;

        private Double ratio;

        private Boolean active;

        private String date;

        private String objDate;

        private String serialDate;

        private Integer number;

        private List<String> roles;

        private Map<String, String> tags;

        private String[] arr;

        private StringBuffer sb;

        private String nullField;

        private final String finalField = "to-final";

    }

    @Data
    public static class PrimitiveFrom {

        private int level;

        private long score;

        private double ratio;

        private boolean active;

    }

    @Data
    public static class PrimitiveTo {

        private int level;

        private long score;

        private double ratio;

        private boolean active;

    }

    @Data
    public static class JsonNameFrom {

        @JsonProperty("json_name")
        private String jsonName;

    }

    public static class NoNoArgConstructor {

        public NoNoArgConstructor(String name) {
        }

    }

    // ============================ 旧语义参考实现 ============================

    /**
     * 旧语义参考实现：源字段驱动遍历 + 运行期按实际值类型判断
     * （与重构前 copy(toMap(from), to) 组合行为等价）
     */
    private static <To> To oldCopy(Object from, To to) {

        if(null == from || null == to) {
            return to;
        }

        val fromFieldMap = CReflectUtils.getInstanceFieldMap(from.getClass());
        val toFieldMap = CReflectUtils.getInstanceFieldMap(to.getClass());

        fromFieldMap.forEach((name, fromField) -> {

            val toField = toFieldMap.get(name);
            if(null == toField
                    || CReflectUtils.isStatic(toField)
                    || CReflectUtils.isFinal(toField)
            ) {
                return;
            }

            val fromValue = CReflectUtils.getValue(from, fromField);
            if(null == fromValue
                    || fromValue instanceof Collection
                    || fromValue instanceof Map
                    || fromValue.getClass().isArray()
            ) {
                return;
            }

            if(toField.getType().isAssignableFrom(fromValue.getClass())) {
                CReflectUtils.setValue(to, toField, fromValue);
                return;
            }

            CConvertUtils.convertOpt(fromValue, toField.getType())
                    .ifPresent((CConsumer<Object>) toValue -> CReflectUtils.setValue(to, toField, toValue));
        });

        return to;
    }

    /**
     * 旧语义参考实现：copy(Map, To)（与重构前 copy(Map, To) 行为等价）
     */
    private static <To> To oldCopyMap(Map<String, ?> fromMap, To to) {

        if(null == fromMap || null == to) {
            return to;
        }

        val toFieldMap = CReflectUtils.getInstanceFieldMap(to.getClass());
        fromMap.forEach((fromKey, fromValue) -> {

            val toField = toFieldMap.get(fromKey);
            if(null == toField
                    || null == fromValue
                    || CReflectUtils.isStatic(toField)
                    || CReflectUtils.isFinal(toField)
            ) {
                return;
            }

            CConvertUtils.convertOpt(fromValue, toField.getType())
                    .ifPresent((CConsumer<Object>) toValue -> CReflectUtils.setValue(to, toField, toValue));
        });

        return to;
    }

    /**
     * 旧语义参考实现：toMap（与重构前 CCollUtils.toMap 组合行为等价：
     * null key/值过滤、双值冲突抛异常、结果不可变）
     */
    private static Map<String, Object> oldToMap(Object object) {
        return oldToMap(object, Field::getName);
    }

    /**
     * 旧语义参考实现：toMap（指定 key 函数）
     */
    private static Map<String, Object> oldToMap(Object object, ToStringFunction<Field> getFieldNameFunction) {

        if(null == object || CClassUtils.isJdkClass(object.getClass())) {
            return CMap.of();
        }

        val fieldMap = CReflectUtils.getInstanceFieldMap(object.getClass());
        val map = new LinkedHashMap<String, Object>();
        fieldMap.forEach((name, field) -> {

            val key = getFieldNameFunction.apply(field);
            if(null == key) {
                return;
            }

            val value = CReflectUtils.getValue(object, field);
            if(null == value) {
                return;
            }

            val oldValue = map.put(key, value);
            if(null != oldValue) {
                throw new IllegalStateException("Conflict key: " + key + ", v1: " + oldValue + ", v2: " + value);
            }
        });

        if(map.isEmpty()) {
            return CMap.of();
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * 旧语义参考实现：copyFromArr 反序遍历（后复制者覆盖先复制者）
     */
    private static <To> To oldCopyFromArr(Object[] fromArr, To to) {

        if(null == fromArr || fromArr.length == 0) {
            return to;
        }

        for (int i = fromArr.length - 1; i >= 0; i--) {
            oldCopy(fromArr[i], to);
        }

        return to;
    }

    // ============================ 对比辅助 ============================

    /**
     * 断言 copy 结果新旧实现逐字段一致
     */
    private static void assertCopySame(Object from, Class<?> toClass) {

        val newTo = CBeanUtils.copy(from, toClass);
        val oldTo = oldCopy(from, CReflectUtils.newInstance(toClass));

        assertBeanFieldSame(toClass, newTo, oldTo);
    }

    /**
     * 断言 copy(Map) 结果新旧实现逐字段一致
     */
    private static void assertCopyMapSame(Map<String, ?> fromMap, Class<?> toClass) {

        val newTo = CBeanUtils.copy(fromMap, toClass);
        val oldTo = oldCopyMap(fromMap, CReflectUtils.newInstance(toClass));

        assertBeanFieldSame(toClass, newTo, oldTo);
    }

    /**
     * 断言 toMap 结果新旧实现一致
     */
    private static void assertToMapSame(Object object) {
        Assertions.assertEquals(oldToMap(object), CBeanUtils.toMap(object));
    }

    /**
     * 逐字段断言两个目标对象字段值一致
     */
    private static void assertBeanFieldSame(Class<?> toClass, Object newTo, Object oldTo) {

        val fieldMap = CReflectUtils.getInstanceFieldMap(toClass);
        Assertions.assertFalse(fieldMap.isEmpty(), "字段列表为空，对比无意义: " + toClass);
        fieldMap.forEach((name, field) -> {
            val newValue = CReflectUtils.getValue(newTo, field);
            val oldValue = CReflectUtils.getValue(oldTo, field);
            Assertions.assertEquals(oldValue, newValue, "field: " + name);
        });
    }

    private static FullFrom newFullFrom() {

        val from = new FullFrom();
        from.setBaseName("base");
        from.setName("name");
        from.setAge(18);
        from.setLevel(5);
        from.setScore(100L);
        from.setRatio(0.5d);
        from.setActive(true);
        from.setDate(new Date(1700000000000L));
        from.setObjDate(new Date(1700000000000L));
        from.setSerialDate(new Date(1700000000000L));
        from.setNumber(7);
        from.setRoles(CList.of("r1"));
        from.setTags(CMap.of("k", "v"));
        from.setArr(new String[] {"a"});
        from.setSb(new StringBuilder("sb"));
        return from;
    }

    // ============================ 兼容性测试 ============================

    /**
     * 全字段复制兼容：基础/包装/日期/Object/父类型声明/继承/null/final/集合/无转换器字段
     */
    @Test
    public void copyFullCompatibility() {
        assertCopySame(newFullFrom(), FullTo.class);
    }

    /**
     * 类型转换兼容：声明 Object/Serializable 实际持有 Date 转 String 命中格式化（objectStr 优先级最低）
     */
    @Test
    public void copyObjectStrCompatibility() {
        assertCopySame(newFullFrom(), FullTo.class);
    }

    /**
     * 原始类型同型字段复制：新实现计划期 SELF 直接写入
     * （旧实现因无原始类型转换器而跳过，属旧缺口修复，与旧行为不同）
     */
    @Test
    public void copyPrimitiveTypeFields() {

        val from = new PrimitiveFrom();
        from.setLevel(5);
        from.setScore(100L);
        from.setRatio(0.5d);
        from.setActive(true);

        val to = CBeanUtils.copy(from, new PrimitiveTo());

        Assertions.assertEquals(5, to.getLevel());
        Assertions.assertEquals(100L, to.getScore());
        Assertions.assertEquals(0.5d, to.getRatio());
        Assertions.assertTrue(to.isActive());
    }

    /**
     * JDK 源类不复制兼容
     */
    @Test
    public void copyJdkSourceCompatibility() {

        val newTo = CBeanUtils.copy("string-source", new FullTo());
        val oldTo = oldCopy("string-source", new FullTo());

        assertBeanFieldSame(FullTo.class, newTo, oldTo);
    }

    /**
     * null 源/目标边界兼容
     */
    @Test
    public void copyNullSourceCompatibility() {

        val to = new FullTo();
        Assertions.assertSame(to, CBeanUtils.copy(null, to));
        Assertions.assertSame(to, oldCopy(null, to));

        Assertions.assertNull(CBeanUtils.copy(newFullFrom(), (FullTo) null));
        Assertions.assertNull(oldCopy(newFullFrom(), (FullTo) null));
    }

    /**
     * Map 源兼容：基础字段、null 值、类型转换、final/集合跳过
     */
    @Test
    public void copyMapCompatibility() {

        assertCopyMapSame(CMap.of("name", "n", "age", 20, "baseName", "base"), FullTo.class);
        assertCopyMapSame(CMap.of("name", null), FullTo.class);
        assertCopyMapSame(CMap.of("date", new Date(1700000000000L)), FullTo.class);
        assertCopyMapSame(CMap.of("finalField", "x"), FullTo.class);
        assertCopyMapSame(CMap.of("roles", CList.of("r")), FullTo.class);
        assertCopyMapSame(CMap.of("sb", new StringBuilder("sb")), FullTo.class);
    }

    /**
     * 各复制入口兼容：copy(Object, Class)/copy(Object, supplier)/copy(Map, Class)/copy(Map, supplier)
     */
    @Test
    public void copyEntryCompatibility() {

        val from = newFullFrom();
        val map = CMap.of("name", "n", "age", 20);

        val newClassTo = CBeanUtils.copy(from, FullTo.class);
        val oldClassTo = oldCopy(from, new FullTo());
        assertBeanFieldSame(FullTo.class, newClassTo, oldClassTo);

        val newSupplierTo = CBeanUtils.copy(from, FullTo::new);
        val oldSupplierTo = oldCopy(from, new FullTo());
        assertBeanFieldSame(FullTo.class, newSupplierTo, oldSupplierTo);

        val newMapClassTo = CBeanUtils.copy(map, FullTo.class);
        val oldMapClassTo = oldCopyMap(map, new FullTo());
        assertBeanFieldSame(FullTo.class, newMapClassTo, oldMapClassTo);

        val newMapSupplierTo = CBeanUtils.copy(map, FullTo::new);
        val oldMapSupplierTo = oldCopyMap(map, new FullTo());
        assertBeanFieldSame(FullTo.class, newMapSupplierTo, oldMapSupplierTo);
    }

    /**
     * copyFromArr 反序遍历覆盖兼容
     */
    @Test
    public void copyFromArrCompatibility() {

        val first = newFullFrom();
        first.setName("first");
        val second = newFullFrom();
        second.setName("second");
        val arr = new Object[] {first, second};

        val newTo = CBeanUtils.copyFromArr(arr, FullTo.class);
        val oldTo = oldCopyFromArr(arr, new FullTo());

        assertBeanFieldSame(FullTo.class, newTo, oldTo);
        Assertions.assertEquals("first", newTo.getName());
    }

    /**
     * copyList / copyListFromMap 兼容
     */
    @Test
    public void copyListCompatibility() {

        val objectList = Arrays.asList(newFullFrom(), newFullFrom());
        val newList = CBeanUtils.copyList(objectList, FullTo.class);
        val oldList = objectList.stream()
                .map(from -> oldCopy(from, new FullTo()))
                .collect(Collectors.toList());
        Assertions.assertEquals(oldList.size(), newList.size());
        for (int i = 0; i < newList.size(); i++) {
            assertBeanFieldSame(FullTo.class, newList.get(i), oldList.get(i));
        }

        val mapList = CList.of(CMap.of("name", "n1", "age", 20), CMap.of("name", "n2"));
        val newMapList = CBeanUtils.copyListFromMap(mapList, FullTo.class);
        val oldMapList = mapList.stream()
                .map(m -> oldCopyMap(m, new FullTo()))
                .collect(Collectors.toList());
        Assertions.assertEquals(oldMapList.size(), newMapList.size());
        for (int i = 0; i < newMapList.size(); i++) {
            assertBeanFieldSame(FullTo.class, newMapList.get(i), oldMapList.get(i));
        }
    }

    /**
     * toMap 兼容：null 值过滤、final/集合字段入 map、不可变
     */
    @Test
    public void toMapCompatibility() {

        val from = newFullFrom();
        from.setName(null);

        assertToMapSame(from);
        assertToMapSame(newFullFrom());
    }

    /**
     * toMap 下划线/json 命名兼容
     */
    @Test
    public void toMapNamedCompatibility() {

        val from = newFullFrom();
        Assertions.assertEquals(
                oldToMap(from, field -> StrUtil.toUnderlineCase(field.getName())),
                CBeanUtils.toMapUnderlineName(from)
        );

        val jsonBean = new JsonNameFrom();
        jsonBean.setJsonName("json");
        Assertions.assertEquals(
                oldToMap(jsonBean, field -> CReflectUtils.getFieldName(field, JsonProperty.class, JsonProperty::value)),
                CBeanUtils.toMapJsonName(jsonBean)
        );
    }

    /**
     * toMap null/JDK 源兼容
     */
    @Test
    public void toMapJdkNullCompatibility() {

        assertToMapSame(null);
        assertToMapSame("string-source");
        assertToMapSame(new FullFrom());
    }

    /**
     * newInstance 兼容：正常类实例化、无 public 无参构造器抛异常
     */
    @Test
    public void newInstanceCompatibility() {

        Assertions.assertNotNull(CReflectUtils.newInstance(FullTo.class));
        Assertions.assertNotNull(CBeanUtils.copy(newFullFrom(), FullTo.class));

        Assertions.assertThrows(RuntimeException.class,
                () -> CReflectUtils.newInstance(NoNoArgConstructor.class));
    }

}
