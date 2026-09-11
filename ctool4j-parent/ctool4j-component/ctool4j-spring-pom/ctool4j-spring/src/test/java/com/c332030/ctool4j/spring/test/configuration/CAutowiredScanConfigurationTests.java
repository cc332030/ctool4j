package com.c332030.ctool4j.spring.test.configuration;

import com.c332030.ctool4j.spring.bean.CSpringConfigBeans;
import com.c332030.ctool4j.spring.configuration.CAutowiredScanConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.c332030.ctool4j.spring.configuration.CSpringConfiguration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

/**
 * <p>
 * Description: CAutowiredScanConfigurationTests
 * </p>
 *
 * <p>
 * 是 {@link CAutowiredScanConfiguration} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>注册阶段需要读取扫描包，而扫描包来自容器中的启动类，因此这里注入一个可用的
 *   {@code ApplicationContext}（仅用于包推导），真实 Bean 定义注册表仍用 {@link DefaultListableBeanFactory}。</li>
 *   <li>断言点放在「注册逻辑的副作用」上：重复执行不增加定义数量、已有同名定义被保留。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对「注册生成类」与「不覆盖使用方定义」的约定。</li>
 *   <li>依据幂等性：同一注册逻辑重复执行不产生重复 Bean 定义。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：注册阶段可重复执行且不产生重复定义；已有同名定义不被覆盖。</li>
 *   <li>未覆盖：真实启动类下推导生成类名的完整装配链路（由 {@code CAutowiredUtilsBootTests} 覆盖）。</li>
 * </ul>
 * <h2>自动注入扫描装配</h2>
 * <ul>
 *   <li>1.1 重复注册不产生重复 Bean 定义（{@code registerInits_idempotent}）</li>
 *   <li>1.2 已有同名定义不被覆盖（{@code registerInits_existingDefinition_kept}）</li>
 * </ul>
 *
 * @since 2026/9/11
 * @version 1.0
 * @see CAutowiredScanConfiguration
 */
public class CAutowiredScanConfigurationTests {

    /**
     * 对应测试用例 1.1：重复注册不产生重复 Bean 定义
     */
    @Test
    public void registerInits_idempotent() {
        // 幂等：注册前先判断 containsBeanDefinition，重复执行不应增加 Bean 定义数量
        GenericApplicationContext context = newContext();
        CAutowiredScanConfiguration configuration = new CAutowiredScanConfiguration();
        configuration.setApplicationContext(context);

        int before = context.getBeanFactory().getBeanDefinitionCount();
        configuration.postProcessBeanDefinitionRegistry(context);
        int afterFirst = context.getBeanFactory().getBeanDefinitionCount();
        configuration.postProcessBeanDefinitionRegistry(context);

        Assertions.assertEquals(before, afterFirst);
        Assertions.assertEquals(afterFirst, context.getBeanFactory().getBeanDefinitionCount());
    }

    /**
     * 对应测试用例 1.2：已有同名定义不被覆盖
     */
    @Test
    public void registerInits_existingDefinition_kept() {
        // 边界：同名定义已存在时必须跳过，避免覆盖使用方自行提供的实现
        GenericApplicationContext context = newContext();
        CAutowiredScanConfiguration configuration = new CAutowiredScanConfiguration();
        configuration.setApplicationContext(context);

        String beanName = CSpringConfigBeans.class.getName() + "Init";
        BeanDefinition existing = Mockito.mock(BeanDefinition.class);
        context.registerBeanDefinition(beanName, existing);

        configuration.postProcessBeanDefinitionRegistry(context);

        Assertions.assertSame(existing, context.getBeanFactory().getBeanDefinition(beanName));
    }

    /**
     * 构造仅含启动类与上下文的测试用上下文（无需真正刷新容器）
     *
     * @return 可用的上下文
     */
    private GenericApplicationContext newContext() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.setEnvironment(new MockEnvironment());
        context.registerBean(CSpringConfiguration.class);
        context.refresh();
        new CSpringConfiguration().setApplicationContext(context);
        return context;
    }

}
