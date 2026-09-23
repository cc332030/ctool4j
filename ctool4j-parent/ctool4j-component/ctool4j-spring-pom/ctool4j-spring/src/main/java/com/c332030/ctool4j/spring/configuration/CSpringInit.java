package com.c332030.ctool4j.spring.configuration;

import com.c332030.ctool4j.spring.bean.CSpringConfigBeans;
import com.c332030.ctool4j.spring.boot.CStartedApplicationRunner;
import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import com.c332030.ctool4j.spring.util.CAutowiredUtils;
import lombok.CustomLog;
import lombok.val;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: CSpringInit
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CSpringInit}：Spring 初始化与启动完成回调。</p>
 * <ul>
 *   <li>{@code onInit()}：{@link ICSpringInit} 的初始化回调（当前无处理逻辑）。</li>
 *   <li>{@code onStarted()}：{@link CStartedApplicationRunner} 的启动完成回调，清除仅在启动阶段有效的缓存。</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>启动阶段的一次性数据挪到 {@code onStarted()} 释放：这些数据在启动完成后不会再被读取，
 *   留着只占内存，而它们的生命周期与本 Bean 无关（本 Bean 是单例、随容器存活）。</li>
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
 *     <td>被清理项本就为空（未在启动期写入）</td>
 *     <td>清理为空操作、不报错，不影响启动</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>随 {@code CSpringConfiguration} 装配的 Spring Boot 应用。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>须在 {@code SpringApplication.run} 流程内承载（{@code CStartedApplicationRunner#run} 在启动完成后回调 {@code onStarted()}）。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>清理只在启动完成后执行一次：启动期间若已有读取方依赖被清理项，须改为由容器 Bean 持有，
 *   不能寄望启动后的缓存仍可用。</li>
 * </ul>
 *
 * @since 2025/11/10
 * @version 1.0
 */
@CustomLog
@Component
public class CSpringInit implements ICSpringInit, CStartedApplicationRunner {

    /**
     * Spring 启动初始化回调（当前无处理逻辑）
     */
    @Override
    public void onInit() {

    }

    /**
     * 应用启动完成回调：清除仅在启动阶段有效的缓存
     *
     * <p><b>详细设计</b>：清理项为启动期一次性写入、启动后不再读取的静态数据——</p>
     * <ul>
     *   <li>{@link CAutowiredUtils} 的扫描包集合：{@code autowiredScan} 只在容器就绪时执行一次，
     *   集合按包扫描结果去重后增长，启动完成后不再被读取；</li>
     *   <li>{@link CSpringConfigBeans} 附加扫描包：仅供测试上下文按需补入，与上一项同源、一并清除。</li>
     * </ul>
     * <p><b>边界</b>：本类只清 {@code ctool4j-spring} 自己的启动期数据；其他模块的装配期缓存
     * （如 {@code ctool4j-web} 条件类的声明集）由<b>该模块自己的 Bean</b> 在启动完成时清理——
     * 依赖方向是单向的（web → spring），反向清理会造出环形依赖。</p>
     */
    @Override
    public void onStarted() {

        val basePackages = CSpringConfigBeans.getBasePackages();
        CAutowiredUtils.clearScannedBasePackages(basePackages);

        log.debug("启动完成，已清除启动阶段缓存，basePackages: {}", basePackages.size());

    }

}
