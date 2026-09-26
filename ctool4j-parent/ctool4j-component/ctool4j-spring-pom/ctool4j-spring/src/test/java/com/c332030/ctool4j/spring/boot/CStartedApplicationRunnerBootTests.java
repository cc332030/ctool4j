package com.c332030.ctool4j.spring.boot;

import com.c332030.ctool4j.spring.configuration.CSpringConfiguration;
import com.c332030.ctool4j.spring.lifecycle.ICStarted;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Description: CStartedApplicationRunnerBootTests
 * </p>
 *
 * <p>是 {@code ICStarted} 触发时机（{@code SpringApplication.run} 执行完成后）的启动型集成测试用例。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>回调时机是本功能的核心，而"run 执行完成后"只有真实走一遍 {@code SpringApplication.run} 才能确认；
 *   单测里手工调 {@code run} 只能证明方法体正确，证明不了它落在启动流程的哪个阶段。</li>
 *   <li>断言点取时序而非"是否被调用"：{@code onStarted()} 内记下此刻 {@code run} 尚未返回，
 *   即回调发生在启动流程内的证据——若回调被挪出运行器阶段，该断言会失败。</li>
 *   <li>用独立的 {@code SpringApplication} 而非 {@code @SpringBootTest} 启动：后者在测试框架内完成启动，
 *   拿不到"run 返回"这一时刻，也就无法断言时序。</li>
 *   <li>启动的容器需携带框架配置（{@code CSpringConfiguration}）才有框架运行器，故显式 {@code @Import}；
 *   web 类型置 {@code NONE}，避免为一次时序验证拉起 web 容器。</li>
 * </ul>
 *
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对"回调时机＝{@code SpringApplication.run} 执行完成后"的约定
 *   （见功能级设计文档 {@code doc/design/spring/started.adoc}）。</li>
 *   <li>依据黑盒原则：只断言可观测结果（回调次数、回调时刻的 run 状态），不假设运行器在容器内的注册细节。</li>
 *   <li>依据测试规范「最真实场景优先」：走真实的 {@code SpringApplication.run}，不绕过它简化。</li>
 * </ul>
 *
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：真实启动完成后回调一次、回调发生在 {@code run} 返回之前（即"执行完成后"这一阶段内）。</li>
 *   <li>未覆盖：多运行器并存时的相对顺序（由 {@code @Order} 决定，不属本接口职责）。</li>
 * </ul>
 *
 * <h2>启动完成后回调</h2>
 * <ul>
 *   <li>1.1 真实 run：回调一次，且在 run 返回之前发生</li>
 * </ul>
 *
 * @since 2026/9/23
 * @version 1.0
 * @see ICStarted
 * @see CStartedApplicationRunner
 */
public class CStartedApplicationRunnerBootTests {

    /**
     * 对应测试用例 1.1：真实 {@code SpringApplication.run} 完成后回调一次，且发生在 run 返回之前
     */
    @Test
    public void onStartedInvokedBeforeRunReturns() {

        val application = new SpringApplication(TestApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);

        RecordingStartedRunner.reset();

        try (val applicationContext = application.run()) {

            Assertions.assertTrue(applicationContext.isActive(), "容器启动后应处于活动状态");
            Assertions.assertEquals(1, RecordingStartedRunner.STARTED_COUNT.get(), "启动完成后应回调一次");
            Assertions.assertFalse(RecordingStartedRunner.RUN_RETURNED.get(),
                "回调必须发生在 run 返回之前（即 run 流程内的启动完成阶段）");

        }

    }

    /**
     * 启动用配置：按 {@code CTool4jSpringBootTest} 的同一口径开启自动配置并引入框架配置，
     * 使容器具备完整启动条件（如 Jackson 的 {@code ObjectMapper}）
     */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(CSpringConfiguration.class)
    public static class TestApplication {

    }

    /**
     * 测试用启动完成回调实现：记录调用次数与调用时刻的 run 状态
     */
    @Component
    public static class RecordingStartedRunner implements CStartedApplicationRunner {

        /**
         * 回调次数
         */
        static final AtomicInteger STARTED_COUNT = new AtomicInteger();

        /**
         * 回调发生时 {@code run} 是否已返回；回调发生在启动流程内时应为 false
         */
        static final AtomicBoolean RUN_RETURNED = new AtomicBoolean(true);

        /**
         * 复位静态状态：同一 JVM 内多次启动容器时互不影响
         */
        static void reset() {
            STARTED_COUNT.set(0);
            RUN_RETURNED.set(true);
        }

        /**
         * 启动完成回调：记下此刻 {@code run} 尚未返回
         */
        @Override
        public void onStarted() {
            RUN_RETURNED.set(false);
            STARTED_COUNT.incrementAndGet();
        }

    }

}
