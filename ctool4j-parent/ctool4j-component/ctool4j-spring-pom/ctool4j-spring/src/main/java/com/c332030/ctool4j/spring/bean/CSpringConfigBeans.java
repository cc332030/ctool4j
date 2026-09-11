package com.c332030.ctool4j.spring.bean;

import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import com.c332030.ctool4j.spring.config.CSpringApplicationConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>
 * Description: CSpringConfigBeans
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CSpringConfigBeans}：框架内部静态访问点，持有配置 Bean 与应用上下文。</p>
 * <ul>
 *   <li>上下文：{@code applicationContext}（{@code CSpringConfiguration} 启动时写入）。</li>
 *   <li>配置：{@code springApplicationConfig}（{@code CAutowiredScan} 注入）。</li>
 *   <li>附加扫描包：{@code basePackages}（测试上下文按需补入使用方声明的包）。</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>用自有静态字段持有 {@code ApplicationContext}，替代 Hutool {@code SpringUtil} 的全局静态上下文：
 *   {@code SpringUtil} 的上下文只有在 {@code CSpringConfiguration} 被装配时才写入，
 *   工具类在上下文就绪前调用会拿到 {@code null}；显式持有 + 启动时写入可让「谁在何时写入」可控可查。</li>
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
 *     <td>容器尚未启动即读取上下文</td>
 *     <td>返回 {@code null}，由调用方在使用点抛错（不静默造一个假上下文）</td>
 *   </tr>
 *   <tr>
 *     <td>配置字段未注入</td>
 *     <td>字段为 {@code null}，取值处抛 NPE（快速失败）</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>框架内部静态工具类读取上下文与配置。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>非 Spring 环境不可用；需由 {@code CSpringConfiguration} 完成装配。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>同一个 JVM 内存在多个上下文时只保留最后一次写入的那个（框架按单应用使用）。</li>
 *   <li>附加扫描包为进程级集合，测试并行执行时相互可见（仅影响扫描范围，不影响正确性）。</li>
 * </ul>
 *
 * @since 2025/11/10
 * @version 1.0
 */
@UtilityClass
@CAutowiredScan
public class CSpringConfigBeans {

    /**
     * 附加扫描基础包（非启动类所在包，如测试上下文按需补入）
     */
    private final Set<String> basePackages = Collections.synchronizedSet(new LinkedHashSet<>());

    /**
     * 补入附加扫描基础包
     *
     * @param basePackage 基础包名（空串/空白忽略）
     */
    public void addBasePackage(String basePackage) {
        if (null == basePackage || basePackage.trim().isEmpty()) {
            return;
        }
        basePackages.add(basePackage.trim());
    }

    /**
     * 获取附加扫描基础包
     *
     * @return 基础包集合（只读副本）
     */
    public Set<String> getBasePackages() {
        synchronized (basePackages) {
            return new LinkedHashSet<>(basePackages);
        }
    }

    /**
     * 应用上下文（容器启动时写入，供静态工具类读取）
     */
    @Getter
    @Setter
    ApplicationContext applicationContext;

    /**
     * 应用配置（{@code CAutowiredScan} 注入）
     */
    @Getter
    @Setter
    @CAutowired
    CSpringApplicationConfig springApplicationConfig;

}
