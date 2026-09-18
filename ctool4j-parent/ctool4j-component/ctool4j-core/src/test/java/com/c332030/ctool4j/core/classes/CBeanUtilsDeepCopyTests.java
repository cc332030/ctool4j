package com.c332030.ctool4j.core.test.classes;

import com.c332030.ctool4j.core.classes.CBeanUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Description: CBeanUtilsDeepCopyTests
 * </p>
 *
 * <p>覆盖 {@code CBeanUtils.copy} 系列的深拷贝语义（不新增入口）：在既有标量复制之外，
 * 集合/Map/数组/Bean/Date 字段按深拷贝写入，且 Map（含 JSONObject 形态）来源同样生效。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>统一通过现有入口驱动：{@code copy(Object, To)}、{@code copy(Object, Class)}、{@code copy(Map, To)}、
 *   {@code copy(Map, Class)}、{@code copyList(Collection, Class)}，不引入新 API；</li>
 *   <li>「副本独立」用 {@code assertNotSame}，「无需拷贝」用 {@code assertSame}，容器实现选择直接断言实现类。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：标量/不可变、Date 防御性拷贝、嵌套 Bean、集合/Map/数组及其元素、Map 键、环状引用、
 *   同源实现与接口降级、comparator 保留、EnumMap/EnumSet、视图替换、raw 与泛型变量、接口字段、
 *   无无参构造降级、跨类型、Map（JSON）来源、null/空/空元素。</li>
 *   <li>未覆盖：record/无参构造缺失的不可变对象（实现明确降级为共享引用）、transient 语义。</li>
 * </ul>
 * <h2>用例</h2>
 * <ul>
 *   <li>1.1 标量与不可变值共享引用（immutableValues_shared）</li>
 *   <li>1.2 Date 防御性拷贝（date_defensiveCopy）</li>
 *   <li>2.1 嵌套 Bean 递归拷贝（bean_nestedDeepCopy）</li>
 *   <li>2.2 集合/Map 字段及其元素（collectionAndMap_elementsDeepCopied）</li>
 *   <li>2.3 数组（基本类型/对象/多维）（array_deepCopy）</li>
 *   <li>2.4 Map 键深拷贝（map_keysDeepCopied）</li>
 *   <li>3.1 自引用与双向引用保持环结构（cycle_preserved）</li>
 *   <li>4.1 同源实现优先（container_sameImplPreferred）</li>
 *   <li>4.2 接口降级为标准实现且保序（container_fallbackStandardImpl）</li>
 *   <li>4.3 TreeSet/TreeMap 保留 comparator（container_comparatorKept）</li>
 *   <li>4.4 EnumMap/EnumSet 重建（container_enumCollections）</li>
 *   <li>4.5 视图/不可变集合替换为可变实现（container_viewReplaced）</li>
 *   <li>5.1 raw 声明按运行时类型拷贝（generic_rawFallsBackToRuntimeType）</li>
 *   <li>5.2 泛型变量按元素运行时类型拷贝（generic_typeVariableFallsBackToRuntimeType）</li>
 *   <li>6.1 接口字段按运行时类型深拷贝（interfaceField_copiedAsRuntimeType）</li>
 *   <li>6.2 无无参构造的目标降级为共享引用（noDefaultCtor_sharedReference）</li>
 *   <li>6.3 跨类型拷贝（crossType_copy）</li>
 *   <li>6.4 Map（JSONObject 形态）来源深拷贝（mapSource_deepCopy）</li>
 *   <li>6.5 集合批量入口与边界（copyList_andEdge）</li>
 *   <li>6.6 Optional 内部值深拷贝、空 Optional 共享（optional_deepCopy）</li>
 *   <li>6.7 函数式接口（lambda）共享且不重复尝试实例化（functionalInterface_sharedAndCached）</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/17
 * @version 1.0
 */
class CBeanUtilsDeepCopyTests {

    /**
     * <p>对应测试用例 1.1：标量与不可变值共享引用（不拷贝）</p>
     */
    @Test
    void immutableValues_shared() {
        val bean = new ScalarBean();
        bean.setName("n");
        bean.setCount(3);
        bean.setAmount(new BigDecimal("1.50"));
        bean.setColor(Color.RED);
        bean.setUuid(UUID.randomUUID());

        val copy = CBeanUtils.copy(bean, new ScalarBean());

        Assertions.assertNotSame(bean, copy);
        Assertions.assertSame(bean.getName(), copy.getName());
        Assertions.assertSame(bean.getAmount(), copy.getAmount());
        Assertions.assertSame(bean.getColor(), copy.getColor());
        Assertions.assertSame(bean.getUuid(), copy.getUuid());
    }

    /**
     * <p>对应测试用例 1.2：Date 可变，做防御性拷贝</p>
     */
    @Test
    void date_defensiveCopy() {
        val bean = new ScalarBean();
        val date = new Date();
        bean.setDate(date);

        val copy = CBeanUtils.copy(bean, new ScalarBean());

        Assertions.assertNotSame(date, copy.getDate());
        Assertions.assertEquals(date, copy.getDate());
    }

    /**
     * <p>对应测试用例 2.1：嵌套 Bean 递归拷贝</p>
     */
    @Test
    void bean_nestedDeepCopy() {
        val outer = newOuter();

        val copy = CBeanUtils.copy(outer, new Outer());

        Assertions.assertNotSame(outer, copy);
        Assertions.assertEquals(outer.getId(), copy.getId());
        Assertions.assertNotSame(outer.getInner(), copy.getInner());
        Assertions.assertEquals(outer.getInner().getName(), copy.getInner().getName());
        Assertions.assertNotSame(outer.getInner().getTags(), copy.getInner().getTags());
    }

    /**
     * <p>对应测试用例 2.2：集合/Map 字段及其元素深拷贝</p>
     */
    @Test
    void collectionAndMap_elementsDeepCopied() {
        val outer = newOuter();

        val copy = CBeanUtils.copy(outer, new Outer());

        Assertions.assertNotSame(outer.getInners(), copy.getInners());
        Assertions.assertNotSame(outer.getInners().get(0), copy.getInners().get(0));
        Assertions.assertEquals(outer.getInners().get(0).getName(), copy.getInners().get(0).getName());

        Assertions.assertNotSame(outer.getMap(), copy.getMap());
        Assertions.assertNotSame(outer.getMap().get("k"), copy.getMap().get("k"));
        Assertions.assertEquals("inner", copy.getMap().get("k").getName());

        Assertions.assertNotSame(outer.getSet(), copy.getSet());
        Assertions.assertEquals(outer.getSet(), copy.getSet());
    }

    /**
     * <p>对应测试用例 2.3：数组深拷贝（基本类型/对象/多维）</p>
     */
    @Test
    void array_deepCopy() {
        val outer = newOuter();

        val copy = CBeanUtils.copy(outer, new Outer());

        Assertions.assertNotSame(outer.getNums(), copy.getNums());
        Assertions.assertArrayEquals(outer.getNums(), copy.getNums());

        Assertions.assertNotSame(outer.getArr(), copy.getArr());
        Assertions.assertNotSame(outer.getArr()[0], copy.getArr()[0]);

        Assertions.assertNotSame(outer.getGrid(), copy.getGrid());
        Assertions.assertNotSame(outer.getGrid()[0], copy.getGrid()[0]);
        Assertions.assertArrayEquals(outer.getGrid()[0], copy.getGrid()[0]);
    }

    /**
     * <p>对应测试用例 2.4：Map 键深拷贝</p>
     */
    @Test
    void map_keysDeepCopied() {
        val outer = new Outer();
        val key = new Inner();
        key.setName("key");
        outer.setByInner(Collections.singletonMap(key, "v"));

        val copy = CBeanUtils.copy(outer, new Outer());

        val copiedKey = copy.getByInner().keySet().iterator().next();
        Assertions.assertNotSame(key, copiedKey);
        Assertions.assertEquals(key.getName(), copiedKey.getName());
    }

    /**
     * <p>对应测试用例 3.1：自引用与双向引用保持环结构，且不栈溢出</p>
     */
    @Test
    void cycle_preserved() {
        val node = new Cyclic();
        node.setV("v");
        node.setOther(node);

        val copy = CBeanUtils.copy(node, new Cyclic());

        Assertions.assertNotSame(node, copy);
        Assertions.assertSame(copy, copy.getOther());

        val a = new Cyclic();
        val b = new Cyclic();
        a.setOther(b);
        b.setOther(a);

        val copyA = CBeanUtils.copy(a, new Cyclic());

        Assertions.assertNotSame(a, copyA);
        Assertions.assertNotSame(b, copyA.getOther());
        Assertions.assertSame(copyA, copyA.getOther().getOther());
    }

    /**
     * <p>对应测试用例 4.1：同源实现优先（LinkedList/ConcurrentHashMap）</p>
     */
    @Test
    void container_sameImplPreferred() {
        val holder = new ImplHolder();
        holder.setLinkedList(new LinkedList<>(Arrays.asList("a", "b")));
        holder.setMap(new ConcurrentHashMap<>(Collections.singletonMap("k", "v")));

        val copy = CBeanUtils.copy(holder, new ImplHolder());

        Assertions.assertEquals(LinkedList.class, copy.getLinkedList().getClass());
        Assertions.assertEquals(ConcurrentHashMap.class, copy.getMap().getClass());
        Assertions.assertNotSame(holder.getLinkedList(), copy.getLinkedList());
    }

    /**
     * <p>对应测试用例 4.2：接口降级为标准实现且保序</p>
     */
    @Test
    void container_fallbackStandardImpl() {
        val holder = new ListHolder();
        holder.setList(new ArrayList<>(Arrays.asList("b", "a")));
        holder.setSet(new LinkedHashSet<>(Arrays.asList("b", "a")));
        holder.setMap(new LinkedHashMap<>(Collections.singletonMap("k", "v")));

        val copy = CBeanUtils.copy(holder, new ListHolder());

        Assertions.assertEquals(ArrayList.class, copy.getList().getClass());
        Assertions.assertEquals(LinkedHashSet.class, copy.getSet().getClass());
        Assertions.assertEquals(LinkedHashMap.class, copy.getMap().getClass());
        Assertions.assertEquals(new ArrayList<>(holder.getSet()), new ArrayList<>(copy.getSet()));
    }

    /**
     * <p>对应测试用例 4.3：TreeSet/TreeMap 保留 comparator</p>
     */
    @Test
    void container_comparatorKept() {
        Comparator<String> comparator = Comparator.reverseOrder();
        val holder = new SortedHolder();
        val set = new TreeSet<String>(comparator);
        set.addAll(Arrays.asList("a", "b"));
        val map = new TreeMap<String, String>(comparator);
        map.put("a", "1");
        holder.setSet(set);
        holder.setMap(map);

        val copy = CBeanUtils.copy(holder, new SortedHolder());

        Assertions.assertSame(comparator, copy.getSet().comparator());
        Assertions.assertSame(comparator, copy.getMap().comparator());
        Assertions.assertEquals(set, copy.getSet());
    }

    /**
     * <p>对应测试用例 4.4：EnumMap/EnumSet 重建</p>
     */
    @Test
    void container_enumCollections() {
        val holder = new EnumHolder();
        EnumMap<Color, String> enumMap = new EnumMap<>(Color.class);
        enumMap.put(Color.RED, "red");
        holder.setEnumMap(enumMap);
        holder.setEnumSet(EnumSet.of(Color.RED, Color.BLUE));

        val copy = CBeanUtils.copy(holder, new EnumHolder());

        Assertions.assertEquals(EnumMap.class, copy.getEnumMap().getClass());
        Assertions.assertEquals("red", copy.getEnumMap().get(Color.RED));
        Assertions.assertNotSame(holder.getEnumMap(), copy.getEnumMap());
        Assertions.assertTrue(copy.getEnumSet().containsAll(holder.getEnumSet()));
        Assertions.assertNotSame(holder.getEnumSet(), copy.getEnumSet());
    }

    /**
     * <p>对应测试用例 4.5：视图/不可变集合替换为可变标准实现</p>
     */
    @Test
    void container_viewReplaced() {
        val holder = new ViewHolder();
        holder.setUnmodifiable(Collections.unmodifiableList(new ArrayList<>(Arrays.asList("a", "b"))));
        holder.setArraysList(Arrays.asList("a", "b"));
        holder.setEmpty(Collections.emptyList());

        val copy = CBeanUtils.copy(holder, new ViewHolder());

        Assertions.assertEquals(ArrayList.class, copy.getUnmodifiable().getClass());
        Assertions.assertEquals(ArrayList.class, copy.getArraysList().getClass());
        Assertions.assertEquals(ArrayList.class, copy.getEmpty().getClass());
        Assertions.assertTrue(copy.getEmpty().isEmpty());
        Assertions.assertDoesNotThrow(() -> copy.getUnmodifiable().add("c"));
        Assertions.assertDoesNotThrow(() -> copy.getArraysList().add("c"));
    }

    /**
     * <p>对应测试用例 5.1：raw 声明按元素运行时类型拷贝</p>
     */
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void generic_rawFallsBackToRuntimeType() {
        val holder = new RawHolder();
        val inner = new Inner();
        inner.setName("inner");
        holder.setItems(new ArrayList<>(Collections.singletonList(inner)));

        val copy = CBeanUtils.copy(holder, new RawHolder());

        Object copiedElement = copy.getItems().get(0);
        Assertions.assertNotSame(inner, copiedElement);
        Assertions.assertEquals(Inner.class, copiedElement.getClass());
        Assertions.assertEquals("inner", ((Inner) copiedElement).getName());
    }

    /**
     * <p>对应测试用例 5.2：泛型变量按元素运行时类型拷贝</p>
     */
    @Test
    void generic_typeVariableFallsBackToRuntimeType() {
        val inner = new Inner();
        inner.setName("inner");
        val page = new Page<Inner>();
        page.setItems(Collections.singletonList(inner));

        val copy = CBeanUtils.copy(page, new Page<Inner>());

        Assertions.assertNotSame(page, copy);
        Assertions.assertNotSame(inner, copy.getItems().get(0));
        Assertions.assertEquals(Inner.class, copy.getItems().get(0).getClass());
    }

    /**
     * <p>对应测试用例 6.1：接口字段按运行时类型深拷贝</p>
     */
    @Test
    void interfaceField_copiedAsRuntimeType() {
        val holder = new ShapeHolder();
        val circle = new Circle();
        circle.setR(3);
        holder.setShape(circle);

        val copy = CBeanUtils.copy(holder, new ShapeHolder());

        Assertions.assertNotSame(holder, copy);
        Assertions.assertNotSame(circle, copy.getShape());
        Assertions.assertEquals(Circle.class, copy.getShape().getClass());
        Assertions.assertEquals(3, ((Circle) copy.getShape()).getR());
    }

    /**
     * <p>对应测试用例 6.2：目标类无无参构造时降级为共享引用</p>
     */
    @Test
    void noDefaultCtor_sharedReference() {
        val holder = new NoCtorHolder();
        val bean = new NoDefaultCtor("v");
        holder.setBean(bean);

        val copy = CBeanUtils.copy(holder, new NoCtorHolder());

        Assertions.assertNotSame(holder, copy);
        Assertions.assertSame(bean, copy.getBean());
    }

    /**
     * <p>对应测试用例 6.3：跨类型拷贝（同名字段）</p>
     */
    @Test
    void crossType_copy() {
        val outer = newOuter();

        val vo = CBeanUtils.copy(outer, OuterVo.class);

        Assertions.assertNotSame(outer, vo);
        Assertions.assertEquals(outer.getId(), vo.getId());
        Assertions.assertNotSame(outer.getInner(), vo.getInner());
        Assertions.assertEquals(outer.getInner().getName(), vo.getInner().getName());
    }

    /**
     * <p>对应测试用例 6.4：Map（JSONObject 形态）来源深拷贝</p>
     */
    @Test
    void mapSource_deepCopy() {
        val inner = new Inner();
        inner.setName("inner");

        val map = new HashMap<String, Object>();
        map.put("id", "id");
        map.put("inners", new ArrayList<>(Collections.singletonList(inner)));

        val copy = CBeanUtils.copy(map, Outer.class);

        Assertions.assertEquals("id", copy.getId());
        Assertions.assertNotSame(map.get("inners"), copy.getInners());
        Assertions.assertNotSame(inner, copy.getInners().get(0));
        Assertions.assertEquals("inner", copy.getInners().get(0).getName());
    }

    /**
     * <p>对应测试用例 6.5：集合批量入口与边界（null/空集合/空元素）</p>
     */
    @Test
    void copyList_andEdge() {
        val inner = new Inner();
        inner.setName("inner");

        List<Inner> list = CBeanUtils.copyList(Collections.singletonList(inner), Inner.class);
        Assertions.assertNotSame(inner, list.get(0));
        Assertions.assertEquals("inner", list.get(0).getName());

        val empty = new ListHolder();
        empty.setList(new ArrayList<>());
        val copyEmpty = CBeanUtils.copy(empty, new ListHolder());
        Assertions.assertTrue(copyEmpty.getList().isEmpty());
        Assertions.assertNotSame(empty.getList(), copyEmpty.getList());

        val withNull = new ListHolder();
        withNull.setList(new ArrayList<>(Collections.singletonList(null)));
        ListHolder copyNull = CBeanUtils.copy(withNull, new ListHolder());
        Assertions.assertEquals(1, copyNull.getList().size());
        Assertions.assertNull(copyNull.getList().get(0));
    }

    /**
     * <p>对应测试用例 6.6：{@code Optional} 自身不可变但内部值可能可变 ⇒ 拷贝内部值；空 Optional 共享</p>
     */
    @Test
    void optional_deepCopy() {

        val inner = new Inner();
        inner.setName("inner");

        val holder = new OptionalHolder();
        holder.setOptional(Optional.of(inner));
        holder.setEmpty(Optional.empty());

        val copy = CBeanUtils.copy(holder, new OptionalHolder());

        Assertions.assertTrue(copy.getOptional().isPresent());
        Assertions.assertNotSame(holder.getOptional(), copy.getOptional());
        Assertions.assertNotSame(inner, copy.getOptional().get(), "Optional 内部可变值应深拷贝");
        Assertions.assertEquals("inner", copy.getOptional().get().getName());
        Assertions.assertSame(holder.getEmpty(), copy.getEmpty(), "空 Optional 无内部值可拷，按共享处理");
    }

    /**
     * <p>对应测试用例 6.7：函数式接口（lambda）按不可拷贝处理 ⇒ 共享引用，且不重复反射尝试实例化</p>
     *
     * <p>lambda 的运行期类由 {@code LambdaMetafactory} 生成（类名含 {@code $$Lambda$}，承载行为而非数据），
     * 实例化它得不到"同样的数据副本"，故按不可拷贝处理（判定按类缓存，多次复制不重复判断）。</p>
     */
    @Test
    void functionalInterface_sharedAndCached() {

        val runner = (Runner) () -> "run";
        val holder = new FunctionalHolder();
        holder.setRunner(runner);

        val first = CBeanUtils.copy(holder, new FunctionalHolder());
        val second = CBeanUtils.copy(holder, new FunctionalHolder());

        Assertions.assertSame(runner, first.getRunner(), "函数式接口值应共享引用");
        Assertions.assertSame(runner, second.getRunner(), "重复复制同样共享（判定已按类缓存）");
        Assertions.assertEquals("run", second.getRunner().run());
    }

    private static Outer newOuter() {
        val inner = new Inner();
        inner.setName("inner");
        inner.setTags(new ArrayList<>(Arrays.asList("t1", "t2")));

        val outer = new Outer();
        outer.setId("id");
        outer.setNums(new int[] {1, 2, 3});
        outer.setGrid(new int[][] {{1, 2}, {3, 4}});
        outer.setInner(inner);
        outer.setInners(new ArrayList<>(Collections.singletonList(inner)));
        outer.setArr(new Inner[] {inner});
        outer.setSet(new LinkedHashSet<>(Collections.singletonList("s")));
        outer.setMap(new HashMap<>(Collections.singletonMap("k", inner)));
        outer.setAmount(new BigDecimal("2.00"));
        outer.setDate(new Date());
        return outer;
    }

    /**
     * 标量/不可变字段
     */
    @Data
    static class ScalarBean {

        private String name;
        private Integer count;
        private BigDecimal amount;
        private Color color;
        private UUID uuid;
        private Date date;
    }

    /**
     * 内层 Bean
     */
    @Data
    static class Inner {

        private String name;
        private List<String> tags;
    }

    /**
     * 综合字段
     */
    @Data
    static class Outer {

        private String id;
        private int[] nums;
        private int[][] grid;
        private Inner inner;
        private List<Inner> inners;
        private Inner[] arr;
        private Set<String> set;
        private Map<String, Inner> map;
        private Map<Inner, String> byInner;
        private BigDecimal amount;
        private Date date;
    }

    /**
     * 跨类型目标（同名字段子集）
     */
    @Data
    static class OuterVo {

        private String id;
        private Inner inner;
    }

    /**
     * 环状引用
     */
    @Data
    @ToString(exclude = "other")
    @EqualsAndHashCode(exclude = "other")
    static class Cyclic {

        private String v;
        private Cyclic other;
    }

    /**
     * 容器实现
     */
    @Data
    static class ImplHolder {

        private LinkedList<String> linkedList;
        private ConcurrentHashMap<String, String> map;
    }

    /**
     * 接口声明
     */
    @Data
    static class ListHolder {

        private List<String> list;
        private Set<String> set;
        private Map<String, String> map;
    }

    /**
     * 有序容器
     */
    @Data
    static class SortedHolder {

        private TreeSet<String> set;
        private TreeMap<String, String> map;
    }

    /**
     * 枚举容器
     */
    @Data
    static class EnumHolder {

        private EnumMap<Color, String> enumMap;
        private EnumSet<Color> enumSet;
    }

    /**
     * 视图/不可变集合
     */
    @Data
    static class ViewHolder {

        private List<String> unmodifiable;
        private List<String> arraysList;
        private List<String> empty;
    }

    /**
     * raw 声明
     */
    @Data
    static class RawHolder {

        @SuppressWarnings("rawtypes")
        private List items;
    }

    /**
     * 泛型变量声明
     *
     * @param <T> 元素类型
     */
    @Data
    static class Page<T> {

        private List<T> items;
    }

    /**
     * 接口字段
     */
    @Data
    static class ShapeHolder {

        private Shape shape;
    }

    /**
     * 形状接口（声明类型不可实例化，实现类可）
     */
    interface Shape {

    }

    /**
     * 形状实现
     */
    @Data
    static class Circle implements Shape {

        private int r;
    }

    /**
     * 仅带参构造（无无参构造，无法反射实例化）
     */
    static class NoDefaultCtor {

        private final String v;

        NoDefaultCtor(String v) {
            this.v = v;
        }
    }

    /**
     * 无无参构造字段的持有者
     */
    @Data
    static class NoCtorHolder {

        private NoDefaultCtor bean;
    }

    /**
     * Queue 声明（ArrayDeque 实现）
     */
    @Data
    static class QueueHolder {

        private ArrayDeque<String> deque;
    }

    /**
     * Optional 字段（含内部可变值/空值两种形态）
     */
    @Data
    static class OptionalHolder {

        private Optional<Inner> optional;

        private Optional<Inner> empty;
    }

    /**
     * 函数式接口字段（lambda 的运行期类无数据语义、不可结构拷贝）
     */
    @Data
    static class FunctionalHolder {

        private Runner runner;
    }

    /**
     * 函数式接口
     */
    @FunctionalInterface
    interface Runner {

        String run();
    }

    /**
     * 枚举
     */
    enum Color {

        RED, BLUE
    }

}
