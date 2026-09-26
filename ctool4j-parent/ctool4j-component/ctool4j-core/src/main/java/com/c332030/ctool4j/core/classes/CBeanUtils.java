package com.c332030.ctool4j.core.classes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.cache.impl.CBiClassValue;
import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.core.util.CMapUtils;
import com.c332030.ctool4j.definition.function.CFunction;
import com.c332030.ctool4j.definition.function.CSupplier;
import com.c332030.ctool4j.definition.function.ToStringFunction;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * <p>
 * Description: CBeanUtils
 * </p>
 *
 * <p>JavaBean 属性复制与对象转 Map 工具类：对象间属性复制（{@code copy} 直连字段、避免 toMap 中转）、
 * 对象转 Map（{@code toMap} 系列，支持原名/下划线/注解 key）、Map 转对象、数组/集合批量复制。</p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>属性复制：{@link #copy(Object, Object)}（核心，直连字段）、{@link #copy(Map, Object)}（Map 源）、
 *   {@link #copy(Object, Class)} / {@link #copy(Object, CSupplier)}（创建目标）、
 *   {@link #copyList(Collection, Class)} / {@link #copyListFromMap(Collection, Class)}（批量）、
 *   {@link #copyFromArr(Object[], Object)}（数组反序覆盖）。</li>
 *   <li>深拷贝：集合/Map/数组/Bean/Date 字段递归拷贝（元素、键值、环状引用均保持结构），
 *   随全部复制入口统一生效（不区分对象源与 Map 源）；
 *   JDK 未提供拷贝协议的可变类型（StringBuilder、AtomicX 等）不做结构拷贝，按共享引用或跳过处理
 *   （JDK9+ 强封装下无法反射内部字段，且结构拷贝易产出空壳对象造成数据丢失）。</li>
 *   <li>跨形态转换：数组 ↔ 容器（源数组 → 目标 List/Set 声明、源容器 → 目标数组声明）、
 *   {@code Map.Entry} → Map/容器、{@code Optional} 拆包写入非 Optional 声明，
 *   一律按<b>目标声明类型</b>重建（不再"跳过不写入"丢字段）。</li>
 *   <li>对象转 Map：{@link #toMap(Object)}、{@link #toMapJsonName(Object)}、{@link #toMapUnderlineName(Object)}、
 *   {@link #toMap(Object, ToStringFunction)}、{@link #toMap(Object, Class, CFunction)}；
 *   值为<b>浅引用</b>（集合/Bean 等与原对象同一引用，不做深拷贝）。</li>
 * </ul>
 *
 * <h2>实现底线（两条，不可破）</h2>
 * <ul>
 *   <li><b>功能完整</b>：语义契约不打折——同名字段配对（不按 getter/setter 语义）、深拷贝覆盖
 *   （集合 / Map / 数组 / Bean / Date / Calendar / Optional）、环状引用与跨字段共享引用只拷一份、
 *   final/static 字段与不可拷贝类型的既有降级、无参构造缺失/含 final 字段的明确降级、
 *   全部入口（对象源、Map/JSON 源、supplier、批量、数组反序）共用同一决议与执行。
 *   任何性能优化都必须先通过全量用例与深拷贝契约用例（环引用、共享引用、各类容器与 Bean 递归）。</li>
 *   <li><b>性能</b>：计划期预计算、运行期不查字段表；热路径只做"取一次值 → 决议一次 → 执行一次 → 写一次"，
 *   判定与查表按类 / 按类对缓存（{@code CClassValue} / {@code CBiClassValue}），身份表按需创建；
 *   优化以"同口径实测有收益"为准，无收益的变体一律回滚——性能数据以性能基准用例的同口径报告为准
 *   （{@code CBeanUtilsPerfTests}：简单 copy / 深拷贝按类型 / 对象转 Map 三通道）。</li>
 * </ul>
 *
 * <h2>深拷贝按类折叠计划</h2>
 * <p>深拷贝到 Bean 目标类时所需的两个"按类恒定"判定——<b>含 final 实例字段</b>、<b>可否无参构造实例化</b>
 * （后者内部还含视图/不可变类名前缀扫描）——原先在运行期各查一次 {@code ClassValue}；
 * 现按目标类在<b>首次深拷贝该类时</b>一次性折叠为 {@link DeepPlan}（{@code DEEP_PLAN_CLASS_VALUE}），
 * 运行期只做一次 {@code ClassValue} 查表。实测收益随"一次复制命中的目标类次数"线性放大
 * （单次命中省约 4.4ns；一次复制命中 41 次时整场景 -3.5%），同口径 A/B 与收敛判定见
 * {@code doc/design/core/perf/beanutils-perf.adoc}。</p>
 *
 * <p><b>不并入折叠的</b>：「构造体抛异常的不支持目标类」是运行期可变状态（登记时刻可能晚于计划创建），
 * 按类缓存会把"登记前"的取值固化，故保持运行期查（理由见 {@link DeepPlan}）。
 * 同轮评估的另两处候选（容器创建折叠为 kind 查表、{@code toMap} 用 {@code put} 返回值判冲突）
 * 经同口径实测无显著收益或负收益，<b>未采纳</b>——优化以实测有收益为准。</p>
 *
 * <h2>设计思路总述</h2>
 * <ul>
 *   <li><b>计划预计算</b>：按 (源类, 目标类) / 按类预计算复制计划与转 Map 计划，
 *   缓存于 {@code CClassValue} / {@code CBiClassValue}（基于 {@code java.lang.ClassValue}，线程安全、按类弱关联）；
 *   字段配对、转换器解析、final 剔除、深拷贝条目定位全部在计划期完成，运行期仅遍历字段数组 + null 判断，
 *   性能接近手工 setter。计划缓存键为 Class，被类加载器持有，无泄漏问题。</li>
 *   <li><b>方法句柄</b>：getter/setter 统一适配为 Object 签名 MethodHandle（{@code Object -> Object}、
 *   {@code (Object, Object) -> void}），运行期 {@code invokeExact} 无签名适配开销；handle 经
 *   {@link CMethodHandleUtils} 获取（Caffeine 弱 key 缓存），无参构造器按类缓存。</li>
 *   <li><b>单一决议 + 单一执行</b>：每个属性只做一次决定——按 (值运行时类型, 目标字段声明类型) 决议为
 *   "共享 / 转换 / 深拷贝 / 跳过"之一，再执行一次、写入一次；深拷贝是决议的结果之一，
 *   不是"先转换写完再深拷贝覆盖"的后置步骤。计划期可证（源声明为 final/原始类型或容器）的字段
 *   把决议内联到条目上，其余按实际值类型查缓存决议（{@code COPY_ACTION_BI_CLASS_VALUE}）。
 *   全部入口（对象源、Map/JSON 源、supplier、批量）共用同一决议与执行，不因源形态产生第二套语义。</li>
 *   <li><b>兜底转换器优先级最低</b>：Object→String 兜底在 {@code CConvertUtils.findConverter} 中只记录不返回，
 *   保证 Date→String 等更精确转换不被抢占。</li>
 * </ul>
 * <h2>深拷贝覆盖（按值的运行时类型）</h2>
 * <table border="1">
 *   <caption>覆盖与例外</caption>
 *   <tr><th>值类型</th><th>处理</th></tr>
 *   <tr><td>集合 / Map / 数组</td><td>深拷贝（同源实现优先、接口降级标准实现、comparator 与 EnumMap/EnumSet 语义保留、元素与键值递归）</td></tr>
 *   <tr><td>Bean（自定义/第三方类且有可用无参构造）</td><td>深拷贝（嵌套 Bean 直接复用 {@code copy} 的计划流程递归）</td></tr>
 *   <tr><td>{@code Date} / {@code Calendar}</td><td>防御性拷贝（新实例）；{@code java.sql.Date}/{@code Time}/{@code Timestamp}
 *   按目标声明类型/源运行时类型<b>保真</b>（不降级为 {@code java.util.Date}，{@code Timestamp} 的纳秒不丢）</td></tr>
 *   <tr><td>{@code Optional}</td><td>深拷贝内部值（空 Optional 共享；{@code OptionalInt/OptionalLong/OptionalDouble} 只包装原始值 ⇒ 共享）；
 *   目标声明<b>非 Optional 族</b>时拆包后按目标声明重建（{@code Optional<List<X>>} → {@code List<X>} 声明可写入）</td></tr>
 *   <tr><td>源数组 → 目标容器声明（如 {@code Inner[]} → {@code List<Inner>}/{@code Set<Inner>}）</td><td><b>按元素重建容器</b>
 *   （按目标声明类型选实现，元素递归深拷贝；数组与集合在"有序、按元素遍历"上同构）</td></tr>
 *   <tr><td>源容器 → 目标数组声明（如 {@code Set<Inner>} → {@code Inner[]}）</td><td><b>按目标组件类型重建数组</b>
 *   （元素递归深拷贝；组件类型装不下的槽位跳过，不抛 {@code ArrayStoreException} 整体失败）</td></tr>
 *   <tr><td>{@code Map.Entry}（如 {@code AbstractMap.SimpleEntry}）</td><td>按目标声明物化：目标为 Map ⇒ 建单条记录；
 *   目标为容器/数组 ⇒ 物化"键、值"两项；其余 ⇒ 按键值对语义建 {@code SimpleEntry}</td></tr>
 *   <tr><td>不可变值（String、包装类、Number 系、时间类、枚举、Class、UUID/Locale/Currency/Charset/URI 等）</td><td>共享引用（无拷贝必要）</td></tr>
 *   <tr><td>函数式接口值（lambda / 方法引用）</td><td>共享引用（承载行为而非数据，结构拷贝无意义）</td></tr>
 *   <tr><td>无可用无参构造的类（仅带参构造的类）、接口与抽象类</td><td>共享引用（无法实例化 ⇒ 明确降级，判定按类缓存）</td></tr>
 *   <tr><td>容器目标声明类型与源容器接口形态不一致（如 List 源 → Set 目标声明、Collection 源 → List/Set 目标声明），
 *   或目标声明为具体实现类（如 ArrayList 源 → LinkedList 目标）</td><td><b>按目标声明类型重建容器</b>（有序优先：目标为 Set 建 LinkedHashSet、
 *   为 Map 建 LinkedHashMap、为 List/Collection 建 ArrayList）；写回目标接口天然成立，不丢字段（判定见 {@code containerFor}）</td></tr>
 *   <tr><td>容器目标声明为<b>另一容器族</b>（如源 List → 目标 Map 声明、源 Map → 目标 List 声明）</td><td><b>跳过不写入</b>
 *   （该字段保持目标对象既有值，不报错）：两族元素形态不同构，物化目标形态须指定"键从哪来、值从哪来"，
 *   无法从源单向决定（判定与拦下见 {@code canWriteBackContainer}）</td></tr>
 *   <tr><td>迭代器（{@code Iterator}）与原始流（{@code Stream}/{@code IntStream} 等）</td><td>按迭代顺序物化后<b>按目标声明类型重建</b>（源视图一次性、共享无意义；见 {@code viewedIterable}）</td></tr>
 *   <tr><td>含 final 实例字段的类</td><td>共享引用（final 字段不可写，结构拷贝会得到"部分字段为空"的对象 ⇒ 明确降级，判定按类缓存）</td></tr>
 *   <tr><td>JDK 未提供拷贝协议的可变类型（{@code StringBuilder}、{@code Atomic*}、{@code BitSet} 等）</td><td>同型共享、跨类型跳过（JDK9+ 强封装无法反射内部字段，结构拷贝会得到空壳对象；需要拷贝时注册显式转换器）</td></tr>
 *   <tr><td>{@code Class}</td><td>共享（不参与拷贝）</td></tr>
 * </table>
 *
 * <h2>日志（重要节点）</h2>
 * <table border="1">
 *   <caption>日志节点</caption>
 *   <tr><th>节点</th><th>级别</th><th>频次</th><th>内容</th></tr>
 *   <tr><td>复制计划构建</td><td>debug</td><td>(源类, 目标类) 一次</td><td>条目数、计划期内联决议数、首次取值时决议数</td></tr>
 *   <tr><td>属性决议</td><td>debug</td><td>(值类型, 目标类型) 一次</td><td>决议结果（共享/转换/深拷贝/跳过）；JDK 未提供拷贝协议时说明按共享或跳过处理</td></tr>
 *   <tr><td>深拷贝不可实例化降级</td><td>debug</td><td>按类一次</td><td>无可用无参构造（lambda/仅带参构造）⇒ 共享引用</td></tr>
 *   <tr><td>容器按目标声明类型重建</td><td>debug</td><td>属性命中时</td><td>目标声明类型与源容器形态不一致（List→Set 等）⇒ 按目标声明类型重建容器</td></tr>
 *   <tr><td>跨容器族的声明</td><td>debug</td><td>属性命中时</td><td>目标声明与源容器不同族（List↔Map）⇒ 该字段跳过不写入（保持既有值）</td></tr>
 *   <tr><td>迭代器/流重建容器</td><td>debug</td><td>属性命中时</td><td>源为一次性视图（Iterator/Stream）⇒ 按迭代顺序物化并按目标声明类型重建</td></tr>
 *   <tr><td>数组跨形态转换</td><td>debug</td><td>属性命中时</td><td>源为数组而目标声明为容器（或反之）⇒ 按目标声明类型重建</td></tr>
 *   <tr><td>{@code Map.Entry} 物化</td><td>debug</td><td>属性命中时</td><td>源为键值对视图 ⇒ 按目标声明物化为 Map / "键、值"两项</td></tr>
 *   <tr><td>{@code Optional} 拆包</td><td>debug</td><td>属性命中时</td><td>目标声明非 Optional 族 ⇒ 拆包后按目标声明重建</td></tr>
 *   <tr><td>目标类无法实例化（构造体抛异常）</td><td>error</td><td>按类一次</td><td>准备阶段判不出、试一次才失败 ⇒ 登记进「不支持的目标类集合」，后续直接跳过不再报错</td></tr>
 *   <tr><td>深拷贝 final 字段降级</td><td>debug</td><td>按类一次</td><td>目标类含 final 实例字段 ⇒ 共享引用（不产出半空对象）</td></tr>
 *   <tr><td>深拷贝深度上限</td><td>debug</td><td>超限时</td><td>超过 {@code DEEP_COPY_MAX_DEPTH} 原样返回引用（防病态深结构）</td></tr>
 *   <tr><td>字段深拷贝失败</td><td>debug</td><td>异常时</td><td>字段名 + 异常；单字段跳过，不影响其余字段</td></tr>
 * </table>
 * <p>级别取舍：复制是热路径，日志只打"按类/按类对一次"或异常场景，热路径零日志；
 * 设计内降级（不可实例化、JDK 非拷贝协议）一律 debug，不产生 warn/error 噪音。</p>
 *
 * <p>详细设计、详细步骤、兜底与已知限制见各方法 javadoc（本类 javadoc 只保留总结）。</p>
 *
 * <h2>适用范围与易误用点</h2>
 * <ul>
 *   <li>适用范围：同名字段的对象/Map（含 JSONObject 形态）复制与对象转 Map；字段按名称配对，<b>不按 getter/setter 语义</b>，
 *   目标类无同名成员即不参与复制（继承字段同样按名称匹配）。</li>
 *   <li>易误用点：{@link #copy(Object, Object)} 默认<b>深拷贝</b>集合/Map/数组/Bean/Date——需要与原对象共享引用时
 *   不应使用本类（见上「深拷贝覆盖」表）；{@link #toMap(Object)} 与之相反，返回的是<b>浅引用</b>视图。</li>
 *   <li>易误用点：复制只写"有同名源字段且目标可写"的字段；目标 final 字段、无同名源字段一律不动（既有值保留）。</li>
 *   <li>易误用点：数组 ↔ 容器、{@code Map.Entry} → Map/容器属<b>按目标声明重建</b>（不是转换器的值形态转换）：
 *   同一源值声明为不同目标类型会得到不同容器形态，是设计内行为、非"拷贝不一致"。</li>
 *   <li>易误用点：容器<b>跨族</b>声明（源 {@code List}/{@code Set} → 目标 {@code Map} 声明，或反向）<b>不支持</b>——
 *   该字段按其既有值保留、<b>不报错</b>（两族元素形态不同构，无法从源单向物化目标形态）；
 *   需要该转换时由调用方显式转换（如 {@code CMapUtils} 一类工具）后另行赋值。</li>
 *   <li>不支持的目标类（状态：设计内降级，可观测）：有无参构造、但构造体必定抛异常的目标类无法靠反射预判，
 *   首次实例化失败即登记进「不支持的目标类集合」（打一次 error 日志），其后该类型一律跳过、不再重复报错。
 *   注意：被跳过的类型其字段<b>保持目标对象既有值</b>，不是共享引用。</li>
 *   <li>已知限制（状态：已备注）：同一被测类存在 5 个 {@code *Tests} 类（规范定式为一个被测类一个
 *   {@code <被测类名>Tests}）。本次评估过合并为一个 {@code CBeanUtilsTests}（内部 {@code @Nested} 分组），
 *   但被 {@code CBeanUtilsPerfTests} 复用的公共夹具（如 {@code ScalarBean}）需一并改动，
 *   属测试结构的独立重构、与本 PR 的行为修复不是一件事，故按"存量随动迁移"保持现状、不在本 PR 内合并。</li>
 * </ul>
 *
 * <p>相关测试（{@code com.c332030.ctool4j.core.classes} / {@code ...core.benchmark}）：
 * {@code CBeanUtilsTests}（跨入口代表用例）、{@code CBeanUtilsCopyContractTests}（单属性决议契约）、
 * {@code CBeanUtilsDeepCopyTests}（深拷贝语义）、{@code CBeanUtilsMoreTests}（各入口与 toMap 语义一致性）、
 * {@code CBeanUtilsCompatibilityTests}（内嵌旧语义参考实现的兼容性对比）、
 * {@code CBeanUtilsPerfTests}（性能基准：简单 copy / 深拷贝按类型 / 对象转 Map 三通道，须显式执行）。
 * 未以 {@code @see} 链接测试类：javadoc 的类路径不含测试源，{@code @see} 会报
 * "reference not found" 并使 javadoc 退出码非 0，在 {@code failOnError=true} 下中断构建
 * （已实测确认：javadoc 对未解析的 {@code @see} 返回退出码 1）。</p>
 *
 * @author c332030
 * @since 1.0
 * @version 1.2
 */
@CustomLog
@UtilityClass
public class CBeanUtils {

    /**
     * 复制计划缓存：按 (源类, 目标类) 对缓存
     */
    private static final CBiClassValue<CopyPlan> COPY_PLAN_BI_CLASS_VALUE = CBiClassValue.of(CBeanUtils::getCopyPlan);

    /**
     * 转 map 计划缓存：按类缓存
     */
    private static final CClassValue<ToMapPlan> TO_MAP_PLAN_CLASS_VALUE = CClassValue.of(CBeanUtils::getToMapPlan);

    /**
     * 属性复制动作缓存：按 (值的运行时类型, 目标字段声明类型) 缓存唯一决议结果
     * <p>仅"计划期无法证明"的字段（源声明类型非 final/原始且非容器）在运行期查此表；
     * 计划期已证实的字段由 {@link CopyEntry#action} 内联，不查表。</p>
     */
    private static final CBiClassValue<CopyAction> COPY_ACTION_BI_CLASS_VALUE =
            CBiClassValue.of(CBeanUtils::resolveCopyAction);

    /**
     * 空复制计划（JDK 源类使用，热路径零操作）
     */
    private static final CopyPlan EMPTY_COPY_PLAN = CopyPlan.builder()
            .entries(new CopyEntry[0])
            .build();

    /**
     * 空转 map 计划（JDK 类使用，热路径零操作）
     */
    private static final ToMapPlan EMPTY_TO_MAP_PLAN = ToMapPlan.builder()
            .entries(new ToMapEntry[0])
            .build();

    /**
     * 深拷贝递归深度上限（超过该层原样返回引用，防病态深结构）
     */
    private static final int DEEP_COPY_MAX_DEPTH = 64;

    /**
     * 不写入哨兵：决议为跳过、转换结果为 null、深拷贝失败时统一返回该值，调用方据此不写字段
     */
    private static final Object SKIP_VALUE = new Object();

    /**
     * 视图/不可变封装的类名前缀：这类实现不做同源复制，深拷贝时降级为可变标准实现
     */
    private static final String[] VIEW_OR_IMMUTABLE_PREFIXES = {
        "java.util.Collections$",
        "java.util.Arrays$",
        "java.util.ImmutableCollections$",
        "java.util.stream.",
        "org.hibernate.collection",
        "com.google.common.collect.Immutable",
        "com.google.common.collect.SingletonImmutable"
    };

    /**
     * map 属性复制到对象（Map/JSON 源）
     *
     * <p><b>详细设计</b>：Map 源无法预计算字段配对，故逐条处理；但与 {@link #copy(Object, Object)}
     * <b>共用同一套"决议 + 执行"</b>（{@link #resolveCopyAction} / {@link #applyCopyAction}），
     * 不因源形态不同而产生第二套复制语义。</p>
     *
     * <p><b>详细步骤</b>：</p>
     * <ol>
     *   <li>{@code fromMap} 或 {@code to} 为 null → 原样返回 {@code to}（不做任何写入）；</li>
     *   <li>取目标类实例字段表（{@link CReflectUtils#getInstanceFieldMap(Class)}），
     *   建深拷贝身份表（{@code visited}，本次调用内共享）；</li>
     *   <li>逐条遍历 map：字段名在目标类中不存在、值为 null、字段为 static 或 final → 跳过该条；</li>
     *   <li>按 (值运行时类型, 字段声明类型) 取决议（{@code COPY_ACTION_BI_CLASS_VALUE}：
     *   共享 / 转换 / 深拷贝 / 跳过），{@link #applyCopyAction} 执行<b>一次</b>，
     *   结果非"跳过"则用 setter 方法句柄（{@link CMethodHandleUtils#getSetterHandle(Field)}）写入；</li>
     *   <li>单个字段执行失败只记 debug 日志，不影响其余字段。</li>
     * </ol>
     *
     * <p><b>边界与取舍</b>：键不匹配即静默跳过（不抛错）；无可用决议（不可赋值、无转换器、目标装不下）
     * 同样静默跳过；目标 final 字段不可写故跳过。因无法预计算字段配对，本方法性能低于
     * {@link #copy(Object, Object)} 计划路径，仅用于源本身即 Map 的场景。</p>
     *
     * @param fromMap 源 map
     * @param to 目标对象
     * @return 目标对象；入参为空时原样返回
     * @param <To> 目标对象泛型
     */
    public <To> To copy(Map<String, ?> fromMap, To to) {

        if(null == fromMap || null == to) {
            return to;
        }

        // 深拷贝身份表：跨字段共享的源对象（如 inner 与 inners 元素为同一实例）复用一个副本
        val visited = new IdentityHashMap<Object, Object>();

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

            val action = COPY_ACTION_BI_CLASS_VALUE.get(fromValue.getClass(), toField.getType());
            val toValue = applyCopyAction(action, fromValue, toField.getGenericType(), fromKey, visited, 0);
            if(SKIP_VALUE != toValue) {
                setValueWithHandle(to, toField, toValue);
            }
        });

        return to;
    }

    /**
     * 对象属性复制（核心方法，直连字段）
     *
     * <p><b>详细设计</b>：走计划（预热）路径，字段配对在计划期完成；每个属性只做一件事——
     * 一次决议（{@link #resolveCopyAction}：共享 / 转换 / 深拷贝 / 跳过）+ 一次写入。
     * 计划期可证的动作直接内联在条目上，
     * 不可证的（源声明为接口/抽象类/Object 等宽类型）运行期按 (值类型, 目标声明类型) 查缓存决议。</p>
     *
     * <p><b>详细步骤</b>：</p>
     * <ol>
     *   <li>{@code from} 或 {@code to} 为 null → 返回 {@code to}；</li>
     *   <li>顶层 {@code from → to} 登记深拷贝身份表，保证自引用字段指向副本自身而非再复制一份；</li>
     *   <li>委托 {@link #copyWithPlan(Object, Object, IdentityHashMap, int)} 按计划逐字段"决议 → 执行 → 写入"；
     *   JDK 源类命中空计划，保持"JDK 类不拷贝"原语义；</li>
     *   <li>返回 {@code to}（目标对象本身，便于链式使用）。</li>
     * </ol>
     *
     * <p><b>计划期规则</b>：对目标类每个实例字段按序判定——JDK 源类返回空计划；目标 final 字段、
     * 源无同名字段 一律剔除；其余生成一条条目（携带 getter/setter 句柄、目标声明类型与泛型、
     * 字段名），并尝试内联决议：</p>
     * <ul>
     *   <li>源声明为集合/Map/数组（值必为容器，且容器源不参与转换）→ 内联深拷贝；</li>
     *   <li>源声明为 final 类或原始类型（值类型必等于声明类型）→ 内联该决议（含跳过，跳过则不生成条目）；</li>
     *   <li>其余（接口/抽象类/非 final 类/Object 等）→ 不内联，运行期按实际值类型决议（详见 {@link #getCopyPlan(Class, Class)}）。</li>
     * </ul>
     *
     * <p><b>兜底</b>：源/目标 null 原样返回；转换结果为 null 视为不写入；深拷贝单字段失败只记 debug 日志、
     * 不影响其余字段。</p>
     *
     * <p><b>边界与已知取舍</b>：目标 final 字段不可写；null 值跳过。
     * 集合/Map/数组/Bean/Date 字段按深拷贝写入（旧实现的"跳过/共享引用"语义已由深拷贝取代）；
     * 原始类型同型字段（int→int 等）直接写入（旧实现无转换器跳过，属旧缺口修复）。</p>
     *
     * @param from 源对象
     * @param to   目标对象
     * @return 目标对象；入参为空时原样返回
     * @param <To> 目标对象泛型
     */
    public <To> To copy(Object from, To to) {

        if(null == from || null == to) {
            return to;
        }

        // 身份表按需创建（见 copyWithPlan）：标量属性复制、深拷贝字段全为 null 的 DTO 零分配
        return copyWithPlan(from, to, null, 0);
    }

    /**
     * 按复制计划执行字段复制（每个属性：一次决议 → 一次执行 → 一次写入）
     *
     * <p><b>详细设计</b>：计划由 {@link #getCopyPlan(Class, Class)} 按 (源类, 目标类) 预计算；
     * 运行期只遍历条目数组，键查表与写入一次完成。条目动作在计划期可证时已内联（{@link CopyEntry#action}），
     * 否则按 (值运行时类型, 目标声明类型) 查 {@code COPY_ACTION_BI_CLASS_VALUE}（预热后为常量级查表）。</p>
     *
     * <p><b>详细步骤</b>：</p>
     * <ol>
     *   <li>取复制计划（首次调用触发计划期计算）；JDK 源类命中空计划，条目数组为空；</li>
     *   <li>逐条：getter 取值 → null 跳过 → 取决议（内联动作或查表）→
     *   {@link #applyCopyAction} 执行一次 → 结果非"跳过"则 setter 写入；</li>
     *   <li>返回 {@code to}。</li>
     * </ol>
     *
     * <p><b>边界与取舍</b>：目标声明接口装不下源容器时（如 List 源 → Set 目标声明）深拷贝返回
     * {@link #SKIP_VALUE}，该字段整条不写入（拷贝与共享引用都写不回目标接口）；集合元素类型优先取目标字段声明泛型，解析不到时按元素运行时类型判定；
     * 环状引用由 {@code visited} 身份表保持结构（复用同一副本）；{@code depth} 为当前对象所在层级
     * （顶层 0，递归 +1，超过 {@code DEEP_COPY_MAX_DEPTH} 原样返回引用）。</p>
     *
     * @param from    源对象
     * @param to      目标对象
     * @param visited 已拷贝对象身份表（源 → 副本），保持环状引用结构；允许为 {@code null}（按需创建，见下）
     * @param depth   当前对象所在层级（深拷贝递归深度保护）
     * @return 目标对象
     * @param <To> 目标对象泛型
     */
    @SneakyThrows
    private static <To> To copyWithPlan(Object from, To to, IdentityHashMap<Object, Object> visited, int depth) {

        // JDK 源类由计划期返回空计划（热路径零判断），保持"JDK 类不拷贝"原语义
        val plan = COPY_PLAN_BI_CLASS_VALUE.get(from.getClass(), to.getClass());
        for (val entry : plan.entries) {

            val fromValue = entry.getterHandle.invokeExact(from);
            if(null == fromValue) {
                continue;
            }

            val action = null != entry.action ? entry.action : entry.actionOf(fromValue.getClass());

            // 共享写入是最高频分支（不可变标量占多数）：在此直写，省去一次决议执行调用与分支分派；
            // 决议本身仍是同一处（同一 action 判定），此处只是执行路径的特化
            if(SHARE_ACTION == action) {
                entry.setterHandle.invokeExact((Object) to, fromValue);
                continue;
            }

            // 身份表按需创建：走到这里说明确有需要"写入中间结果"的值（共享分支与 null 值均已提前跳过），
            // 故纯共享（全 SHARE）或全 null 字段的 DTO 不会走到这里（身份表零分配）；创建时登记顶层 from→to，
            // 保证自引用字段指向副本自身而非再复制一份。
            // 显式给定较小初始容量：IdentityHashMap 默认按 32 槽建表，而一次复制的参与对象通常只有个位数，
            // 默认容量即"每次深拷贝多分配一个 32 槽数组"（实测约 27ns/copy），此处按 4 起步
            if(null == visited) {
                visited = new IdentityHashMap<>(4);
                visited.put(from, to);
            }

            val toValue = applyCopyAction(action, fromValue, entry.genericType, entry.fieldName, visited, depth);
            if(SKIP_VALUE == toValue) {
                continue;
            }

            // 写回兜底：转换/深拷贝结果理论上与目标声明类型兼容，仍兜住极端情形（如容器跨类型拷贝）；
            // 单字段写失败只记 debug 日志并跳过，不影响其余字段（与"字段深拷贝失败"同一原则）
            try {
                entry.setterHandle.invokeExact((Object) to, toValue);
            } catch (Throwable e) {
                log.debug("字段写入失败，跳过：{}.{}", to.getClass().getName(), entry.fieldName, e);
            }
        }

        return to;
    }

    /**
     * 对象属性复制
     * @param fromMap 属性 map
     * @param toClass 目标对象类
     * @param <To> 目标对象泛型
     * @return 目标对象
     */
    public <To> To copy(Map<String, ?> fromMap, Class<To> toClass) {
        if(null == fromMap) {
            return null;
        }
        return copy(fromMap, CReflectUtils.newInstance(toClass));
    }

    /**
     * 对象属性复制
     * <p>直接新建目标对象走字段复制路径（避免 Map 中转），语义与 copy(Object, To) 一致</p>
     * @param from 源对象
     * @param toClass 目标对象类
     * @param <To> 目标对象泛型
     * @return 目标对象
     */
    public <To> To copy(Object from, Class<To> toClass) {
        return copy(from, CReflectUtils.newInstance(toClass));
    }

    /**
     * 对象属性复制
     * @param fromMap 源 map
     * @param toSupplier 目标对象提供者
     * @param <To> 目标对象泛型
     * @return 目标对象
     */
    public <To> To copy(Map<String, ?> fromMap, CSupplier<To> toSupplier) {
        if(null == fromMap) {
            return null;
        }
        return copy(fromMap, toSupplier.get());
    }

    /**
     * 对象属性复制
     * <p>直接新建目标对象走字段复制路径（不经 Map 中转），语义与 {@link #copy(Object, Object)} 完全一致</p>
     * @param from 源对象
     * @param toSupplier 目标对象提供者
     * @param <To> 目标对象泛型
     * @return 目标对象
     */
    public <To> To copy(Object from, CSupplier<To> toSupplier) {
        return copy(from, toSupplier.get());
    }

    /**
     * 集合对象属性复制
     * @param fromCollection 源集合
     * @param toSupplier 目标对象提供者
     * @param <To> 目标对象泛型
     * @return 目标对象集合
     */
    public <To> List<To> copyListFromMap(Collection<? extends Map<String, ?>> fromCollection, CSupplier<To> toSupplier) {
        if(CollUtil.isEmpty(fromCollection)) {
            return CList.of();
        }
        return fromCollection.stream()
                .filter(Objects::nonNull)
                .map(from -> copy(from, toSupplier))
                .collect(Collectors.toList());
    }

    /**
     * 集合对象属性复制
     * @param fromCollection 源集合
     * @param toClass 目标对象类
     * @param <To> 目标对象泛型
     * @return 目标对象集合
     */
    public <To> List<To> copyListFromMap(Collection<? extends Map<String, ?>> fromCollection, Class<To> toClass) {
        return copyListFromMap(fromCollection, () -> CReflectUtils.newInstance(toClass));
    }

    /**
     * 集合对象属性复制
     * @param fromCollection 源集合
     * @param toClass 目标对象类型
     * @param <To> 目标对象泛型
     * @return 目标对象集合
     */
    public <To> List<To> copyList(Collection<?> fromCollection, Class<To> toClass) {
        if(CollUtil.isEmpty(fromCollection)) {
            return CList.of();
        }
        return copyList(fromCollection, () -> CReflectUtils.newInstance(toClass));
    }

    /**
     * 集合对象属性复制
     * <p>元素走字段复制路径（与 {@link #copy(Object, Object)} 同一流程），不经 Map 中转</p>
     * @param fromCollection 源集合
     * @param toSupplier 目标对象获取方法
     * @param <To> 目标对象泛型
     * @return 目标对象集合
     */
    public <To> List<To> copyList(Collection<?> fromCollection, CSupplier<To> toSupplier) {
        if(CollUtil.isEmpty(fromCollection)) {
            return CList.of();
        }
        return fromCollection.stream()
                .filter(Objects::nonNull)
                .map(from -> copy(from, toSupplier.get()))
                .collect(Collectors.toList());
    }

    /**
     * 深拷贝分派：不可变判定 → 环处理 → 深度保护 → 数组/集合/Map/时间/Bean 分支
     *
     * @param from        源值
     * @param declaredType 声明类型（可空，空则按运行时类型）
     * @param visited     已拷贝身份表
     * @param depth       当前深度
     * @return 副本或原引用
     */
    private static Object deepCopyValue(Object from, Type declaredType, IdentityHashMap<Object, Object> visited, int depth) {

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
            val innerType = toOptional ? deepCopyTypeArgument(declaredType, 0) : declaredType;
            val optionalValue = deepCopyValue(optional.get(), innerType, visited, depth + 1);
            return toOptional ? Optional.ofNullable(optionalValue) : optionalValue;
        }

        // JDK 未提供拷贝协议的类型（StringBuilder/AtomicX/BitSet 等）不做结构拷贝，原样返回（共享）：
        // 集合元素、嵌套字段等不经过 copy 决议，需在此兜住（详见 isDeepCopyCapable）
        if(!isDeepCopyCapable(fromClass) || !isDeepCopyCapable(toClass)) {
            return from;
        }

        return deepCopyBean(from, toClass, visited, depth);
    }

    /**
     * 不可拷贝判定缓存：按类缓存（判定结果按类恒定，且含注解查询，热路径避免重复反射）
     */
    private static final CClassValue<Boolean> IS_NOT_COPYABLE_CLASS_VALUE =
            CClassValue.of(CBeanUtils::checkNotCopyable);

    /**
     * 判定类型是否无需拷贝（不可变或按要求放行）
     *
     * @param type 类型
     * @return true 表示原样返回（不拷贝）
     */
    private static boolean isNotCopyable(Class<?> type) {
        return IS_NOT_COPYABLE_CLASS_VALUE.get(type);
    }

    /**
     * 不可拷贝判定的实际计算（仅首次按类执行，结果进 {@link #IS_NOT_COPYABLE_CLASS_VALUE} 缓存）
     *
     * @param type 类型
     * @return true 表示无需拷贝
     */
    private static boolean checkNotCopyable(Class<?> type) {

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
    private static boolean isLambdaType(Class<?> type) {
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
    private static Object deepCopyArray(
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
        val componentType = null != toClass && toClass.isArray()
                ? toClass.getComponentType() : fromComponentType;
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
    private static boolean canWriteBackContainer(Class<?> toClass, Class<?> fromKind) {

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
    private static boolean isAssignableToComponent(Class<?> componentType, Object element) {

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
    private static Class<?> wrapperOf(Class<?> primitive) {

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
    private static Object deepCopyArrayToContainer(
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
    private static Object deepCopyElementsToContainer(
            Collection<?> elements, Type declaredType, Class<?> containerClass,
            IdentityHashMap<Object, Object> visited, int depth
    ) {

        val copy = (Collection<Object>) newDeepCopyContainer(containerClass);
        // 实参解析与不可变判定按声明泛型缓存（见 typeArgumentMemo）
        val elementMemo = typeArgumentMemo(declaredType, 0);
        val elementType = elementMemo.type;
        val elementImmutable = elementMemo.immutable;
        for (val element : elements) {
            copy.add(elementImmutable ? element : deepCopyValue(element, elementType, visited, depth + 1));
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
    private static Object fallbackContainer(Class<?> container) {

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
    private static Object newDeepCopyContainer(Class<?> toClass) {

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
        val fallbackInstance = fallback == container ? null : newDeepCopyInstance(fallback);

        return null != fallbackInstance ? fallbackInstance
                : (Set.class.isAssignableFrom(container) ? new LinkedHashSet<>() : new ArrayList<>());
    }

    /**
     * 迭代器 / 原始流判定：这类值不可复用、但迭代顺序稳定，可据此重建目标容器
     *
     * <p>{@link Iterator}（含集合的 {@code iterator()}）、{@code java.util.stream.BaseStream}
     * （{@code Stream} / {@code IntStream} / {@code LongStream} / {@code DoubleStream}）没有无参构造、
     * 不是集合，按 {@link #isDeepCopyCapable} 之外的路径会被当作"不可实例化 Bean"退化为共享引用——
     * 共享的是一个一次性、对齐到源集合状态的视图，既非副本也不可复用（原实现的静默降级点）。</p>
     *
     * <p>本方法把它们收敛为"可重新迭代的集合"：调用方据此按目标声明类型重建标准容器
     * （{@code Iterable} 形态，见 {@link #deepCopyIterable}）。视图类（{@code Collections.unmodifiableList}
     * 等）不在此列——它们本身就是集合，走常规集合深拷贝。</p>
     *
     * @param from 源值
     * @return 可重新迭代的集合；不属迭代器/流时返回 {@code null}
     */
    private static Collection<?> viewedIterable(Object from) {

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
    private static Collection<?> collectIterator(Iterator<?> from) {

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
    private static Collection<?> collectStream(BaseStream<?, ?> from) {

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
    private static Object deepCopyIterable(
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
    private static Class<?> containerFor(Class<?> target, Class<?> fallback) {

        if(null == target || Object.class == target) {
            return fallback;
        }

        // 目标派生部分（可用实现类 / 按接口族的标准实现）与 fallback 无关，按目标类缓存：
        // 该判定含 assignable 链与"可按类实例化"查表，是深拷贝热点上的固定成本（实测每次约 50ns）
        val derived = CONTAINER_FOR_CLASS_VALUE.get(target);
        return null != derived ? derived : fallback;
    }

    /**
     * 目标容器实现选择缓存：按目标声明类型缓存「可用实现类 / 按接口族的标准实现」
     *
     * <p>判定结果只依赖目标类（{@code fallback} 仅在"目标无法判定族"时兜底，不进缓存），
     * 故可安全按类缓存——深拷贝热点每次建容器都走此判定，缓存后由"assignable 链 + 实例化查表"
     * 降为一次 {@code ClassValue} 查表。</p>
     */
    private static final CClassValue<Class<?>> CONTAINER_FOR_CLASS_VALUE =
            CClassValue.of(CBeanUtils::resolveContainerFor);

    /**
     * 目标容器实现的实际选择（仅首次按目标类执行，见 {@link #CONTAINER_FOR_CLASS_VALUE}）
     *
     * @param target 目标声明类型（非 null、非 Object，调用方已判）
     * @return 容器实现类；无法判定族时返回 {@code null}（由调用方按 fallback 处置）
     */
    private static Class<?> resolveContainerFor(Class<?> target) {

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
    private static Class<?> usableContainerClass(Class<?> target) {

        if(null == target || Object.class == target || target.isInterface() || Modifier.isAbstract(target.getModifiers())) {
            return null;
        }

        return INSTANTIABLE_CLASS_VALUE.get(target) ? target : null;
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
    private static Object deepCopyCollection(
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
            copy.add(elementImmutable ? item : deepCopyValue(item, elementType, visited, depth + 1));
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
    private static Object deepCopyCollectionToArray(
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
            Array.set(copy, index++, immutableFastPath
                    ? element : deepCopyValue(element, componentType, visited, depth + 1));
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
    private static Object deepCopyEntry(
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
    private static Object deepCopyEntryToMap(Object key, Object value, Class<?> toClass) {

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
    private static Object newDate(Object from, Class<?> toClass) {

        val time = ((Date) from).getTime();
        val fromClass = from.getClass();

        val targetClass = null != toClass && Date.class.isAssignableFrom(toClass)
                && toClass.isInstance(from) ? toClass : fromClass;

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
    private static Object deepCopyMap(
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
            val key = keyImmutable ? entry.getKey() : deepCopyValue(entry.getKey(), keyType, visited, depth + 1);
            val value = valueImmutable ? entry.getValue() : deepCopyValue(entry.getValue(), valueType, visited, depth + 1);
            copy.put(key, value);
        }

        return copy;
    }

    /**
     * JavaBean 深拷贝：按同名字段配对；目标类不可实例化时按降级规则处置
     *
     * <p><b>详细设计</b>：复用 {@link #copyWithPlan(Object, Object, IdentityHashMap, int)}（即
     * {@link #copy(Object, Object)} 的同一套计划流程），不另写一套字段配对——字段集合、final/static 剔除、
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
    private static Object deepCopyBean(Object from, Class<?> toClass, IdentityHashMap<Object, Object> visited, int depth) {

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
            return UNSUPPORTED_TARGET_CLASSES.containsKey(toClass) ? SKIP_VALUE : from;
        }

        // 身份表登记须先于字段复制：自引用字段据此指向副本自身
        visited.put(from, to);

        return copyWithPlan(from, to, visited, depth);
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
    private static final CClassValue<DeepPlan> DEEP_PLAN_CLASS_VALUE = CClassValue.of(CBeanUtils::computeDeepPlan);

    /**
     * 计算目标类的深拷贝计划（仅首次按类执行）
     *
     * @param type 目标类（非 null）
     * @return 折叠后的深拷贝计划
     */
    private static DeepPlan computeDeepPlan(Class<?> type) {
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
    private static Object newContainer(Object from, Class<?> toClass) {

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
        val container = containerFor(toClass, from instanceof Map ? Map.class : Collection.class);

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
    private static Object newStandardContainer(Object from, Class<?> toClass) {

        // 源实现类与目标实现类不一致时交回反射路径（保持原容器实现语义）
        if(from.getClass() != toClass) {
            return null;
        }

        val size = from instanceof Collection ? ((Collection<?>) from).size() : 0;
        val mapSize = from instanceof Map ? ((Map<?, ?>) from).size() : 0;

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
    private static final CClassValue<Boolean> INSTANTIABLE_CLASS_VALUE = CClassValue.of(CBeanUtils::checkInstantiable);

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
    private static final ConcurrentMap<Class<?>, Boolean> UNSUPPORTED_TARGET_CLASSES = new ConcurrentHashMap<>();

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
    private static Object newDeepCopyInstance(Class<?> type) {
        return newDeepCopyInstance(type, DEEP_PLAN_CLASS_VALUE.get(type));
    }

    /**
     * 按类实例化（接收调用方已查到的 {@link DeepPlan}，避免重复查表）
     *
     * @param type 类型
     * @param plan 该类型的深拷贝计划（调用方已查出，非 null）
     * @return 实例或 null（不可实例化 / 构造失败）
     */
    private static Object newDeepCopyInstance(Class<?> type, DeepPlan plan) {

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
    private static final CClassValue<Boolean> HAS_FINAL_FIELD_CLASS_VALUE =
            CClassValue.of(CBeanUtils::checkHasFinalInstanceField);

    /**
     * 目标类是否含 final 实例字段的实际判定（仅首次按类执行，命中时打印一次 debug）
     *
     * @param type 类型（非 null）
     * @return true 表示含 final 实例字段
     */
    private static boolean checkHasFinalInstanceField(Class<?> type) {

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
    private static boolean checkInstantiable(Class<?> type) {

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
    private static boolean isViewOrImmutable(Class<?> type) {

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
    private static Class<?> resolveDeepCopyTargetClass(Type declaredType, Class<?> fromClass) {

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
    private static boolean isContainerClass(Class<?> type) {
        return Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type) || type.isArray();
    }

    /**
     * 取 {@code Type} 的原始类
     *
     * @param type 类型
     * @return 原始类或 null
     */
    private static Class<?> rawDeepCopyClass(Type type) {

        if(type instanceof Class) {
            return (Class<?>) type;
        }
        if(type instanceof ParameterizedType) {
            val raw = ((ParameterizedType) type).getRawType();
            return raw instanceof Class ? (Class<?>) raw : null;
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
    private static Type deepCopyTypeArgument(Type type, int index) {

        if(type instanceof ParameterizedType) {
            val arguments = ((ParameterizedType) type).getActualTypeArguments();
            return index < arguments.length ? unwrapDeepCopyWildcard(arguments[index]) : null;
        }
        if(type instanceof WildcardType) {
            return index == 0 ? unwrapDeepCopyWildcard(type) : null;
        }
        if(type instanceof GenericArrayType) {
            return index == 0 ? unwrapDeepCopyWildcard(((GenericArrayType) type).getGenericComponentType()) : null;
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
    private static final ConcurrentMap<TypeArgumentKey, TypeArgumentMemo> TYPE_ARGUMENT_CACHE =
            new ConcurrentHashMap<>();

    /**
     * 类型实参缓存容量上限（缓存键为"类元数据的泛型信息"，实际条目数远小于此值；
     * 达上限即停止写入，命中已有条目不受影响——缓存只影响性能、不影响正确性）
     */
    private static final int TYPE_ARGUMENT_CACHE_MAX_SIZE = 16_384;

    /**
     * 解析类型实参并缓存"类型 + 不可变标志"（深拷贝容器/Map 的热路径入口）
     *
     * @param declaredType 声明类型（可空）
     * @param index        位置
     * @return 解析结果（永不返回 null；解析不出时为 {@link TypeArgumentMemo#EMPTY}）
     */
    private static TypeArgumentMemo typeArgumentMemo(Type declaredType, int index) {

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
            return null != previous ? previous : memo;
        }

        return memo;
    }

    /**
     * 类型实参的实际解析（缓存未命中时执行一次，见 {@link #typeArgumentMemo}）
     *
     * @param key 缓存键（声明泛型 + 位置）
     * @return 解析结果（永不返回 null）
     */
    private static TypeArgumentMemo resolveTypeArgument(TypeArgumentKey key) {

        val resolved = deepCopyTypeArgument(key.type, key.index);
        return null == resolved
                ? TypeArgumentMemo.EMPTY
                : new TypeArgumentMemo(resolved, isDefinitelyImmutable(resolved));
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
    private static Type unwrapDeepCopyWildcard(Type type) {

        if(!(type instanceof WildcardType)) {
            return type;
        }

        val wildcard = (WildcardType) type;
        val upperBounds = wildcard.getUpperBounds();
        if(upperBounds.length > 0 && Object.class != upperBounds[0]) {
            return upperBounds[0];
        }
        val lowerBounds = wildcard.getLowerBounds();
        return lowerBounds.length > 0 ? lowerBounds[0] : null;
    }

    /**
     * 对象转 map，使用 json 属性名
     * @param object 源对象
     * @return 值 map
     */
    public Map<String, Object> toMapJsonName(Object object) {
        if(null == object) {
            return CMap.of();
        }
        return toMap(object, JsonProperty.class, JsonProperty::value);
    }

    /**
     * 对象转 map，使用 下划线 属性名
     * @param object 源对象
     * @return 值 map
     */
    public Map<String, Object> toMapUnderlineName(Object object) {
        if(null == object) {
            return CMap.of();
        }
        return toMap(object, field -> StrUtil.toUnderlineCase(field.getName()));
    }

    /**
     * 对象转 map，使用注解 key
     * @param object 源对象
     * @param annotationClass 注解类
     * @param annotationValueFunction 注解值获取方法
     * @param <T> 注解泛型
     * @return 值 map
     */
    public <T extends Annotation> Map<String, Object> toMap(
            Object object,
            Class<T> annotationClass,
            CFunction<T, String> annotationValueFunction
    ) {
        if(null == object) {
            return CMap.of();
        }
        return toMap(object, field -> CReflectUtils.getFieldName(field, annotationClass, annotationValueFunction));
    }

    /**
     * 对象转 map
     * @param object 对象
     * @return 值 map
     */
    public Map<String, Object> toMap(Object object) {
        if(null == object) {
            return CMap.of();
        }
        return toMap(object, Field::getName);
    }

    /**
     * 对象转 map（核心方法，字段名由入参函数决定）
     *
     * <p><b>详细设计</b>：走计划（预热）路径。按类取 {@link ToMapPlan}
     * （{@code TO_MAP_PLAN_CLASS_VALUE}：字段 + getter MethodHandle，含 final 字段，不剔除集合字段），
     * 运行期遍历计划数组即可，校验与字段收集均在计划期完成，性能瓶颈仅在 map 写入。</p>
     *
     * <p><b>详细步骤</b>：</p>
     * <ol>
     *   <li>{@code object} 为 null → 返回空 map（{@link CMap#of()}）；</li>
     *   <li>取转 Map 计划；JDK 类命中空计划（保持"JDK 类转 map 为空"原语义）；</li>
     *   <li>按计划长度预分配可变 map，遍历条目：key 为 null 跳过 → getter 取值 → 值为 null 跳过 →
     *   {@code putIfAbsent} 写入；写入时已有非空旧值说明 key 冲突，抛 {@link IllegalStateException}；</li>
     *   <li>结果 map 为空返回 {@link CMap#of()}，否则返回不可变视图。</li>
     * </ol>
     *
     * <p><b>边界与已知取舍</b>：final 字段值同样进入 map；null key 过滤、null 值跳过；
     * key 冲突（不同字段映射到同名 key）抛 {@link IllegalStateException}（与 merge 语义一致）；
     * <b>值为浅引用</b>——集合/Bean 等可变值与原对象指向同一实例（本方法只做字段取值，不做深拷贝），
     * 需要副本请改用 {@link #copy(Object, Object)}。</p>
     *
     * @param object 对象
     * @param getFieldNameFunction 获取字段名方法
     * @return 对象值 map（不可变）；object 为 null 时返回空 map
     */
    @SneakyThrows
    public Map<String, Object> toMap(Object object, ToStringFunction<Field> getFieldNameFunction) {

        if(null == object) {
            return CMap.of();
        }

        // JDK 类由计划期返回空计划（热路径零判断），保持"JDK 类转 map 为空"原语义
        val plan = TO_MAP_PLAN_CLASS_VALUE.get(object.getClass());
        val map = CMapUtils.<String, Object>newMap(String.class, plan.entries.length);
        for (val entry : plan.entries) {

            val key = getFieldNameFunction.apply(entry.field);
            if(null == key) {
                continue;
            }

            val value = entry.getterHandle.invokeExact(object);
            if(null == value) {
                continue;
            }

            // key 冲突且双值非空时抛 IllegalStateException（与 merge 语义一致）
            val oldValue = map.putIfAbsent(key, value);
            if(null != oldValue) {
                throw new IllegalStateException("Conflict key: " + key + ", v1: " + oldValue + ", v2: " + value);
            }
        }

        if(map.isEmpty()) {
            return CMap.of();
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * 对象数组元素属性复制，反顺序遍历
     *
     * <p><b>详细设计</b>：从数组末尾向前遍历，逐个把元素复制到同一目标对象，
     * 使数组靠前的元素覆盖靠后的元素（后者先写、前者后写），便于按优先级传入多来源对象。</p>
     *
     * <p><b>详细步骤</b>：数组为空（null 或长度 0）→ 原样返回 {@code to}；
     * 否则下标自 {@code length - 1} 递减到 0，逐个调用 {@link #copy(Object, Object)}（空元素在其内部被跳过），
     * 最终返回 {@code to}。</p>
     *
     * @param fromArr 源对象数组
     * @param to 目标对象
     * @return 目标对象；数组为空时原样返回
     * @param <To> 目标对象泛型
     */
    public <To> To copyFromArr(Object[] fromArr, To to) {

        if(ArrayUtil.isEmpty(fromArr)) {
            return to;
        }

        for (int i = fromArr.length-1; i >= 0; i--) {
            val source = fromArr[i];
            CBeanUtils.copy(source, to);
        }

        return to;
    }

    /**
     * 对象数组元素属性复制，反顺序遍历
     * @param fromArr 源对象数组
     * @param toClass 目标对象类
     * @return 目标对象
     * @param <To> 目标对象泛型
     */
    public <To> To copyFromArr(Object[] fromArr, Class<To> toClass) {

        if(ArrayUtil.isEmpty(fromArr)) {
            return null;
        }

        val to = CReflectUtils.newInstance(toClass);
        return copyFromArr(fromArr, to);
    }

    /**
     * 共享写入动作（不可变值与目标兼容：直接写入同一引用）
     */
    private static final CopyAction SHARE_ACTION = CopyAction.builder().kind(CopyActionKind.SHARE).build();

    /**
     * 深拷贝写入动作（可变值与目标兼容，或跨类型结构拷贝）
     */
    private static final CopyAction DEEP_ACTION = CopyAction.builder().kind(CopyActionKind.DEEP).build();

    /**
     * 跳过动作（无可用写入方式）
     */
    private static final CopyAction SKIP_ACTION = CopyAction.builder().kind(CopyActionKind.SKIP).build();

    /**
     * 决议单个属性的复制动作（<b>唯一决议点</b>）
     *
     * <p><b>决议规则</b>（先看值侧是否可变，再看目标能否容纳）：</p>
     * <table border="1">
     *   <caption>决议表</caption>
     *   <tr><th>值的运行时类型</th><th>目标声明类型</th><th>决议</th></tr>
     *   <tr>
     *     <td>不可拷贝/不可变（String、包装类、Number、时间、枚举、Class、Optional、UUID、Locale、Currency、Charset、URI 等）</td>
     *     <td>可赋值（含原始↔包装等价）</td>
     *     <td>共享写入</td>
     *   </tr>
     *   <tr>
     *     <td>不可拷贝/不可变</td>
     *     <td>不可赋值</td>
     *     <td>显式转换器 → 转换；无转换器 → 跳过</td>
     *   </tr>
     *   <tr>
     *     <td>可拷贝/可变（集合、Map、数组、Bean、Date 等）</td>
     *     <td>可赋值</td>
     *     <td>深拷贝（不可变运行期值由深拷贝原样返回，结果等价共享）</td>
     *   </tr>
     *   <tr>
     *     <td>可拷贝/可变</td>
     *     <td>不可赋值</td>
     *     <td>显式转换器优先 → 转换；否则目标确定不可变 → 跳过；否则跨类型深拷贝（如 StringBuilder→StringBuffer）</td>
     *   </tr>
     * </table>
     *
     * <p><b>注</b>：源<b>声明为容器</b>（集合/Map/数组）的字段在计划期即内联为深拷贝（见
     * {@link #resolvePlanAction}），不经本运行期决议表；其目标不兼容时由写回失败降级为跳过（见 {@code copyWithPlan}）。</p>
     *
     * <p><b>设计要点</b>：本方法取代旧实现中"计划期 assignable 判断 + {@code convertOpt} + 后置深拷贝"三处
     * 并行决定写什么值的问题——一个属性一次决议。转换器复用 {@link CConvertUtils#getConverter(Class, Class)}
     * 的既有语义（Object 源兜底优先级最低，Date→String 等精确转换不被抢占）；
     * {@link CFunction#SELF}（可赋值）不视为转换器，避免与共享/深拷贝分支重复。</p>
     *
     * @param valueClass 值的运行时类型
     * @param toClass    目标字段声明类型
     * @return 决议结果（共享 / 转换 / 深拷贝 / 跳过）
     */
    private static CopyAction resolveCopyAction(Class<?> valueClass, Class<?> toClass) {

        val action = doResolveCopyAction(valueClass, toClass);

        // 决议按 (值类型, 目标类型) 仅计算一次（结果进 COPY_ACTION_BI_CLASS_VALUE 缓存），此处日志不会进热路径
        log.debug("属性决议（首次计算并缓存）：{} → {}：{}", valueClass.getName(), toClass.getName(), action.kind);
        return action;
    }

    /**
     * 决议的实际计算（仅首次按 (值类型, 目标类型) 执行，见 {@link #resolveCopyAction(Class, Class)}）
     *
     * @param valueClass 值的运行时类型
     * @param toClass    目标字段声明类型
     * @return 决议结果
     */
    private static CopyAction doResolveCopyAction(Class<?> valueClass, Class<?> toClass) {

        if(isNotCopyable(valueClass)) {
            // 不可变值：无深拷贝可言——可赋值即共享，否则只能转换（无转换器则跳过）
            if(ClassUtil.isAssignable(toClass, valueClass)) {
                return SHARE_ACTION;
            }
            val converter = CConvertUtils.getConverter(valueClass, toClass);
            return null != converter && CFunction.SELF != converter ? convertAction(converter) : SKIP_ACTION;
        }

        // 可变值：同型/父型/Object 声明 ⇒ 深拷贝（核心语义）；
        // JDK 未提供拷贝协议的可变类型（StringBuilder/AtomicX/BitSet 等）无法结构拷贝 ⇒ 共享引用（详见 isDeepCopyCapable）
        if(ClassUtil.isAssignable(toClass, valueClass)) {
            if(isDeepCopyCapable(valueClass)) {
                return DEEP_ACTION;
            }
            log.debug("JDK 类型未提供拷贝协议，属性按共享引用处理：{}", valueClass.getName());
            return SHARE_ACTION;
        }

        // Optional 族：可能需"拆包"写入非 Optional 声明（如 Optional<List<X>> → List<X> 字段）。
        // 这类跨声明不能走类型转换器——转换器只做值形态转换（如 toString），拆包是结构语义，
        // 且转换器对 Optional 常返回自身（写回目标必然 ClassCastException）；
        // 故与"同型可赋值"同等优先判为深拷贝，交 deepCopyValue 的 Optional 分支按目标声明决定是否拆包
        if(Optional.class.isAssignableFrom(valueClass) && !Optional.class.isAssignableFrom(toClass)) {
            return DEEP_ACTION;
        }

        // 跨类型：显式转换器优先
        val converter = CConvertUtils.getConverter(valueClass, toClass);
        if(null != converter && CFunction.SELF != converter) {
            return convertAction(converter);
        }

        // 无转换器：目标确定不可变（装不下）或目标不可结构拷贝 ⇒ 跳过；否则按目标声明类型做结构深拷贝
        if(isDefinitelyImmutable(toClass)) {
            return SKIP_ACTION;
        }
        if(!isDeepCopyCapable(toClass)) {
            log.debug("目标类型为 JDK 未提供拷贝协议的类型，属性跳过不写入：{} → {}", valueClass.getName(), toClass.getName());
            return SKIP_ACTION;
        }
        return DEEP_ACTION;
    }

    /**
     * 类型是否支持结构深拷贝（自定义/第三方类，或已具备拷贝协议的 JDK 类型）
     *
     * <p>已具备拷贝协议的 JDK 类型：集合、Map、数组、{@code Date}、{@code Calendar}、{@code Optional}
     * （{@link #deepCopyValue(Object, Type, IdentityHashMap, int)} 中有专门分支）。</p>
     *
     * <p>其余 JDK 类型（{@code StringBuilder}/{@code AtomicInteger}/{@code BitSet} 等）<b>不做字段级结构拷贝</b>：
     * JDK9+ 强封装下无法反射访问其内部字段，且结构拷贝易产出"空壳对象"（静默丢数据，比共享引用更危险）；
     * 故这类值退化为共享引用（同型可赋值时）或跳过，需要拷贝时请通过
     * {@link CConvertUtils#addConverter(Class, Class, CFunction)} 注册显式转换器。</p>
     *
     * <p><b>不做按类缓存</b>：非 JDK 类在 {@code isJdkClass} 处即返回 true，内联判定约 3~5ns，
     * 比一次 ClassValue 查表（约 9ns）更快；实测归一化指标反而变差，故保持内联判定
     * （缓存只对"判定成本高于查表成本"的类型成立，如 JDK 容器的 assignableFrom 链）。</p>
     *
     * @param type 类型（值的运行时类型或目标声明类型）
     * @return true 表示支持结构深拷贝
     */
    private static boolean isDeepCopyCapable(Class<?> type) {

        if(!CClassUtils.isJdkClass(type)) {
            return true;
        }

        return Collection.class.isAssignableFrom(type)
                || Map.class.isAssignableFrom(type)
                || type.isArray()
                || Date.class.isAssignableFrom(type)
                || Calendar.class.isAssignableFrom(type)
                || Optional.class.isAssignableFrom(type);
    }

    /**
     * 构建转换动作
     *
     * @param converter 转换器
     * @return 转换动作
     */
    private static CopyAction convertAction(CFunction<Object, ?> converter) {
        return CopyAction.builder()
                .kind(CopyActionKind.CONVERT)
                .converter(converter)
                .build();
    }

    /**
     * 执行属性复制动作（<b>唯一执行点</b>）：一个属性只执行、只写入一次
     *
     * @param action      决议结果
     * @param fromValue   值（非 null，调用方已判空）
     * @param genericType 目标字段声明泛型（深拷贝解析元素/键值类型）
     * @param fieldName   字段名（仅用于失败日志）
     * @param visited     深拷贝身份表（源 → 副本）
     * @param depth       当前对象所在层级（深拷贝递归深度保护基数）
     * @return 要写入的值；{@link #SKIP_VALUE} 表示不写入（跳过 / 转换结果为 null / 深拷贝失败）
     */
    private static Object applyCopyAction(
            CopyAction action, Object fromValue, Type genericType, String fieldName,
            IdentityHashMap<Object, Object> visited, int depth
    ) {

        switch (action.kind) {

            case SHARE:
                return fromValue;

            case CONVERT: {
                // 转换器可能返回 null：视为不写入（保持既有语义）
                val converted = action.converter.apply(fromValue);
                return null == converted ? SKIP_VALUE : converted;
            }

            case DEEP: {
                try {
                    return deepCopyValue(fromValue, genericType, visited, depth + 1);
                } catch (Throwable e) {
                    log.debug("字段深拷贝失败，跳过：{}", fieldName, e);
                    return SKIP_VALUE;
                }
            }

            default:
                return SKIP_VALUE;
        }
    }

    /**
     * 构建复制计划：为每个"可写且有同名源字段"的目标字段生成一条条目（字段配对在计划期完成）
     *
     * <p>计划期剔除目标 final 字段与源对象无同名字段；其余字段尝试<b>内联决议</b>
     * （{@link CopyEntry#action}），内联条件按"运行期值类型能否由声明类型确定"判定：</p>
     * <ul>
     *   <li><b>源声明为集合/Map/数组</b>：值必为容器（可变），且容器源不参与转换 ⇒ 一律内联深拷贝
     *   （与目标不兼容时由深拷贝/写入失败降级为跳过，与运行期决议结果一致）；</li>
     *   <li><b>源声明为 final 类或原始类型</b>：运行期类型必等于声明类型 ⇒ 内联
     *   {@link #resolveCopyAction(Class, Class)} 的决议（决议为跳过则不生成条目）；</li>
     *   <li><b>其余</b>（接口、抽象类、非 final 类、Object 等宽声明）：不内联，运行期按实际值类型决议
     *   （{@code COPY_ACTION_BI_CLASS_VALUE}）。不可按声明类型内联的原因：声明 Object 实际持有 Date 时，
     *   必须由运行期命中 Date→String 精确转换（Object→String 兜底优先级最低，
     *   见 {@link CConvertUtils#getConverter(Class, Class)}），计划期以 Object 决议会误判。</li>
     * </ul>
     *
     * <p>JDK 源类返回空计划（判断按类恒定，置于计划期仅计算一次，热路径零判断）。</p>
     *
     * @param fromClass 源类
     * @param toClass   目标类
     * @return 复制计划
     */
    private static CopyPlan getCopyPlan(Class<?> fromClass, Class<?> toClass) {

        // JDK 源类无实例字段可拷贝（判断结果按类恒定，置于计划期仅计算一次）
        if(CClassUtils.isJdkClass(fromClass)) {
            return EMPTY_COPY_PLAN;
        }

        val fromFieldMap = CReflectUtils.getInstanceFieldMap(fromClass);
        val entries = new ArrayList<CopyEntry>();
        // 计数用显式 int：val 为 final（声明即定值）不可自增，需可变的局部变量
        int inlinedCount = 0;
        for (val toField : CReflectUtils.getInstanceFieldMap(toClass).values()) {

            if(CReflectUtils.isFinal(toField)) {
                continue; // final 字段不可写
            }

            val fromField = fromFieldMap.get(toField.getName());
            if(null == fromField) {
                continue; // 源对象无同名字段
            }

            val action = resolvePlanAction(fromField.getType(), toField.getType());
            if(SKIP_ACTION == action) {
                continue; // 计划期可证无需写入：不生成条目
            }
            if(null != action) {
                inlinedCount++;
            }

            // 一次性：计划按 (源类, 目标类) 只构建一次（COPY_PLAN_BI_CLASS_VALUE），句柄存入 CopyEntry 长期持有，
            // 故用生成版并按统一 Object 签名适配，不进句柄缓存
            entries.add(newCopyEntry(
                    CMethodHandleUtils.toGetterHandle(fromField).asType(CMethodHandleUtils.GETTER_HANDLE_TYPE),
                    CMethodHandleUtils.toSetterHandle(toField).asType(CMethodHandleUtils.SETTER_HANDLE_TYPE),
                    toField.getType(),
                    toField.getGenericType(),
                    toField.getName(),
                    action
            ));
        }

        // 计划按 (源类, 目标类) 仅构建一次：字段配对与可证决议在此定稿（非内联字段首次取值时决议并缓存）
        log.debug(
                "构建复制计划 {} → {}：条目 {}（计划期内联决议 {} / 首次取值时决议 {}）",
                fromClass.getName(), toClass.getName(), entries.size(), inlinedCount, entries.size() - inlinedCount
        );

        return CopyPlan.builder()
                .entries(entries.toArray(new CopyEntry[0]))
                .build();
    }

    /**
     * 计划期决议：仅当运行期值类型可由声明类型确定时才内联（否则返回 null 交由运行期决议）
     *
     * @param fromType 源字段声明类型
     * @param toType   目标字段声明类型
     * @return 可内联的决议（共享/转换/深拷贝/跳过）；不可证返回 null
     */
    private static CopyAction resolvePlanAction(Class<?> fromType, Class<?> toType) {

        // 容器声明：值必为容器（可变）且容器源不参与转换 ⇒ 深拷贝
        if(isContainerType(fromType)) {
            return DEEP_ACTION;
        }

        // final 类与原始类型：运行期类型必等于声明类型 ⇒ 决议恒定，可内联
        if(fromType.isPrimitive() || Modifier.isFinal(fromType.getModifiers())) {
            return resolveCopyAction(fromType, toType);
        }

        return null;
    }

    /**
     * 是否集合/Map/数组声明（这类字段的值必为容器，由深拷贝条目处理）
     *
     * @param type 声明类型
     * @return true 表示容器声明
     */
    private static boolean isContainerType(Class<?> type) {
        return Collection.class.isAssignableFrom(type)
                || Map.class.isAssignableFrom(type)
                || type.isArray();
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
    private static boolean isDefinitelyImmutable(Type declaredType) {

        val rawClass = rawDeepCopyClass(declaredType);
        return null != rawClass && Modifier.isFinal(rawClass.getModifiers()) && isNotCopyable(rawClass);
    }

    /**
     * 构建转 map 计划（字段 + getter MethodHandle，含 final 与集合字段）
     *
     * @param objClass 目标对象类
     * @return 转 map 计划；JDK 类返回空计划
     */
    private static ToMapPlan getToMapPlan(Class<?> objClass) {

        // JDK 类转 map 为空（判断结果按类恒定，置于计划期仅计算一次）
        if(CClassUtils.isJdkClass(objClass)) {
            return EMPTY_TO_MAP_PLAN;
        }

        val fieldMap = CReflectUtils.getInstanceFieldMap(objClass);
        val entries = new ArrayList<ToMapEntry>(fieldMap.size());
        for (val field : fieldMap.values()) {
            // 一次性：转 map 计划按类只构建一次（TO_MAP_PLAN_CLASS_VALUE），句柄存入 ToMapEntry 长期持有，同复制计划用生成版
            val getterHandle = CMethodHandleUtils.toGetterHandle(field).asType(CMethodHandleUtils.GETTER_HANDLE_TYPE);
            val entry = ToMapEntry.builder()
                    .field(field)
                    .getterHandle(getterHandle)
                    .build();
            entries.add(entry);
        }
        val entriesArray = entries.toArray(new ToMapEntry[0]);
        return ToMapPlan.builder()
                .entries(entriesArray)
                .build();
    }

    /**
     * 构建复制条目（字段配对 + 目标类型信息 + 可内联决议）
     *
     * @param getterHandle 源 getter 方法句柄
     * @param setterHandle 目标 setter 方法句柄
     * @param toType       目标字段声明类型（运行期决议的键）
     * @param genericType  目标字段声明泛型（深拷贝解析元素/键值类型）
     * @param fieldName    字段名（失败日志）
     * @param action       计划期内联的决议；null 表示运行期按实际值类型决议
     * @return 复制条目
     */
    private static CopyEntry newCopyEntry(
            MethodHandle getterHandle, MethodHandle setterHandle, Class<?> toType, Type genericType,
            String fieldName, CopyAction action
    ) {
        return new CopyEntry(getterHandle, setterHandle, toType, genericType, fieldName, action);
    }

    /**
     * 使用缓存的 setter 方法句柄写入字段值
     */
    @SneakyThrows
    private static void setValueWithHandle(Object to, Field toField, Object toValue) {
        CMethodHandleUtils.getSetterHandle(toField).invoke(to, toValue);
    }

    /**
     * 复制计划：按 (源类, 目标类) 预计算的字段级复制条目
     * <p>每个可写同名字段一条条目，条目自带"计划期可证的内联决议"或留空交由运行期决议
     * （见 {@link #resolvePlanAction(Class, Class)}）。</p>
     */
    @Builder
    private static final class CopyPlan {

        final CopyEntry[] entries;

    }

    /**
     * 复制条目：一个属性一条，运行期"取一次值 → 决议一次（首算即缓存）→ 执行一次 → 写一次"
     */
    private static final class CopyEntry {

        final MethodHandle getterHandle;

        final MethodHandle setterHandle;

        /** 目标字段声明类型（运行期决议键；内联决议时仅作记录） */
        final Class<?> toType;

        /** 目标字段声明泛型（深拷贝解析集合元素/Map 键值类型） */
        final Type genericType;

        /** 字段名（失败日志，Map 源场景由调用方传 map key） */
        final String fieldName;

        /** 计划期内联决议；null 表示运行期按 (值类型, toType) 查 {@code COPY_ACTION_BI_CLASS_VALUE} */
        final CopyAction action;

        /**
         * 首次决议缓存：同一实体类型反复复制时值类型稳定，热路径只做一次类比较（不查决议表）；
         * 值类型变化时重新决议并覆盖。持有"类 + 决议"的不可变对（单 volatile 读，避免读到半更新对）
         */
        private volatile ActionMemo memo;

        CopyEntry(
                MethodHandle getterHandle, MethodHandle setterHandle, Class<?> toType, Type genericType,
                String fieldName, CopyAction action
        ) {
            this.getterHandle = getterHandle;
            this.setterHandle = setterHandle;
            this.toType = toType;
            this.genericType = genericType;
            this.fieldName = fieldName;
            this.action = action;
        }

        /**
         * 取该字段在当前值类型下的决议（首次计算后缓存，见 {@link ActionMemo}）
         *
         * @param valueClass 值的运行时类型
         * @return 决议结果
         */
        CopyAction actionOf(Class<?> valueClass) {

            val current = memo;
            if(null != current && valueClass == current.valueClass) {
                return current.action;
            }

            val resolved = COPY_ACTION_BI_CLASS_VALUE.get(valueClass, toType);
            memo = new ActionMemo(valueClass, resolved);
            return resolved;
        }

    }

    /**
     * 首次决议缓存对：值运行时类型 + 其决议结果（不可变，单 volatile 引用读写保证成对可见）
     */
    private static final class ActionMemo {

        final Class<?> valueClass;

        final CopyAction action;

        ActionMemo(Class<?> valueClass, CopyAction action) {
            this.valueClass = valueClass;
            this.action = action;
        }

    }

    /**
     * 复制动作（一个属性一次决议的结果）
     */
    @Builder
    private static final class CopyAction {

        final CopyActionKind kind;

        /** 转换器（仅 {@link CopyActionKind#CONVERT} 使用） */
        final CFunction<Object, ?> converter;

    }

    /**
     * 复制动作类型
     */
    private enum CopyActionKind {

        /** 共享写入：不可变值与目标兼容，直接写入同一引用 */
        SHARE,

        /** 转换后写入：显式转换器（含 Object 源兜底） */
        CONVERT,

        /** 深拷贝后写入：集合/Map/数组/Bean/Date 等可变值 */
        DEEP,

        /** 不写入：不可赋值、无转换器且目标装不下 */
        SKIP

    }

    /**
     * 转 map 计划：按类预计算的字段与 getter 方法句柄（含 final 字段）
     */
    @Builder
    private static final class ToMapPlan {

        final ToMapEntry[] entries;

    }

    /**
     * 转 map 条目
     */
    @Builder
    private static final class ToMapEntry {

        final Field field;

        final MethodHandle getterHandle;

    }

}
