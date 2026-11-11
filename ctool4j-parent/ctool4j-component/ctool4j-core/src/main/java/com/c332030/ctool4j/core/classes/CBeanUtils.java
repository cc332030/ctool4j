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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

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
 *   一律按<b>目标声明类型</b>重建（不再"跳过不写入"丢字段）。
 *   具体物化实现见 {@link CDeepCopyUtils}（深拷贝子系统，包内可见）。</li>
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
 * 现按目标类在<b>首次深拷贝该类时</b>一次性折叠为 {@code DeepPlan}（{@code DEEP_PLAN_CLASS_VALUE}），
 * 运行期只做一次 {@code ClassValue} 查表。实测收益随"一次复制命中的目标类次数"线性放大
 * （单次命中省约 4.4ns；一次复制命中 41 次时整场景 -3.5%），同口径 A/B 与收敛判定见
 * {@code doc/design/core/perf/beanutils-perf.adoc}。</p>
 *
 * <p><b>不并入折叠的</b>：「构造体抛异常的不支持目标类」是运行期可变状态（登记时刻可能晚于计划创建），
 * 按类缓存会把"登记前"的取值固化，故保持运行期查（理由见 {@code DeepPlan}）。
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
 * @version 1.3
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
    private final CClassValue<ToMapPlan> TO_MAP_PLAN_CLASS_VALUE = CClassValue.of(CBeanUtils::getToMapPlan);

    /**
     * 属性复制动作缓存：按 (值的运行时类型, 目标字段声明类型) 缓存唯一决议结果
     * <p>仅"计划期无法证明"的字段（源声明类型非 final/原始且非容器）在运行期查此表；
     * 计划期已证实的字段由 {@link CopyEntry#action} 内联，不查表。</p>
     */
    private final CBiClassValue<CopyAction> COPY_ACTION_BI_CLASS_VALUE =
            CBiClassValue.of(CBeanUtils::resolveCopyAction);

    /**
     * 空复制计划（JDK 源类使用，热路径零操作）
     */
    private final CopyPlan EMPTY_COPY_PLAN = CopyPlan.builder()
            .entries(new CopyEntry[0])
            .build();

    /**
     * 空转 map 计划（JDK 类使用，热路径零操作）
     */
    private final ToMapPlan EMPTY_TO_MAP_PLAN = ToMapPlan.builder()
            .entries(new ToMapEntry[0])
            .build();


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
            if(CDeepCopyUtils.SKIP_VALUE != toValue) {
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
     * {@link CDeepCopyUtils#SKIP_VALUE}，该字段整条不写入（拷贝与共享引用都写不回目标接口）；集合元素类型优先取目标字段声明泛型，解析不到时按元素运行时类型判定；
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
    static <To> To copyWithPlan(Object from, To to, IdentityHashMap<Object, Object> visited, int depth) {

        // JDK 源类由计划期返回空计划（热路径零判断），保持"JDK 类不拷贝"原语义
        val plan = COPY_PLAN_BI_CLASS_VALUE.get(from.getClass(), to.getClass());
        for (val entry : plan.entries) {

            val fromValue = entry.getterHandle.invokeExact(from);
            if(null == fromValue) {
                continue;
            }

            CopyAction action = entry.action;
            if(null == action) {
                action = entry.actionOf(fromValue.getClass());
            }

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
            if(CDeepCopyUtils.SKIP_VALUE == toValue) {
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
    private final CopyAction SHARE_ACTION = CopyAction.builder().kind(CopyActionKind.SHARE).build();

    /**
     * 深拷贝写入动作（可变值与目标兼容，或跨类型结构拷贝）
     */
    private final CopyAction DEEP_ACTION = CopyAction.builder().kind(CopyActionKind.DEEP).build();

    /**
     * 跳过动作（无可用写入方式）
     */
    private final CopyAction SKIP_ACTION = CopyAction.builder().kind(CopyActionKind.SKIP).build();

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
    private CopyAction resolveCopyAction(Class<?> valueClass, Class<?> toClass) {

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
    private CopyAction doResolveCopyAction(Class<?> valueClass, Class<?> toClass) {

        if(CDeepCopyUtils.isNotCopyable(valueClass)) {
            // 不可变值：无深拷贝可言——可赋值即共享，否则只能转换（无转换器则跳过）
            if(ClassUtil.isAssignable(toClass, valueClass)) {
                return SHARE_ACTION;
            }
            val converter = CConvertUtils.getConverter(valueClass, toClass);
            if(null == converter || CFunction.SELF == converter) {
                return SKIP_ACTION;
            }
            return convertAction(converter);
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
        if(CDeepCopyUtils.isDefinitelyImmutable(toClass)) {
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
     * （{@link CDeepCopyUtils#deepCopyValue(Object, Type, IdentityHashMap, int)} 中有专门分支）。</p>
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
    boolean isDeepCopyCapable(Class<?> type) {

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
    private CopyAction convertAction(CFunction<Object, ?> converter) {
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
     * @return 要写入的值；{@link CDeepCopyUtils#SKIP_VALUE} 表示不写入（跳过 / 转换结果为 null / 深拷贝失败）
     */
    private Object applyCopyAction(
            CopyAction action, Object fromValue, Type genericType, String fieldName,
            IdentityHashMap<Object, Object> visited, int depth
    ) {

        switch (action.kind) {

            case SHARE:
                return fromValue;

            case CONVERT: {
                // 转换器可能返回 null：视为不写入（保持既有语义）
                val converted = action.converter.apply(fromValue);
                if(null == converted) {
                    return CDeepCopyUtils.SKIP_VALUE;
                }
                return converted;
            }

            case DEEP: {
                try {
                    return CDeepCopyUtils.deepCopyValue(fromValue, genericType, visited, depth + 1);
                } catch (Throwable e) {
                    log.debug("字段深拷贝失败，跳过：{}", fieldName, e);
                    return CDeepCopyUtils.SKIP_VALUE;
                }
            }

            default:
                return CDeepCopyUtils.SKIP_VALUE;
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
    private CopyPlan getCopyPlan(Class<?> fromClass, Class<?> toClass) {

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
    private CopyAction resolvePlanAction(Class<?> fromType, Class<?> toType) {

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
    private boolean isContainerType(Class<?> type) {
        return Collection.class.isAssignableFrom(type)
                || Map.class.isAssignableFrom(type)
                || type.isArray();
    }

    /**
     * 构建转 map 计划（字段 + getter MethodHandle，含 final 与集合字段）
     *
     * @param objClass 目标对象类
     * @return 转 map 计划；JDK 类返回空计划
     */
    private ToMapPlan getToMapPlan(Class<?> objClass) {

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
    private CopyEntry newCopyEntry(
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
