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
 *   <li>覆盖（容器写回）：目标声明类型为容器接口时的实现保持与可写回判定（7.2 不兼容跳过、7.3 兼容保持）。</li>
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
 *   <li>7.2 容器目标声明类型与源接口形态不兼容 ⇒ 跳过不写入（containerWriteBack_incompatibleInterfaceSkipped，
 *   内含兼容目标的逐元素深拷贝对照：跨接口跳过只发生在不兼容的目标声明上）</li>
 *   <li>7.3 兼容的容器目标按源接口形态降级且可写回（containerWriteBack_compatibleKept）</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/17
 * @version 1.2
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
     * <p>对应测试用例 7.2：容器目标声明类型与源容器接口形态不兼容 ⇒ 跳过不写入</p>
     *
     * <p>回归点：拷贝结果按类写回目标字段时，若目标声明类型是另一容器接口（如源 List、目标 Set），
     * 写回必然 {@code ClassCastException}；旧实现先拷贝再写、把该异常吞成 debug"字段写入失败，跳过"，
     * 表现为"字段静默为空 + 每次多一次注定失败的拷贝"。现改为拷贝前判定、该字段跳过不写入
     * （共享引用同样写不回目标接口，故只有"跳过"是自洽语义）。</p>
     */
    @Test
    void containerWriteBack_incompatibleInterfaceSkipped() {

        val inner = new Inner();
        inner.setName("inner");

        val source = new MismatchSourceHolder();
        source.setItems(new ArrayList<>(Collections.singletonList(inner)));

        // 对照：目标声明 List<Inner> 与源兼容 ⇒ 正常深拷贝（容器与元素都是副本）
        val copied = CBeanUtils.copy(source, MismatchListTargetHolder.class);
        Assertions.assertNotSame(source.getItems(), copied.getItems());
        Assertions.assertNotSame(source.getItems().get(0), copied.getItems().get(0));

        // 目标声明 Set、源为 List ⇒ 目标接口装不下源容器：跳过不写入（字段保持 null）
        val mismatched = CBeanUtils.copy(source, MismatchSetTargetHolder.class);
        Assertions.assertNotSame(source, mismatched, "目标对象本身仍正常创建");
        Assertions.assertNull(
                mismatched.getItems(),
                "目标声明接口装不下源容器时应跳过不写入（而非尝试写回后静默失败）"
        );
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
     * 枚举
     */
    enum Color {

        RED, BLUE
    }

}
