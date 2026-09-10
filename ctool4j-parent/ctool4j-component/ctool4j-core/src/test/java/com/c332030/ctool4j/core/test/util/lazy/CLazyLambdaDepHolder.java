package com.c332030.ctool4j.core.test.util.lazy;

import com.c332030.ctool4j.core.util.CLazyRef;

/**
 * <p>
 * Description: CLazyLambdaDepHolder
 * </p>
 * <p>
 * 用 {@link CLazyRef} + <b>lambda</b> 懒加载可选依赖 {@link COptionalDep}，与
 * {@link CLazyMethodRefDepHolder}（方法引用）对照。
 * </p>
 * <p>
 * 关键：本类用于验证"依赖可能缺失"场景，故 lambda 的返回类型擦除为 {@code Object}
 * （{@code (Object) new COptionalDep()}），使 lambda 引导的 {@code instantiatedMethodType} 不引用
 * {@link COptionalDep}，从而类初始化不加载该依赖、取值时才加载（用例 1.13）。
 * </p>
 * <p>
 * 对照：{@link CLazyMethodRefDepHolder} 的方法引用写法无法延迟，类初始化即报错（用例 1.14）。
 * 注意"lambda 返回具体类型"的写法与此类似，但项目内未为其单独建用例（见测试文档"未覆盖"）。
 * </p>
 *
 * @since 2026/9/10
 */
public class CLazyLambdaDepHolder {

    /**
     * 懒加载可选依赖（lambda 写法，返回类型擦除为 Object 以延迟类加载）
     */
    public static final CLazyRef<Object> DEP = CLazyRef.of(() -> (Object) new COptionalDep());

    /**
     * 取值（触发懒加载）
     *
     * @return 可选依赖实例
     */
    public static Object dep() {
        return DEP.get();
    }

}
