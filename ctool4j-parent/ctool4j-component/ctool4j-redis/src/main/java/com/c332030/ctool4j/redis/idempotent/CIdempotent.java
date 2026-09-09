package com.c332030.ctool4j.redis.idempotent;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Description: CIdempotent
 * </p>
 * <p>
 * 基于 Redis 分布式锁的幂等/防并发注解，作用于方法。同一业务 key 在任一时刻只能有一个
 * 调用执行业务，其余并发或重复调用抛 {@link CIdempotentException}，防止重复提交与并发穿透。
 * </p>
 * <p>
 * 幂等 key 由应用前缀 + 分组类简单名 + 方法名 + 业务 id 拼接；分组类由 {@link #group()}
 * 指定（其简单名作为 key 的类段），业务 id 通过 {@link #id()} 简单 el 表达式
 * （{@code 参数名.属性名.属性名…}，参考 {@code @CCacheable.key()}）从方法参数取值，
 * 由公共解析器 {@code CRedisKeyUtils}（内部走 {@code CElKeyResolveUtils}）求值。
 * </p>
 * <p>
 * 默认 key 含方法名段（{@link #useMethodName()} 默认 true），各方法按自身隔离；
 * 多个方法需共用同一幂等维度时，将 {@link #useMethodName()} 设为 false 去掉方法名段即可。
 * </p>
 *
 * @see "doc/design/redis/CIdempotent.adoc"
 * @see "doc/design/redis/CIdempotentAspect.adoc"
 * @see "doc/design/redis/CIdempotentAspectTests.adoc"
 * @since 2026/9/9
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CIdempotent {

    /**
     * 分组类实例，其简单名作为幂等 key 的类段
     * <p>多个方法需共用同一幂等维度时，指定相同的分组类，并将 {@link #useMethodName()}
     * 设为 false，即可让这些方法共享同一执行权（例如同一业务实体的多个操作入口）。</p>
     *
     * @return 分组类
     */
    Class<?> group();

    /**
     * 业务 id 简单 el 表达式，格式：{@code 参数名.属性名.属性名…}
     * <p>如 {@code "userId"}、{@code "req.userId"}，支持一级到任意深度。</p>
     * <p>语义：表达式只从方法参数取值，作为幂等 key 的业务维度输入。为空或求值为 null
     * 时，幂等不带业务维度（按分组类+方法名全局隔离）。</p>
     * <p>校验：表达式在首次使用时解析并校验（参数名存在、属性链可达），解析结果按方法缓存复用；
     * 属性链出现循环引用（同一对象实例被重复访问）时运行期报错。</p>
     * <p>约束：{@code 参数名} 依赖编译期保留形参名，消费方工程需开启
     * {@code -parameters}（或编译保留形参名），否则无法按参数名取值，会在首次使用时报
     * 「参数名不存在」。</p>
     *
     * @return 业务 id el 表达式，空字符串表示不按业务维度隔离
     */
    String id() default "";

    /**
     * 是否将方法名纳入幂等 key
     * <p>默认 {@code true}（key 含 {@code 分组类简单名:方法名}），各方法按自身隔离。</p>
     * <p>多个方法需共用一个幂等维度时（如同一业务的不同入口），设为 {@code false}
     * 去掉方法名段，key 变为 {@code 应用前缀:分组类简单名:业务id}，
     * 同组多个方法共享该执行权。</p>
     *
     * @return 是否使用方法名；默认 true
     */
    boolean useMethodName() default true;

    /**
     * 幂等冲突时抛出的异常消息
     *
     * @return 异常消息
     */
    String message() default "重复请求，请勿重复提交";

}
