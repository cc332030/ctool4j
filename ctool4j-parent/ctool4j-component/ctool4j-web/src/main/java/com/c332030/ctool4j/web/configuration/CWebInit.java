package com.c332030.ctool4j.web.configuration;

import com.c332030.ctool4j.spring.boot.CStartedApplicationRunner;
import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import com.c332030.ctool4j.web.exception.condition.ConditionalOnMissingExceptionHandlerCondition;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: CWebInit
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CWebInit} 为 web 模块 Spring 生命周期回调，{@code @Component} + 实现 {@link ICSpringInit}、{@link CStartedApplicationRunner}：</p>
 * <ul>
 *   <li>{@code onInit()}：容器初始化完成回调（当前无处理逻辑）。</li>
 *   <li>{@code onStarted()}：应用启动完成回调，清理本模块的装配期缓存。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>onInit 当前为空</td>
 *     <td>无实际行为，仅作为扩展点</td>
 *   </tr>
 *   <tr>
 *     <td>被清理项本就为空（未在装配期写入）</td>
 *     <td>清理为空操作、不报错，不影响启动</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>web 模块的 Spring 生命周期扩展点：启动初始化与启动完成清理。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>须在 {@code SpringApplication.run} 流程内承载（{@code CStartedApplicationRunner#run} 在启动完成后回调 {@code onStarted()}）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>清理只在启动完成后执行一次：启动期间若已有读取方依赖被清理项，须改为由容器 Bean 持有，
 *   不能寄望启动后的缓存仍可用。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>生命周期回调</b></p>
 * <ul>
 *   <li>{@code onInit} 经 {@link ICSpringInit} 在容器初始化完成时回调，
 *   {@code onStarted} 经 {@link CStartedApplicationRunner} 在 {@code SpringApplication.run} 执行完成后回调。</li>
 * </ul>
 * <p><b>清理放在本模块</b></p>
 * <ul>
 *   <li>被清理项（{@link ConditionalOnMissingExceptionHandlerCondition} 的按 advice 类型声明集）
 *   属本模块，故清理的调用方也落在本模块——依赖方向是单向的（web → spring），由 spring 反向清理会造出环形依赖。</li>
 * </ul>
 *
 * @since 2026/1/9
 * @version 1.1
 * @see "doc/design/spring/started.adoc"
 */
@CustomLog
@Component
public class CWebInit implements ICSpringInit, CStartedApplicationRunner {

    /**
     * Spring 启动初始化回调（当前无处理逻辑）
     */
    @Override
    public void onInit() {

    }

    /**
     * 应用启动完成回调：清空本模块的装配期缓存
     *
     * <p><b>详细设计</b>：清理项为「装配期一次性写入、启动后不再读取」的进程级静态数据——
     * {@link ConditionalOnMissingExceptionHandlerCondition} 的按 advice 类型声明集缓存。
     * 它不随任何 Bean 销毁，容器关闭也带不走，故须显式释放。</p>
     */
    @Override
    public void onStarted() {

        ConditionalOnMissingExceptionHandlerCondition.clearDeclarationsCache();

        log.debug("启动完成，已清除 web 模块装配期缓存");

    }

}
