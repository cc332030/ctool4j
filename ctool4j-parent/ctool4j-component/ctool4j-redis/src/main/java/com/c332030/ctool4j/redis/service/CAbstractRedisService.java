package com.c332030.ctool4j.redis.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * <p>
 * Description: CAbstractRedisService Redis Service 抽象基类
 * </p>
 *
 * <p>{@code CAbstractRedisService<K, V>} 为 Redis Service 抽象基类，实现 {@code ICRedisService<K, V>}，
 * 注入 {@code RedisTemplate<K, V>}，子类直接复用基类的默认键值操作。</p>
 *
 * <h2>设计要点</h2>
 * <h3>语义约定</h3>
 * <ul>
 *   <li>通过 {@code @Autowired} 字段注入 {@code RedisTemplate<K, V>}。</li>
 *   <li>子类继承全部 {@code ICRedisService} 默认方法，仅需提供模板。</li>
 * </ul>
 * <h3>实现方式</h3>
 * <ul>
 *   <li>lombok {@code @Getter}/{@code @Setter} 暴露 {@code redisTemplate}，实现 {@code getRedisTemplate()}。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>{@code redisTemplate} 未注入：使用时的默认方法将 NPE（依赖 Spring 注入）。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <h3>适用场景</h3>
 * <ul>
 *   <li>需要基于 {@code RedisTemplate} 的键值操作 Service 基类（如 {@code CStringStringRedisService}）。</li>
 * </ul>
 * <h3>不适用/边界场景</h3>
 * <ul>
 *   <li>不提供具体序列化策略，由子类/模板决定。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>注入依赖 Spring 容器；非 Spring 环境无法直接使用。</li>
 * </ul>
 *
 * <p>测试内容见 {@code CStringStringRedisServiceTests}。</p>
 *
 * @since 2024/3/8
 */
public abstract class CAbstractRedisService<K, V> implements ICRedisService<K, V> {

    /**
     * RedisTemplate
     */
    @Getter
    @Setter
    @Autowired
    RedisTemplate<K, V> redisTemplate;

}
