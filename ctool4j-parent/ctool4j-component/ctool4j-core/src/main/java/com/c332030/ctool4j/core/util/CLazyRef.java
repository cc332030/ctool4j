package com.c332030.ctool4j.core.util;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

/**
 * <p>
 * Description: CLazyRef
 * </p>
 * <p>
 * 懒加载值持有器：构造时传入 {@link Supplier}，仅在首次 {@link #get()} 时调用 supplier 求值，
 * 之后缓存复用。将双重检查锁（DCL）样板封装在此，使用方无需重复编写判空/加锁代码。
 * </p>
 * <p>
 * 关键用途：类字段可声明为 {@code CLazyRef}，从而把"可选依赖的类加载/初始化"推迟到真正使用时，
 * 避免类初始化阶段因 supplier 引用的类缺失而抛 {@link NoClassDefFoundError}（class not found）。
 * </p>
 * <p>
 * 能否真正延迟，取决于 supplier 的写法（依赖<b>已存在</b>时各写法均可用）：
 * </p>
 * <ul>
 * <li>{@link NoClassDefFoundError} 场景（依赖可能缺失）须用 <b>lambda + 返回类型擦除为
 *     {@code Object}</b>（{@code () -> (Object) Xxx.create()}）：lambda 引导生成的
 *     {@code instantiatedMethodType} 不引用目标类，故类初始化不加载、首次 {@link #get()} 才加载。</li>
 * <li><b>方法引用</b>（{@code Xxx::create}）或 <b>lambda 返回具体类型</b>：构造 supplier 时即需解析
 *     目标类类型，类初始化阶段就会加载该类，无法延迟到 {@link #get()}。</li>
 * </ul>
 * <p>
 * 详见与  用例 1.13/1.14。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CLazyRef&lt;T&gt;} 为懒加载值持有器，实现 {@code java.util.function.Supplier&lt;T&gt;}：</p>
 * <p>设计目标：把"判空 + 加锁 + 缓存"的双重检查锁（DCL）样板封装到一个可复用类中，使用方只需 {@code CLazyRef.of(() -&gt; xxx)} 声明字段即可获得线程安全的懒加载，无需重复编写 DCL 代码。</p>
 * <p>命名：{@code CLazyRef} 遵循本项目"类统一 {@code C} 前缀"规范；{@code Ref} 后缀表示"对一个值的延迟引用持有"， 与 core 模块既有 {@code CRefClassValue}/{@code CRefBiClassValue} 的 {@code Ref} 用法一致。该名与 {@code java.lang.Lazy} （JDK 实际无此类）无继承/扩展关系，仅借语义相近的 {@code Lazy} 命名。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>supplier 为 null</td>
 *     <td>{@code of} 抛 {@code IllegalArgumentException}（Lombok {@code @NonNull} 生成，非 {@code NullPointerException}）</td>
 *   </tr>
 *   <tr>
 *     <td>supplier 返回 null</td>
 *     <td>缓存 null，只求值一次，后续 get 返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>首次 get 抛异常</td>
 *     <td>异常向上抛出，{@code initialized} 保持 false，下次 get 重新尝试求值</td>
 *   </tr>
 *   <tr>
 *     <td>并发首次 get</td>
 *     <td>只执行一次 supplier，其余线程等待后读同一结果</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>昂贵对象的懒初始化（生成器、连接、客户端等），且线程安全共享。</li>
 *   <li>可选依赖的延迟加载（避免类初始化阶段触发 {@code NoClassDefFoundError}）。</li>
 *   <li>替代手写双重检查锁样板。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不需要线程安全、或要求"每次 get 都重新求值"时不应使用。</li>
 *   <li>不支持"求值失败后自动多次重试缓存"，异常时下次 get 会重新求值。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>求值失败不缓存（下次 get 重新求值），符合"失败可重试"预期；如需缓存失败需外部处理。</li>
 *   <li>单值持有，不支持按 key 分别懒加载（按 key 场景用 {@code cache.impl.CClassValue} 或缓存实现）。</li>
 *   <li>因封装了 {@code synchronized}，首次求值存在一次锁开销；已通过无锁快速路径（volatile 读）规避稳态开销。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>双重检查锁（DCL）封装</b></p>
 * <ul>
 *   <li>字段 {@code volatile boolean initialized} 作为求值标志，{@code T value} 保存结果。</li>
 *   <li>{@code value} 无需 volatile：写 {@code value} 在 {@code synchronized} 内，其后写 {@code initialized=true}；</li>
 *   <li>读线程通过 volatile 读 {@code initialized==true} 建立 happens-before，能看见之前的 {@code value} 写。</li>
 * </ul>
 * <p><b>null 结果缓存</b></p>
 * <ul>
 *   <li>用 {@code initialized} 标志（而非 {@code value != null}）判断是否已求值：supplier 返回 {@code null} 时同样只求值一次，</li>
 *   <li>避免"结果为 null 导致每次都重新求值"的常见缺陷。</li>
 * </ul>
 * <p><b>类加载延迟（class not found 防御）</b></p>
 * <ul>
 *   <li>避免类初始化阶段因 supplier 引用的类缺失而抛 {@code NoClassDefFoundError}。</li>
 *   <li>各种 supplier 写法在<b>依赖存在</b>时均正常可用、不抛异常：<b>lambda</b>（{@code () -&gt; X.create()}）、</li>
 *   <li><b>静态方法引用</b>（{@code X::create}）、<b>实例方法引用</b>（{@code instance::create}），可直接声明为具体类型</li>
 *   <li>{@code CLazyRef&lt;X&gt;}；{@code CLazyRef} 亦可直接用于<b>静态字段初始化</b>（用例 1.7~1.12）。</li>
 *   <li><b>方法引用</b>（{@code X::new} / {@code X::create}）或 <b>lambda 返回具体类型 {@code X}</b>：构造 supplier 时即需解析</li>
 *   <li>报 {@code invokedynamic} 引导失败的 {@code BootstrapMethodError}（cause 为 {@code NoClassDefFoundError}，用例 1.14</li>
 *   <li>记录该限制）；lambda 未擦除时同理（该 lambda 写法未单独建用例）。</li>
 *   <li><b>lambda + 返回类型擦除为 {@code Object}</b>（{@code () -&gt; (Object) new X()}，即 {@code CLazyLambdaDepHolder} 的写法）：</li>
 *   <li>上述两种写法分别由 {@code CLazyLambdaDepHolder}（擦除写法）与 {@code CLazyMethodRefDepHolder}（具体类型写法）</li>
 *   <li>承载，各在"有依赖 / 无依赖"两种环境验证（用例 1.11~1.14）：有依赖时两种写法均不抛异常；</li>
 *   <li>无依赖时擦除写法初始化通过、取值才抛错，具体类型写法初始化即抛错。</li>
 *   <li><b>实践建议</b>：{@code CIdUtils.UUID_V7_GENERATOR} 属"依赖可能缺失"场景，必须用 {@code CLazyRef&lt;Object&gt;} +</li>
 *   <li><b>擦除返回类型的 lambda</b>（{@code () -&gt; (Object) Generators.timeBasedEpochGenerator()}），取值时强转。</li>
 *   <li>java-uuid-generator 为 Maven {@code optional}，依赖它的模块（如 {@code ctool4j-mybatis-base}）<b>不一定引入</b>，</li>
 *   <li>而 {@code CIdUtils} 会被 {@code CBizIdUtils} 等间接触发类加载——若不擦除，这些模块将在类初始化阶段抛</li>
 *   <li>{@code NoClassDefFoundError}。故此类场景<b>必须</b>用擦除写法。</li>
 * </ul>
 *
 * @param <T> 值类型
 * @since 2026/9/10
 * @version 1.0
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CLazyRef<T> implements Supplier<T> {

    /**
     * 值提供者
     */
    private final Supplier<T> supplier;

    /**
     * 是否已求值（volatile 保证可见性，作为双重检查锁的判定标志）
     */
    private volatile boolean initialized;

    /**
     * 缓存的求值结果（可能为 null，由 initialized 区分"未求值"）
     */
    private T value;

    /**
     * 创建懒加载值持有器
     * <ul>
     *   <li>{@code of(Supplier&lt;T&gt;)}：静态工厂，创建持有器（不立即求值）。</li>
     * </ul>
     *
     * @param supplier 值提供者（延迟到首次 get 时调用）
     * @param <T>      值泛型
     * @return CLazyRef
     * @throws IllegalArgumentException supplier 为 null（Lombok {@code @NonNull} 生成）
     */
    public static <T> CLazyRef<T> of(@NonNull Supplier<T> supplier) {
        return new CLazyRef<>(supplier);
    }

    /**
     * 获取值：首次调用时求值并缓存，后续直接返回缓存
     * <ul>
     *   <li>{@code get()}：首次调用时执行 supplier 求值并缓存，后续直接返回缓存值。</li>
     *   <li>{@code get()}：先读 {@code initialized}（无锁快速路径），未求值才进入 {@code synchronized(this)} 二次判空并求值。</li>
     *   <li>关键用途：类字段声明为 {@code CLazyRef}，可把"可选依赖类的加载/初始化"推迟到真正 {@code get()} 时，</li>
     *   <li><b>依赖可能缺失</b>时（调用方可能未引入，期望"不调用则不加载"），能否延迟到 {@code get()} 取决于写法：</li>
     *   <li>{@code X} 的类型，目标类会在<b>类初始化</b>阶段被加载，缺失时即抛错，无法延迟到 {@code get()}——方法引用缺失时</li>
     *   <li>可完全延迟到首次 {@code get()}，缺失时在 {@code get()} 才抛错（用例 1.13）。</li>
     *   <li>求值路径由 {@code synchronized(this)} 保护，并发首次 {@code get()} 也只会执行一次 supplier（其余线程阻塞后读缓存）。</li>
     * </ul>
     *
     * @return 值（supplier 返回 null 时同样缓存，不重复求值）
     */
    @Override
    public T get() {

        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    value = supplier.get();
                    initialized = true;
                }
            }
        }

        return value;
    }

}
