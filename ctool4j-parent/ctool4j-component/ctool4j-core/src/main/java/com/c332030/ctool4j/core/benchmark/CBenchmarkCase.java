package com.c332030.ctool4j.core.benchmark;

/**
 * <p>
 * Description: 性能基准用例
 * </p>
 * <p>
 * 基准代码放于测试源码目录（不匹配 surefire 测试类命名、无 @Test 方法），
 * 不随 mvn test 执行、不参与打包；仅通过 exec-maven-plugin 显式运行。
 * </p>
 *
 * @since 2026/8/16
 */
public interface CBenchmarkCase {

    /**
     * 用例名称（用于结果对比展示）
     */
    String name();

    /**
     * 全局准备，执行前调用一次（创建源数据等）
     */
    void prepare();

    /**
     * 单次执行被测操作，返回值用于防止 JIT 消除死代码
     */
    Object run();

}
