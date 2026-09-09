package com.c332030.ctool4j.redis.rate;

import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.redis.util.CRedisKeyUtils;
import com.c332030.ctool4j.redis.util.CRedisUtils;
import com.c332030.ctool4j.spring.util.CAspectUtils;
import lombok.CustomLog;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * <p>
 * Description: CRateLimitAspect
 * </p>
 * <p>
 * 限流切面：拦截标注 {@code @CRateLimit} 的方法，基于 Redis 固定窗口计数器
 * （INCR + 首次 EXPIRE 原子脚本，见 {@link CRedisUtils#incrExpire}）限定调用频率。
 * </p>
 * <p>
 * 限流 key 格式：应用前缀 + 类简单名 + 方法名（可省略，见 {@code useMethodName()}）+ 业务 id
 * （业务 id 为空时省略）。业务 id 由 {@link CRedisKeyUtils#resolveBizId} 按注解 {@code id()}
 * 表达式求值；每次调用原子自增，自增值超过 {@code count()} 时抛 {@link CRateLimitException}，
 * 否则放行。
 * </p>
 *
 * @see "doc/design/redis/CRateLimitAspect.adoc"
 * @see "doc/design/redis/CRateLimitAspectTests.adoc"
 * @since 2026/9/8
 */
@CustomLog
@Aspect
@Component
public class CRateLimitAspect {

    /**
     * 限流切面：计数并判断是否超阈值，超阈值抛限流异常，否则放行。
     *
     * @param joinPoint 切入点
     * @return 方法执行结果
     */
    @Around("@annotation(com.c332030.ctool4j.redis.rate.CRateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) {

        val method = CAspectUtils.getMethod(joinPoint);
        val rateLimit = CReflectUtils.getAnnotationCached(method, CRateLimit.class);
        val count = rateLimit.count();
        val interval = rateLimit.interval();

        if (count <= 0) {
            throw new IllegalArgumentException(
                "@CRateLimit count 必须大于 0，方法: " + method);
        }
        if (interval <= 0) {
            throw new IllegalArgumentException(
                "@CRateLimit interval 必须大于 0，方法: " + method);
        }

        val specKey = CRedisKeyUtils.resolveBizId(joinPoint.getArgs(), method, rateLimit.id());
        val limitKey = buildLimitKey(method, rateLimit, specKey);

        val current = CRedisUtils.incrExpire(limitKey, 1, Duration.ofSeconds(interval));
        if (log.isDebugEnabled()) {
            log.debug("rate limit key: {}, current: {}, count: {}", limitKey, current, count);
        }

        if (current > count) {
            log.warn("rate limited key: {}, current: {}, count: {}", limitKey, current, count);
            throw new CRateLimitException(rateLimit.message());
        }

        return CAspectUtils.process(joinPoint);
    }

    /**
     * 构建限流 redis key。
     * <p>格式：{@code 应用前缀:类简单名:方法名:业务id（若存在）}。
     * 段简单名取方法声明类；{@code useMethodName()} 为 false 时省略方法名段；
     * 业务 id 为 null/空白时省略末段。委托 {@link CRedisKeyUtils#buildKey} 生成。</p>
     *
     * @param method    限流方法
     * @param rateLimit 限流注解
     * @param specKey   业务 id；为 null/空白表示不按业务维度
     * @return 限流 key
     */
    private String buildLimitKey(Method method, CRateLimit rateLimit, Object specKey) {

        return CRedisKeyUtils.buildKey(
            CRedisUtils.getApplicationPrefix(),
            method.getDeclaringClass().getSimpleName(),
            method.getName(),
            rateLimit.useMethodName(),
            specKey
        );
    }

}
