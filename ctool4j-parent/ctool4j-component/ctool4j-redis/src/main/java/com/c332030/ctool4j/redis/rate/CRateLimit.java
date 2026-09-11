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
 * <h2>设计要点</h2>
 * <ul>
 *   <li>固定窗口计数器（INCR + 首次 EXPIRE 原子脚本，见 {@code CRedisUtils.incrExpire}），实现简单、原子。</li>
 *   <li>业务 id 用于区分不同调用主体（如用户/商户/IP），使限流维度更精确。</li>
 *   <li>参数校验：count/interval 必须为正，非法时切面抛 {@code IllegalArgumentException}。</li>
 * </ul>
 * <p><b>id() 表达式</b></p>
 * <ul>
 *   <li>格式 {@code 参数名.属性名.属性名…}，参考 {@code @CCacheable.key()}。</li>
 *   <li>由公共解析器 {@code CElKeyResolveUtils} 求值，从方法参数取业务维度值。</li>
 *   <li>为空或求值为 null 时，限流不带业务维度（按方法全局限流）。</li>
 * </ul>
 * <p><b>useMethodName() 方法名隔离</b></p>
 * <ul>
 *   <li>默认 {@code true}：key 含 {@code 类简单名:方法名}，各方法按自身隔离。</li>
 *   <li>设为 {@code false}：key 去掉方法名段（{@code 应用前缀:类简单名:业务id}），同类的多个方法共享同一限流桶。</li>
 *   <li>用途：同一业务的多个入口/共用一个限流维度时，避免各方法独立计数导致总频率失控。</li>
 * </ul>
 * <p><b>CRateLimitException</b></p>
 * <p>继承 {@code CException}（运行时异常）。限流触发时抛出，携带 {@code message()} 指定的消息。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>count &lt;= 0</td>
 *     <td>切面抛 IllegalArgumentException</td>
 *   </tr>
 *   <tr>
 *     <td>interval &lt;= 0</td>
 *     <td>切面抛 IllegalArgumentException</td>
 *   </tr>
 *   <tr>
 *     <td>id 表达式非法/循环引用</td>
 *     <td>切面抛 IllegalArgumentException/IllegalStateException（由 CElKeyResolveUtils 抛）</td>
 *   </tr>
 *   <tr>
 *     <td>id 求值为 null</td>
 *     <td>按方法全局限流</td>
 *   </tr>
 *   <tr>
 *     <td>窗口内超阈值</td>
 *     <td>抛 CRateLimitException</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>接口限流（按用户/IP/商户等维度）。</li>
 *   <li>高频操作的频率控制。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>固定窗口计数器在窗口临界点存在"窗口翻转瞬间突增"的边界问题（窗口初重置时刻允许冲过阈值一次）。</li>
 *   <li>若需更平滑的限流（令牌桶/滑动窗口）需另行实现；本实现聚焦简单可靠的固定窗口计数。</li>
 *   <li>计数依赖 Redis 可用性：Redis 异常时限流判定会抛出异常（非降级放行），调用方需自行处理 Redis 故障策略。</li>
 * </ul>
 *
 * @since 2026/9/8
 * @version 1.0
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
