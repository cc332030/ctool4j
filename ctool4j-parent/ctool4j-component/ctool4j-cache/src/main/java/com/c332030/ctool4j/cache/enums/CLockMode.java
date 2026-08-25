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
 * @see "doc/design/cache/CLockMode.adoc"
 * @see "doc/design/cache/CCacheService.adoc"
 * @since 2026/8/24
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