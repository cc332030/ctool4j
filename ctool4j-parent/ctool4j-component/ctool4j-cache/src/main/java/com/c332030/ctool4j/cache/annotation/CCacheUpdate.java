package com.c332030.ctool4j.cache.annotation;

import com.c332030.ctool4j.cache.aop.CDefaultCacheIdConverter;
import com.c332030.ctool4j.cache.aop.ICCacheIdConverter;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CCacheUpdate
 * </p>
 *
 * <p>
 * 标注在 update 方法上，方法执行成功后把返回值写入（更新）对应缓存。
 * 与 {@link CCacheable} 配合使用：相同的 {@code namespace} + 相同的 key 解析规则定位到
 * 被缓存的数据，方法成功执行后用其返回值覆盖缓存，及时更新、不必等缓存过期。
 * </p>
 * <p>
 * 与 {@link CCacheRemove} 的区别：本注解会写入新值（方法返回值），通常在 update 场景使用。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCacheUpdate}（方法/类型级注解）由 {@code CCacheAspect} 拦截，在<b>标注方法执行成功</b>后用返回值写入（更新） 对应缓存，及时更新缓存、不必等缓存过期，也避免失效期内的脏读。</p>
 * <ul>
 *   <li>与 {@code @CCacheable} 配合：相同的 {@code namespace} + 相同的 key 解析规则定位到被缓存的数据。</li>
 *   <li>方法执行<b>成功</b>（未抛异常）后才写入缓存；异常时向上抛出且不写入。</li>
 *   <li>方法返回 {@code null} 时不写入（避免缓存空值）。</li>
 *   <li>支持本地（Caffeine）与 Redis 两种模式（{@code local} 属性），与 {@code @CCacheable} 对应。</li>
 * </ul>
 * <p>五个属性：</p>
 * <ul>
 *   <li>{@code local}：是否本地缓存（默认 {@code true} 本地 Caffeine；{@code false} 走 Redis）</li>
 *   <li>{@code idConverter}：缓存 ID 生成类（默认 {@code CDefaultCacheIdConverter}）</li>
 *   <li>{@code namespace}：缓存命名空间类（必填，须与对应的 {@code @CCacheable} 一致）</li>
 *   <li>{@code key}：缓存 key 简单 el 表达式（默认 {@code ""}，与 {@code @CCacheable.key()} 语义一致）</li>
 *   <li>{@code expire}：过期时间（秒），{@code 0} 永久（须与对应的 {@code @CCacheable.expire()} 一致）</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>方法返回 null</td>
 *     <td>不写入缓存</td>
 *   </tr>
 *   <tr>
 *     <td>无缓存 key（参数为 null / 表达式链某级为 null）</td>
 *     <td>跳过写入，不抛错</td>
 *   </tr>
 *   <tr>
 *     <td>方法执行抛异常</td>
 *     <td>向上传播，不写入缓存</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要更新某业务缓存的方法（如 update），与 {@code @CCacheable} 同 namespace + 同 key 规则。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>删除 / 释放缓存的场景请用 {@code CCacheRemove}（本注解只写新值）。</li>
 *   <li>本地缓存为 JVM 内单机缓存，多实例部署需使用 Redis 模式。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>与 {@code @CCacheable} 的 namespace、key 规则、idConverter、expire 必须保持一致，否则定位不到目标缓存</li>
 *   <li>或写入到不同的 expire 分组。</li>
 *   <li>{@code @CCacheUpdate} 要求方法有返回值（用返回值作为新缓存值）；方法返回 null 不写入。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义约定</b></p>
 * <ul>
 *   <li>与 {@code @CCacheable} 共用同一套 key 解析逻辑（el 表达式或默认 {@code @CCacheId}）。</li>
 *   <li>本地缓存写入 namespace 下 {@code expire} 指定的分组 Cache（{@code getCache(namespace, expire)}）；</li>
 *   <li>{@code expire} 须与对应 {@code @CCacheable} 一致，才能命中同一分组。</li>
 *   <li>Redis 缓存写入 {@code namespace:cacheKey}，用 {@code CCacheService.setValue}（带过期）。</li>
 *   <li>方法执行异常时向上传播，<b>不写入缓存</b>，避免把失败结果写入缓存。</li>
 * </ul>
 * <p><b>实现方式</b></p>
 * <ul>
 *   <li>纯注解声明；切面通过 {@code @Around("@annotation(...CCacheUpdate)")} 拦截。</li>
 *   <li>先执行原方法（{@code CAspectUtils.process}），成功后调用 {@code updateCache} 用返回值写入缓存。</li>
 * </ul>
 *
 * @since 2026/9/11
 * @version 1.0
 */
@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CCacheUpdate {

    /**
     * 自定义缓存 key 的简单 el 表达式，与 {@link CCacheable#key()} 语义一致。
     * <p>为空时走默认逻辑（第一个参数的 {@code @CCacheId} 字段）。</p>
     *
     * @return 缓存 key 简单 el 表达式
     */
    String key() default "";

    /**
     * 是否本地缓存
     * @return true 本地缓存，false redis 缓存
     */
    boolean local() default true;

    /**
     * 缓存 id 生成类
     * @return 缓存 id 生成类
     */
    Class<? extends ICCacheIdConverter<?, ?>> idConverter() default CDefaultCacheIdConverter.class;

    /**
     * 缓存命名空间类（须与对应的 {@link CCacheable#namespace()} 一致）
     * @return 缓存命名空间类
     */
    Class<?> namespace();

    /**
     * 缓存过期时间，单位秒，为 0 则为永久（须与对应的 {@link CCacheable#expire()} 一致）
     * @return 缓存过期时间
     */
    int expire() default 0;

}
