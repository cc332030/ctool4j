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
 * 详见 {@code doc/design/core/CLazyRef.adoc} 2.3 节与 {@code doc/design/core/CLazyRefTests.adoc} 用例 1.13/1.14。
 * </p>
 *
 * @param <T> 值类型
 * @since 2026/9/10
 * @see "doc/design/core/CLazyRef.adoc"
 * @see "doc/design/core/CLazyRefTests.adoc"
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
