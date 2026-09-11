package com.c332030.ctool4j.cache.annotation;

import com.c332030.ctool4j.cache.aop.CDefaultCacheIdConverter;
import com.c332030.ctool4j.cache.aop.ICCacheIdConverter;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CCacheable
 * </p>
 *
 * <p>方法/类型级缓存注解：由 {@code CCacheAspect} 在方法执行前读取缓存、未命中时执行原方法并写缓存
 * （本地 Caffeine 或 Redis，由 {@link #local()} 决定）。</p>
 *
 * <h2>设计思路总述</h2>
 * <ul>
 *   <li>本注解是纯声明：切面通过 {@code @Around("@annotation(...CCacheable)")} 拦截，
 *   {@code resolveCacheKey} 依据 {@code key()} 是否非空分派到「简单 el 表达式」或「{@code @CCacheId} 默认逻辑」。</li>
 *   <li>el 表达式由 {@code CElKeyResolveUtils} 在首次使用时解析校验（懒校验）、结果按方法缓存复用，
 *   运行期经 MethodHandle getter 链求值，不牺牲启动性能。</li>
 *   <li>{@code namespace} 必填（无默认值）且用于隔离不同业务的缓存 key；本地与 Redis 两模式的 key 语义不同，
 *   不可混用（Redis key 为 {@code namespace:cacheKey}）。</li>
 * </ul>
 * <p>各属性的详细设计与边界见对应属性 javadoc。</p>
 * <h2>设计要点</h2>
 * <p><b>属性目录</b></p>
 * <ul>
 *   <li>{@link #local()}：是否本地缓存（默认 {@code true} 本地 Caffeine；{@code false} 走 Redis）。</li>
 *   <li>{@link #idConverter()}：缓存 ID 生成类（默认 {@code CDefaultCacheIdConverter}）。</li>
 *   <li>{@link #namespace()}：缓存命名空间类（必填，用于区分缓存分组，如 key 前缀）。</li>
 *   <li>{@link #expire()}：过期时间（秒），{@code 0} 表示永久（默认 {@code 0}）。</li>
 *   <li>{@link #key()}：缓存 key 简单 el 表达式（默认 {@code ""}，为空走默认 {@code @CCacheId} 逻辑）。</li>
 * </ul>
 *
 *
 * @since 2025/9/27
 * @version 1.0
 */
@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CCacheable {

    /**
     * 自定义缓存 key 的简单 el 表达式，格式：{@code 参数名.属性名.属性名…}
     * <p>如 {@code "user.id"}、{@code "req.userId"}，支持一级到任意深度，属性可以是嵌套对象。</p>
     * <p>语义：表达式取值仅作为缓存 key 输入（不经 {@code idConverter} 传入原始参数对象）；
     * 一级表达式 {@code key="参数名"} 会把该参数对象整体按 {@code toString} 作 key（不触发
     * {@code @CCacheId} 规则）。为 key 的唯一性，建议取到业务属性而非整个 POJO。</p>
     * <p>为空时走默认逻辑（第一个参数的 {@code @CCacheId} 字段）；表达式为空且参数对象无
     * {@code @CCacheId} 字段时运行期报错，需显式配置 {@code key()} 或 {@code @CCacheId}。</p>
     * <p>校验：表达式在首次使用时解析并校验（参数名存在、属性链可达），解析结果按方法缓存复用，
     * 仅首次调用有一次解析开销；属性链出现循环引用（同一对象实例被重复访问）时运行期报错。</p>
     * <p>约束：{@code 参数名} 依赖编译期保留形参名，消费方工程需开启
     * {@code -parameters}（或编译保留形参名），否则无法按参数名取值，会在首次使用时报
     * 「参数名不存在」。</p>
     *
     * @return 缓存 key 简单 el 表达式，空字符串表示不使用 el（走默认逻辑）
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
     * 缓存命名空间类
     * @return 缓存命名空间类
     */
    Class<?> namespace();

    /**
     * 缓存过期时间，单位秒，为 0 则为永久
     * @return 缓存过期时间
     */
    int expire() default 0;

}
