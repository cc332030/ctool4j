package com.c332030.ctool4j.core.classes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
 *   <li>覆盖：标量/不可变、Date/Calendar 防御性拷贝、嵌套 Bean、集合/Map/数组及其元素、Map 键、环状引用、
 *   跨字段共享引用去重、深度上限降级、同源实现与接口降级、comparator 保留、EnumMap/EnumSet、视图替换、
 *   raw 与泛型变量、接口字段、无无参构造降级、跨类型、Map（JSON）来源、null/空/空元素、
 *   Optional（含原始值包装的 OptionalInt/Long/Double）、身份表按需创建（分配层面）。</li>
 *   <li>覆盖（容器形态）：目标声明类型为容器接口/具体实现类时的按目标重建（7.2 跨接口、7.3 兼容保持、
 *   7.4 父类→子类实现类与具体有序实现）、一次性视图的重建（7.5 迭代器/集合视图/流）、
 *   不可实例化目标类的登记与跳过（7.6）、数组↔容器跨形态转换（7.7）、{@code Map.Entry} 物化（7.8）、
 *   {@code java.sql} 时间子类保真（7.9）、{@code Optional} 拆包（7.10）、跨容器族声明的跳过（7.11）、
 *   按目标类折叠的深拷贝计划的降级分支优先级（7.12）。</li>
 *   <li>未覆盖：record（无可用无参构造、含 final 字段，实现明确降级为共享引用）、transient 语义。</li>
 * </ul>
 * <h2>用例</h2>
 * <ul>
 *   <li>1.1 标量与不可变值共享引用（immutableValues_shared）</li>
 *   <li>1.2 Date 防御性拷贝（date_defensiveCopy）</li>
 *   <li>1.3 Calendar 防御性拷贝（calendar_defensiveCopy）</li>
 *   <li>2.1 嵌套 Bean 递归拷贝（bean_nestedDeepCopy）</li>
 *   <li>2.2 集合/Map 字段及其元素（collectionAndMap_elementsDeepCopied）</li>
 *   <li>2.3 数组（基本类型/对象/多维）（array_deepCopy）</li>
 *   <li>2.4 Map 键深拷贝（map_keysDeepCopied）</li>
 *   <li>2.5 容器元素为 JDK 非拷贝协议 ⇒ 元素共享（collection_jdkNonCopyableElements_shared）</li>
 *   <li>3.1 自引用与双向引用保持环结构（cycle_preserved）</li>
 *   <li>3.2 跨字段共享同一实例只拷一份（sharedReference_singleCopy）</li>
 *   <li>3.3 超过深度上限的层原样返回引用（deepCopyDepthLimit_returnsReference）</li>
 *   <li>4.1 同源实现优先（container_sameImplPreferred）</li>
 *   <li>4.2 接口降级为标准实现且保序（container_fallbackStandardImpl）</li>
 *   <li>4.3 TreeSet/TreeMap 保留 comparator（container_comparatorKept）</li>
 *   <li>4.4 EnumMap/EnumSet 重建（container_enumCollections）</li>
 *   <li>4.5 视图/不可变集合替换为可变实现（container_viewReplaced）</li>
 *   <li>4.6 非标准容器（ArrayDeque）同源实现深拷贝（container_queueFallbackArrayDeque）</li>
 *   <li>4.7 空 EnumMap 仍保持 EnumMap 实现（container_emptyEnumMap_copied）</li>
 *   <li>5.1 raw 声明按运行时类型拷贝（generic_rawFallsBackToRuntimeType）</li>
 *   <li>5.2 泛型变量按元素运行时类型拷贝（generic_typeVariableFallsBackToRuntimeType）</li>
 *   <li>6.1 接口字段按运行时类型深拷贝（interfaceField_copiedAsRuntimeType）</li>
 *   <li>6.2 无无参构造的目标降级为共享引用（noDefaultCtor_sharedReference）</li>
 *   <li>6.3 跨类型拷贝（crossType_copy）</li>
 *   <li>6.4 Map（JSONObject 形态）来源深拷贝（mapSource_deepCopy）</li>
 *   <li>6.5 集合批量入口与边界（copyList_andEdge）</li>
 *   <li>6.6 Optional 内部值深拷贝、空 Optional 共享（optional_deepCopy）</li>
 *   <li>6.7 函数式接口（lambda）共享且不重复尝试实例化（functionalInterface_sharedAndCached）</li>
 *   <li>6.8 含 final 实例字段的目标降级为共享引用（finalField_sharedReference）</li>
 *   <li>6.9 OptionalInt/OptionalLong/OptionalDouble 只包原始值 ⇒ 共享（optionalPrimitive_shared）</li>
 *   <li>7.1 身份表按需创建的语义等价性：标量 DTO、深拷贝字段全为 null 时结果正确（identityMap_createdOnDemand）</li>
 *   <li>7.2 跨接口容器按目标声明类型重建（crossInterfaceContainer_rebuiltByTargetDeclaration，含 List/Collection 声明对照）</li>
 *   <li>7.3 兼容的容器目标按源接口形态降级且可写回（containerWriteBack_compatibleKept）</li>
 *   <li>7.4 父类向子类实现类转换：目标声明比源更具体时按目标类型重建（container_concreteTargetImplRebuilt）</li>
 *   <li>7.5 迭代器/集合视图/流按目标声明类型重建（iteratorAndStream_rebuiltByTargetDeclaration）</li>
 *   <li>7.6 有无参构造但构造体抛异常的目标类：登记「不支持的目标类集合」、只尝试一次（uninstantiableByThrowingCtor_registeredOnceAndSkipped）</li>
 *   <li>7.7 数组 ↔ 容器按目标声明跨形态转换（arrayAndContainer_convertedByTargetDeclaration）</li>
 *   <li>7.8 {@code Map.Entry} 按目标声明物化（Map / 容器两向）（mapEntry_materializedByTargetDeclaration）</li>
 *   <li>7.9 {@code java.sql} 时间子类按目标类型保真（sqlDateSubclasses_keptByTargetType）</li>
 *   <li>7.10 {@code Optional} 拆包写入非 Optional 声明的目标字段（optional_unwrappedForNonOptionalTarget）</li>
 *   <li>7.11 跨容器族声明（源集合 → 目标 {@code Map} 声明）整条跳过不写入、且不抛异常
 *   （crossFamilyContainer_skippedWithoutCasting）</li>
 *   <li>7.12 按目标类折叠的深拷贝计划：含 final 字段优先于不可实例化（final 优先共享、
 *   不因折叠而改变降级分支）（deepPlan_finalFieldTakesPrecedenceOverUninstantiable）</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/17
 * @version 1.3
 * @see CBeanUtils
 * @see CBeanUtilsTests
 * @see CBeanUtilsCopyContractTests
 * @see CBeanUtilsMoreTests
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

        val list = CBeanUtils.copyList(Collections.singletonList(inner), Inner.class);
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

    /**
     * <p>对应测试用例 6.8：目标类含 final 实例字段 ⇒ 无法完整结构拷贝，降级为共享引用</p>
     *
     * <p>既有 copy 契约不写 final 字段，若强行结构拷贝只会得到"部分字段为空"的对象（静默丢数据），
     * 故按类判定后整体退化为共享引用（判定按类缓存、只打印一次 debug）。</p>
     */
    @Test
    void finalField_sharedReference() {

        val holder = new FinalFieldHolder();
        holder.setInner(new FinalFieldInner());

        val copy = CBeanUtils.copy(holder, new FinalFieldHolder());

        Assertions.assertNotSame(holder, copy);
        Assertions.assertSame(
                holder.getInner(), copy.getInner(),
                "目标类含 final 字段无法完整拷贝 ⇒ 共享引用（不产出半空对象）"
        );
    }

    /**
     * <p>对应测试用例 1.3：{@code Calendar} 可变，做防御性拷贝（与 Date 同一原则）</p>
     */
    @Test
    void calendar_defensiveCopy() {
        val holder = new CalendarHolder();
        val calendar = Calendar.getInstance();
        calendar.setTimeInMillis(1700000000000L);
        holder.setCalendar(calendar);

        val copy = CBeanUtils.copy(holder, new CalendarHolder());

        Assertions.assertNotSame(calendar, copy.getCalendar());
        Assertions.assertEquals(calendar.getTimeInMillis(), copy.getCalendar().getTimeInMillis());

        copy.getCalendar().setTimeInMillis(0L);
        Assertions.assertEquals(1700000000000L, calendar.getTimeInMillis(), "副本变更不应影响源");
    }

    /**
     * <p>对应测试用例 3.2：跨字段共享同一实例 ⇒ 只拷一份（身份表去重），副本内部保持共享结构</p>
     *
     * <p>{@link #newOuter()} 中同一个 {@code inner} 实例被 {@code inner}、{@code inners[0]}、
     * {@code arr[0]}、{@code map["k"]} 同时引用，复制后应全部指向同一个副本。</p>
     */
    @Test
    void sharedReference_singleCopy() {
        val outer = newOuter();

        val copy = CBeanUtils.copy(outer, new Outer());

        Assertions.assertNotSame(outer.getInner(), copy.getInner());
        Assertions.assertSame(copy.getInner(), copy.getInners().get(0), "集合元素应复用字段的同一副本");
        Assertions.assertSame(copy.getInner(), copy.getArr()[0], "数组元素应复用字段的同一副本");
        Assertions.assertSame(copy.getInner(), copy.getMap().get("k"), "Map 值应复用字段的同一副本");
    }

    /**
     * <p>对应测试用例 3.3：超过深度上限（64）的层原样返回源引用，且不抛错、不栈溢出</p>
     */
    @Test
    void deepCopyDepthLimit_returnsReference() {
        val head = new Cyclic();
        head.setV("0");
        Cyclic tail = head;
        for (int i = 1; i <= 80; i++) {
            val next = new Cyclic();
            next.setV(String.valueOf(i));
            tail.setOther(next);
            tail = next;
        }

        val copy = Assertions.assertDoesNotThrow(() -> CBeanUtils.copy(head, new Cyclic()));

        Assertions.assertNotSame(head, copy);
        Assertions.assertEquals("0", copy.getV());

        // 沿两条链同步前进，直到副本链上的节点与源链节点为同一实例（即超限层原样返回）
        Cyclic copied = copy;
        Cyclic source = head;
        int depth = 0;
        while (null != copied && null != source && copied != source) {
            copied = copied.getOther();
            source = source.getOther();
            depth++;
        }
        Assertions.assertSame(source, copied, "超过深度上限的层应原样返回源引用");
        Assertions.assertTrue(depth <= 65, "深度上限应在 64 层附近生效，实际共享于第 " + depth + " 层");
    }

    /**
     * <p>对应测试用例 6.9：{@code OptionalInt/OptionalLong/OptionalDouble} 只包装原始值 ⇒ 共享引用</p>
     */
    @Test
    void optionalPrimitive_shared() {
        val holder = new PrimitiveOptionalHolder();
        val oi = OptionalInt.of(1);
        val ol = OptionalLong.of(2L);
        val od = OptionalDouble.of(3D);
        holder.setOi(oi);
        holder.setOl(ol);
        holder.setOd(od);

        val copy = CBeanUtils.copy(holder, new PrimitiveOptionalHolder());

        Assertions.assertSame(oi, copy.getOi());
        Assertions.assertSame(ol, copy.getOl());
        Assertions.assertSame(od, copy.getOd());
    }

    /**
     * <p>对应测试用例 7.1：身份表按需创建的语义等价性——标量 DTO、深拷贝字段全为 null 时结果正确</p>
     *
     * <p>本用例只断言<b>语义结果</b>（不可变值共享、null 字段保持 null、不抛错）；「身份表是否按需创建」
     * 属分配行为、无法低成本从外部观测，由实现结构保证（{@code copyWithPlan} 中共享分支与 null 值
     * 在创建身份表前即 {@code continue}），不在此断言。环引用/共享引用用例覆盖身份表照常创建的场景。</p>
     */
    @Test
    void identityMap_createdOnDemand() {
        val scalar = new ScalarBean();
        scalar.setName("n");
        scalar.setCount(1);

        val scalarCopy = CBeanUtils.copy(scalar, new ScalarBean());
        Assertions.assertEquals("n", scalarCopy.getName());
        Assertions.assertEquals(1, scalarCopy.getCount());

        val holder = new Outer();
        holder.setId("id");

        val holderCopy = CBeanUtils.copy(holder, new Outer());
        Assertions.assertEquals("id", holderCopy.getId());
        Assertions.assertNull(holderCopy.getInner());
        Assertions.assertNull(holderCopy.getInners());
        Assertions.assertNull(holderCopy.getArr());
        Assertions.assertNull(holderCopy.getMap());
        Assertions.assertNull(holderCopy.getNums());
    }

    /**
     * <p>对应测试用例 7.2：跨接口容器按<b>目标声明类型</b>重建（源 List → 目标 Set 声明）</p>
     *
     * <p>回归点（原实现为"跳过不写入"，此处改为受支持并断言写回成功）：拷贝结果曾按源实现类创建，
     * 写回目标字段声明为另一容器接口（如源 {@code List}、目标 {@code Set}）时必然 {@code ClassCastException}，
     * 该异常被吞成 debug"字段写入失败，跳过"，表现为"字段静默为空 + 每次多一次注定失败的拷贝"。
     * 现改为<b>按目标声明类型重建容器</b>：源 {@code List} → 目标 {@code Set} 声明时建 {@code LinkedHashSet}，
     * 写回目标接口天然成立、元素照常深拷贝（有序优先，见 {@code containerFor}）。</p>
     *
     * <p>断言口径：①容器与元素都是副本（{@code assertNotSame}）；②结果为 {@code Set} 且元素等价源元素；
     * ③对照项——目标声明 {@code List} 时仍是 {@code List}、目标声明 {@code Collection} 时按目标族建 {@code ArrayList}。</p>
     */
    @Test
    void crossInterfaceContainer_rebuiltByTargetDeclaration() {

        val inner = new Inner();
        inner.setName("inner");

        val source = new MismatchSourceHolder();
        source.setItems(new ArrayList<>(Collections.singletonList(inner)));

        // 对照：目标声明 List<Inner> 与源同族 ⇒ 正常深拷贝（容器与元素都是副本）
        val copied = CBeanUtils.copy(source, MismatchListTargetHolder.class);
        Assertions.assertNotSame(source.getItems(), copied.getItems());
        Assertions.assertNotSame(source.getItems().get(0), copied.getItems().get(0));

        // 目标声明 Set、源为 List ⇒ 按目标声明类型重建（LinkedHashSet），不再是"跳过不写入"
        val mismatched = CBeanUtils.copy(source, MismatchSetTargetHolder.class);
        Assertions.assertNotNull(mismatched.getItems(), "跨接口容器应按目标声明类型重建、不再跳过不写入");
        Assertions.assertTrue(
                mismatched.getItems() instanceof LinkedHashSet,
                "目标声明为 Set（接口）时应重建为有序的 LinkedHashSet，实际："
                        + mismatched.getItems().getClass().getName()
        );
        Assertions.assertNotSame(source.getItems(), mismatched.getItems());
        Assertions.assertEquals(1, mismatched.getItems().size());
        val copiedInner = mismatched.getItems().iterator().next();
        Assertions.assertNotSame(inner, copiedInner, "元素也应是深拷贝副本");
        Assertions.assertEquals("inner", copiedInner.getName());

        // 目标声明 Collection、源为 List ⇒ 按目标族重建为 ArrayList
        val collectionTarget = CBeanUtils.copy(source, MismatchCollectionTargetHolder.class);
        Assertions.assertNotNull(collectionTarget.getItems());
        Assertions.assertTrue(
                collectionTarget.getItems() instanceof ArrayList,
                "目标声明为 Collection 时应重建为 ArrayList，实际："
                        + collectionTarget.getItems().getClass().getName()
        );
    }

    /**
     * <p>对应测试用例 7.4：父类向子类实现类转换（目标声明为源容器族的更具体实现）</p>
     *
     * <p>覆盖（与 7.2 同源漏点）：目标声明为<b>具体实现类</b>且比源更具体时（{@code ArrayList} 源 →
     * {@code LinkedList} 目标），按目标声明类型重建该实现——旧实现用源实现类兜底、或用目标类重建却
     * 不校验，两者都会与"具体看目标属性的类型"的语义不符；此处断言结果就是目标声明的那一个实现。
     * 同时覆盖 {@code Queue} 声明（建 {@code ArrayDeque}）与具体有序实现（{@code TreeSet} 保目标语义）。</p>
     */
    @Test
    void container_concreteTargetImplRebuilt() {

        val source = new ConcreteSourceHolder();
        source.setList(new ArrayList<>(Arrays.asList("a", "b")));
        source.setQueue(new ArrayList<>(Collections.singletonList("q")));
        source.setSet(new HashSet<>(Collections.singletonList("s")));

        ConcreteTargetHolder copied = CBeanUtils.copy(source, ConcreteTargetHolder.class);

        // ArrayList 源 → LinkedList 目标声明：按目标声明类型重建
        Assertions.assertTrue(
                copied.getList() instanceof LinkedList,
                "目标声明为 LinkedList 时应按目标类型重建，实际：" + copied.getList().getClass().getName()
        );
        Assertions.assertEquals(Arrays.asList("a", "b"), new ArrayList<>(copied.getList()));
        Assertions.assertNotSame(source.getList(), copied.getList());

        // ArrayList 源 → Queue 目标声明：按目标接口族建 ArrayDeque
        Assertions.assertTrue(
                copied.getQueue() instanceof ArrayDeque,
                "目标声明为 Queue 时应重建为 ArrayDeque，实际：" + copied.getQueue().getClass().getName()
        );
        Assertions.assertEquals(Collections.singletonList("q"), new ArrayList<>(copied.getQueue()));

        // HashSet 源 → TreeSet 目标声明：按目标声明重建（目标的有序语义优先于源的插入序）
        Assertions.assertTrue(
                copied.getSet() instanceof TreeSet,
                "目标声明为 TreeSet 时应按目标类型重建，实际：" + copied.getSet().getClass().getName()
        );
        Assertions.assertEquals(Collections.singletonList("s"), new ArrayList<>(copied.getSet()));
    }

    /**
     * <p>对应测试用例 7.5：迭代器（{@code Iterator}）与原始流（{@code Stream}/{@code IntStream}）按目标声明类型重建</p>
     *
     * <p>回归点（本类同源漏点）：{@code Iterator}、集合视图（{@code keySet()}/{@code values()}/{@code entrySet()}）
     * 与 {@code java.util.stream.*} 都是"无无参构造、非集合"的一次性视图，旧实现把它们当"不可实例化 Bean"
     * ⇒ <b>静默共享引用</b>（共享出去的是一个对齐到源集合状态、不可复用的视图，既非副本也不可复用）。
     * 现改为：按<b>迭代顺序</b>物化后按目标声明类型重建标准容器。</p>
     *
     * <p>断言口径：①迭代器 → {@code List} 声明得到 {@code ArrayList}、元素已取出、源被消费；
     * ②对象流 → {@code Set} 声明得到 {@code LinkedHashSet}；③原始流 → {@code List} 声明得到
     * {@code ArrayList} 且元素为包装类型（自动装箱）。</p>
     */
    @Test
    void iteratorAndStream_rebuiltByTargetDeclaration() {

        val source = new ViewSourceHolder();
        source.setFromIterator(new ArrayList<>(Arrays.asList("i1", "i2")).iterator());
        source.setFromKeySet(new LinkedHashMap<String, String>() {{ put("k", "v"); }}.keySet());
        source.setFromStream(Stream.of("s1", "s2"));
        source.setFromIntStream(IntStream.of(1, 2));

        ViewTargetHolder copied = CBeanUtils.copy(source, ViewTargetHolder.class);

        // 迭代器 → List 声明：物化并按目标族重建（不是共享那个一次性迭代器）
        Assertions.assertTrue(
                copied.getFromIterator() instanceof ArrayList,
                "迭代器应按目标声明重建为 ArrayList，实际：" + copied.getFromIterator().getClass().getName()
        );
        Assertions.assertEquals(Arrays.asList("i1", "i2"), copied.getFromIterator());

        // 集合视图 → Set 声明：重建为有序 Set（源 keySet 不是 Set 副本）
        Assertions.assertTrue(
                copied.getFromKeySet() instanceof LinkedHashSet,
                "集合视图应按目标声明重建为 LinkedHashSet，实际：" + copied.getFromKeySet().getClass().getName()
        );
        Assertions.assertEquals(new LinkedHashSet<>(Collections.singletonList("k")), copied.getFromKeySet());

        // 对象流 → List 声明
        Assertions.assertTrue(copied.getFromStream() instanceof ArrayList);
        Assertions.assertEquals(Arrays.asList("s1", "s2"), copied.getFromStream());

        // 原始流 → List 声明（元素自动装箱）
        Assertions.assertTrue(copied.getFromIntStream() instanceof ArrayList);
        Assertions.assertEquals(Arrays.asList(1, 2), copied.getFromIntStream());
    }

    /**
     * <p>对应测试用例 7.6：有无参构造、但构造体必定抛异常的目标类 ⇒ 登记「不支持的目标类集合」并跳过</p>
     *
     * <p>回归点（用户指出的第二类不可实例化）：{@code checkInstantiable} 只能在准备阶段判"有无无参构造"，
     * 判不出"构造体必定抛异常"（如内部 {@code throw new UnsupportedOperationException()}）——
     * 旧实现每次深拷贝到该类型都要重新反射构造、重新抛异常、重新打日志（热点路径上退化成"每次一个异常"）。
     * 现改为：首次实例化失败即<b>登记进不支持的目标类集合</b>（打一次 error 日志），其后该类型一律跳过。</p>
     *
     * <p>断言口径：①首次拷贝该类型字段被跳过（保持目标对象既有值、不共享、不抛错）；
     * ②重复拷贝仍跳过且行为一致（幂等，不因登记状态改变结果）；③<b>其他类再碰到该类型也直接跳过</b>
     * （跨源类复用同一集合）——这正是用户要的"下一次其他的类碰到这个类，就可以跳过"；
     * ④由 {@code throwingCtorInvocationCount} 断言<b>只尝试实例化一次</b>（不是每次一个异常）。</p>
     */
    @Test
    void uninstantiableByThrowingCtor_registeredOnceAndSkipped() {

        ThrowingCtorTarget.resetInvocationCount();

        // 首次：源值本身可构造（用不受构造异常影响的同类型替代）——
        // 目标字段声明为 ThrowingCtorTarget，深拷贝按声明类型实例化时失败 ⇒ 登记，字段跳过（保持既有值）
        ThrowingCtorSource first = new ThrowingCtorSource();
        first.setTarget(ThrowingCtorTarget.sample());
        ThrowingCtorSource firstCopied = CBeanUtils.copy(first, new ThrowingCtorSource());
        Assertions.assertNull(firstCopied.getTarget(), "不可实例化的目标类字段应跳过不写入");
        Assertions.assertEquals(1, ThrowingCtorTarget.invocationCount(), "首次应尝试实例化一次");

        // 重复：仍跳过，且不再尝试实例化（命中不支持集合后直接返回）
        ThrowingCtorSource secondCopied = CBeanUtils.copy(first, new ThrowingCtorSource());
        Assertions.assertNull(secondCopied.getTarget());
        Assertions.assertEquals(1, ThrowingCtorTarget.invocationCount(), "登记后不应再次尝试实例化");

        // 其他源类碰到同一目标类型：同样直接跳过（跨源类复用「不支持的目标类集合」）
        OtherThrowingCtorSource other = new OtherThrowingCtorSource();
        other.setTarget(ThrowingCtorTarget.sample());
        OtherThrowingCtorSource otherCopied = CBeanUtils.copy(other, new OtherThrowingCtorSource());
        Assertions.assertNull(otherCopied.getTarget(), "其他类碰到该类型也应直接跳过");
        Assertions.assertEquals(1, ThrowingCtorTarget.invocationCount(), "其他类不应再尝试实例化");

        // Map 源入口共用同一决议链，同样跳过
        val mapSource = new LinkedHashMap<String, Object>();
        mapSource.put("target", ThrowingCtorTarget.sample());
        ThrowingCtorSource fromMap = CBeanUtils.copy(mapSource, new ThrowingCtorSource());
        Assertions.assertNull(fromMap.getTarget(), "Map 源入口同样跳过");
        Assertions.assertEquals(1, ThrowingCtorTarget.invocationCount());
    }

    /**
     * <p>对应测试用例 7.7：数组 ↔ 容器的跨形态转换（两向都不再"跳过不写入"）</p>
     *
     * <p>回归点（本次补全的漏点）：数组与集合在"有序、按元素遍历"这一语义上同构，
     * 但旧实现里两侧都按"目标声明装不下源实现"处理 ⇒ <b>整条跳过</b>（字段保持 null，静默丢数据）：</p>
     * <ul>
     *   <li>源数组 → 目标 {@code List}/{@code Set} 声明：数组写不回集合，写回时 {@code ClassCastException}；</li>
     *   <li>源集合 → 目标数组声明：集合写不回数组，同样失败。</li>
     * </ul>
     *
     * <p>断言口径：①{@code String[]} → {@code List<String>} 得到 {@code ArrayList} 且元素深拷贝；
     * ②{@code Inner[]} → {@code Set<Inner>} 得到 {@code LinkedHashSet}；③{@code Set<Inner>} →
     * {@code Inner[]} 得到目标组件类型的数组、元素为副本；④{@code int[]} → {@code List<Integer>}
     * 元素自动装箱；⑤{@code Object[]} 含不可转换元素（{@code Integer}）而目标为 {@code String[]} 时
     * <b>跳过该槽位而非整体失败</b>。</p>
     */
    @Test
    void arrayAndContainer_convertedByTargetDeclaration() {

        val source = new ArraySourceHolder();
        source.setStrings(new String[]{"a", "b"});
        source.setBeans(new ArraySourceHolder.Inner[]{new ArraySourceHolder.Inner("x")});
        source.setBeansAsCollection(new LinkedHashSet<>(Arrays.asList(new ArraySourceHolder.Inner("y"))));
        source.setInts(new int[]{1, 2});

        val copied = CBeanUtils.copy(source, ArrayTargetHolder.class);

        // ① 数组 → List 声明
        Assertions.assertTrue(copied.getStrings() instanceof ArrayList, "String[] 应按目标声明重建为 ArrayList");
        Assertions.assertEquals(Arrays.asList("a", "b"), copied.getStrings());

        // ② 数组 → Set 声明
        Assertions.assertTrue(copied.getBeans() instanceof LinkedHashSet, "Inner[] 应按目标声明重建为 LinkedHashSet");
        Assertions.assertEquals(1, copied.getBeans().size());
        Assertions.assertNotSame(source.getBeans()[0], copied.getBeans().iterator().next(), "元素应为副本");

        // ③ 集合 → 数组声明（组件类型取自声明，元素为副本）
        Assertions.assertNotNull(copied.getBeansAsCollection(), "集合应能写入数组声明字段（不再跳过）");
        Assertions.assertEquals(ArraySourceHolder.Inner[].class, copied.getBeansAsCollection().getClass());
        Assertions.assertEquals(1, copied.getBeansAsCollection().length);
        Assertions.assertNotSame(
                source.getBeansAsCollection().iterator().next(), copied.getBeansAsCollection()[0], "元素应为副本"
        );

        // ④ 基本类型数组 → 容器声明（自动装箱）
        Assertions.assertEquals(Arrays.asList(1, 2), copied.getInts());

        // ⑤ 组件类型不兼容：跳过该槽位、不整体失败
        val mismatch = new ObjectArraySourceHolder();
        mismatch.setValues(new Object[]{"ok", 1});
        val mismatchCopied = CBeanUtils.copy(mismatch, StringArrayTargetHolder.class);
        Assertions.assertNotNull(mismatchCopied.getValues(), "组件类型不匹配不应导致整条跳过");
        Assertions.assertEquals("ok", mismatchCopied.getValues()[0]);
        Assertions.assertNull(mismatchCopied.getValues()[1], "装不进目标组件类型的槽位应跳过（保持默认值）");
    }

    /**
     * <p>对应测试用例 7.8：{@code Map.Entry} 按目标声明类型物化（Map / 容器 两向）</p>
     *
     * <p>回归点（本次补全的漏点）：{@code AbstractMap.SimpleEntry} 这类键值对视图<b>有无参构造、但不是 Map</b>，
     * 旧实现把它当普通 Bean 深拷贝 ⇒ 拷出的是同型 Entry（内容为空），写回目标 {@code Map} 声明字段时
     * {@code ClassCastException}、整条跳过。</p>
     *
     * <p>断言口径：①{@code Map.Entry} → {@code Map} 声明建出单条记录且键值均为副本；
     * ②{@code Map.Entry} → {@code List} 声明物化为"键、值"两项。</p>
     */
    @Test
    void mapEntry_materializedByTargetDeclaration() {

        val entrySource = new EntrySourceHolder();
        entrySource.setEntry(new AbstractMap.SimpleEntry<>("k", new ArraySourceHolder.Inner("v")));

        val copied = CBeanUtils.copy(entrySource, EntryTargetHolder.class);

        // ① Entry → Map 声明（字段同名 entry）
        Assertions.assertNotNull(copied.getEntry(), "Entry 应能写入 Map 声明字段（不再跳过）");
        Assertions.assertEquals(1, copied.getEntry().size());
        Assertions.assertEquals("v", copied.getEntry().get("k").name, "值应为源的副本");

        // ② Entry → List 声明：物化为 [键, 值]
        val listSource = new EntryListSourceHolder();
        listSource.setEntry(new AbstractMap.SimpleEntry<>("k", new ArraySourceHolder.Inner("v")));
        val listCopied = CBeanUtils.copy(listSource, EntryListTargetHolder.class);
        Assertions.assertNotNull(listCopied.getEntry(), "Entry 应能写入容器声明字段");
        Assertions.assertEquals(2, listCopied.getEntry().size());
        Assertions.assertEquals("k", listCopied.getEntry().get(0));
    }

    /**
     * <p>对应测试用例 7.9：{@code java.sql} 时间子类按目标声明类型保真（不降级为 {@code java.util.Date}）</p>
     *
     * <p>回归点（本次补全的漏点）：旧实现一律 {@code new Date(getTime())} ⇒ {@code java.sql.Date}/
     * {@code Time}/{@code Timestamp} 全部降级为 {@code java.util.Date}：写回 {@code java.sql.*} 声明字段时
     * {@code ClassCastException}（丢字段），写回 {@code Object} 声明时值类型静默变化（{@code Timestamp}
     * 的纳秒精度同样丢失）。</p>
     *
     * <p>断言口径：①{@code java.sql.Date} → {@code java.sql.Date} 声明保持实现类；②{@code Timestamp} →
     * {@code Timestamp} 保持实现类且<b>纳秒不丢</b>；③{@code java.util.Date} → {@code java.util.Date}
     * 声明按运行时类保真，不被"目标父类型"改写成 {@code java.sql.Date}。</p>
     */
    @Test
    void sqlDateSubclasses_keptByTargetType() {

        val source = new SqlDateSourceHolder();
        source.setSqlDate(new java.sql.Date(1_000_000L));
        val timestamp = new java.sql.Timestamp(2_000_000L);
        // setNanos 后 getTime() 会把纳秒折算进毫秒（JDK 语义），故以 setNanos 后的 getTime() 为基准断言
        timestamp.setNanos(123_456_789);
        source.setTimestamp(timestamp);
        source.setUtilDate(new java.util.Date(3_000_000L));

        val copied = CBeanUtils.copy(source, SqlDateTargetHolder.class);

        Assertions.assertEquals(java.sql.Date.class, copied.getSqlDate().getClass(), "java.sql.Date 不应降级");
        Assertions.assertEquals(1_000_000L, copied.getSqlDate().getTime());

        Assertions.assertEquals(java.sql.Timestamp.class, copied.getTimestamp().getClass(), "Timestamp 不应降级");
        Assertions.assertEquals(timestamp.getTime(), copied.getTimestamp().getTime(), "毫秒值应与源一致");
        Assertions.assertEquals(123_456_789, copied.getTimestamp().getNanos(), "纳秒精度不应丢失");

        Assertions.assertEquals(java.util.Date.class, copied.getUtilDate().getClass(), "java.util.Date 不应被改写为子类");
        Assertions.assertEquals(3_000_000L, copied.getUtilDate().getTime());
    }

    /**
     * <p>对应测试用例 7.10：{@code Optional} 拆包写入非 Optional 声明的目标字段</p>
     *
     * <p>回归点（本次补全的漏点）：源字段 {@code Optional<List<Inner>>}、目标字段 {@code List<Inner>}
     * 时，旧实现把内部值拷好后<b>重新包回 {@code Optional}</b>，写回 {@code List} 声明
     * {@code ClassCastException}、整条跳过。</p>
     *
     * <p>断言口径：①{@code Optional<List<Inner>>} → {@code List<Inner>} 拆包写入且元素为副本；
     * ②空 {@code Optional} → 非 Optional 声明写 {@code null}；③{@code Optional<List<Inner>>} →
     * {@code Optional<List<Inner>>} 声明仍包回 {@code Optional}（原语义不变）。</p>
     */
    @Test
    void optional_unwrappedForNonOptionalTarget() {

        val source = new OptionalUnwrapSourceHolder();
        val inner = new ArraySourceHolder.Inner("o");
        source.setOptionalList(Optional.of(new ArrayList<>(Collections.singletonList(inner))));
        source.setEmptyOptional(Optional.empty());

        val copied = CBeanUtils.copy(source, OptionalUnwrapTargetHolder.class);

        Assertions.assertNotNull(copied.getOptionalList(), "Optional 应能拆包写入容器声明字段（不再跳过）");
        Assertions.assertEquals(1, copied.getOptionalList().size());
        Assertions.assertNotSame(inner, copied.getOptionalList().get(0), "元素应为副本");

        Assertions.assertNull(copied.getEmptyOptional(), "空 Optional 拆包后应写 null");

        // 目标仍为 Optional 声明时保持原语义（包回 Optional）
        val keeper = new OptionalUnwrapSourceHolder();
        keeper.setOptionalList(Optional.of(new ArrayList<>(Collections.singletonList(new ArraySourceHolder.Inner("p")))));
        val kept = CBeanUtils.copy(keeper, OptionalKeepTargetHolder.class);
        Assertions.assertTrue(kept.getOptionalList().isPresent(), "Optional 声明目标应保持 Optional 包装");
        Assertions.assertEquals("p", kept.getOptionalList().get().get(0).name);
    }

    /**
     * <p>对应测试用例 7.11：跨容器族的容器声明（源集合 → 目标 {@code Map} 声明）整条跳过、且不抛异常</p>
     *
     * <p><b>回归点</b>：目标声明为<b>另一容器族</b>时（源 {@code List}/{@code Set} → 声明 {@code Map}，
     * 或反向），集合与 {@code Map} 在"逐元素深拷贝"的形态上不同构（{@code Map} 的元素是键值对而非元素），
     * 无法从源单向物化目标形态，故按"跳过不写入"处理。</p>
     *
     * <p>压测面有二，缺一即回归：</p>
     * <ol>
     *   <li><b>不按声明族的实现去强转源容器</b>：曾按目标接口族把拷贝体归一成 {@code LinkedHashMap}，
     *   随后走集合路径把结果强转为 {@code Collection} → {@code ClassCastException}；</li>
     *   <li><b>不把源类写进目标声明族的 setter</b>：曾退回源运行时类（{@code Collections$EmptyList} 等），
     *   写回时在 MethodHandle 签名层抛 {@code ClassCastException}。</li>
     * </ol>
     * <p>两者都会被 {@code applyCopyAction} / 写回兜底的 catch 吞成 debug 日志，从而"看起来只是字段为空"，
     * 故本条按"无异常 + 字段保持既有值 + 其余字段照常拷贝"三项一并断言（用例用
     * {@code DefaultUncaughtExceptionHandler} 无法观测被吞异常，改以"不抛出"与字段值双向判定）。</p>
     *
     * <p>断言口径：①源 {@code ArrayList} → 声明 {@code Map}：不抛异常、字段保持 {@code null}；
     * ②空集合（{@code Collections.emptyList()}，其运行时类是包私有的 {@code Collections$EmptyList}）
     * 同样不抛异常、字段保持 {@code null}；③反向（源 {@code Map} → 声明 {@code List}）同样跳过；
     * ④同一目标对象上的可拷贝字段不受影响（跳过只作用于该字段）。</p>
     */
    @Test
    void crossFamilyContainer_skippedWithoutCasting() {

        // ① 源 ArrayList → 目标声明 Map
        val collectionSource = new CrossFamilyCollectionSourceHolder();
        collectionSource.setItems(new ArrayList<>(Collections.singletonList("a")));
        collectionSource.setName("kept");

        val mapTarget = CBeanUtils.copy(collectionSource, CrossFamilyMapTargetHolder.class);
        Assertions.assertNull(mapTarget.getItems(), "跨容器族应跳过不写入（保持既有值，不构造半成品）");
        Assertions.assertEquals("kept", mapTarget.getName(), "跳过只作用于该字段，其余字段照常拷贝");

        // ② 空集合：运行时类为包私有的 Collections$EmptyList（曾据此被误当作 Map 族）
        val emptySource = new CrossFamilyCollectionSourceHolder();
        emptySource.setItems(Collections.emptyList());

        val emptyCopied = CBeanUtils.copy(emptySource, CrossFamilyMapTargetHolder.class);
        Assertions.assertNull(emptyCopied.getItems(), "空集合同样按跨族跳过，且不得抛异常");

        // ③ 反向：源 Map → 目标声明 List
        val mapSource = new CrossFamilyMapSourceHolder();
        val map = new LinkedHashMap<String, String>();
        map.put("k", "v");
        mapSource.setItems(map);

        val listTarget = CBeanUtils.copy(mapSource, CrossFamilyListTargetHolder.class);
        Assertions.assertNull(listTarget.getItems(), "Map 源 → List 声明应跳过不写入");

        // ④ 目标对象既有值保留（跳过 = 不写入，不是写 null）
        val preset = new CrossFamilyMapTargetHolder();
        val presetMap = new LinkedHashMap<String, String>();
        presetMap.put("p", "q");
        preset.setItems(presetMap);
        CBeanUtils.copy(collectionSource, preset);
        Assertions.assertSame(presetMap, preset.getItems(), "跳过不写入应保留目标对象既有值");
    }

    /**
     * <p>对应测试用例 7.3：兼容的容器目标按源接口形态降级、且拷贝结果可写回目标声明类型</p>
     *
     * <p>覆盖：接口声明（List/Set/Queue）与有序容器（SortedSet/SortedMap 保 comparator）。
     * 判据是"写入后取值与源语义等价且实现可接受"。</p>
     */
    @Test
    void containerWriteBack_compatibleKept() {

        val source = new CompatibleSourceHolder();
        source.setList(new LinkedList<>(Collections.singletonList("a")));
        source.setSet(new LinkedHashSet<>(Collections.singletonList("b")));
        source.setQueue(new ArrayDeque<>(Collections.singletonList("q")));
        source.setSortedSet(new TreeSet<>(Comparator.reverseOrder()));
        source.getSortedSet().addAll(Arrays.asList("x", "y"));
        source.setSortedMap(new TreeMap<>(Comparator.reverseOrder()));
        source.getSortedMap().put("k", "v");

        val copied = CBeanUtils.copy(source, CompatibleTargetHolder.class);

        Assertions.assertNotSame(source.getList(), copied.getList());
        Assertions.assertEquals(new ArrayList<>(source.getList()), new ArrayList<>(copied.getList()));
        Assertions.assertNotSame(source.getSet(), copied.getSet());
        Assertions.assertEquals(source.getSet(), copied.getSet());
        Assertions.assertNotSame(source.getQueue(), copied.getQueue());
        Assertions.assertEquals(new ArrayList<>(source.getQueue()), new ArrayList<>(copied.getQueue()));

        // 有序容器保 comparator：拷贝后仍是同一排序语义（反序）
        Assertions.assertNotSame(source.getSortedSet(), copied.getSortedSet());
        Assertions.assertEquals(Arrays.asList("y", "x"), new ArrayList<>(copied.getSortedSet()));
        Assertions.assertEquals(
                new ArrayList<>(source.getSortedMap().keySet()),
                new ArrayList<>(copied.getSortedMap().keySet())
        );
    }

    /**
     * <p>对应测试用例 7.12：按目标类折叠的深拷贝计划——"含 final 字段"的降级语义不被折叠改变</p>
     *
     * <p><b>回归点</b>：{@code deepCopyBean} 原先在运行期依次判"含 final 实例字段 → 是否已登记不支持
     * → 可否无参构造"，优化后这些按类恒定的判定折叠进一次 {@code ClassValue} 查表（{@code DeepPlan}）。
     * 折叠不得改变对外可观测的降级语义：<b>含 final 字段 ⇒ 共享引用</b>（既有 {@code copy} 契约不写
     * final 字段，结构拷贝会产出"部分字段为空"的对象），且这类目标类<b>不会被尝试构造</b>
     * （不产生"构造体抛异常"的 error 日志、不进「不支持的目标类集合」）。</p>
     *
     * <p><b>断言口径</b>：①含 final 字段的目标类，字段<b>共享</b>（{@code assertSame}，非副本、非跳过）；
     * ②该类型的<b>构造尝试次数恒为 0</b>（含 final 判定优先，不进入构造）；③同一目标类型重复复制
     * 结果一致（计划按类缓存后幂等）；④含 final 但可实例化的类同样共享。</p>
     *
     * <p><b>等价性说明（为何②是有效判据）</b>：含 final 字段的类若构造失败，其失败会在
     * {@code UNSUPPORTED_TARGET_CLASSES} 登记——但登记的前提是"尝试过构造"，而含 final 判定在此之先，
     * 故该类型永远不会被登记。因此"含 final ⇒ 共享"与"含 final ⇒ 跳过"在本实现下不可能同时可达；
     * ②（构造计数为 0）正是把这一"不可能被登记"保证钉住的可观测判据。</p>
     *
     * <p><b>测试数据选择理由</b>：源值经 {@code sample()}（绕过构造体）取得——该类型构造体必抛异常，
     * 无法直接 {@code new}；同时触发"含 final 字段"与"构造体抛异常"两条降级路径，最能暴露折叠改错次序。</p>
     */
    @Test
    void deepPlan_finalFieldTakesPrecedenceOverUninstantiable() {

        FinalAndUninstantiable.resetCtorCount();

        val first = new FinalAndUninstantiableHolder();
        first.setTarget(FinalAndUninstantiable.sample());
        val firstCopied = CBeanUtils.copy(first, new FinalAndUninstantiableHolder());

        Assertions.assertSame(first.getTarget(), firstCopied.getTarget(),
                "含 final 字段 ⇒ 共享引用（不得因折叠而变为跳过不写入或副本）");
        Assertions.assertEquals(0, FinalAndUninstantiable.ctorCount(),
                "含 final 字段的类不应被尝试构造");

        // 计划按类缓存后幂等：重复复制结果一致、仍不尝试构造
        val second = new FinalAndUninstantiableHolder();
        second.setTarget(FinalAndUninstantiable.sample());
        val secondCopied = CBeanUtils.copy(second, new FinalAndUninstantiableHolder());
        Assertions.assertSame(second.getTarget(), secondCopied.getTarget(), "计划折叠后行为幂等");
        Assertions.assertEquals(0, FinalAndUninstantiable.ctorCount(), "计划按类缓存后仍不应尝试构造");

        // 含 final 但可实例化：同样共享（不因"可实例化"而尝试构造副本）
        val finalOnly = new FinalOnlySource();
        finalOnly.setTarget(new FinalOnlyTarget());
        val finalOnlyCopied = CBeanUtils.copy(finalOnly, new FinalOnlySource());
        Assertions.assertSame(finalOnly.getTarget(), finalOnlyCopied.getTarget(),
                "含 final 字段的可实例化类仍共享引用");
    }

    /**
     * <p>对应测试用例 2.5：容器元素为 JDK 非拷贝协议类型（StringBuilder）⇒ 容器副本、元素共享</p>
     */
    @Test
    void collection_jdkNonCopyableElements_shared() {
        val sb = new StringBuilder("sb");
        val holder = new SbListHolder();
        holder.setList(new ArrayList<>(Collections.singletonList(sb)));

        val copy = CBeanUtils.copy(holder, new SbListHolder());

        Assertions.assertNotSame(holder.getList(), copy.getList(), "容器本身应深拷贝");
        Assertions.assertEquals(1, copy.getList().size());
        Assertions.assertSame(sb, copy.getList().get(0), "JDK 非拷贝协议元素应共享引用");
    }

    /**
     * <p>对应测试用例 4.6：非标准容器（ArrayDeque）按同源实现深拷贝——元素独立、实现保持</p>
     */
    @Test
    void container_queueFallbackArrayDeque() {
        val holder = new QueueHolder();
        holder.setDeque(new ArrayDeque<>(Arrays.asList("a", "b")));

        val copy = CBeanUtils.copy(holder, new QueueHolder());

        Assertions.assertNotSame(holder.getDeque(), copy.getDeque());
        Assertions.assertEquals(ArrayDeque.class, copy.getDeque().getClass());
        Assertions.assertEquals(new ArrayList<>(holder.getDeque()), new ArrayList<>(copy.getDeque()));
    }

    /**
     * <p>对应测试用例 4.7：空 EnumMap 仍保持 EnumMap 实现（保留键类型）且为空</p>
     *
     * <p>回归点：{@code EnumMap(Map)} 对空源无法推断键类型会抛异常，故须走 {@code EnumMap(EnumMap)}
     * 保留键类型的构造；曾误降级为标准 Map，导致写回 {@code EnumMap} 声明字段时 {@code ClassCastException}。</p>
     */
    @Test
    void container_emptyEnumMap_copied() {
        val holder = new EnumHolder();
        holder.setEnumMap(new EnumMap<>(Color.class));

        val copy = CBeanUtils.copy(holder, new EnumHolder());

        Assertions.assertNotSame(holder.getEnumMap(), copy.getEnumMap());
        Assertions.assertTrue(copy.getEnumMap().isEmpty());
        Assertions.assertEquals(EnumMap.class, copy.getEnumMap().getClass(), "空 EnumMap 仍应保持 EnumMap 实现");
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
     * 含 final 实例字段的内层 Bean（final 字段不可写 ⇒ 无法完整结构拷贝）
     */
    @Data
    static class FinalFieldInner {

        private final String name = "inner";
    }

    /**
     * 持有"含 final 字段内层 Bean"的外层 Bean
     */
    @Data
    static class FinalFieldHolder {

        private FinalFieldInner inner;
    }

    /**
     * Calendar 字段（可变值类型，防御性拷贝）
     */
    @Data
    static class CalendarHolder {

        private Calendar calendar;
    }

    /**
     * OptionalInt/OptionalLong/OptionalDouble 字段（只包原始值 ⇒ 共享）
     */
    @Data
    static class PrimitiveOptionalHolder {

        private OptionalInt oi;

        private OptionalLong ol;

        private OptionalDouble od;
    }

    /**
     * 函数式接口
     */
    @FunctionalInterface
    interface Runner {

        String run();
    }

    /**
     * 持有"JDK 非拷贝协议元素集合"的外层 Bean（元素按共享处理）
     */
    @Data
    static class SbListHolder {

        private List<StringBuilder> list;
    }

    /**
     * List 源（用于与兼容/不兼容目标声明对照）
     */
    @Data
    static class MismatchSourceHolder {

        private List<Inner> items;
    }

    /**
     * List 目标声明（与源兼容）
     */
    @Data
    static class MismatchListTargetHolder {

        private List<Inner> items;
    }

    /**
     * Set 目标声明（与源 List 不兼容）
     */
    @Data
    static class MismatchSetTargetHolder {

        private Set<Inner> items;
    }

    /**
     * 兼容形态源：接口声明 + 有序容器
     */
    @Data
    static class CompatibleSourceHolder {

        private List<String> list;
        private Set<String> set;
        private Queue<String> queue;
        private SortedSet<String> sortedSet;
        private SortedMap<String, String> sortedMap;
    }

    /**
     * 兼容形态目标：接口声明 + 有序容器
     */
    @Data
    static class CompatibleTargetHolder {

        private List<String> list;
        private Set<String> set;
        private Queue<String> queue;
        private SortedSet<String> sortedSet;
        private SortedMap<String, String> sortedMap;
    }

    /**
     * Collection 目标声明（与源 List 不同族，用于 7.2 的按目标族重建断言）
     */
    @Data
    static class MismatchCollectionTargetHolder {

        private Collection<Inner> items;
    }

    /**
     * 具体实现类源：List/Queue/Set 三种具体实现声明
     */
    @Data
    static class ConcreteSourceHolder {

        private ArrayList<String> list;
        private ArrayList<String> queue;
        private HashSet<String> set;
    }

    /**
     * 具体实现类目标：目标声明比源更具体（LinkedList/TreeSet）或换族（Queue）
     */
    @Data
    static class ConcreteTargetHolder {

        private LinkedList<String> list;
        private Queue<String> queue;
        private TreeSet<String> set;
    }

    /**
     * 一次性视图源：迭代器 / 集合视图 / 对象流 / 原始流
     */
    @Data
    static class ViewSourceHolder {

        private Iterator<String> fromIterator;
        private Set<String> fromKeySet;
        private Stream<String> fromStream;
        private IntStream fromIntStream;
    }

    /**
     * 一次性视图目标：容器声明均与源形态不同族，验证按目标类型重建
     */
    @Data
    static class ViewTargetHolder {

        private List<String> fromIterator;
        private Set<String> fromKeySet;
        private List<String> fromStream;
        private List<Integer> fromIntStream;
    }

    /**
     * 有无参构造、但构造体必定抛异常的目标类（准备阶段判不出，只能"试一次才知道"）
     *
     * <p>静态计数器用于断言"只尝试实例化一次"——这正是「不支持的目标类集合」的判据：
     * 首次失败即登记，其后任何源类再碰到该类型都不再尝试。</p>
     *
     * <p>样本值由 {@link #sample()} 经序列化绕开构造体取得（构造体必抛异常，无法直接 {@code new}）——
     * 测试要的是"值存在、但目标类型无法实例化"这一组合。</p>
     */
    static class ThrowingCtorTarget {

        private static final AtomicInteger INVOCATION_COUNT = new AtomicInteger();

        private String name;

        private boolean failed;

        ThrowingCtorTarget() {
            INVOCATION_COUNT.incrementAndGet();
            throw new UnsupportedOperationException("构造体必定抛异常（模拟不可初始化）");
        }

        /**
         * 取一个不经构造体的样本实例（供测试构造源值）
         *
         * <p>构造体必抛异常，故不能用 {@code new}；此处用 {@code sun.misc.Unsafe#allocateInstance}
         * 的等价能力（反射调 {@code Object} 层）不可行，改用 JDK 自带的"绕过构造"入口——
         * {@code ReflectionFactory}。它属 JDK 内部 API，测试环境可用（生产代码不用）。</p>
         *
         * @return 未初始化的样本实例
         */
        static ThrowingCtorTarget sample() {
            try {
                val factory = Class.forName("sun.reflect.ReflectionFactory").getMethod("getReflectionFactory").invoke(null);
                val method = factory.getClass().getMethod("newConstructorForSerialization", Class.class, java.lang.reflect.Constructor.class);
                val ctor = (java.lang.reflect.Constructor<?>) method.invoke(factory, ThrowingCtorTarget.class, Object.class.getDeclaredConstructor());
                ctor.setAccessible(true);
                return (ThrowingCtorTarget) ctor.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("测试样本创建失败", e);
            }
        }

        static int invocationCount() {
            return INVOCATION_COUNT.get();
        }

        static void resetInvocationCount() {
            INVOCATION_COUNT.set(0);
        }
    }

    /**
     * 持有不可实例化目标类字段的源
     */
    @Data
    static class ThrowingCtorSource {

        private ThrowingCtorTarget target;
    }

    /**
     * 同时含 final 实例字段、且构造体抛异常的目标类（7.12：折叠后降级分支优先级）
     *
     * <p>用于验证折叠计划不改变原有判定次序：含 final 字段的判定先于"可实例化"判定，
     * 故结果应为<b>共享引用</b>。样本值同样经「绕过构造」取得。</p>
     */
    static class FinalAndUninstantiable {

        private static final AtomicInteger CTOR_COUNT = new AtomicInteger();

        private final String name = "final";

        FinalAndUninstantiable() {
            CTOR_COUNT.incrementAndGet();
            throw new UnsupportedOperationException("构造体必定抛异常（7.12 复合降级场景）");
        }

        static int ctorCount() {
            return CTOR_COUNT.get();
        }

        static void resetCtorCount() {
            CTOR_COUNT.set(0);
        }

        static FinalAndUninstantiable sample() {
            try {
                val factory = Class.forName("sun.reflect.ReflectionFactory").getMethod("getReflectionFactory").invoke(null);
                val method = factory.getClass().getMethod("newConstructorForSerialization", Class.class, java.lang.reflect.Constructor.class);
                val ctor = (java.lang.reflect.Constructor<?>) method.invoke(
                        factory, FinalAndUninstantiable.class, Object.class.getDeclaredConstructor());
                ctor.setAccessible(true);
                return (FinalAndUninstantiable) ctor.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("测试样本创建失败", e);
            }
        }
    }

    /**
     * 持有 {@link FinalAndUninstantiable} 字段的源（7.12）
     */
    @Data
    static class FinalAndUninstantiableHolder {

        private FinalAndUninstantiable target;
    }

    /**
     * 含 final 实例字段、但可正常实例化的目标类（7.12：final 优先共享）
     */
    static class FinalOnlyTarget {

        private final String name = "final";
    }

    /**
     * 持有 {@link FinalOnlyTarget} 字段的源（7.12）
     */
    @Data
    static class FinalOnlySource {

        private FinalOnlyTarget target;
    }

    /**
     * 另一个持有同一不可实例化目标类的源（验证跨源类复用「不支持的目标类集合」）
     */
    @Data
    static class OtherThrowingCtorSource {

        private ThrowingCtorTarget target;
    }

    /**
     * 数组型源：数组 ↔ 容器两向转换的样本
     */
    @Data
    static class ArraySourceHolder {

        private String[] strings;
        private Inner[] beans;
        private Set<Inner> beansAsCollection;
        private int[] ints;

        @Data
        static class Inner {

            private String name;

            Inner() {
            }

            Inner(String name) {
                this.name = name;
            }
        }
    }

    /**
     * 容器型目标：全部按容器声明接收源数组
     */
    @Data
    static class ArrayTargetHolder {

        private List<String> strings;
        private Set<ArraySourceHolder.Inner> beans;
        private ArraySourceHolder.Inner[] beansAsCollection;
        private List<Integer> ints;
    }

    /**
     * {@code Object[]} 源（含装不进目标组件类型的元素，用于验证"跳过槽位而非整体失败"）
     */
    @Data
    static class ObjectArraySourceHolder {

        private Object[] values;
    }

    /**
     * {@code String[]} 目标（组件类型比源更具体）
     */
    @Data
    static class StringArrayTargetHolder {

        private String[] values;
    }

    /**
     * {@code Map.Entry} 源：键值对视图 → Map / 容器 两向物化
     */
    @Data
    static class EntrySourceHolder {

        private Map.Entry<String, ArraySourceHolder.Inner> entry;
    }

    /**
     * {@code Map.Entry} 目标：Map 声明与容器声明各一
     */
    @Data
    static class EntryTargetHolder {

        /**
         * 与源同名为 {@code entry}（copy 按同名字段配对），声明为 Map 形态
         */
        private Map<String, ArraySourceHolder.Inner> entry;
    }

    /**
     * {@code Map.Entry} 目标是容器声明的源（字段同名，声明为 List 形态）
     */
    @Data
    static class EntryListSourceHolder {

        private Map.Entry<String, ArraySourceHolder.Inner> entry;
    }

    /**
     * {@code Map.Entry} 目标是容器声明
     */
    @Data
    static class EntryListTargetHolder {

        private List<Object> entry;
    }

    /**
     * {@code java.sql} 时间子类源
     */
    @Data
    static class SqlDateSourceHolder {

        private java.sql.Date sqlDate;
        private java.sql.Timestamp timestamp;
        private java.util.Date utilDate;
    }

    /**
     * 时间目标：与源同型声明（验证按声明/运行时类型保真）
     */
    @Data
    static class SqlDateTargetHolder {

        private java.sql.Date sqlDate;
        private java.sql.Timestamp timestamp;
        private java.util.Date utilDate;
    }

    /**
     * {@code Optional} 源：内部值可深拷贝
     */
    @Data
    static class OptionalUnwrapSourceHolder {

        private Optional<List<ArraySourceHolder.Inner>> optionalList;
        private Optional<String> emptyOptional;
    }

    /**
     * 非 {@code Optional} 目标：接收拆包后的内部值
     */
    @Data
    static class OptionalUnwrapTargetHolder {

        private List<ArraySourceHolder.Inner> optionalList;
        private String emptyOptional;
    }

    /**
     * {@code Optional} 目标：保持 {@code Optional} 包装语义
     */
    @Data
    static class OptionalKeepTargetHolder {

        private Optional<List<ArraySourceHolder.Inner>> optionalList;
    }

    /**
     * 跨容器族源：集合（{@code List} 声明）
     */
    @Data
    static class CrossFamilyCollectionSourceHolder {

        private List<String> items;

        private String name;
    }

    /**
     * 跨容器族目标：{@code Map} 声明（与集合源不同族，应跳过不写入）
     */
    @Data
    static class CrossFamilyMapTargetHolder {

        private Map<String, String> items;

        private String name;
    }

    /**
     * 跨容器族源：{@code Map} 声明（与集合目标反向）
     */
    @Data
    static class CrossFamilyMapSourceHolder {

        private Map<String, String> items;
    }

    /**
     * 跨容器族目标：{@code List} 声明
     */
    @Data
    static class CrossFamilyListTargetHolder {

        private List<String> items;
    }

    /**
     * 枚举
     */
    enum Color {

        RED, BLUE
    }

}
