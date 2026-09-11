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
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code resolveBizId}：按注解 {@code id()} 的简单 el 表达式从方法参数解析业务 id。</li>
 *   <li>{@code buildKey}：按统一格式构建 Redis 业务 key。</li>
 *   <li>{@code isBlankSpecKey}：判断业务 id 是否为空（null/空白），决定是否省略 key 末段。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>id 表达式为空/空白</td>
 *     <td>resolveBizId 返回 null（省略业务维度）</td>
 *   </tr>
 *   <tr>
 *     <td>id 表达式非法/参数名不存在</td>
 *     <td>抛 IllegalArgumentException（CElKeyResolveUtils）</td>
 *   </tr>
 *   <tr>
 *     <td>属性链循环引用</td>
 *     <td>运行期抛 IllegalStateException</td>
 *   </tr>
 *   <tr>
 *     <td>业务 id 为 null/空白</td>
 *     <td>buildKey 省略末段</td>
 *   </tr>
 * </table>
 * <h2>设计要点</h2>
 * <p><b>业务 key 统一格式</b></p>
 * <ul>
 *   <li>格式：{@code 应用前缀:段简单名[:方法名][:业务id]}。</li>
 *   <li>段简单名：限流取方法声明类简单名；幂等取注解分组类简单名（由调用方传入）。</li>
 *   <li>{@code useMethodName} 为 false 时省略方法名段（同段多个方法共享同一 key 维度）。</li>
 *   <li>业务 id 为空（null/空白字符串）时省略末段。</li>
 * </ul>
 * <p><b>业务 id 解析</b></p>
 * <ul>
 *   <li>复用 {@code CElKeyResolveUtils}：首次使用时解析并校验表达式（参数名存在、属性链可达），按方法缓存。</li>
 *   <li>表达式为空/空白返回 null（按段全局不区分业务维度）；表达式非法或属性链循环引用时抛异常。</li>
 * </ul>
 * <p><b>key 段分隔</b></p>
 * <ul>
 *   <li>沿用 {@code CRedisUtils.KEY_SEPARATOR}（{@code ":"}）作为段分隔符，与应用前缀等其它 key 保持一致。</li>
 * </ul>
 *
 * @since 2026/9/9
 * @version 1.0
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
