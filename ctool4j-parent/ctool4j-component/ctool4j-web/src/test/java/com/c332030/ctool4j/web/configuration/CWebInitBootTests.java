package com.c332030.ctool4j.web.configuration;

import com.c332030.ctool4j.spring.configuration.CSpringConfiguration;
import com.c332030.ctool4j.spring.lifecycle.ICStarted;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Description: CWebInitBootTests
 * </p>
 *
 * <p>是 {@link CWebInit} 启动完成回调的启动型集成测试用例。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>{@code CWebInit} 的清理动作发生在"容器就绪之后"，而这一点只有真实走一遍 {@code SpringApplication.run} 才确认得了；
 *   单测里手工调 {@code onStarted()} 只能证明方法体正确、证明不了它落在启动流程的哪个阶段。</li>
 *   <li>断言点取两处，缺一不可：① {@code CWebInit} 在真实启动后确实被回调过（触发链
 *   {@code ICStarted} → {@code CWebInit.onStarted} 通）；② 真实启动不因清理而失败（容器活动）。</li>
 *   <li>用独立的 {@code SpringApplication} 而非 {@code @SpringBootTest}：后者在测试框架内完成启动，
 *   拿不到"run 走完运行器阶段"这一时刻。</li>
 *   <li>启动的容器须携带框架配置（{@code CSpringConfiguration}）才有 {@code ICStarted} 的运行器路径，
 *   故显式 {@code @Import}；web 类型置 {@code NONE}，避免为一次回调验证拉起 web 容器。</li>
 * </ul>
 *
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对"启动完成回调＝{@code SpringApplication.run} 执行完成后"的约定
 *   （见功能级设计文档 {@code doc/design/spring/started.adoc}）。</li>
 *   <li>依据黑盒原则：只断言可观测结果（回调次数、容器状态），不假设运行器在容器内的注册细节。</li>
 *   <li>依据测试规范「最真实场景优先」：走真实的 {@code SpringApplication.run}，不绕过它简化。</li>
 * </ul>
 *
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：真实启动完成后 {@code CWebInit} 回调一次、容器保持活动。</li>
 *   <li>未覆盖：清理的具体条目（属 {@code ConditionalOnMissingExceptionHandlerConditionTests} 的用例面）；
 *   多运行器并存时的相对顺序（由 {@code @Order} 决定）。</li>
 * </ul>
 *
 * <h2>启动完成后回调</h2>
 * <ul>
 *   <li>1.1 真实 run：{@code CWebInit} 回调一次，容器活动</li>
 * </ul>
 *
 * @since 2026/9/23
 * @version 1.0
 * @see CWebInit
 */
public class CWebInitBootTests {

    /**
     * 对应测试用例 1.1：真实 {@code SpringApplication.run} 完成后 {@code CWebInit} 回调一次
     */
    @Test
    public void onStartedInvokedOnRealBoot() {

        val application = new SpringApplication(TestApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);

        RecordingWebInit.reset();

        try (val applicationContext = application.run()) {

            Assertions.assertTrue(applicationContext.isActive(), "容器启动后应处于活动状态");
            Assertions.assertEquals(1, RecordingWebInit.STARTED_COUNT.get(),
                "启动完成后应回调一次，且清理不中断启动");

        }

    }

    /**
     * 启动用配置：按 {@code CTool4jSpringBootTest} 的同一口径开启自动配置并引入框架配置，
     * 使容器具备完整启动条件与 {@code ICStarted} 运行器路径
     */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(CSpringConfiguration.class)
    public static class TestApplication {

    }

    /**
     * 测试用启动完成回调实现：记录 {@code CWebInit} 的回调次数
     * <p>与 {@code CWebInit} 同类型（{@code CWebInit}）——断言的是框架自带的那个实现被真实启动回调到。</p>
     */
    @Component
    public static class RecordingWebInit extends CWebInit {

        /**
         * 回调次数
         */
        static final AtomicInteger STARTED_COUNT = new AtomicInteger();

        /**
         * 复位静态状态：同一 JVM 内多次启动容器时互不影响
         */
        static void reset() {
            STARTED_COUNT.set(0);
        }

        /**
         * 启动完成回调：记下调用次数后走父类（框架）的清理实现
         */
        @Override
        public void onStarted() {

            STARTED_COUNT.incrementAndGet();

            super.onStarted();
        }

    }

}
