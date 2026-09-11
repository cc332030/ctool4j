package com.c332030.ctool4j.redis.idempotent;

import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.redis.service.impl.CLockService;
import com.c332030.ctool4j.redis.util.CRedisKeyUtils;
import com.c332030.ctool4j.redis.util.CRedisUtils;
import com.c332030.ctool4j.spring.util.CAspectUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * <p>
 * Description: CIdempotentAspect
 * </p>
 * <p>
 * 幂等切面：拦截标注 {@code @CIdempotent} 的方法，基于 {@link CLockService} 分布式锁实现
 * 同一业务 key 的互斥执行。加锁成功则在锁内执行业务，释放锁；加锁失败（同一 key 已有调用
 * 正在进行，即重复提交或并发穿透）抛 {@link CIdempotentException}。
 * </p>
 * <p>
 * 幂等 key 格式：应用前缀 + 分组类简单名 + 方法名（可省略，见 {@code useMethodName()}）+ 业务 id
 * （业务 id 为空时省略）。业务 id 由 {@link CRedisKeyUtils#resolveBizId} 按注解 {@code id()}
 * 表达式求值。
 * </p>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>id 表达式非法</td>
 *     <td>抛 IllegalArgumentException（CElKeyResolveUtils，经 CRedisKeyUtils）</td>
 *   </tr>
 *   <tr>
 *     <td>id 求值为 null</td>
 *     <td>按分组类+方法名全局隔离</td>
 *   </tr>
 *   <tr>
 *     <td>加锁失败（重复/并发）</td>
 *     <td>抛 CIdempotentException</td>
 *   </tr>
 *   <tr>
 *     <td>锁内业务异常</td>
 *     <td>传播业务异常（CLockService finally 释放锁）</td>
 *   </tr>
 *   <tr>
 *     <td>Redis 异常</td>
 *     <td>向上抛出（不静默降级放行，由调用方决定故障策略）</td>
 *   </tr>
 * </table>
 * <h2>设计要点</h2>
 * <p><b>幂等 key</b></p>
 * <ul>
 *   <li>格式：{@code 应用前缀:分组类简单名:方法名:业务id}（业务 id 为空时省略最后一段）。</li>
 *   <li>应用前缀取自 {@code CRedisUtils.getApplicationPrefix()}（优先 group，其次 name）。</li>
 *   <li>分组类简单名取自注解 {@code group()}（而非方法声明类），支持多方法共用同一分组。</li>
 *   <li>注解 {@code useMethodName()} 为 false 时省略方法名段，同组多个方法共享同一执行权。</li>
 *   <li>业务 id 由公共工具 {@code CRedisKeyUtils.resolveBizId}（内部走 {@code CElKeyResolveUtils}）按 {@code id()} 表达式取值，区分调用维度；key 构建复用 {@code CRedisKeyUtils.buildKey}。</li>
 * </ul>
 * <p><b>执行权获取（复用 CLockService）</b></p>
 * <ul>
 *   <li>复用 {@code CLockService.lock(key).onLockFail(抛异常).execute(业务)} 模板。</li>
 *   <li>加锁成功 → 锁内执行业务（{@code CAspectUtils.process}）→ 释放锁。</li>
 *   <li>加锁失败（同一 key 已有调用）→ 执行 {@code onLockFail} 回调抛 {@code CIdempotentException}。</li>
 *   <li>默认 {@code waitTime=ZERO} 不等待，并发立即可感知拒绝。</li>
 * </ul>
 * <p><b>业务 id 解析</b></p>
 * <ul>
 *   <li>复用 {@code CRedisKeyUtils.resolveBizId}（内部走 {@code CElKeyResolveUtils}）解析注解 {@code id()} 表达式，从方法参数取业务维度值。</li>
 *   <li>表达式为空或求值为 null/空白时，幂等不带业务维度（按分组类+方法名全局隔离）。</li>
 * </ul>
 *
 * @since 2026/9/9
 * @version 1.0
 */
@CustomLog
@Aspect
@Component
@AllArgsConstructor
public class CIdempotentAspect {

    /**
     * 分布式锁服务
     */
    private final CLockService lockService;

    /**
     * 幂等切面：获取同一业务 key 的执行权，成功则锁内执行业务，失败抛幂等异常。
     *
     * @param joinPoint 切入点
     * @return 方法执行结果
     */
    @Around("@annotation(com.c332030.ctool4j.redis.idempotent.CIdempotent)")
    public Object idempotent(ProceedingJoinPoint joinPoint) {

        val method = CAspectUtils.getMethod(joinPoint);
        val idempotent = CReflectUtils.getAnnotationCached(method, CIdempotent.class);

        val specKey = CRedisKeyUtils.resolveBizId(joinPoint.getArgs(), method, idempotent.id());
        val idempotentKey = buildIdempotentKey(method, idempotent, specKey);

        return lockService.lock(idempotentKey)
            .onLockFail(lock -> {
                log.warn("idempotent conflict key: {}", idempotentKey);
                throw new CIdempotentException(idempotent.message());
            })
            .execute(() -> {
                if (log.isDebugEnabled()) {
                    log.debug("idempotent locked key: {}", idempotentKey);
                }
                return CAspectUtils.process(joinPoint);
            });
    }

    /**
     * 构建幂等 redis key。
     * <p>格式：{@code 应用前缀:分组类简单名:方法名:业务id（若存在）}。
     * 段简单名取注解 {@code group()} 的简单名（而非方法声明类，以支持多方法共用同一分组）；
     * {@code useMethodName()} 为 false 时省略方法名段；业务 id 为 null/空白时省略末段。
     * 委托 {@link CRedisKeyUtils#buildKey} 生成。</p>
     *
     * @param method     幂等方法
     * @param idempotent 幂等注解
     * @param specKey    业务 id；为 null/空白表示不按业务维度
     * @return 幂等 key
     */
    private String buildIdempotentKey(Method method, CIdempotent idempotent, Object specKey) {

        return CRedisKeyUtils.buildKey(
            CRedisUtils.getApplicationPrefix(),
            idempotent.group().getSimpleName(),
            method.getName(),
            idempotent.useMethodName(),
            specKey
        );
    }

}
