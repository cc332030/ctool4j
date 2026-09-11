package com.c332030.ctool4j.spring.configuration;

import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import com.c332030.ctool4j.definition.constant.CTool4jConstants;
import com.c332030.ctool4j.spring.util.CAutowiredUtils;
import com.c332030.ctool4j.spring.util.CSpringUtils;
import lombok.val;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CStrUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>
 * Description: CAutowiredScanConfiguration
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CAutowiredScanConfiguration}：{@code CAutowiredScan} 的装配入口，负责「注册生成类 + 执行注入」。</p>
 * <ul>
 *   <li>注册：把编译期生成的 {@code {类名}Init} 注册为 Bean，使业务模块是否被组件扫描不再构成前提。</li>
 *   <li>注入：调用 {@link CAutowiredUtils#autowiredScan(ApplicationContext)} 完成静态字段注入。</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>不在编译期写死注册文件</b>：生成类位于业务包内、由业务模块自有 classpath 承载，
 *   写死 {@code META-INF/services} 或 {@code spring.factories} 会与模块拆分强耦合；
 *   改为运行期扫描 {@code CAutowiredScan} 推导生成类名，模块移动/改名无需改配置。</li>
 *   <li><b>不依赖业务包被组件扫描</b>：生成类的可见性取决于各模块的 {@code @ComponentScan} 配置，
 *   本类在容器注册阶段统一注册，去掉该隐含前提。</li>
 *   <li><b>注册与注入落在容器生命周期的两个节点</b>：注册必须早于容器实例化
 *   （{@link BeanDefinitionRegistryPostProcessor}），注入必须晚于容器就绪
 *   （{@link ContextRefreshedEvent}），两者的前置条件相反，故不由同一个回调完成。</li>
 *   <li>生成类为「构造器注入、无字段、无生命周期方法」的简单类，注册
 *   {@link BeanDefinition#setBeanClassName(String)} 走无参构造即可，无需工厂方法。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>未找到任何 {@code CAutowiredScan} 类</td>
 *     <td>两次扫描结果均为空集合，静默完成，不影响启动</td>
 *   </tr>
 *   <tr>
 *     <td>标记类未生成 {@code {类名}Init}（未启用注解处理器、增量产物缺失）</td>
 *     <td>跳过注册该生成类，由 {@code CAutowiredScan} 声明缺失单独暴露；注入仍按类执行</td>
 *   </tr>
 *   <tr>
 *     <td>生成类无法加载（包名/类名不符）</td>
 *     <td>捕获 {@link Throwable} 后跳过，避免单个异常类中断全部装配</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>启用 {@code CAutowiredScan} / {@code CAutowired} 自动注入的 Spring Boot 应用。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不适用于非 Spring 环境；无 Spring 上下文时静态字段不会被注入。</li>
 *   <li>依赖 {@code @SpringBootApplication} 启动类以确定扫描包（见 {@link CSpringUtils#getBasePackages()}）。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>包扫描在启动阶段执行两次（注册一次、注入一次），带来极小的启动开销。</li>
 *   <li>不处理 {@code CAutowiredScan} 出现在非 Spring 模块（如纯 SDK 模块）中的情形。</li>
 *   <li>扫描范围为 {@code com.c332030.ctool4j} 基础包与各启动类所在包；启动类不在其中时需自行声明扫描包。</li>
 * </ul>
 *
 * @since 2026/9/11
 * @version 1.0
 * @see CAutowiredUtils#autowiredScan(ApplicationContext)
 */
@Lazy(false)
@Configuration
public class CAutowiredScanConfiguration implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {

    /**
     * 应用上下文（就绪后用于取 Bean 完成注入）
     */
    private ApplicationContext applicationContext;

    /**
     * 标记注册阶段已完成，避免重复执行
     */
    private boolean registered;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        // 复用已有注册表（Bean 已定义）时不会回调 postProcessBeanDefinitionRegistry，此处补注册
        if (applicationContext instanceof GenericApplicationContext) {
            registerInits((BeanDefinitionRegistry) ((GenericApplicationContext) applicationContext).getBeanFactory());
        }
        // 否则由 postProcessBeanDefinitionRegistry 回调完成注册
    }

    /**
     * 注册编译期生成的 {@code {类名}Init} 组件
     *
     * <p><b>详细步骤</b>：扫描 classpath 上全部标注 {@code CAutowiredScan} 的类，
     * 按「包名 + 类名 + {@code Init}」推导生成类名并注册为 Bean；推导结果去重，
     * 无法加载的生成类跳过。</p>
     *
     * @param registry Bean 定义注册表
     * @throws BeansException Bean 定义注册失败时抛出
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        registerInits(registry);
    }

    /**
     * 注册编译期生成的 {@code {类名}Init} 组件（幂等）
     *
     * @param registry Bean 定义注册表
     */
    private void registerInits(BeanDefinitionRegistry registry) {

        if (registered) {
            return;
        }
        registered = true;

        val initClassNames = new LinkedHashSet<String>();
        for (val candidateClassName : listAutowiredScanClassNames()) {
            val separatorIndex = candidateClassName.lastIndexOf('.');
            val packageName = separatorIndex < 0 ? "" : candidateClassName.substring(0, separatorIndex) + ".";
            val simpleName = candidateClassName.substring(separatorIndex + 1);
            initClassNames.add(packageName + simpleName + "Init");
        }

        for (val initClassName : initClassNames) {
            if (registry.containsBeanDefinition(initClassName)) {
                continue;
            }

            try {
                Class.forName(initClassName, false, getClassLoader());
            } catch (Throwable ignored) {
                // 未生成或不可加载的 Init 跳过注册，不影响其余装配
                continue;
            }

            RootBeanDefinition beanDefinition = new RootBeanDefinition();
            beanDefinition.setBeanClassName(initClassName);
            registry.registerBeanDefinition(initClassName, beanDefinition);
        }

    }

    /**
     * 执行全部标注 {@code CAutowiredScan} 的类的静态字段注入
     *
     * <p>触发点是上下文刷新完成事件（而非 {@code InitializingBean}）：生成类由本类在注册阶段加入 Bean 定义，
     * 须等这些 Bean 实例化完成、上下文可用后才能取到 Bean 写入静态字段。</p>
     *
     * <p><b>详细步骤</b>：按包扫描全部标注 {@code CAutowiredScan} 的类 → 跳过非 {@code Class} 元素 →
     * 以 {@code 包名 + 类名 + "Init"} 取生成类 Bean → 调用
     * {@link CAutowiredUtils#autowiredScan(ApplicationContext)} 完成注入。</p>
     *
     * @param event 上下文刷新完成事件
     */
    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshed(ContextRefreshedEvent event) {
        CAutowiredUtils.autowiredScan(applicationContext);
    }

    /**
     * 注册阶段已完成全部 Bean 定义改写，此处无需额外处理
     *
     * @param beanFactory Bean 工厂
     * @throws BeansException 处理失败时抛出
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 生成类的注册已在 postProcessBeanDefinitionRegistry 完成，此处无需额外改写
    }

    /**
     * 扫描出所有标注 {@code CAutowiredScan} 的类全限定名
     *
     * @return 类全限定名集合，未命中时为空集合
     */
    private Collection<String> listAutowiredScanClassNames() {

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(CAutowiredScan.class));

        val classNames = new LinkedHashSet<String>();
        for (String basePackage : listBasePackages()) {
            scanner.findCandidateComponents(basePackage)
                .forEach(candidate -> classNames.add(candidate.getBeanClassName()));
        }
        return classNames;

    }

    /**
     * 取扫描基础包集合
     *
     * <p>取 {@code CTool4jConstants.BASE_PACKAGE} 与各 {@code @SpringBootApplication} 启动类所在包的并集：
     * 工具类可能落在基础包内（框架自有），而业务方的启动类又可能在其他包。</p>
     *
     * @return 基础包名集合
     */
    private Set<String> listBasePackages() {

        val basePackages = new LinkedHashSet<String>();
        basePackages.add(CTool4jConstants.BASE_PACKAGE);

        val beans = applicationContext.getBeansWithAnnotation(SpringBootApplication.class);
        beans.values().forEach(bean -> {
            val scanBasePackages = AnnotationUtils.findAnnotation(bean.getClass(), SpringBootApplication.class)
                .scanBasePackages();
            if (ArrayUtil.isNotEmpty(scanBasePackages)) {
                for (val scanBasePackage : scanBasePackages) {
                    val basePackage = CStrUtils.toAvailable(scanBasePackage);
                    if (StrUtil.isNotBlank(basePackage)) {
                        basePackages.add(basePackage);
                    }
                }
                return;
            }
            basePackages.add(ClassUtils.getPackageName(bean.getClass()));
        });

        return basePackages;

    }

    /**
     * 获取用于加载生成类的类加载器
     *
     * @return 当前线程上下文类加载器，取不到时回退为本类加载器
     */
    private ClassLoader getClassLoader() {
        val classLoader = Thread.currentThread().getContextClassLoader();
        return null == classLoader ? getClass().getClassLoader() : classLoader;
    }

}
