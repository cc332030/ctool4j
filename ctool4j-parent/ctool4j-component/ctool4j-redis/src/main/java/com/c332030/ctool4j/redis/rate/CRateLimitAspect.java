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
 * <h2>能力目录</h2>
 * <p>{@code CRateLimitAspect}（{@code @Aspect} + {@code @Component}）拦截标注 {@code @CRateLimit} 的方法， 在 Redis 中按 key 原子计数，根据计数与阈值决定放行或抛限流异常。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>count &lt;= 0</td>
 *     <td>抛 IllegalArgumentException</td>
 *   </tr>
 *   <tr>
 *     <td>interval &lt;= 0</td>
 *     <td>抛 IllegalArgumentException</td>
 *   </tr>
 *   <tr>
 *     <td>id 表达式非法</td>
 *     <td>抛 IllegalArgumentException（CElKeyResolveUtils，经 CRedisKeyUtils）</td>
 *   </tr>
 *   <tr>
 *     <td>超阈值</td>
 *     <td>抛 CRateLimitException</td>
 *   </tr>
 *   <tr>
 *     <td>Redis 异常</td>
 *     <td>向上抛出（不静默降级放行，由调用方决定故障策略）</td>
 *   </tr>
 *   <tr>
 *     <td>自增返回 null（脚本异常）</td>
 *     <td>快速失败抛 IllegalStateException（不放行）</td>
 *   </tr>
 * </table>
 * <h2>设计要点</h2>
 * <p><b>限流 key</b></p>
 * <ul>
 *   <li>格式：{@code 应用前缀:类简单名:方法名:业务id}（业务 id 为空时省略最后一段）。</li>
 *   <li>应用前缀取自 {@code CRedisUtils.getApplicationPrefix()}（优先 group，其次 name）。</li>
 *   <li>类简单名/方法名标识被限流的方法，避免不同方法相互影响；注解 {@code useMethodName()} 为 false 时省略方法名段，</li>
 *   <li>同类的多个方法共用同一限流桶。</li>
 *   <li>业务 id 由公共工具 {@code CRedisKeyUtils.resolveBizId}（内部走 {@code CElKeyResolveUtils}）按 {@code id()} 表达式取值，区分调用维度；key 构建复用 {@code CRedisKeyUtils.buildKey}。</li>
 * </ul>
 * <p><b>计数（固定窗口）</b></p>
 * <ul>
 *   <li>复用 {@code CRedisUtils.incrExpire(key, 1, Duration.ofSeconds(interval))}：</li>
 *   <li>Lua 脚本原子 {@code INCRBY 1}，首次自增时设置 {@code EXPIRE interval}。</li>
 *   <li>返回自增值 {@code current}；{@code current &gt; count} 时判定超阈值。</li>
 *   <li>边界：{@code current == count} 允许（第 count 次调用放行），第 count+1 次才拦截。</li>
 *   <li>{@code current} 为 null（Redis 自增脚本执行异常）时快速失败抛 {@code IllegalStateException}，不放行，避免限流失效被绕过。</li>
 * </ul>
 * <p><b>参数校验</b></p>
 * <ul>
 *   <li>{@code count &lt;= 0} 或 {@code interval &lt;= 0} 时抛 {@code IllegalArgumentException}，避免误配导致限流失效。</li>
 * </ul>
 *
 * @since 2026/9/8
 * @version 1.0
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
        if (null == current) {
            // Redis 自增异常（脚本执行失败等）返回 null：快速失败，不放行，避免限流失效被绕过
            throw new IllegalStateException(
                "@CRateLimit 自增计数返回 null，key: " + limitKey + ", 方法: " + method);
        }
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
