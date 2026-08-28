package com.c332030.ctool4j.core.test.classes;

import cn.hutool.core.date.DateUtil;
import com.c332030.ctool4j.core.classes.CBeanUtils;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.definition.interfaces.ICValue;
import com.c332030.ctool4j.test.definition.model.UserDto;
import com.c332030.ctool4j.test.definition.model.UserRsp;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
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
 * <p>
 * 完整测试设计（测试架构、参考实现手法、覆盖场景、未覆盖场景与兼容性考量）见
 * 设计文档 doc/design/core/CBeanUtils.adoc；测试用例分类与编号见测试文档
 * doc/design/core/CBeanUtilsTests.adoc，各测试方法在 javadoc 中标注对应编号。
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
     * 对应测试用例 1.2.1
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
     * 对应测试用例 1.2.2
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
     * 对应测试用例 1.1.1
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
     * 对应测试用例 1.1.2
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
     * 对应测试用例 1.1.3
     */
    @Test
    public void copyObjectToObjectSkipFinal() {

        val to = CBeanUtils.copy(newFrom(), new ToBean());

        Assertions.assertEquals("to-init", to.getFinalField());
    }

    /**
     * 测试 Class 入口（copy(Object, Class)）同样跳过集合/Map/数组与 final 字段
     * 对应测试用例 1.2.5
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
     * 对应测试用例 1.1.4
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
     * 对应测试用例 1.3.1
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
     * 对应测试用例 1.3.2
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
     * 对应测试用例 1.1.6
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
     * 对应测试用例 2.1
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
     * 对应测试用例 2.2
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
     * 对应测试用例 2.3
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
     * 对应测试用例 3.1
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
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> map.put("x", 1));
    }

    /**
     * 测试 toMap 下划线命名
     * 对应测试用例 3.2
     */
    @Test
    public void toMapUnderline() {

        val map = CBeanUtils.toMapUnderlineName(newFrom());

        Assertions.assertEquals("base", map.get("base_name"));
    }

    /**
     * 测试 toMap 使用 json 属性名
     * 对应测试用例 3.3
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
     * 对应测试用例 3.4
     */
    @Test
    public void toMapConflict() {

        val from = newFrom();

        Assertions.assertThrowsExactly(IllegalStateException.class,
                () -> CBeanUtils.toMap(from, field -> "conflict"));
    }

    /**
     * 测试 Map 入口类型转换（Integer -> BigDecimal）
     * 对应测试用例 1.2.3
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
     * 对应测试用例 1.2.4
     */
    @Test
    public void copyMapUnmodifiable() {

        val to = CBeanUtils.copy(CMap.of("name", "n"), new ToBean());

        Assertions.assertEquals("n", to.getName());
    }

    /**
     * 测试 toMap 空对象返回空不可变 Map
     * 对应测试用例 3.5
     */
    @Test
    public void toMapEmpty() {

        val map = CBeanUtils.toMap(new EmptyBean());

        Assertions.assertTrue(map.isEmpty());
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> map.put("x", 1));
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
     * 对应测试用例 3.6
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
     * 对应测试用例 1.1.5
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
     * 对应测试用例 3.7
     */
    @Test
    public void toMapIncludesFinal() {

        val map = CBeanUtils.toMap(newFrom());

        Assertions.assertEquals("from-init", map.get("finalField"));
    }

    /**
     * 测试 toMap 对 JDK 类返回空 Map（JDK 判断下沉计划期后语义保持）
     * 对应测试用例 3.8
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
     * 对应测试用例 4.11
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

    @Data
    public static class WrapperToPrimitiveFrom {

        private Integer intValue;

        private Long longValue;

        private Float floatValue;

        private Double doubleValue;

        private Boolean boolValue;

        private Character charValue;

        private Byte byteValue;

        private Short shortValue;

    }

    @Data
    public static class WrapperToPrimitiveTo {

        private int intValue;

        private long longValue;

        private float floatValue;

        private double doubleValue;

        private boolean boolValue;

        private char charValue;

        private byte byteValue;

        private short shortValue;

    }

    /**
     * 测试源包装类型 -> 目标基础类型字段（拆箱写入，修复前被静默跳过）
     * <p>CClassConvert 已有 Long→int（intValue）等转换类，配合 ClassUtil.isAssignable
     * 支持包装/基础等价后，同类型包装→基础直接拆箱写入（Integer→int 等）</p>
     * 对应测试用例 4.1
     */
    @Test
    public void copyWrapperToPrimitive() {

        val from = new WrapperToPrimitiveFrom();
        from.setIntValue(1);
        from.setLongValue(2L);
        from.setFloatValue(3.0f);
        from.setDoubleValue(4.0d);
        from.setBoolValue(true);
        from.setCharValue('c');
        from.setByteValue((byte) 5);
        from.setShortValue((short) 6);

        val to = CBeanUtils.copy(from, new WrapperToPrimitiveTo());

        Assertions.assertEquals(1, to.getIntValue());
        Assertions.assertEquals(2L, to.getLongValue());
        Assertions.assertEquals(3.0f, to.getFloatValue());
        Assertions.assertEquals(4.0d, to.getDoubleValue());
        Assertions.assertTrue(to.isBoolValue());
        Assertions.assertEquals('c', to.getCharValue());
        Assertions.assertEquals((byte) 5, to.getByteValue());
        Assertions.assertEquals((short) 6, to.getShortValue());
    }

    /**
     * 持有 Long 值的测试对象（LongToIntFrom 与 IntToLongTo 原结构相同，合并复用）
     */
    @Data
    public static class LongValueBean {

        private Long value;

    }

    /**
     * 持有 int 值的测试对象（IntToLongFrom 与 LongToIntTo 原结构相同，合并复用）
     */
    @Data
    public static class IntValueBean {

        private int value;

    }

    /**
     * 测试 Long -> int 走 CClassConvert.intValue 转换器（非同类型拆箱场景）
     * 对应测试用例 4.2
     */
    @Test
    public void copyLongToInt() {

        val from = new LongValueBean();
        from.setValue(10L);

        val to = CBeanUtils.copy(from, new IntValueBean());

        Assertions.assertEquals(10, to.getValue());
    }

    /**
     * 测试 int -> Long 走 CClassConvert.toLong 转换器（copyLongToInt 的反向对称场景）
     * 对应测试用例 4.3
     */
    @Test
    public void copyIntToLong() {

        val from = new IntValueBean();
        from.setValue(10);

        val to = CBeanUtils.copy(from, new LongValueBean());

        Assertions.assertEquals(10L, to.getValue());
    }

    @Data
    public static class StrToNumberFrom {

        private String intVal;

        private String longVal;

        private String decimalVal;

        private String floatVal;

        private String doubleVal;

        private String boolVal;

    }

    @Data
    public static class StrToNumberTo {

        private Integer intVal;

        private Long longVal;

        private BigDecimal decimalVal;

        private Float floatVal;

        private Double doubleVal;

        private Boolean boolVal;

    }

    /**
     * 测试 String -> 数字/布尔：toInt/toLong/toBigDecimal/toFloat/toDouble/toBoolean 转换器
     * 对应测试用例 4.12
     */
    @Test
    public void copyStrToNumber() {

        val from = new StrToNumberFrom();
        from.setIntVal("123");
        from.setLongVal("456");
        from.setDecimalVal("7.89");
        from.setFloatVal("1.5");
        from.setDoubleVal("2.5");
        from.setBoolVal("1");

        val to = CBeanUtils.copy(from, new StrToNumberTo());

        Assertions.assertEquals(123, to.getIntVal());
        Assertions.assertEquals(456L, to.getLongVal());
        Assertions.assertEquals(0, to.getDecimalVal().compareTo(new BigDecimal("7.89")));
        Assertions.assertEquals(1.5f, to.getFloatVal());
        Assertions.assertEquals(2.5d, to.getDoubleVal());
        // toBoolean 额外兼容数字 "1" 表示 true
        Assertions.assertTrue(to.getBoolVal());
    }

    @Data
    public static class NumberToStrFrom {

        private Integer intVal;

        private Long longVal;

        private BigDecimal decimalVal;

        private Float floatVal;

        private Double doubleVal;

        private Boolean boolVal;

    }

    @Data
    public static class NumberToStrTo {

        private String intVal;

        private String longVal;

        private String decimalVal;

        private String floatVal;

        private String doubleVal;

        private String boolVal;

    }

    /**
     * 测试数字/布尔 -> String：intStr/longStr/bigDecimalStr/floatStr/doubleStr/booleanStr 转换器
     * 对应测试用例 4.4
     */
    @Test
    public void copyNumberToStr() {

        val from = new NumberToStrFrom();
        from.setIntVal(123);
        from.setLongVal(456L);
        from.setDecimalVal(new BigDecimal("7.89"));
        from.setFloatVal(1.5f);
        from.setDoubleVal(2.5d);
        from.setBoolVal(true);

        val to = CBeanUtils.copy(from, new NumberToStrTo());

        Assertions.assertEquals("123", to.getIntVal());
        Assertions.assertEquals("456", to.getLongVal());
        Assertions.assertEquals("7.89", to.getDecimalVal());
        Assertions.assertEquals("1.5", to.getFloatVal());
        Assertions.assertEquals("2.5", to.getDoubleVal());
        Assertions.assertEquals("true", to.getBoolVal());
    }

    @Data
    public static class DateConvertFrom {

        private String dateStr;

        private Date dateVal;

        private Long millsVal;

        private Date dateToInstant;

        private Instant instantVal;

        private Date dateToStr;

    }

    @Data
    public static class DateConvertTo {

        private Date dateStr;

        private Long dateVal;

        private Date millsVal;

        private Instant dateToInstant;

        private Date instantVal;

        private String dateToStr;

    }

    /**
     * 测试日期系列转换：String->Date、Date->Long、Long->Date、Date->Instant、Instant->Date、Date->String
     * 对应测试用例 4.5
     */
    @Test
    public void copyDateConvert() {

        val date = new Date(1700000000000L);

        val from = new DateConvertFrom();
        from.setDateStr("2026-08-16 12:00:00");
        from.setDateVal(date);
        from.setMillsVal(1700000000000L);
        from.setDateToInstant(date);
        from.setInstantVal(Instant.ofEpochMilli(1700000000000L));
        from.setDateToStr(date);

        val to = CBeanUtils.copy(from, new DateConvertTo());

        Assertions.assertEquals(DateUtil.parse("2026-08-16 12:00:00"), to.getDateStr());
        Assertions.assertEquals(1700000000000L, to.getDateVal());
        Assertions.assertEquals(date, to.getMillsVal());
        Assertions.assertEquals(Instant.ofEpochMilli(1700000000000L), to.getDateToInstant());
        Assertions.assertEquals(date, to.getInstantVal());
        Assertions.assertEquals(DateUtil.formatDateTime(date), to.getDateToStr());
    }

    @Data
    public static class ToBigDecimalFrom {

        private int intVal;

        private long longVal;

        private Integer integerVal;

        private Long longWrapperVal;

        private double doubleVal;

        private float floatVal;

    }

    @Data
    public static class ToBigDecimalTo {

        private BigDecimal intVal;

        private BigDecimal longVal;

        private BigDecimal integerVal;

        private BigDecimal longWrapperVal;

        private BigDecimal doubleVal;

        private BigDecimal floatVal;

    }

    /**
     * 测试数字 -> BigDecimal：toBigDecimal(int/long/Integer/Long/double/float) 转换器
     * 对应测试用例 4.6
     */
    @Test
    public void copyToBigDecimal() {

        val from = new ToBigDecimalFrom();
        from.setIntVal(5);
        from.setLongVal(100L);
        from.setIntegerVal(5);
        from.setLongWrapperVal(100L);
        from.setDoubleVal(2.5d);
        from.setFloatVal(1.5f);

        val to = CBeanUtils.copy(from, new ToBigDecimalTo());

        Assertions.assertEquals(0, to.getIntVal().compareTo(new BigDecimal("5")));
        Assertions.assertEquals(0, to.getLongVal().compareTo(new BigDecimal("100")));
        Assertions.assertEquals(0, to.getIntegerVal().compareTo(new BigDecimal("5")));
        Assertions.assertEquals(0, to.getLongWrapperVal().compareTo(new BigDecimal("100")));
        Assertions.assertEquals(0, to.getDoubleVal().compareTo(new BigDecimal("2.5")));
        Assertions.assertEquals(0, to.getFloatVal().compareTo(new BigDecimal("1.5")));
    }

    @Data
    public static class DecimalToFloatDoubleFrom {

        private BigDecimal decimalToDouble;

        private BigDecimal decimalToFloat;

        private Float floatToDouble;

    }

    @Data
    public static class DecimalToFloatDoubleTo {

        private double decimalToDouble;

        private float decimalToFloat;

        private double floatToDouble;

    }

    /**
     * 测试 BigDecimal -> double/float、Float -> double（doubleValue/floatValue 转换器）
     * 对应测试用例 4.7
     */
    @Test
    public void copyDecimalToFloatDouble() {

        val from = new DecimalToFloatDoubleFrom();
        from.setDecimalToDouble(new BigDecimal("2.5"));
        from.setDecimalToFloat(new BigDecimal("1.5"));
        from.setFloatToDouble(2.5f);

        val to = CBeanUtils.copy(from, new DecimalToFloatDoubleTo());

        Assertions.assertEquals(2.5d, to.getDecimalToDouble());
        Assertions.assertEquals(1.5f, to.getDecimalToFloat());
        Assertions.assertEquals(2.5d, to.getFloatToDouble());
    }

    @Getter
    @AllArgsConstructor
    public enum IntValueEnum implements ICValue<Integer> {

        ONE(1), TWO(2);

        private final Integer value;

    }

    @Getter
    @AllArgsConstructor
    public enum StrValueEnum implements ICValue<String> {

        A("a"), B("b");

        private final String value;

    }

    @Data
    public static class EnumValueFrom {

        private IntValueEnum intEnumVal;

        private StrValueEnum strEnumVal;

    }

    @Data
    public static class EnumValueTo {

        private Integer intEnumVal;

        private String strEnumVal;

    }

    /**
     * 测试 ICValue 枚举 -> Integer/String（toEnumIntegerValue/toEnumStringValue 转换器）
     * 对应测试用例 4.10
     */
    @Test
    public void copyEnumToValue() {

        val from = new EnumValueFrom();
        from.setIntEnumVal(IntValueEnum.ONE);
        from.setStrEnumVal(StrValueEnum.A);

        val to = CBeanUtils.copy(from, new EnumValueTo());

        Assertions.assertEquals(1, to.getIntEnumVal());
        Assertions.assertEquals("a", to.getStrEnumVal());
    }

    @Data
    public static class NumConvertFrom {

        private long longToInteger;

        private Long wrapperLongToInt;

        private Integer intToLong;

        private Integer intToPrimitiveLong;

    }

    @Data
    public static class NumConvertTo {

        private Integer longToInteger;

        private Integer wrapperLongToInt;

        private Long intToLong;

        private long intToPrimitiveLong;

    }

    /**
     * 测试数字互转（非同型数字走转换器）：
     * long/Long -> Integer（toInt(long)/toInt(Long)）、Integer -> Long（toLong(Integer)）、
     * Integer -> long（longValue(Integer)，包装与原始非等价故走转换器而非 SELF）
     * 对应测试用例 4.8
     */
    @Test
    public void copyNumConvert() {

        val from = new NumConvertFrom();
        from.setLongToInteger(100L);
        from.setWrapperLongToInt(200L);
        from.setIntToLong(300);
        from.setIntToPrimitiveLong(400);

        val to = CBeanUtils.copy(from, new NumConvertTo());

        Assertions.assertEquals(100, to.getLongToInteger());
        Assertions.assertEquals(200, to.getWrapperLongToInt());
        Assertions.assertEquals(300L, to.getIntToLong());
        Assertions.assertEquals(400L, to.getIntToPrimitiveLong());
    }

    @Data
    public static class StrToPrimitiveFloatDoubleFrom {

        private String floatVal;

        private String doubleVal;

    }

    @Data
    public static class StrToPrimitiveFloatDoubleTo {

        private float floatVal;

        private double doubleVal;

    }

    /**
     * 测试 String -> 原始 float/double（floatValue(String)/doubleValue(String) 转换器，
     * 与 String -> Float/Double 的 toFloat/toDouble 区分）
     * 对应测试用例 4.9
     */
    @Test
    public void copyStrToPrimitiveFloatDouble() {

        val from = new StrToPrimitiveFloatDoubleFrom();
        from.setFloatVal("1.5");
        from.setDoubleVal("2.5");

        val to = CBeanUtils.copy(from, new StrToPrimitiveFloatDoubleTo());

        Assertions.assertEquals(1.5f, to.getFloatVal());
        Assertions.assertEquals(2.5d, to.getDoubleVal());
    }

}
