package com.c332030.ctool4j.redis.rate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Description: CRateLimit
 * </p>
 * <p>
 * 基于 Redis 的固定窗口限流注解，作用于方法。在 {@code interval} 秒窗口内，允许
 * {@code count} 次调用；超过阈值抛 {@link CRateLimitException}。
 * </p>
 * <p>
 * 限流 key 由应用前缀 + 类简单名 + 方法名 + 业务 id 拼接；业务 id 通过 {@link #id()}
 * 简单 el 表达式（{@code 参数名.属性名.属性名…}，参考 {@code @CCacheable.key()}）从方法参数取值，
 * 由公共解析器 {@code CRedisKeyUtils}（内部走 {@code CElKeyResolveUtils}）求值。{@code id()} 为空或求值为 null 时，
 * 限流不带业务维度（按方法全局限流）。
 * </p>
 * <p>
 * 默认 key 含方法名段（{@link #useMethodName()} 默认 true），各方法按自身隔离；
 * 多个方法需共用同一限流桶时，将 {@link #useMethodName()} 设为 false 去掉方法名段即可。
 * </p>
 *
 * @see "doc/design/redis/CRateLimit.adoc"
 * @see "doc/design/redis/CRateLimitAspect.adoc"
 * @see "doc/design/redis/CRateLimitAspectTests.adoc"
 * @since 2026/9/8
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CRateLimit {

    /**
     * 业务 id 简单 el 表达式，格式：{@code 参数名.属性名.属性名…}
     * <p>如 {@code "userId"}、{@code "req.userId"}，支持一级到任意深度。</p>
     * <p>语义：表达式只从方法参数取值，作为限流 key 的业务维度输入。为空或求值为 null
     * 时，限流不带业务维度（按方法全局限流）。</p>
     * <p>校验：表达式在首次使用时解析并校验（参数名存在、属性链可达），解析结果按方法缓存复用；
     * 属性链出现循环引用（同一对象实例被重复访问）时运行期报错。</p>
     * <p>约束：{@code 参数名} 依赖编译期保留形参名，消费方工程需开启
     * {@code -parameters}（或编译保留形参名），否则无法按参数名取值，会在首次使用时报
     * 「参数名不存在」。</p>
     *
     * @return 业务 id el 表达式，空字符串表示不按业务维度限流
     */
    String id() default "";

    /**
     * 时间窗口内允许的最大调用次数
     *
     * @return 允许次数，必须大于 0
     */
    int count();

    /**
     * 时间窗口大小（秒）
     *
     * @return 窗口大小（秒），必须大于 0
     */
    long interval();

    /**
     * 是否将方法名纳入限流 key
     * <p>默认 {@code true}（key 含 {@code 类简单名:方法名}），各方法按自身隔离。</p>
     * <p>多个方法需共用同一限流桶时（如同一业务的多个入口、共用一个限流维度），
     * 设为 {@code false} 去掉方法名段，key 变为 {@code 应用前缀:类简单名:业务id}，
     * 同类的多个方法共享该限流桶。</p>
     *
     * @return 是否使用方法名；默认 true
     */
    boolean useMethodName() default true;

    /**
     * 限流时抛出的异常消息
     *
     * @return 异常消息
     */
    String message() default "请求过于频繁，请稍后再试";

}
