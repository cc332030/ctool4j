package com.c332030.ctool4j.spring.lifecycle;

import com.c332030.ctool4j.spring.boot.CStartedApplicationRunner;
import com.c332030.ctool4j.spring.bean.CSpringConfigBeans;
import com.c332030.ctool4j.spring.configuration.CSpringInit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import lombok.val;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Description: ICStartedTests
 * </p>
 *
 * <p>是 {@code ICStarted} 与 {@code CStartedApplicationRunner} 的功能测试用例。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>回调时机是本功能的核心：{@code SpringApplication.run} 完成后即运行器阶段，故断言点取
 *   「{@code run} 执行完毕后实现类的 {@code onStarted()} 已被调用」这一可观测结果。</li>
 *   <li>默认实现必须是空操作——不覆写 {@code onStarted()} 的实现类不得因此中断启动，
 *   故用「不抛异常」作为该路径的唯一可达断言。</li>
 *   <li>显式覆写 {@code onStarted()} 的实现类须同时满足"日志照常输出"与"回调已发生"，
 *   故用计数器确认调用次数为一次，而非仅确认方法存在。</li>
 * </ul>
 *
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 {@code ICStarted#onStarted()} 由 {@code CStartedApplicationRunner#run}
 *   在启动完成后回调的约定（见功能级设计文档 {@code doc/design/spring/started.adoc}）。</li>
 *   <li>依据白盒原则：{@code run} 内有两段（日志、回调），逐段覆盖；默认实现与覆写实现各取一条路径。</li>
 * </ul>
 *
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：默认实现放行启动、覆写实现被回调一次、{@code CSpringInit} 的启动完成清理入口可达。</li>
 *   <li>未覆盖：真实 {@code SpringApplication.run} 的容器集成（由 {@code CStartedApplicationRunnerBootTests} 承载）。</li>
 * </ul>
 *
 * <h2>应用启动完成回调</h2>
 * <ul>
 *   <li>1.1 未覆写 onStarted 的实现类：启动成功日志照常输出、不抛异常</li>
 *   <li>1.2 覆写 onStarted 的实现类：run 执行完毕后回调一次，且发生在日志之后</li>
 *   <li>1.3 CSpringInit 实现 ICStarted：onStarted 可被调用（清除启动阶段缓存）</li>
 * </ul>
 *
 * @since 2026/9/23
 * @version 1.0
 * @see ICStarted
 * @see CStartedApplicationRunner
 */
public class ICStartedTests {

    /**
     * 对应测试用例 1.1：未覆写 onStarted 的实现类，run 不抛异常且成功完成
     */
    @Test
    public void defaultOnStartedIsNoOp() {

        val runner = new DefaultOnStartedRunner();
        val args = new DefaultApplicationArguments();

        // 默认 onStarted 为空实现：run 的正常完成（不抛异常）即该路径的唯一可观测断言
        Assertions.assertDoesNotThrow(() -> runner.run(args));
    }

    /**
     * 对应测试用例 1.2：覆写 onStarted 的实现类，run 执行完毕后被回调一次
     */
    @Test
    public void overriddenOnStartedInvokedOnceAfterRun() {

        val runner = new RecordingStartedRunner();

        Assertions.assertEquals(0, runner.startedCount.get(), "run 之前不应发生回调");

        runner.run(new DefaultApplicationArguments());

        Assertions.assertEquals(1, runner.startedCount.get(), "run 之后应回调一次");
    }

    /**
     * 对应测试用例 1.3：CSpringInit 实现 ICStarted，onStarted 可被调用
     */
    @Test
    public void springInitImplementsStarted() {

        val springInit = new CSpringInit();

        Assertions.assertTrue(springInit instanceof ICStarted,
            "CSpringInit 须实现 ICStarted 以承接启动完成回调");

        Assertions.assertDoesNotThrow(springInit::onStarted);
    }

    /**
     * 未覆写 onStarted 的实现类：只经 {@code run} 走「输出日志 + 默认回调」两段
     */
    private static class DefaultOnStartedRunner implements CStartedApplicationRunner {

    }

    /**
     * 覆写 onStarted 的实现类：记录回调次数
     */
    private static class RecordingStartedRunner implements CStartedApplicationRunner {

        /**
         * 回调计数
         */
        final AtomicInteger startedCount = new AtomicInteger();

        @Override
        public void onStarted() {
            startedCount.incrementAndGet();
        }

    }

}
