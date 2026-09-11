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
 * <h2>能力目录</h2>
 * <p>{@code CBenchmarkCase} 为性能基准用例接口，定义基准用例的三要素：</p>
 * <ul>
 *   <li>{@code name()}：用例名称（结果对比展示用）</li>
 *   <li>{@code prepare()}：全局准备（执行前调用一次，创建源数据等）</li>
 *   <li>{@code run()}：单次执行被测操作，返回值用于防止 JIT 消除死代码</li>
 * </ul>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>实现性能基准用例，供 {@code CBenchmarkRunner.run} 执行。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>通过返回值（blackhole 累计）防止 JIT 消除死代码。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>基准用例约定</b></p>
 * <ul>
 *   <li>基准代码放于测试源码目录（不匹配 surefire 测试类命名、无 @Test 方法），不随 mvn test 执行、</li>
 *   <li>不参与打包；仅通过 exec-maven-plugin 显式运行。</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.0
 */
public interface CBenchmarkCase {

    /**
     * 用例名称（用于结果对比展示）
     *
     * @return 用例名称
     */
    String name();

    /**
     * 全局准备，执行前调用一次（创建源数据等）
     */
    void prepare();

    /**
     * 单次执行被测操作，返回值用于防止 JIT 消除死代码
     *
     * @return 被测操作结果
     */
    Object run();

}
