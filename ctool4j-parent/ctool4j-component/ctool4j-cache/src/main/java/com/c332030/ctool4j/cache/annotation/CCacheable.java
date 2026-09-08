package com.c332030.ctool4j.cache.annotation;

import com.c332030.ctool4j.cache.aop.CDefaultCacheIdConverter;
import com.c332030.ctool4j.cache.aop.ICCacheIdConverter;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CCacheable
 * </p>
 *
 * @see "doc/design/cache/CCacheable.adoc"
 * @see "doc/design/cache/CCacheAspectTests.adoc"
 * @since 2025/9/27
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
