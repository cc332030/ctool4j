package com.c332030.ctool4j.web.exception.condition;

import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

/**
 * <p>
 * Description: ConditionalOnMissingExceptionHandlerConditionTests
 * </p>
 *
 * <h2>测试用例目录</h2>
 * <ul>
 *   <li>1.1 valueName 无匹配：条件成立，被标注 bean 装配（valueNameNotMatched_beanRegistered）</li>
 *   <li>1.2 valueName 有匹配（容器内 advice 处理了该类名）：条件不成立，被标注 bean 不装配（valueNameMatched_beanSkipped）</li>
 *   <li>1.3 valueName 指向不存在的类（模拟 Jetty 无 Tomcat 类的场景）：条件成立且不抛异常（valueNameClassAbsent_beanRegistered）</li>
 *   <li>1.4 value 按类型匹配（回归）：容器内 advice 处理该类型时被标注 bean 不装配（valueMatched_beanSkipped）</li>
 * </ul>
 *
 * <h2>测试设计</h2>
 * <ul>
 *   <li>依据测试方法（正例/分支覆盖）：valueName 命中与未命中、类名不存在、value 分支回归。</li>
 *   <li>覆盖场景：见上方编号索引；用 {@code AnnotationConfigApplicationContext} 装配真实容器，按 bean 是否注册断言条件结果。</li>
 *   <li>未覆盖：真实容器（Jetty）下的端到端装配——1.3 以"类名不存在仍可判断"覆盖该兼容性要点。</li>
 *   <li>夹具隔离：本测试的 advice 夹具带 {@code @Profile}（条件测试专用 profile），避免被同包的 Boot 测试上下文组件扫描到。</li>
 *   <li>依据：条件注解为类名匹配时不解析目标类，故缺失该类的环境不因类加载失败而中断装配。</li>
 * </ul>
 *
 * @since 2026/9/18
 * @version 1.0
 * @see ConditionalOnMissingExceptionHandler
 */
public class ConditionalOnMissingExceptionHandlerConditionTests {

    /**
     * 条件测试专用 profile：隔离本类夹具，避免进入其它测试的组件扫描
     */
    private static final String PROFILE = "ctool4j-condition-tests";

    /**
     * 对应测试用例 1.1：valueName 无匹配：被标注 bean 装配
     */
    @Test
    public void valueNameNotMatched_beanRegistered() {

        try (val context = newContext()) {

            context.register(ValueNameHandler.class);
            context.refresh();

            Assertions.assertEquals(1, context.getBeanNamesForType(ValueNameHandler.class).length);
        }
    }

    /**
     * 对应测试用例 1.2：valueName 有匹配：被标注 bean 不装配
     */
    @Test
    public void valueNameMatched_beanSkipped() {

        try (val context = newContext()) {

            context.register(IOExceptionAdvice.class, ValueNameHandler.class);
            context.refresh();

            Assertions.assertEquals(0, context.getBeanNamesForType(ValueNameHandler.class).length);
        }
    }

    /**
     * 对应测试用例 1.3：valueName 指向不存在的类：条件成立且不抛异常（跨容器兼容要点）
     */
    @Test
    public void valueNameClassAbsent_beanRegistered() {

        try (val context = newContext()) {

            context.register(AbsentClassNameHandler.class);
            context.refresh();

            Assertions.assertEquals(1, context.getBeanNamesForType(AbsentClassNameHandler.class).length);
        }
    }

    /**
     * 对应测试用例 1.4：value 按类型匹配（回归）：有匹配时不装配
     */
    @Test
    public void valueMatched_beanSkipped() {

        try (val context = newContext()) {

            context.register(IOExceptionAdvice.class, ValueTypeHandler.class);
            context.refresh();

            Assertions.assertEquals(0, context.getBeanNamesForType(ValueTypeHandler.class).length);
        }
    }

    /**
     * 构建已激活条件测试 profile 的容器
     *
     * @return 应用上下文
     */
    private static AnnotationConfigApplicationContext newContext() {

        val context = new AnnotationConfigApplicationContext();
        context.getEnvironment().setActiveProfiles(PROFILE);

        return context;
    }

    /**
     * 按类名匹配（valueName = IOException）的条件 bean
     */
    @Profile(PROFILE)
    @ControllerAdvice
    @ConditionalOnMissingExceptionHandler(valueName = "java.io.IOException")
    static class ValueNameHandler {

    }

    /**
     * 按类名匹配、且类不存在的条件 bean（模拟容器私有类型缺失的环境）
     */
    @Profile(PROFILE)
    @ControllerAdvice
    @ConditionalOnMissingExceptionHandler(valueName = "org.apache.catalina.connector.ClientAbortException")
    static class AbsentClassNameHandler {

    }

    /**
     * 按类型匹配（value = IOException）的条件 bean
     */
    @Profile(PROFILE)
    @ControllerAdvice
    @ConditionalOnMissingExceptionHandler(IOException.class)
    static class ValueTypeHandler {

    }

    /**
     * 声明处理 IOException 的 advice：使上述两个条件 bean 均不装配
     */
    @Profile(PROFILE)
    @ControllerAdvice
    static class IOExceptionAdvice {

        /**
         * 处理 IOException
         *
         * @param e IO 异常
         */
        @ExceptionHandler(IOException.class)
        public void handle(IOException e) {
        }

    }

}
