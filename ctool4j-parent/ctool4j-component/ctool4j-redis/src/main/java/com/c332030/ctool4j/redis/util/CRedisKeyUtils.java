package com.c332030.ctool4j.redis.util;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CElKeyResolveUtils;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.reflect.Method;

/**
 * <p>
 * Description: CRedisKeyUtils
 * </p>
 * <p>
 * Redis 业务 key 构建与业务 id 解析的公共工具，供基于 Redis 的切面
 * （如 {@code CRateLimitAspect} 限流、{@code CIdempotentAspect} 幂等）复用，
 * 避免各切面重复实现 key 拼接与 el 表达式求值逻辑。
 * </p>
 * <p>
 * 业务 key 统一格式：{@code 应用前缀:段简单名[:方法名][:业务id]}。
 * 段简单名取类简单名（限流用方法声明类、幂等用注解分组类）；{@code useMethodName} 为 false
 * 时省略方法名段；业务 id 为空（null/空白）时省略末段。
 * </p>
 *
 * @see "doc/design/redis/CRedisKeyUtils.adoc"
 * @see "doc/design/redis/CRedisKeyUtilsTests.adoc"
 * @since 2026/9/9
 */
@UtilityClass
public class CRedisKeyUtils {

    /**
     * 解析业务 id。
     * <p>{@code idExpr} 为空或空白时返回 null（调用方按段全局不区分业务维度）；
     * 否则交由 {@link CElKeyResolveUtils} 按方法解读 el 表达式并求值。
     * 表达式非法或属性链循环引用时抛异常（由 {@link CElKeyResolveUtils} 抛出）。</p>
     *
     * @param args   方法实参
     * @param method 目标方法
     * @param idExpr 业务 id el 表达式
     * @return 业务 id；无业务维度时返回 null
     */
    public Object resolveBizId(Object[] args, Method method, String idExpr) {

        if (null == idExpr || idExpr.trim().isEmpty()) {
            return null;
        }
        return CElKeyResolveUtils.getResolver(method, idExpr).resolve(args);
    }

    /**
     * 构建 Redis 业务 key。
     * <p>格式：{@code 应用前缀:段简单名[:方法名][:业务id]}。
     * {@code useMethodName} 为 false 时省略方法名段；业务 id 为 null/空白时省略末段。</p>
     *
     * @param prefix       应用前缀（取自 {@link CRedisUtils#getApplicationPrefix()}）
     * @param simpleName   段简单名（限流为方法声明类、幂等为分组类）
     * @param methodName   方法名
     * @param useMethodName 是否将方法名纳入 key
     * @param bizId        业务 id；为 null/空白表示不按业务维度
     * @return 构建后的 key
     */
    public String buildKey(
        String prefix,
        String simpleName,
        String methodName,
        boolean useMethodName,
        Object bizId
    ) {

        val base = prefix + CRedisUtils.KEY_SEPARATOR + simpleName
            + (useMethodName ? CRedisUtils.KEY_SEPARATOR + methodName : "");

        if (isBlankSpecKey(bizId)) {
            return base;
        }
        return base + CRedisUtils.KEY_SEPARATOR + bizId;
    }

    /**
     * 判断业务 id 是否为空（null 或空白字符串），空则省略 key 末段
     *
     * @param specKey 业务 id
     * @return 是否为空
     */
    public boolean isBlankSpecKey(Object specKey) {
        return null == specKey
            || (specKey instanceof CharSequence
                && StrUtil.isBlank((CharSequence) specKey));
    }

}
