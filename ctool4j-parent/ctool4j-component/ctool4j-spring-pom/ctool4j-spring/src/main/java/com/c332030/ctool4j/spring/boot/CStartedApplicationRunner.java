package com.c332030.ctool4j.spring.boot;

import com.c332030.ctool4j.core.log.CLogUtils;
import com.c332030.ctool4j.spring.lifecycle.ICStarted;
import com.c332030.ctool4j.spring.util.CSpringUtils;
import lombok.val;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * <p>
 * Description: CStartedApplicationRunner
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CStartedApplicationRunner}：应用启动完成运行器。</p>
 * <ul>
 *   <li>{@code run(ApplicationArguments)}：输出启动成功日志，并回调 {@link ICStarted#onStarted()}。</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>回调点取 {@code ApplicationRunner}：{@code SpringApplication.run} 在刷新上下文之后调用
 *   {@code callRunners}，运行器正在该阶段执行，即"run 执行完成后"的既定切入点。</li>
 *   <li>启动成功日志先于 {@code onStarted()} 输出：启动完成的事实先落日志，再执行启动后处理，
 *   使 {@code onStarted()} 内的耗时或异常在日志上有明确的起点。</li>
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
 *     <td>实现类未覆写 {@code onStarted()}</td>
 *     <td>默认实现为空操作，启动成功日志照常输出、不中断启动</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要在 {@code SpringApplication.run} 执行完成后处理一次、同时输出启动成功日志的场景。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需 Spring Boot 应用启动流程承载；非 Boot 启动的容器不触发本运行器。</li>
 *   <li>容器内存在多个 {@code ApplicationRunner} 时，执行顺序由 {@code @Order} 决定，
 *   本接口不提供跨运行器的排序保证。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>{@code onStarted()} 抛出的异常按 Spring Boot 对运行器的既定口径处理——启动失败、
 *   容器关闭，不由本类吞掉或改写该语义。</li>
 * </ul>
 *
 * @since 2026/5/12
 * @version 1.0
 */
public interface CStartedApplicationRunner extends ApplicationRunner, ICStarted {

    /**
     * 应用启动后输出启动成功日志，并回调 {@link ICStarted#onStarted()}
     *
     * @param args 启动参数
     */
    @Override
    default void run(ApplicationArguments args) {

        val log = CLogUtils.getLog(CStartedApplicationRunner.class);
        log.info("(♥◠‿◠)ﾉﾞ  {} 启动成功  ლ(´ڡ`ლ)ﾞ", CSpringUtils.getApplicationName());

        onStarted();

    }

}
