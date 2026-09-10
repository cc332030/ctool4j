package com.c332030.ctool4j.core.test.util.lazy;

import com.c332030.ctool4j.core.util.CLazyRef;

/**
 * <p>
 * Description: CLazyMethodRefDepHolder
 * </p>
 * <p>
 * 与 {@link CLazyLambdaDepHolder} 对照：静态字段用 {@link CLazyRef} + <b>方法引用</b>
 * 懒加载可选依赖 {@link COptionalDep}，用于对比两种 supplier 写法在"有依赖 / 无依赖"两种环境下的差异。
 * </p>
 * <p>
 * 方法引用在构造 supplier 时即需解析目标类型，故无依赖环境下类初始化即报错（用例 1.14）；
 * 有依赖环境下正常可用（用例 1.12）。
 * </p>
 *
 * @since 2026/9/10
 */
public class CLazyMethodRefDepHolder {

    /**
     * 懒加载可选依赖（方法引用写法）
     */
    public static final CLazyRef<COptionalDep> DEP = CLazyRef.of(COptionalDep::new);

    /**
     * 取值（触发懒加载）
     *
     * @return 可选依赖实例
     */
    public static COptionalDep dep() {
        return DEP.get();
    }

}
