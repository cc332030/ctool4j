package com.c332030.ctool4j.core.classes;

import com.c332030.ctool4j.core.cache.impl.CClassValue;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.reflect.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.*;

/**
 * <p>
 * Description: CDeepCopyUtils，{@link CBeanUtils} 的深拷贝子系统。
 * </p>
 *
 * <p>
 * 从 {@code CBeanUtils} 拆出的深拷贝物化实现：按值的运行时类型递归拷贝集合 / Map / 数组 / Bean / Date，
 * 并按<b>目标声明类型</b>重建跨形态值（数组 ↔ 容器、{@code Map.Entry} → Map/容器、{@code Optional} 拆包）。
 * 复制计划（字段配对、决议、写回）仍在 {@link CBeanUtils}，本类只负责「一个值怎么变成副本」。
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>唯一对外入口：{@code deepCopyValue}（分派：不可变判定 → 环处理 → 深度保护 → 数组/集合/Map/Entry/时间/Optional/Bean）。</li>
 *   <li>容器重建：{@code deepCopyArray} / {@code deepCopyCollection} / {@code deepCopyMap} / {@code deepCopyIterable}
 *   及"按目标声明选容器"的 {@code containerFor} / {@code resolveContainerFor}。</li>
 *   <li>Bean 递归：{@code deepCopyBean}（复用 {@link CBeanUtils} 的复制计划流程，不另写字段配对）。</li>
 *   <li>类型推断：{@code resolveDeepCopyTargetClass} / {@code deepCopyTypeArgument} / {@code typeArgumentMemo}
 *   与类型实参记忆 {@code TypeArgumentKey} / {@code TypeArgumentMemo}。</li>
 *   <li>按类折叠计划：{@code DeepPlan}（含 final 字段、可否无参构造）+ {@code INSTANTIABLE_CLASS_VALUE}。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>不可拷贝类型（JDK 未提供拷贝协议）</td>
 *     <td>同型共享引用、跨类型跳过</td>
 *   </tr>
 *   <tr>
 *     <td>递归超过 {@code DEEP_COPY_MAX_DEPTH}</td>
 *     <td>原样返回引用（防病态深结构）</td>
 *   </tr>
 *   <tr>
 *     <td>目标类无可用无参构造 / 含 final 实例字段 / 构造体抛异常</td>
 *     <td>共享引用；构造体抛异常的类型登记后跳过</td>
 *   </tr>
 *   <tr>
 *     <td>目标声明与源容器跨族（List ↔ Map）</td>
 *     <td>返回 {@code SKIP_VALUE}，调用方不写入该字段</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>{@link CBeanUtils} 的全部复制入口（对象源、Map 源、supplier、批量、数组反序）中，值需要物化为副本的场景。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>字段配对、决议（共享/转换/深拷贝/跳过）与写回不属本类，见 {@link CBeanUtils}。</li>
 *   <li>本类不读取源对象字段表，只处理"已取到的值"。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>与 {@link CBeanUtils} 存在<b>包内双向引用</b>（Bean 递归需要复制计划流程），故二者同包、均为包内可见实现，
 *   不做对外公开 API。</li>
 *   <li>判定按类缓存（{@code CClassValue}），缓存键为 {@code Class}，无泄漏；不支持的目标类集合为运行期可变状态，
 *   刻意不进按类缓存（否则会把"登记前"的取值固化）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>按目标声明重建</b>：同一源值声明为不同目标类型会得到不同容器形态，是设计内行为。</li>
 *   <li><b>单一入口</b>：{@code deepCopyValue} 是唯一分派点，其余方法按类型族拆分，便于单独验证。</li>
 *   <li><b>热路径克制</b>：日志只打"按类一次"或异常场景，热路径零日志（与拆分前一致）。</li>
 * </ul>
 *
 * @since 2026/11/10
 * @version 1.0
 */
@CustomLog
@UtilityClass
class CDeepCopyUtils {

    /**
     * 深拷贝递归深度上限（超过该层原样返回引用，防病态深结构）
     */
    static final int DEEP_COPY_MAX_DEPTH = 64;

    /**
     * 不写入哨兵：决议为跳过、转换结果为 null、深拷贝失败时统一返回该值，调用方据此不写字段
     */
    static final Object SKIP_VALUE = new Object();

    /**
     * 视图/不可变封装的类名前缀：这类实现不做同源复制，深拷贝时降级为可变标准实现
     */
    static final String[] VIEW_OR_IMMUTABLE_PREFIXES = {
        "java.util.Collections$",
        "java.util.Arrays$",
        "java.util.ImmutableCollections$",
        "java.util.stream.",
        "org.hibernate.collection",
        "com.google.common.collect.Immutable",
        "com.google.common.collect.SingletonImmutable"
    };

    /**
     * 深拷贝分派：不可变判定 → 环处理 → 深度保护 → 数组/集合/Map/时间/Bean 分支
     *
     * @param from        源值
     * @param declaredType 声明类型（可空，空则按运行时类型）
     * @param visited     已拷贝身份表
     * @param depth       当前深度
     * @return 副本或原引用
     */
    static Object deepCopyValue(Object from, Type declaredType, IdentityHashMap<Object, Object> visited, int depth) {

        if(null == from) {
            return null;
        }

        val fromClass = from.getClass();
        if(isNotCopyable(fromClass)) {
            return from;
        }

        val copied = visited.get(from);
        if(null != copied) {
            return copied;
        }

        if(depth >= DEEP_COPY_MAX_DEPTH) {
            log.debug("深拷贝超过深度上限 {}，原样返回：{}", DEEP_COPY_MAX_DEPTH, fromClass.getName());
            return from;
        }

        // 目标类须先于分支求出：数组 ↔ 容器的跨形态转换、Map.Entry → Map、
        // 时间子类保真都按"目标声明类型"决定重建方式
        val toClass = resolveDeepCopyTargetClass(declaredType, fromClass);

        if(fromClass.isArray()) {
            // 数组：目标声明为容器时按元素重建容器（否则按源/目标组件类型新建同长数组）
            return deepCopyArray(from, declaredType, toClass, visited, depth);
        }

        // 迭代器 / 原始流：源本身不可复用（一次性、无结构），但其**迭代顺序是稳定的**，
        // 故按目标声明类型重建为标准容器（见 deepCopyIterable）
        val viewed = viewedIterable(from);
        if(null != viewed) {
            return deepCopyIterable(viewed, declaredType, fromClass, visited, depth);
        }

        if(from instanceof Collection) {
            // 跨接口容器（如源 List → 目标 Set 声明）按目标声明类型重建容器——
            // 拷贝结果按目标类型创建，写回目标接口天然成立，无需"跳过不写入"
            if(!canWriteBackContainer(toClass, Collection.class)) {
                log.debug("目标声明装不下源容器（跨容器族），属性跳过不写入：{} → {}",
                        fromClass.getName(), toClass.getName());
                return SKIP_VALUE;
            }
            return deepCopyCollection((Collection<?>) from, declaredType, toClass, visited, depth);
        }
        if(from instanceof Map) {
            if(!canWriteBackContainer(toClass, Map.class)) {
                log.debug("目标声明装不下源容器（跨容器族），属性跳过不写入：{} → {}",
                        fromClass.getName(), toClass.getName());
                return SKIP_VALUE;
            }
            return deepCopyMap((Map<?, ?>) from, declaredType, toClass, visited, depth);
        }
        // Map.Entry 是"单个键值对"的视图：目标声明为 Map 时物化为单条记录，
        // 目标声明为容器/数组时物化"键、值"两项，目标声明为 Entry 时按同型字段拷贝
        if(from instanceof Map.Entry) {
            return deepCopyEntry((Map.Entry<?, ?>) from, declaredType, toClass, visited, depth);
        }
        if(from instanceof Date) {
            // 按目标声明类型重建：同族目标按声明/运行时类保真，
            // java.sql.Date / Time / Timestamp 不降级为 java.util.Date（丢失精度与类型语义）
            return newDate(from, toClass);
        }
        if(from instanceof Calendar) {
            return ((Calendar) from).clone();
        }

        // Optional：自身不可变但内部值可能可变（Optional<List<X>> 等）⇒ 拷贝内部值后重新包装；
        // 空 Optional 无内部值，直接共享（OptionalInt/Long/Double 只包装原始值，不属此分支）
        if(from instanceof Optional) {
            val optional = (Optional<?>) from;
            // 目标声明不是 Optional 族（如 Optional<List<X>> → List<X> 声明）⇒ 拆包按目标声明重建，
            // 否则写回目标字段必然 ClassCastException、整条跳过（丢字段）
            val toOptional = null == toClass || Optional.class.isAssignableFrom(toClass);
            if(!optional.isPresent()) {
                return toOptional ? from : null;
            }

            // 目标为 Optional 族：声明形如 Optional<T>，内部值按第 0 个类型实参（=T）处理；
            // 目标非 Optional（拆包）：声明的就是拆包后的类型本身（如 List<Inner>），
            // 直接以该声明作为内部值的声明类型，不再取其实参（否则会退化成元素类型、拆包后按错类型重建）
            Type innerType = declaredType;
            if(toOptional) {
                innerType = deepCopyTypeArgument(declaredType, 0);
            }
            val optionalValue = deepCopyValue(optional.get(), innerType, visited, depth + 1);
            if(toOptional) {
                return Optional.ofNullable(optionalValue);
            }
            return optionalValue;
        }

        // JDK 未提供拷贝协议的类型（StringBuilder/AtomicX/BitSet 等）不做结构拷贝，原样返回（共享）：
        // 集合元素、嵌套字段等不经过 copy 决议，需在此兜住（详见 isDeepCopyCapable）
        if(!CBeanUtils.isDeepCopyCapable(fromClass) || !CBeanUtils.isDeepCopyCapable(toClass)) {
            return from;
        }

        return deepCopyBean(from, toClass, visited, depth);
    }

    /**
     * 不可拷贝判定缓存：按类缓存（判定结果按类恒定，且含注解查询，热路径避免重复反射）
     */
    static final CClassValue<Boolean> IS_NOT_COPYABLE_CLASS_VALUE =
            CClassValue.of(CDeepCopyUtils::checkNotCopyable);

    /**
     * 判定类型是否无需拷贝（不可变或按要求放行）
     *
     * @param type 类型
     * @return true 表示原样返回（不拷贝）
     */
    static boolean isNotCopyable(Class<?> type) {
        return IS_NOT_COPYABLE_CLASS_VALUE.get(type);
    }

    /**
     * 不可拷贝判定的实际计算（仅首次按类执行，结果进 {@link #IS_NOT_COPYABLE_CLASS_VALUE} 缓存）
     *
     * @param type 类型
     * @return true 表示无需拷贝
     */
    static boolean checkNotCopyable(Class<?> type) {

        if(type.isPrimitive() || type.isEnum() || type.isAnnotation() || type.isInterface()) {
            return true;
        }
        if(Class.class == type || String.class == type) {
            return true;
        }
        if(Number.class.isAssignableFrom(type) || Boolean.class == type || Character.class == type) {
            return true;
        }
        if(Temporal.class.isAssignableFrom(type) || TemporalAmount.class.isAssignableFrom(type)) {
            return true;
        }
        if(UUID.class == type || Locale.class == type || Currency.class == type
                || Charset.class == type || URI.class == type) {
            return true;
        }

        // 函数式接口声明与 lambda/方法引用的运行期类：承载行为而非数据，深拷贝无意义 ⇒ 共享引用
        return type.isAnnotationPresent(FunctionalInterface.class) || isLambdaType(type);
    }

    /**
     * 是否 lambda / 方法引用的运行期类（{@code LambdaMetafactory} 生成的 {@code $$Lambda$} 类）
     *
     * <p>该类是 JVM 合成类、持有行为而非数据，实例化它也得不到"同样的数据副本"，故按不可拷贝处理。</p>
     *
     * <p><b>两种类名形态都要认</b>：JDK 8 的 lambda 类名是 {@code pkg.Foo$$Lambda$1/0x...}，
     * JDK 15+ 改为<b>隐藏类</b>、类名形如 {@code pkg.Foo$$Lambda/0x00000008000c0c40}——
     * 分隔符从 {@code $} 变成 {@code /}（且无序号）。只匹配 {@code $$Lambda$} 在最新 LTS 档位会漏判，
     * 使 lambda 值被当成普通 Bean 走深拷贝（既不共享、也拷不出语义）。</p>
     *
     * @param type 类型
     * @return true 表示 lambda / 方法引用的运行期类
     */
    static boolean isLambdaType(Class<?> type) {
        val name = type.getName();
        // JDK8：pkg.Foo$$Lambda$1/0x...；JDK15+ 隐藏类：pkg.Foo$$Lambda/0x...
        return name.contains("$$Lambda$") || name.contains("$$Lambda/");
    }

    /**
     * 数组深拷贝（基本类型/对象/多维统一按组件类型递归）
     *
     * @param from    源数组
     * @param visited 已拷贝身份表
     * @param depth   当前深度
     * @return 新数组
     */
    static Object deepCopyArray(
            Object from, Type declaredType, Class<?> toClass,
            IdentityHashMap<Object, Object> visited, int depth
    ) {

        // 目标声明为容器（如源 Inner[] → 目标 List<Inner> 声明）：按元素重建容器。
        // 写入目标接口/实现类天然成立，不再因"数组无法赋给集合"而整条跳过（丢字段）
        if(null != toClass && !toClass.isArray() && Collection.class.isAssignableFrom(toClass)) {
            return deepCopyArrayToContainer(from, declaredType, toClass, visited, depth);
        }

        val length = Array.getLength(from);
        val fromComponentType = from.getClass().getComponentType();
        // 目标组件类型优先：源 Object[] → 目标声明 String[] 时按目标组件建数组（元素按运行时类型转换）
        Class<?> componentType = fromComponentType;
        if(null != toClass && toClass.isArray()) {
            componentType = toClass.getComponentType();
        }
        val copy = Array.newInstance(componentType, length);

        visited.put(from, copy);

        // 元素类型确定不可变（原始类型、String/包装类等）且组件类型未变：直接整段复制，
        // 省去逐元素取值/分派/装箱
        if(componentType == fromComponentType
                && (componentType.isPrimitive() || isDefinitelyImmutable(componentType))) {
            System.arraycopy(from, 0, copy, 0, length);
            return copy;
        }

        for (int i = 0; i < length; i++) {
            val element = Array.get(from, i);
            // 目标组件类型装不下该元素（如 Object[] 含 Integer 而目标为 String[]）：
            // 不写入（跳过该槽位）而非抛 ArrayStoreException 整体失败
            if(null != element && !isAssignableToComponent(componentType, element)) {
                log.debug("数组元素类型 {}({}) 无法装入目标组件类型 {}，跳过该槽位：{}",
                        element.getClass().getName(), element, componentType.getName(), i);
                continue;
            }
            Array.set(copy, i, deepCopyValue(element, componentType, visited, depth + 1));
        }

        return copy;
    }

    /**
     * 容器拷贝结果能否写回目标声明类型（按源容器的形态判定，避免跨族强转抛 {@code ClassCastException}）
     *
     * <p>判据是"目标声明类型能否接收该形态的拷贝结果"：{@code Collection} 拷贝结果须能赋给目标声明，
     * {@code Map} 同理。跨容器族（源 {@code List} → 声明 {@code Map}）时两者形态不同构、结果装不下，
     * 此处拦下并返回 {@link #SKIP_VALUE}（{@link #resolveDeepCopyTargetClass} 对跨族声明保留声明类型，
     * 故本判定即旧实现 {@code isContainerWriteBack} 的等效防线）。</p>
     *
     * <p><b>数组声明不在拦下面内</b>：容器 → 数组是已支持的跨形态转换（按目标组件类型重建数组），
     * 由 {@link #deepCopyCollection} 的数组分支承接，可写回天然成立。</p>
     *
     * <p>同族跨接口（源 {@code List} → 声明 {@code Set}）在此恒为可写：拷贝结果按目标声明类型创建，
     * 由 {@link #containerFor} 保证（见 {@link #deepCopyCollection}）。</p>
     *
     * @param toClass  目标类（按声明类型求得）
     * @param fromKind 源容器形态（{@code Collection} 或 {@code Map}）
     * @return true 表示可写回
     */
    static boolean canWriteBackContainer(Class<?> toClass, Class<?> fromKind) {

        // 未声明/宽声明：结果按源形态返回，写回由 setter 兜底
        if(null == toClass || Object.class == toClass) {
            return true;
        }

        // 目标声明为数组：容器 → 数组属已支持的跨形态转换（按目标组件类型重建数组，
        // 见 deepCopyCollectionToArray），不在此拦下
        if(toClass.isArray()) {
            return true;
        }

        return fromKind.isAssignableFrom(toClass) || toClass == fromKind;
    }

    /**
     * 元素能否写入目标数组组件类型（基本类型按其包装类型判定，其余按可赋性判定）
     *
     * @param componentType 目标数组组件类型
     * @param element       元素（非 null）
     * @return true 表示可写入
     */
    static boolean isAssignableToComponent(Class<?> componentType, Object element) {

        if(componentType.isPrimitive()) {
            return wrapperOf(componentType).isInstance(element);
        }
        // 目标组件为 Object（或元素类型的父类型）：一律可写
        return componentType.isInstance(element) || Object.class == componentType;
    }

    /**
     * 基本类型 → 包装类型（数组元素装箱后按包装类型判定可赋性）
     *
     * @param primitive 基本类型
     * @return 对应包装类型；非基本类型原样返回
     */
    static Class<?> wrapperOf(Class<?> primitive) {

        if(boolean.class == primitive) { return Boolean.class; }
        if(byte.class == primitive) { return Byte.class; }
        if(char.class == primitive) { return Character.class; }
        if(short.class == primitive) { return Short.class; }
        if(int.class == primitive) { return Integer.class; }
        if(long.class == primitive) { return Long.class; }
        if(float.class == primitive) { return Float.class; }
        if(double.class == primitive) { return Double.class; }

        return primitive;
    }

    /**
     * 数组 → 容器：按目标声明类型重建容器，元素按声明泛型/运行时类型递归深拷贝
     *
     * <p>数组与集合在"有序、按元素遍历"这一语义上同构，故跨形态转换不丢数据；
     * 目标声明为 {@code Set} 时按插入序去重（元素为副本，去重按副本的相等性）。</p>
     *
     * @param from         源数组
     * @param declaredType 目标字段声明泛型（元素类型解析用）
     * @param toClass      目标容器类型（接口或实现类）
     * @param visited      已拷贝身份表
     * @param depth        当前深度
     * @return 新容器
     */
    static Object deepCopyArrayToContainer(
            Object from, Type declaredType, Class<?> toClass,
            IdentityHashMap<Object, Object> visited, int depth
    ) {

        val length = Array.getLength(from);
        val containerClass = containerFor(toClass, ArrayList.class);
        val elements = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            elements.add(Array.get(from, i));
        }

        return deepCopyElementsToContainer(elements, declaredType, containerClass, visited, depth);
    }

    /**
     * 元素集合 → 目标容器：按声明泛型/运行时类型递归深拷贝后写入
     *
     * <p>数组→容器、Map.Entry→容器等"已物化为元素列表"的路径共用：容器按目标声明类型创建
     * （见 {@link #containerFor}），元素类型优先取声明泛型、解析不到时按元素运行时类型判定。</p>
     *
     * @param elements    源元素（顺序即写入顺序）
     * @param declaredType 声明类型（元素类型解析用）
     * @param containerClass 目标容器实现类
     * @param visited     已拷贝身份表
     * @param depth       当前深度
     * @return 新容器
     */
    @SuppressWarnings("unchecked")
    static Object deepCopyElementsToContainer(
            Collection<?> elements, Type declaredType, Class<?> containerClass,
            IdentityHashMap<Object, Object> visited, int depth
    ) {

        val copy = (Collection<Object>) newDeepCopyContainer(containerClass);
        // 实参解析与不可变判定按声明泛型缓存（见 typeArgumentMemo）
        val elementMemo = typeArgumentMemo(declaredType, 0);
        val elementType = elementMemo.type;
        val elementImmutable = elementMemo.immutable;
        for (val element : elements) {
            Object copyElement = element;
            if(!elementImmutable) {
                copyElement = deepCopyValue(element, elementType, visited, depth + 1);
            }
            copy.add(copyElement);
        }

        return copy;
    }

    /**
     * 目标实现构造失败时的兜底容器：按目标实现类所属接口族取"有序优先"的标准实现
     *
     * <p>与 {@link #resolveContainerFor} 的"按目标声明类型选实现"同源——兜底结果仍要写回目标声明，
     * 故族必须取自目标实现类而非源容器；目标不属任何容器族时退化为 {@link ArrayList}
     * （{@link #containerFor} 一旦判出实现类，其族必已确定，此分支仅作不可达兜底）。</p>
     *
     * @param container 目标实现类（{@link #containerFor} 的输出，非 null）
     * @return 兜底容器实例
     */
    static Object fallbackContainer(Class<?> container) {

        if(Set.class.isAssignableFrom(container)) {
            return new LinkedHashSet<>();
        }
        if(Map.class.isAssignableFrom(container)) {
            return new LinkedHashMap<>();
        }
        if(Queue.class.isAssignableFrom(container) || Deque.class.isAssignableFrom(container)) {
            return new ArrayDeque<>();
        }

        return new ArrayList<>();
    }

    /**
     * 按目标容器类型直接创建容器（不依赖源容器）：跨形态转换（数组/Entry → 容器）的落点
     *
     * <p>与 {@link #newContainer(Object, Class)} 的分工：后者需要源容器来决定
     * "专用容器保语义 / 有序容器保 comparator / 同源实现优先"，本条只有目标类型，
     * 故按目标声明类型建立空容器（语义见 {@link #containerFor}）。</p>
     *
     * @param toClass 目标容器实现类
     * @return 空容器实例
     */
    static Object newDeepCopyContainer(Class<?> toClass) {

        val container = containerFor(toClass, List.class);
        // 标准实现直接构造（热点快路径：省去可按类实例化查表与构造句柄调用）
        val standard = newStandardContainer(container, container);
        if(null != standard) {
            return standard;
        }

        val instance = newDeepCopyInstance(container);
        if(null != instance) {
            return instance;
        }

        // 兜底：走到这里说明目标实现类的构造体抛异常（已登记"不支持的目标类集合"）。
        // 改按目标接口族的标准实现重建，使调用方仍拿得到可用容器（容器语义"有顺序、可迭代"总成立）
        val fallback = containerFor(container, List.class);
        Object fallbackInstance = null;
        if(fallback != container) {
            fallbackInstance = newDeepCopyInstance(fallback);
        }
        if(null != fallbackInstance) {
            return fallbackInstance;
        }

        // 目标实现类也无从构造 ⇒ 退回该目标接口族的标准实现（顺序集合 / 不可迭代时的 List 兜底）
        if(Set.class.isAssignableFrom(container)) {
            return new LinkedHashSet<>();
        }
        return new ArrayList<>();
    }

    /**
     * 迭代器 / 原始流判定：这类值不可复用、但迭代顺序稳定，可据此重建目标容器
     *
     * <p>{@link Iterator}（含集合的 {@code iterator()}）、{@code java.util.stream.BaseStream}
     * （{@code Stream} / {@code IntStream} / {@code LongStream} / {@code DoubleStream}）没有无参构造、
     * 不是集合，按 {@link CBeanUtils#isDeepCopyCapable} 之外的路径会被当作"不可实例化 Bean"退化为共享引用——
     * 共享的是一个一次性、对齐到源集合状态的视图，既非副本也不可复用（原实现的静默降级点）。</p>
     *
     * <p>本方法把它们收敛为"可重新迭代的集合"：调用方据此按目标声明类型重建标准容器
     * （{@code Iterable} 形态，见 {@link #deepCopyIterable}）。视图类（{@code Collections.unmodifiableList}
     * 等）不在此列——它们本身就是集合，走常规集合深拷贝。</p>
     *
     * @param from 源值
     * @return 可重新迭代的集合；不属迭代器/流时返回 {@code null}
     */
    static Collection<?> viewedIterable(Object from) {

        if(from instanceof Collection) {
            return null;
        }
        if(from instanceof Iterator) {
            return collectIterator((Iterator<?>) from);
        }
        if(from instanceof BaseStream) {
            return collectStream((BaseStream<?, ?>) from);
        }

        return null;
    }

    /**
     * 迭代器物化为集合（源迭代器因此被消费，属预期：调用方本就要按迭代顺序取值）
     *
     * @param from 源迭代器
     * @return 迭代顺序的集合（{@link ArrayList}）
     */
    static Collection<?> collectIterator(Iterator<?> from) {

        val collected = new ArrayList<>();
        from.forEachRemaining(collected::add);
        return collected;
    }

    /**
     * 流物化为集合（源流因此被消费、无法再次使用，属预期：流本身即一次性）
     *
     * <p>对象流与三种原始流分派：{@link BaseStream} 只声明 {@code close}/{@code iterator} 等共同能力，
     * {@code forEach} 在各子类型上签名不同（原始流按 {@code IntConsumer} 等），故按具体类型分派——
     * 不可通用地调用 {@code forEach}（编译期即不成立）；原始流元素自动装箱为包装类型，
     * 与"元素按运行时类型处理"一致。</p>
     *
     * @param from 源流
     * @return 流的元素集合（{@link ArrayList}）
     */
    static Collection<?> collectStream(BaseStream<?, ?> from) {

        val collected = new ArrayList<>();
        if(from instanceof Stream) {
            ((Stream<?>) from).forEach(collected::add);
        } else if(from instanceof IntStream) {
            ((IntStream) from).forEach(collected::add);
        } else if(from instanceof LongStream) {
            ((LongStream) from).forEach(collected::add);
        } else if(from instanceof DoubleStream) {
            ((DoubleStream) from).forEach(collected::add);
        }

        return collected;
    }

    /**
     * 迭代器 / 流的深拷贝：按目标声明类型重建标准容器，元素按声明泛型/运行时类型递归
     *
     * <p><b>重建优先于共享</b>：源是"一次性视图"，共享回去既不是副本也不可复用（见
     * {@link #viewedIterable}），故按目标声明类型重建；目标类型装不下（如声明为无关接口）时按
     * {@link #containerFor} 的兜底规则降级为最贴近的标准实现，不再整体跳过——容器语义
     * 「有元素顺序、可重新迭代」在重建后总成立。</p>
     *
     * @param from         物化后的源集合
     * @param declaredType 目标字段声明泛型（元素类型解析用）
     * @param fromClass    源运行时类（日志）
     * @param visited      已拷贝身份表
     * @param depth        当前深度
     * @return 重建后的容器
     */
    static Object deepCopyIterable(
            Collection<?> from, Type declaredType, Class<?> fromClass,
            IdentityHashMap<Object, Object> visited, int depth
    ) {

        val rawTarget = rawDeepCopyClass(declaredType);
        val toClass = containerFor(rawTarget, List.class);

        log.debug("迭代器/流按目标声明类型重建容器：{} → {}", fromClass.getName(), toClass.getName());

        return deepCopyCollection(from, declaredType, toClass, visited, depth);
    }

    /**
     * 目标容器实现选择：目标声明类型可用即用，否则按目标接口族取最贴近的标准实现
     *
     * <p><b>跨接口容器按目标声明类型重建</b>（本类的容器语义）：源与目标接口形态不一致时
     * （如源 {@code List} → 目标 {@code Set} 声明，或源 {@code Collection} → 目标 {@code List}），
     * 拷贝结果<b>按目标声明类型创建</b>，于是写回目标接口天然成立，无需"跳过不写入"。</p>
     *
     * <p><b>有序优先</b>：目标为无序实现（{@code HashSet} / {@code HashMap}）时改用对应的
     * <b>插入序实现</b>（{@code LinkedHashSet} / {@code LinkedHashMap}）——拷贝是"按源顺序逐元素搬运"，
     * 保留插入序使结果可预期、可复现；同时注明保序语义取自源迭代顺序，非源的 comparator 语义
     * （目标为 {@code TreeSet} / {@code TreeMap} 这类有序实现时按目标的有序语义重建）。</p>
     *
     * @param target   目标声明类型（可空）
     * @param fallback 目标不可用且无法判定族时的兜底实现
     * @return 容器实现类
     */
    static Class<?> containerFor(Class<?> target, Class<?> fallback) {

        if(null == target || Object.class == target) {
            return fallback;
        }

        // 目标派生部分（可用实现类 / 按接口族的标准实现）与 fallback 无关，按目标类缓存：
        // 该判定含 assignable 链与"可按类实例化"查表，是深拷贝热点上的固定成本（实测每次约 50ns）
        val derived = CONTAINER_FOR_CLASS_VALUE.get(target);
        if(null != derived) {
            return derived;
        }
        return fallback;
    }

    /**
     * 目标容器实现选择缓存：按目标声明类型缓存「可用实现类 / 按接口族的标准实现」
     *
     * <p>判定结果只依赖目标类（{@code fallback} 仅在"目标无法判定族"时兜底，不进缓存），
     * 故可安全按类缓存——深拷贝热点每次建容器都走此判定，缓存后由"assignable 链 + 实例化查表"
     * 降为一次 {@code ClassValue} 查表。</p>
     */
    static final CClassValue<Class<?>> CONTAINER_FOR_CLASS_VALUE =
            CClassValue.of(CDeepCopyUtils::resolveContainerFor);

    /**
     * 目标容器实现的实际选择（仅首次按目标类执行，见 {@link #CONTAINER_FOR_CLASS_VALUE}）
     *
     * @param target 目标声明类型（非 null、非 Object，调用方已判）
     * @return 容器实现类；无法判定族时返回 {@code null}（由调用方按 fallback 处置）
     */
    static Class<?> resolveContainerFor(Class<?> target) {

        val usable = usableContainerClass(target);
        if(null != usable) {
            return usable;
        }

        // 目标为接口/抽象类/不可实例化：按目标接口族选"有序优先"的标准实现
        if(Set.class.isAssignableFrom(target)) {
            return LinkedHashSet.class;
        }
        if(Map.class.isAssignableFrom(target)) {
            return LinkedHashMap.class;
        }
        if(Queue.class.isAssignableFrom(target) || Deque.class.isAssignableFrom(target)) {
            return ArrayDeque.class;
        }
        if(List.class.isAssignableFrom(target) || Collection.class.isAssignableFrom(target)
                || Iterable.class.isAssignableFrom(target)) {
            return ArrayList.class;
        }

        return null;
    }

    /**
     * 目标容器实现类的可用判定：抽象/接口/不可实例化一律不可用（交回 {@link #containerFor} 按接口族降级）
     *
     * <p>{@code HashSet} / {@code HashMap} 属"无序标准实现"：可用性上仍算可用（调用方显式声明即尊重其语义），
     * 是否改取插入序实现由 {@link #containerFor} 的调用点决定，不在此处改写用户的显式声明类型。</p>
     *
     * @param target 目标声明类型（可空）
     * @return 可实例化的实现类；不可用时返回 {@code null}
     */
    static Class<?> usableContainerClass(Class<?> target) {

        if(null == target || Object.class == target || target.isInterface() || Modifier.isAbstract(target.getModifiers())) {
            return null;
        }

        if(INSTANTIABLE_CLASS_VALUE.get(target)) {
            return target;
        }
        return null;
    }

    /**
     * 集合深拷贝：容器同源优先 + 接口降级，元素按声明泛型/运行时类型递归
     *
     * @param from         源集合
     * @param declaredType 声明类型
     * @param toClass      目标实现类
     * @param visited      已拷贝身份表
     * @param depth        当前深度
     * @return 新集合
     */
    @SuppressWarnings("unchecked")
    static Object deepCopyCollection(
            Collection<?> from, Type declaredType, Class<?> toClass,
            IdentityHashMap<Object, Object> visited, int depth
    ) {

        // 目标声明为数组（如源 List<Inner> → 目标 Inner[] 声明）：按元素重建数组。
        // 写入目标数组天然成立，不再因"集合无法赋给数组"而整条跳过（丢字段）
        if(null != toClass && toClass.isArray()) {
            return deepCopyCollectionToArray(from, toClass.getComponentType(), visited, depth);
        }

        val copy = (Collection<Object>) newContainer(from, toClass);
        visited.put(from, copy);

        // 元素类型确定不可变（如 List<String>）：直接加入，省去逐元素深拷贝分派（不可变值的拷贝结果即自身）；
        // 实参解析与不可变判定按声明泛型缓存（见 typeArgumentMemo），热路径只是一次查表
        val element = typeArgumentMemo(declaredType, 0);
        val elementType = element.type;
        val elementImmutable = element.immutable;
        for (val item : from) {
            Object copyItem = item;
            if(!elementImmutable) {
                copyItem = deepCopyValue(item, elementType, visited, depth + 1);
            }
            copy.add(copyItem);
        }

        return copy;
    }

    /**
     * 容器 → 数组：按目标数组组件类型重建，元素递归深拷贝
     *
     * <p>容器与数组在"有序、按元素遍历"这一语义上同构，故跨形态转换不丢数据；
     * 元素装入目标组件类型不下的槽位时跳过该槽位（不抛 {@code ArrayStoreException} 整体失败），
     * 与 {@link #deepCopyArray} 的组件类型转换口径一致。</p>
     *
     * @param from          源容器
     * @param componentType 目标数组组件类型
     * @param visited       已拷贝身份表
     * @param depth         当前深度
     * @return 新数组
     */
    static Object deepCopyCollectionToArray(
            Collection<?> from, Class<?> componentType,
            IdentityHashMap<Object, Object> visited, int depth
    ) {

        val length = from.size();
        val copy = Array.newInstance(componentType, length);
        visited.put(from, copy);

        int index = 0;
        // 元素不可变且组件类型接得住时直接整段赋值（省去逐元素分派）
        val immutableFastPath = componentType.isPrimitive() || isDefinitelyImmutable(componentType);
        for (val element : from) {
            if(null != element && !isAssignableToComponent(componentType, element)) {
                log.debug("容器元素类型 {}({}) 无法装入目标组件类型 {}，跳过该槽位：{}",
                        element.getClass().getName(), element, componentType.getName(), index);
                index++;
                continue;
            }
            Object copyElement = element;
            if(!immutableFastPath) {
                copyElement = deepCopyValue(element, componentType, visited, depth + 1);
            }
            Array.set(copy, index++, copyElement);
        }

        return copy;
    }

    /**
     * Map.Entry 深拷贝：按目标声明类型物化——Map 声明建单条记录，容器/数组声明物化"键、值"两项
     *
     * <p>{@link Map.Entry} 是"单个键值对"的视图，本身（{@code AbstractMap.SimpleEntry} 等）虽有
     * 无参构造但字段语义与目标 Map 不同，直接按 Bean 拷贝会丢内容。故按目标声明类型分派：</p>
     * <ul>
     *   <li>目标声明为 {@code Map}（含实现类）⇒ 建同型 Map 并放入拷后的键值对（{@link #deepCopyEntryToMap}）；</li>
     *   <li>目标声明为容器/数组 ⇒ 物化为 {@code [键, 值]} 两项（{@link #deepCopyElementsToContainer} /
     *   {@link #deepCopyCollectionToArray}）；</li>
     *   <li>其余（含目标声明为 {@code Entry}/{@code Object}）⇒ 按键值对语义建 {@code SimpleEntry}。</li>
     * </ul>
     *
     * @param from         源 Entry
     * @param declaredType 目标字段声明泛型
     * @param toClass      目标类
     * @param visited      已拷贝身份表
     * @param depth        当前深度
     * @return 副本
     */
    static Object deepCopyEntry(
            Map.Entry<?, ?> from, Type declaredType, Class<?> toClass,
            IdentityHashMap<Object, Object> visited, int depth
    ) {

        // 键/值声明类型：Map<K,V> 声明时取 (键=第 0 参, 值=第 1 参)；容器声明时取值类型为元素类型
        // 实参解析按声明泛型缓存（见 typeArgumentMemo）
        val keyType = typeArgumentMemo(declaredType, 0).type;
        val valueType = typeArgumentMemo(declaredType, 1).type;
        val key = deepCopyValue(from.getKey(), keyType, visited, depth + 1);
        val value = deepCopyValue(from.getValue(), valueType, visited, depth + 1);

        if(null != toClass && Map.class.isAssignableFrom(toClass) && toClass != from.getClass()) {
            return deepCopyEntryToMap(key, value, toClass);
        }
        if(null != toClass && toClass.isArray() && !toClass.isInstance(from)) {
            return deepCopyCollectionToArray(Arrays.asList(key, value), toClass.getComponentType(), visited, depth);
        }
        if(null != toClass && !toClass.isArray() && !Map.class.isAssignableFrom(toClass)
                && !Map.Entry.class.isAssignableFrom(toClass) && Collection.class.isAssignableFrom(toClass)) {
            return deepCopyElementsToContainer(Arrays.asList(key, value), declaredType, containerFor(toClass, List.class), visited, depth);
        }

        return new AbstractMap.SimpleEntry<>(key, value);
    }

    /**
     * Map.Entry → Map：建同型 Map（声明为具体实现类时按其重建）并放入已拷好的键值对
     *
     * @param key     已拷好的键
     * @param value   已拷好的值
     * @param toClass 目标 Map 类型
     * @return 单条记录的 Map
     */
    @SuppressWarnings("unchecked")
    static Object deepCopyEntryToMap(Object key, Object value, Class<?> toClass) {

        val instance = newDeepCopyContainer(toClass);
        if(instance instanceof Map) {
            ((Map<Object, Object>) instance).put(key, value);
            return instance;
        }

        val map = new LinkedHashMap<Object, Object>();
        map.put(key, value);
        return map;
    }

    /**
     * 时间深拷贝：按目标声明类型/源运行时类型重建，保持具体子类语义
     *
     * <p>{@code java.sql.Date}（仅日期）/ {@code Time}（仅时间）/ {@code Timestamp}（含纳秒）
     * 与 {@code java.util.Date} 的字段语义不同：一律降级为 {@code java.util.Date} 会丢类型语义
     * （写回 {@code java.sql.*} 声明字段时 {@code ClassCastException}、写回 {@code Object} 声明时
     * 值类型静默变化）。故按"目标声明类型能接住源运行时类型"时取目标类型、否则取源运行时类型。</p>
     *
     * @param from    源时间值
     * @param toClass 目标类（可空/可为 {@code java.util.Date} 等父类型）
     * @return 同语义副本
     */
    static Object newDate(Object from, Class<?> toClass) {

        val time = ((Date) from).getTime();
        val fromClass = from.getClass();

        Class<?> targetClass = fromClass;
        if(null != toClass && Date.class.isAssignableFrom(toClass)
                && toClass.isInstance(from)) {
            targetClass = toClass;
        }

        if(java.sql.Timestamp.class == targetClass) {
            // Timestamp 的 getTime() 含毫秒、nanos 需单独取回：否则纳秒精度丢失
            val timestamp = new java.sql.Timestamp(time);
            timestamp.setNanos(((java.sql.Timestamp) from).getNanos());
            return timestamp;
        }
        if(java.sql.Date.class == targetClass) {
            return new java.sql.Date(time);
        }
        if(java.sql.Time.class == targetClass) {
            return new java.sql.Time(time);
        }

        return new Date(time);
    }

    /**
     * Map 深拷贝：键与值均递归拷贝
     *
     * @param from         源 Map
     * @param declaredType 声明类型
     * @param toClass      目标实现类
     * @param visited      已拷贝身份表
     * @param depth        当前深度
     * @return 新 Map
     */
    @SuppressWarnings("unchecked")
    static Object deepCopyMap(
            Map<?, ?> from, Type declaredType, Class<?> toClass,
            IdentityHashMap<Object, Object> visited, int depth
    ) {

        val copy = (Map<Object, Object>) newContainer(from, toClass);
        visited.put(from, copy);

        // 键/值类型确定不可变（如 Map<String,String>）：直接放入，省去逐项深拷贝分派；
        // 实参解析与不可变判定按声明泛型缓存（见 typeArgumentMemo）
        val keyMemo = typeArgumentMemo(declaredType, 0);
        val valueMemo = typeArgumentMemo(declaredType, 1);
        val keyType = keyMemo.type;
        val valueType = valueMemo.type;
        val keyImmutable = keyMemo.immutable;
        val valueImmutable = valueMemo.immutable;

        for (val entry : from.entrySet()) {
            Object key = entry.getKey();
            if(!keyImmutable) {
                key = deepCopyValue(key, keyType, visited, depth + 1);
            }
            Object value = entry.getValue();
            if(!valueImmutable) {
                value = deepCopyValue(value, valueType, visited, depth + 1);
            }
            copy.put(key, value);
        }

        return copy;
    }

    /**
     * JavaBean 深拷贝：按同名字段配对；目标类不可实例化时按降级规则处置
     *
     * <p><b>详细设计</b>：复用 {@link CBeanUtils#copyWithPlan(Object, Object, IdentityHashMap, int)}（即
     * {@link CBeanUtils#copy(Object, Object)} 的同一套计划流程），不另写一套字段配对——字段集合、final/static 剔除、
     * 类型转换与深拷贝规则因此与 {@code copy} 完全一致，避免两套实现行为漂移；
     * 目标类由 {@link #resolveDeepCopyTargetClass(Type, Class)} 决定（跨类型时即目标声明类型）。</p>
     *
     * <p><b>按目标类折叠的深拷贝计划</b>（{@link #DEEP_PLAN_CLASS_VALUE}）：本方法所需的两个"按类恒定"
     * 判定——目标类<b>是否含 final 实例字段</b>、<b>是否可实例化</b>（接口/抽象类/视图封装/无无参构造，
     * 内部还含视图/不可变类名前缀扫描）——原先在运行期各查一次 {@code ClassValue}；现按目标类在
     * <b>首次深拷贝该类时</b>一次性折叠为 {@link DeepPlan}，运行期只做<b>一次</b> {@code ClassValue} 查表
     * （详见该类 javadoc 的「深拷贝按类折叠计划」；实测收益与测量口径见
     * {@code doc/design/core/perf/beanutils-perf.adoc}）。「构造体抛异常的不支持目标类」不并入折叠
     * （运行期可变状态，理由见 {@link DeepPlan}），仍在此查一次 {@link #UNSUPPORTED_TARGET_CLASSES}。</p>
     *
     * <p><b>不可实例化的两条降级路径</b>（区分清楚，勿混）：</p>
     * <ul>
     *   <li>目标类<b>本来就没有可用无参构造</b>（接口/抽象类/仅带参构造/lambda/视图封装）⇒
     *   计划期即判出并打一次 debug ⇒ <b>共享引用</b>
     *   （这类类型多为"不可变视图/合成类"，共享比丢字段安全）；</li>
     *   <li>目标类<b>有无参构造、但构造体抛异常</b>（计划期判不出）⇒ 登记进
     *   {@link #UNSUPPORTED_TARGET_CLASSES}（首次 error、其后直接跳过）⇒ <b>该字段跳过不写入</b>
     *   （保持目标对象既有值）——这类是代码/配置缺陷，共享出去等于把"构造不出来的对象"继续传播。</li>
     * </ul>
     *
     * @param from    源对象
     * @param toClass 目标类
     * @param visited 已拷贝身份表
     * @param depth   当前深度
     * @return 副本；共享引用或 {@link #SKIP_VALUE}
     */
    static Object deepCopyBean(Object from, Class<?> toClass, IdentityHashMap<Object, Object> visited, int depth) {

        val plan = DEEP_PLAN_CLASS_VALUE.get(toClass);

        // 含 final 实例字段：既有 copy 契约不写 final 字段，结构拷贝会得到"部分字段为空"的对象（静默丢数据）
        // ⇒ 整体退化为共享引用（计划期判定、只打印一次 debug）
        if(plan.sharedSource) {
            return from;
        }

        // 「构造体抛异常」已登记的目标类：该字段跳过不写入（不再尝试、不再报错）。
        // 该项是运行期可变状态（登记时刻晚于计划创建时刻），故在运行期查、不进按类折叠的计划
        if(UNSUPPORTED_TARGET_CLASSES.containsKey(toClass)) {
            return SKIP_VALUE;
        }

        val to = newDeepCopyInstance(toClass, plan);
        if(null == to) {
            // 无可用无参构造（计划期判出、按类打一次 debug）⇒ 共享引用；
            // 构造体抛异常（计划期判不出、已登记）⇒ 跳过不写入
            if(UNSUPPORTED_TARGET_CLASSES.containsKey(toClass)) {
                return SKIP_VALUE;
            }
            return from;
        }

        // 身份表登记须先于字段复制：自引用字段据此指向副本自身
        visited.put(from, to);

        return CBeanUtils.copyWithPlan(from, to, visited, depth);
    }

    /**
     * 深拷贝按目标类折叠的计划：把"按类恒定"的三项判定折叠为一次查表
     *
     * <p><b>为什么需要</b>：{@link #deepCopyBean(Object, Class, IdentityHashMap, int)} 原先在运行期连续查
     * 三个按类恒定的判定——{@link #HAS_FINAL_FIELD_CLASS_VALUE}（含 final 实例字段）、
     * {@link #INSTANTIABLE_CLASS_VALUE}（可否无参构造，内部还含视图/不可变前缀扫描）、
     * {@link #UNSUPPORTED_TARGET_CLASSES}（{@code ConcurrentHashMap#containsKey}，构造体抛异常的目标类）。
     * 三者都只依赖目标类、且一次深拷贝里会按层级重复命中，属可折叠的重复成本。</p>
     *
     * <p><b>本计划的内容</b>：{@link #sharedSource}（含 final ⇒ 共享引用）、
     * {@link #instantiable}（可否无参构造）。折叠后运行期为<b>一次</b> {@code ClassValue} 查表。</p>
     *
     * <p><b>为何「构造体抛异常」不进本计划</b>：它是<b>运行期可变状态</b>——计划期判不出（须"试一次才知道"），
     * 且登记时刻可能晚于计划创建时刻（先建计划、后失败登记），按类缓存会把"登记前"的取值固化下来、
     * 此后永远看不到登记结果。故保持为独立的运行期登记集合 {@link #UNSUPPORTED_TARGET_CLASSES}，
     * 由 {@link #deepCopyBean(Object, Class, IdentityHashMap, int)} 在运行期查。</p>
     */
    private static final class DeepPlan {

        /** 目标类含 final 实例字段 ⇒ 共享引用（既有 copy 契约不写 final 字段） */
        final boolean sharedSource;

        /** 目标类可用无参构造实例化 */
        final boolean instantiable;

        DeepPlan(boolean sharedSource, boolean instantiable) {
            this.sharedSource = sharedSource;
            this.instantiable = instantiable;
        }
    }

    /**
     * 按目标类缓存深度拷贝计划（{@link DeepPlan}）：首次深拷贝该类时计算并折叠，
     * 其后每次深拷贝只做一次 {@code ClassValue} 查表（判定结果按类恒定）
     */
    static final CClassValue<DeepPlan> DEEP_PLAN_CLASS_VALUE = CClassValue.of(CDeepCopyUtils::computeDeepPlan);

    /**
     * 计算目标类的深拷贝计划（仅首次按类执行）
     *
     * @param type 目标类（非 null）
     * @return 折叠后的深拷贝计划
     */
    static DeepPlan computeDeepPlan(Class<?> type) {
        return new DeepPlan(
                HAS_FINAL_FIELD_CLASS_VALUE.get(type),
                INSTANTIABLE_CLASS_VALUE.get(type)
        );
    }

    /**
     * 创建目标容器：专用容器保语义 → 有序容器保 comparator → 目标声明实现优先 → 按目标接口族降级
     *
     * <p><b>目标声明类型优先</b>（跨接口容器的落点）：{@code toClass} 由
     * {@link #resolveDeepCopyTargetClass(Type, Class)} 与 {@link #containerFor(Class, Class)}
     * 按<b>目标字段声明类型</b>求出，故源 {@code List} → 目标 {@code Set} 声明时在此建出
     * {@code LinkedHashSet}、源 {@code Collection} → 目标 {@code List} 声明时建出 {@code ArrayList}，
     * 写回目标接口天然成立（不再"跳过不写入"）。目标声明为具体实现类（如 {@code LinkedList}）时同样按其重建。</p>
     *
     * <p><b>专用/有序容器先于目标声明</b>：{@code EnumSet} / {@code EnumMap} 的键值类型语义、
     * {@code SortedMap} / {@code SortedSet} 的 comparator 语义是"拷贝后必须仍是同一语义"的硬约束，
     * 故优先于按目标声明类型重建；两边都成立时（目标声明与源容器族一致）结果相同，不产生歧义。</p>
     *
     * @param from    源容器
     * @param toClass 目标实现类（按目标声明类型求得）
     * @return 新容器实例
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static Object newContainer(Object from, Class<?> toClass) {

        if(from instanceof EnumSet) {
            return EnumSet.copyOf((EnumSet) from);
        }
        if(from instanceof EnumMap) {
            // 空与非空一律走 EnumMap(EnumMap) 构造：保留键类型（EnumMap(Map) 对空源无法推断键类型会抛异常，
            // 且降级为标准 Map（LinkedHashMap）无法写回 EnumMap 声明字段，写回时 ClassCastException）
            return new EnumMap((EnumMap) from);
        }

        // 有序容器必须保留 comparator，优先于"目标声明实现"（按目标无参构造会丢排序语义）
        if(from instanceof SortedMap) {
            return new TreeMap<>((Comparator) ((SortedMap<?, ?>) from).comparator());
        }
        if(from instanceof SortedSet) {
            return new TreeSet<>((Comparator) ((SortedSet<?>) from).comparator());
        }

        // 目标声明实现优先：跨接口容器（List 源 → Set 目标声明）在此按目标类型建容器；
        // 目标为具体实现类时据此重建（父类 → 子类实现类），不再用源实现类兜底
        Class<?> containerFamily = Collection.class;
        if(from instanceof Map) {
            containerFamily = Map.class;
        }
        val container = containerFor(toClass, containerFamily);

        // 标准实现直接构造（深拷贝热点）：省去"可按类实例化"查表与构造句柄调用，
        // 并按源大小预分配容量；语义与反射路径一致（同为空容器）
        val standard = newStandardContainer(from, container);
        if(null != standard) {
            return standard;
        }

        val instance = newDeepCopyInstance(container);
        if(null != instance) {
            return instance;
        }

        // 兜底：containerFor 已保证返回可实例化实现，走到这里说明其构造失败（构造体内抛异常等），
        // 由 newDeepCopyInstance 按"不支持的目标类集合"登记并跳过，此处按"目标声明类型"的接口族
        // 取标准实现——兜底结果仍要写回目标声明，按<b>源容器族</b>兜底会与目标族不一致
        // （如源 List → 目标 Set 声明时兜底成 ArrayList，写回仍是 ClassCastException）
        return fallbackContainer(container);
    }

    /**
     * 标准容器直接构造快路径（仅当源实现类与目标实现类一致时生效）
     *
     * <p>深拷贝热点上的容器创建：{@code ArrayList}/{@code HashMap}/{@code HashSet}/{@code LinkedHashMap}/
     * {@code LinkedHashSet} 直接 {@code new}（并按键/元素数预分配容量），省去"可按类实例化"查表与构造句柄调用；
     * 其余实现（含自定义实现、需要保 comparator 的有序容器）返回 {@code null}，交回反射路径处理。</p>
     *
     * @param from    源容器
     * @param toClass 目标实现类
     * @return 空容器实例；非标准实现返回 {@code null}
     */
    static Object newStandardContainer(Object from, Class<?> toClass) {

        // 源实现类与目标实现类不一致时交回反射路径（保持原容器实现语义）
        if(from.getClass() != toClass) {
            return null;
        }

        int size = 0;
        if(from instanceof Collection) {
            size = ((Collection<?>) from).size();
        }
        int mapSize = 0;
        if(from instanceof Map) {
            mapSize = ((Map<?, ?>) from).size();
        }

        if(ArrayList.class == toClass) {
            return new ArrayList<>(size);
        }
        if(LinkedHashMap.class == toClass) {
            return new LinkedHashMap<>(mapSize);
        }
        if(HashMap.class == toClass) {
            return new HashMap<>(mapSize);
        }
        if(LinkedHashSet.class == toClass) {
            return new LinkedHashSet<>(size);
        }
        if(HashSet.class == toClass) {
            return new HashSet<>(size);
        }

        return null;
    }

    /**
     * 可按类实例化判定缓存（判定按类恒定，含无参构造存在性检查）
     *
     * <p>接口/抽象类、视图与不可变封装、无无参构造的类（lambda、record、仅带参构造的类）不可实例化；
     * 判定结果按类缓存后，这类值每次深拷贝都直接退化为共享引用——不再反复反射尝试、不再反复抛异常。</p>
     */
    static final CClassValue<Boolean> INSTANTIABLE_CLASS_VALUE = CClassValue.of(CDeepCopyUtils::checkInstantiable);

    /**
     * 不支持的目标类集合：准备阶段判不出来、实例化时才失败的类
     *
     * <p><b>为什么需要</b>：{@link #checkInstantiable} 只能在<b>准备阶段</b>判"有无可用无参构造"；
     * 另一类目标类<b>有无参构造、但构造体必定抛异常</b>（如内部 `throw new UnsupportedOperationException()`、
     * 依赖未注入的字段而 NPE）——这类无法靠反射预判，只能"试一次才知道"。</p>
     *
     * <p><b>不加本集合会怎样</b>：每次深拷贝到该类型都要重新反射构造、重新抛异常、重新打日志——
     * 热点路径上退化成"每次一个异常"的稳定开销（异常构造含栈回溯，成本远高于普通分支）。</p>
     *
     * <p><b>本集合的语义</b>：首次实例化失败即登记并打一次 error 日志（可观测、可排查）；
     * 其后任何对象再碰到该类型直接按"不可拷贝"跳过，不再尝试实例化、不再重复报错。
     * 用 {@link ConcurrentHashMap} 的 keySet 保证并发安全与幂等（同一类只登记一次）。</p>
     *
     * <p><b>为什么是 error 而非 debug</b>：与"无无参构造"（设计内降级，debug）不同，
     * 构造体抛异常属<b>代码/配置缺陷</b>（类自称可实例化却构造不出来），需在日志里可见；
     * 但只报一次，避免热路径刷屏。</p>
     */
    static final ConcurrentMap<Class<?>, Boolean> UNSUPPORTED_TARGET_CLASSES = new ConcurrentHashMap<>();

    /**
     * 按类实例化（视图/不可变封装、接口与抽象类、无无参构造的类不尝试）
     *
     * <p>与 {@link #newDeepCopyInstance(Class, DeepPlan)} 的分工：本重载供<b>容器创建路径</b>
     * （目标类不是 Bean、无"含 final 字段"语义）使用，自行取一次计划；深拷贝 Bean 路径
     * 由 {@link #deepCopyBean(Object, Class, IdentityHashMap, int)} 传入已查到的计划，不重复查表。</p>
     *
     * @param type 类型
     * @return 实例或 null（不可实例化 / 构造失败）
     */
    static Object newDeepCopyInstance(Class<?> type) {
        return newDeepCopyInstance(type, DEEP_PLAN_CLASS_VALUE.get(type));
    }

    /**
     * 按类实例化（接收调用方已查到的 {@link DeepPlan}，避免重复查表）
     *
     * @param type 类型
     * @param plan 该类型的深拷贝计划（调用方已查出，非 null）
     * @return 实例或 null（不可实例化 / 构造失败）
     */
    static Object newDeepCopyInstance(Class<?> type, DeepPlan plan) {

        if(null == type || !plan.instantiable || UNSUPPORTED_TARGET_CLASSES.containsKey(type)) {
            return null;
        }

        try {
            return CReflectUtils.newInstance(type);
        } catch (Throwable e) {
            // 计划期判不出、只能"试一次才知道"的目标类：首次打 error 留痕并登记，其后直接跳过（防热路径反复报错）
            if(null == UNSUPPORTED_TARGET_CLASSES.putIfAbsent(type, Boolean.TRUE)) {
                log.error("目标类无法实例化（构造体抛出异常），已登记为不支持、后续直接跳过：{}", type.getName(), e);
            }
            return null;
        }
    }

    /**
     * 目标类是否含 final 实例字段（判定按类缓存）
     *
     * <p>final 实例字段不可写（既有 copy 契约不覆盖 final 字段），故含 final 字段的目标类无法完整结构拷贝，
     * 深拷贝时整体退化为共享引用——避免产出"部分字段为空"的对象（静默丢数据比共享引用更危险）。</p>
     */
    static final CClassValue<Boolean> HAS_FINAL_FIELD_CLASS_VALUE =
            CClassValue.of(CDeepCopyUtils::checkHasFinalInstanceField);

    /**
     * 目标类是否含 final 实例字段的实际判定（仅首次按类执行，命中时打印一次 debug）
     *
     * @param type 类型（非 null）
     * @return true 表示含 final 实例字段
     */
    static boolean checkHasFinalInstanceField(Class<?> type) {

        for (val field : CReflectUtils.getInstanceFieldMap(type).values()) {
            if(CReflectUtils.isFinal(field)) {
                log.debug("目标类含 final 实例字段，无法完整深拷贝，退化为共享引用：{}.{}", type.getName(), field.getName());
                return true;
            }
        }

        return false;
    }

    /**
     * 可按类实例化的实际判定（仅首次按类执行）
     *
     * @param type 类型（非 null，调用方已判空）
     * @return true 表示可用无参构造实例化
     */
    static boolean checkInstantiable(Class<?> type) {

        if(type.isInterface() || Modifier.isAbstract(type.getModifiers()) || isViewOrImmutable(type)) {
            return false;
        }

        for (val constructor : type.getDeclaredConstructors()) {
            if(0 == constructor.getParameterCount()) {
                return true;
            }
        }

        // 不可实例化值退化为共享引用：属设计内降级，仅首次按类打印一次 debug
        log.debug("目标类无可用无参构造，深拷贝退化为共享引用：{}", type.getName());
        return false;
    }

    /**
     * 是否视图/不可变封装（这类实现不做同源复制，降级为可变标准实现）
     *
     * @param type 类型
     * @return true 表示视图/不可变封装
     */
    static boolean isViewOrImmutable(Class<?> type) {

        val name = type.getName();
        for (val prefix : VIEW_OR_IMMUTABLE_PREFIXES) {
            if(name.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 解析深拷贝目标类：声明类型比源更具体时用声明类型，否则用运行时类型；容器声明另按接口族归一
     *
     * <p>三种取值：</p>
     * <ol>
     *   <li><b>声明类型比源更具体</b>（如源 {@code ArrayList} → 声明 {@code LinkedList}）⇒ 用声明类型
     *   （父类向子类实现类转换，按目标类型重建）；</li>
     *   <li><b>声明为源类型的父类型/接口</b>（{@code Object}/{@code Serializable} 等）⇒ 用运行时类型；</li>
     *   <li><b>容器声明与源容器族不一致</b>（如源 {@code ArrayList} → 声明 {@code Set}）⇒ 不是"父类型"、
     *   也不能直接用源实现类（源实现类写不回 {@code Set}），故按<b>目标接口族</b>归一为可实例化的标准实现
     *   （见 {@link #containerFor}）。</li>
     * </ol>
     *
     * <p><b>容器声明一律按目标声明类型归一</b>：不止同族跨接口（{@code List} → {@code Set}、{@code Set} →
     * {@code Queue} 等），<b>跨族</b>声明（源 {@code List} → 声明 {@code Map}、源 {@code Map} → 声明
     * {@code List}）同样保留目标声明类型、由 {@link #containerFor} 给出该族的兜底实现。</p>
     *
     * <p><b>为什么跨族也保留声明类型</b>：跨族时源与目标在"逐元素深拷贝"的形态上不同构
     * （{@code Map} 的元素是键值对而非元素），要物化另一形态必须指定"键从哪来、值从哪来"，
     * 无法从源单向决定——故 {@link #deepCopyValue} 由
     * {@link #canWriteBackContainer(Class, Class) canWriteBackContainer} 整条<b>跳过不写入</b>
     * （旧实现同一语义：不构造半成品、不丢字段以外的行为）。此处若退回源运行时类，上层会改用源类
     * 去写声明为目标族的 setter，在 MethodHandle 签名层抛 {@code ClassCastException}
     * （与"跳过不写入"只差一个被吞掉的异常，却是每次命中的稳定开销）。</p>
     *
     * @param declaredType 声明类型
     * @param fromClass    源运行时类型
     * @return 目标类
     */
    static Class<?> resolveDeepCopyTargetClass(Type declaredType, Class<?> fromClass) {

        val rawClass = rawDeepCopyClass(declaredType);
        if(null == rawClass || Object.class == rawClass) {
            return fromClass;
        }
        // 声明为源类型的父类型/接口（Object/Serializable/List 等）时按运行时类型拷贝
        // （源 ArrayList 声明为 List：保留 ArrayList 实现语义，而非按接口重建）
        if(rawClass.isAssignableFrom(fromClass)) {
            return fromClass;
        }
        // 源可赋给声明类型（父类 → 子类实现类，如源 ArrayList → 声明 LinkedList）时按声明类型重建
        if(fromClass.isAssignableFrom(rawClass)) {
            return rawClass;
        }
        // 数组声明：源是容器时属"容器 → 数组"跨形态转换，保留数组声明本身
        if(rawClass.isArray()) {
            return rawClass;
        }
        // 容器声明：一律按<b>目标声明类型</b>归一到可实例化的实现类/兜底实现。
        // 跨族声明（源 List → 声明 Map）同样保留声明类型，由上层按"目标装不下源容器的形态"
        // 整条跳过（见 deepCopyValue 的 canWriteBackContainer）；若在此退回源类，
        // 上层会改用源类去写目标声明类型的 setter，在 MethodHandle 签名层抛 ClassCastException
        if(isContainerClass(rawClass)) {
            return containerFor(rawClass, fromClass);
        }

        return rawClass;
    }

    /**
     * 是否容器声明（集合 / Map / 数组）
     *
     * @param type 声明类型
     * @return true 表示容器声明
     */
    static boolean isContainerClass(Class<?> type) {
        return Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type) || type.isArray();
    }

    /**
     * 取 {@code Type} 的原始类
     *
     * @param type 类型
     * @return 原始类或 null
     */
    static Class<?> rawDeepCopyClass(Type type) {

        if(type instanceof Class) {
            return (Class<?>) type;
        }
        if(type instanceof ParameterizedType) {
            val raw = ((ParameterizedType) type).getRawType();
            if(raw instanceof Class) {
                return (Class<?>) raw;
            }
            return null;
        }

        return null;
    }

    /**
     * 取 {@code Type} 第 index 个类型实参（通配符取上界/下界，类型变量返回 null）
     *
     * @param type  声明类型
     * @param index 位置
     * @return 元素/键/值类型，无法解析返回 null
     */
    static Type deepCopyTypeArgument(Type type, int index) {

        if(type instanceof ParameterizedType) {
            val arguments = ((ParameterizedType) type).getActualTypeArguments();
            if(index < arguments.length) {
                return unwrapDeepCopyWildcard(arguments[index]);
            }
            return null;
        }
        if(type instanceof WildcardType) {
            if(0 == index) {
                return unwrapDeepCopyWildcard(type);
            }
            return null;
        }
        if(type instanceof GenericArrayType) {
            if(0 == index) {
                return unwrapDeepCopyWildcard(((GenericArrayType) type).getGenericComponentType());
            }
            return null;
        }

        return null;
    }

    /**
     * 类型实参解析缓存：{@code (声明泛型, 位置) → 元素/键值类型} 与"确定不可变"标志
     *
     * <p>深拷贝时每次都要把字段声明泛型解析成元素/键值类型（{@code Type} 走查 + 通配符收敛，
     * 实测约 8ns/次），再判一次"确定不可变"（再约 9ns/次）；而这两个结果只依赖
     * {@code Type}，与具体值无关、可跨对象复用。{@link Type} 的实现类（含
     * {@code ParameterizedTypeImpl}）均已定义 {@code equals}/{@code hashCode}（按原始类型 + 实参比较），
     * 故可按值缓存；缓存条目与类加载器同生命周期（键为方法/字段的泛型信息，随类元数据存活）。</p>
     */
    static final ConcurrentMap<TypeArgumentKey, TypeArgumentMemo> TYPE_ARGUMENT_CACHE =
            new ConcurrentHashMap<>();

    /**
     * 类型实参缓存容量上限（缓存键为"类元数据的泛型信息"，实际条目数远小于此值；
     * 达上限即停止写入，命中已有条目不受影响——缓存只影响性能、不影响正确性）
     */
    static final int TYPE_ARGUMENT_CACHE_MAX_SIZE = 16_384;

    /**
     * 解析类型实参并缓存"类型 + 不可变标志"（深拷贝容器/Map 的热路径入口）
     *
     * @param declaredType 声明类型（可空）
     * @param index        位置
     * @return 解析结果（永不返回 null；解析不出时为 {@link TypeArgumentMemo#EMPTY}）
     */
    static TypeArgumentMemo typeArgumentMemo(Type declaredType, int index) {

        if(null == declaredType) {
            return TypeArgumentMemo.EMPTY;
        }

        val key = new TypeArgumentKey(declaredType, index);
        val cached = TYPE_ARGUMENT_CACHE.get(key);
        if(null != cached) {
            return cached;
        }

        val memo = resolveTypeArgument(key);
        // 上限守卫：写入前判容量，防极端场景无限增长（未写入时下次重算，仅损失命中率）
        if(TYPE_ARGUMENT_CACHE.size() < TYPE_ARGUMENT_CACHE_MAX_SIZE) {
            val previous = TYPE_ARGUMENT_CACHE.putIfAbsent(key, memo);
            if(null != previous) {
                return previous;
            }
            return memo;
        }

        return memo;
    }

    /**
     * 类型实参的实际解析（缓存未命中时执行一次，见 {@link #typeArgumentMemo}）
     *
     * @param key 缓存键（声明泛型 + 位置）
     * @return 解析结果（永不返回 null）
     */
    static TypeArgumentMemo resolveTypeArgument(TypeArgumentKey key) {

        val resolved = deepCopyTypeArgument(key.type, key.index);
        if(null == resolved) {
            return TypeArgumentMemo.EMPTY;
        }
        return new TypeArgumentMemo(resolved, isDefinitelyImmutable(resolved));
    }

    /**
     * 类型实参缓存的键：{@code (声明泛型, 位置)}（按值相等，见 {@link #typeArgumentMemo}）
     */
    private static final class TypeArgumentKey {

        final Type type;

        final int index;

        TypeArgumentKey(Type type, int index) {
            this.type = type;
            this.index = index;
        }

        @Override
        public boolean equals(Object o) {

            if(this == o) {
                return true;
            }
            if(!(o instanceof TypeArgumentKey)) {
                return false;
            }

            val other = (TypeArgumentKey) o;
            return index == other.index && Objects.equals(type, other.type);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(type) * 31 + index;
        }

    }

    /**
     * 类型实参解析结果：类型 + "确定不可变"标志（不可变标志在解析期一并判出、不重复计算）
     */
    private static final class TypeArgumentMemo {

        /** 空结果（声明解析不出实参）：统一复用，避免逐次分配 */
        static final TypeArgumentMemo EMPTY = new TypeArgumentMemo(null, false);

        final Type type;

        final boolean immutable;

        TypeArgumentMemo(Type type, boolean immutable) {
            this.type = type;
            this.immutable = immutable;
        }

    }

    /**
     * 通配符收敛：{@code ? extends X}/{@code ? super X} → X；纯 {@code ?} → null
     *
     * @param type 类型
     * @return 收敛后的类型或 null
     */
    static Type unwrapDeepCopyWildcard(Type type) {

        if(!(type instanceof WildcardType)) {
            return type;
        }

        val wildcard = (WildcardType) type;
        val upperBounds = wildcard.getUpperBounds();
        if(upperBounds.length > 0 && Object.class != upperBounds[0]) {
            return upperBounds[0];
        }
        val lowerBounds = wildcard.getLowerBounds();
        if(lowerBounds.length > 0) {
            return lowerBounds[0];
        }
        return null;
    }
    /**
     * 声明类型是否"确定无需深拷贝"（final 类，且属不可变值类型）
     *
     * <p>final 类不可能持有子类实例，运行期类型即声明类型，故计划期判定安全
     * （如 {@code String}/{@code Integer}/{@code LocalDate}）；非 final 类与不可解析的声明
     * （接口/抽象类/类型变量/通配符）一律视为可能需深拷贝，运行期再按实际值类型判定。</p>
     *
     * @param declaredType 字段声明类型（含泛型）
     * @return true 表示确定无需深拷贝
     */
    static boolean isDefinitelyImmutable(Type declaredType) {

        val rawClass = rawDeepCopyClass(declaredType);
        return null != rawClass && Modifier.isFinal(rawClass.getModifiers()) && isNotCopyable(rawClass);
    }
}
