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
 * <h2>设计要点</h2>
 * <ul>
 *   <li>复用 {@code CLockService}（Redisson 分布式锁）实现互斥：加锁成功 → 锁内执行业务 → 释放锁；</li>
 *   <li>加锁失败（同一 key 已有调用）→ 抛幂等异常。</li>
 *   <li>业务 id 用于区分不同调用主体（如用户/商户），使幂等维度更精确。</li>
 *   <li>默认不等待锁，并发立即可感知拒绝（防重复提交场景）。</li>
 * </ul>
 * <p><b>id() 表达式</b></p>
 * <ul>
 *   <li>格式 {@code 参数名.属性名.属性名…}，参考 {@code @CCacheable.key()}。</li>
 *   <li>由公共解析器 {@code CElKeyResolveUtils} 求值，从方法参数取业务维度值。</li>
 *   <li>为空或求值为 null 时，幂等不带业务维度（按分组类+方法名全局隔离）。</li>
 * </ul>
 * <p><b>group() 分组类</b></p>
 * <ul>
 *   <li>必填。取 {@code group()} 的简单名作为幂等 key 的类段，而非方法声明类的简单名。</li>
 *   <li>这样可让多个方法（甚至跨类）通过指定相同分组类共享同一幂等维度。</li>
 *   <li>配合 {@code useMethodName()} 关闭方法名段，即可让多个方法共享同一执行权。</li>
 * </ul>
 * <p><b>useMethodName() 方法名隔离</b></p>
 * <ul>
 *   <li>默认 {@code true}：key 含 {@code 分组类简单名:方法名}，各方法按自身隔离。</li>
 *   <li>设为 {@code false}：key 去掉方法名段（{@code 应用前缀:分组类简单名:业务id}），同组多个方法共享同一执行权。</li>
 *   <li>用途：同一业务的多个入口/共用一个幂等维度时，避免各方法独立、无法拦截跨方法的重复提交。</li>
 * </ul>
 * <p><b>CIdempotentException</b></p>
 * <p>继承 {@code CException}（运行时异常）。幂等冲突（获取执行权失败）时抛出，携带 {@code message()} 指定的消息。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>id 表达式非法/循环引用</td>
 *     <td>抛 IllegalArgumentException/IllegalStateException（由 CElKeyResolveUtils 抛）</td>
 *   </tr>
 *   <tr>
 *     <td>id 求值为 null</td>
 *     <td>按分组类+方法名全局隔离</td>
 *   </tr>
 *   <tr>
 *     <td>加锁失败（重复/并发）</td>
 *     <td>抛 CIdempotentException</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>接口防重复提交（用户短时间重复点击、重复提交表单）。</li>
 *   <li>防并发穿透（同一业务 key 同一时刻仅允许一个执行）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>幂等只在持锁期间（方法执行期间）生效：方法完成后锁释放，后续新请求可再次获取执行权。</li>
 *   <li>若需「完成后不可重放」需另做持久标记，超出本注解职责。</li>
 *   <li>默认不等待锁，加锁失败立即抛异常；若需排队等待可在 {@code CLockService} 配置 {@code waitTime}，</li>
 *   <li>但本切面当前固定不等待。</li>
 *   <li>计数/锁完全依赖 Redis 可用性：Redis 异常时幂等判定会抛出异常（非静默放行），调用方需自行处理。</li>
 *   <li>同名重载方法若不指定 {@code useMethodName=false} 会共享同一执行权；需区分时依赖业务 id 体现差异。</li>
 * </ul>
 *
 * @since 2026/9/9
 * @version 1.0
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
