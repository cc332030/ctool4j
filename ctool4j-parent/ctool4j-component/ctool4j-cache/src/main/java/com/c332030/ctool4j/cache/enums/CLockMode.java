package com.c332030.ctool4j.cache.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: CLockMode
 * </p>
 *
 * <p>缓存构建器锁模式：指定并发控制范围</p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CLockMode} 为缓存构建器的锁模式枚举，用于指定并发控制范围：</p>
 * <ul>
 *   <li>{@code LOCAL}：当前实例内互斥——使用 JVM 内按 key 的本地锁，轻量、不依赖 Redis，仅保证单实例不并发。</li>
 *   <li>{@code DISTRIBUTED}：多实例互斥——使用 Redis 分布式锁，多实例间全局互斥（默认）。</li>
 * </ul>
 * <p>每个枚举常量携带 {@code text} 描述字段。</p>
 * <h2>设计要点</h2>
 * <p><b>两种锁模式</b></p>
 * <ul>
 *   <li>{@code LOCAL}：JVM 内本地锁，适用于单实例部署、无跨进程并发场景，无外部依赖、开销低。</li>
 *   <li>{@code DISTRIBUTED}：Redis 分布式锁，适用于多实例部署、需全局互斥的场景，依赖 Redis。</li>
 * </ul>
 * <p><b>描述字段</b></p>
 * <ul>
 *   <li>{@code text}：供日志、提示等场景使用的中文描述。</li>
 * </ul>
 *
 * @since 2026/8/24
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum CLockMode {

    /**
     * 当前实例内互斥：使用 JVM 内按 key 的本地锁，轻量、不依赖 Redis，仅保证单实例不并发
     */
    LOCAL("本地锁"),

    /**
     * 多实例互斥：使用 Redis 分布式锁，多实例间全局互斥（默认）
     */
    DISTRIBUTED("分布式锁"),

    ;

    /**
     * 描述
     */
    final String text;

}