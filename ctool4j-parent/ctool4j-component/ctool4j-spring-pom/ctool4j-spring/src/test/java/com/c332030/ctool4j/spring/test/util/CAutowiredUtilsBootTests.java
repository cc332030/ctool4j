package com.c332030.ctool4j.spring.test.util;

import com.c332030.ctool4j.spring.annotation.CSpringBootApplication;
import com.c332030.ctool4j.spring.bean.CSpringConfigBeans;
import com.c332030.ctool4j.spring.test.annotation.CTool4jSpringBootTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CAutowiredUtilsBootTests
 * </p>
 *
 * <p>
 * 是 {@code CAutowiredScanConfiguration} 与 {@code CAutowiredUtils.autowiredScan} 的容器集成测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>启动真实容器，验证「扫描 → 注入静态字段」的完整链路确实生效，而不只是单测中的方法调用。</li>
 *   <li>静态工具类不是 Spring Bean，注入只能由 {@code CAutowiredScanConfiguration} 在容器侧触发，
 *   因此断言点为「非 Bean 的静态工具类字段在容器启动后已被写入」。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 {@code CAutowiredScan} 在 Spring 环境下的自动注入约定。</li>
 *   <li>依据黑盒原则：只断言可观测结果（静态字段是否可读、是否非空），不假设内部注册细节。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：容器启动后 {@code CSpringConfigBeans} 的静态注入字段非空。</li>
 *   <li>未覆盖：包外模块的扫描（由使用方集成验证）。</li>
 * </ul>
 * <h2>容器内自动注入</h2>
 * <ul>
 *   <li>1.1 容器启动后静态注入字段非空（{@code autowired}）</li>
 * </ul>
 *
 * @since 2025/12/28
 * @version 1.0
 */
@CTool4jSpringBootTest
public class CAutowiredUtilsBootTests {

    /**
     * 对应测试用例 1.1：容器启动后静态注入字段非空
     */
    @Test
    public void autowired() {

        Assertions.assertNotNull(CSpringConfigBeans.getSpringApplicationConfig());

    }

}
