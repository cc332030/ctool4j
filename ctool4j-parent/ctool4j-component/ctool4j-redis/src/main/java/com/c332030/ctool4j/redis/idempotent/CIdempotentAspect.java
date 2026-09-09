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
 * @see "doc/design/redis/CIdempotentAspect.adoc"
 * @see "doc/design/redis/CIdempotentAspectTests.adoc"
 * @since 2026/9/9
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
