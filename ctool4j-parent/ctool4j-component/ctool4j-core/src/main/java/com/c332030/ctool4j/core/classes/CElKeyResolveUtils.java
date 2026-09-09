package com.c332030.ctool4j.core.classes;

import com.c332030.ctool4j.core.util.CLocalCacheUtils;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * <p>
 * Description: CElKeyResolveUtils
 * </p>
 * <p>
 * 简单 el 表达式（{@code 参数名.属性名.属性名…}）解析与运行求值器。
 * 在首次使用时（懒校验）解析为「参数下标 + 属性链」，按方法缓存；运行期按对象实际
 * 类型逐级经 MethodHandle getter 取值。供缓存 key、限流业务 id 等取值场景复用。
 * </p>
 * <p>
 * 高性能：表达式只解析一次并按方法缓存；属性 getter 复用
 * {@link CReflectUtils#getGetterHandleMap} 按类缓存的 MethodHandle，运行期零字符串解析、零反射查表。
 * </p>
 *
 * @see "doc/design/core/CElKeyResolveUtils.adoc"
 * @see "doc/design/core/CElKeyResolveUtilsTests.adoc"
 * @since 2026/9/8
 */
@UtilityClass
public class CElKeyResolveUtils {

    /**
     * 合法标识符：Java 参数名/属性名的字符集合
     */
    private final Pattern IDENTIFIER_PATTERN = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*");

    /**
     * 表达式解析器缓存：key 弱引用（Method → Resolver），避免类加载器无法回收
     */
    private final Cache<Method, Resolver> RESOLVER_CACHE =
        CLocalCacheUtils.<Method, Resolver>cacheBuilder()
            .weakKeys()
            .build();

    /**
     * 获取指定方法在指定表达式下的解析器（首次使用时解析一次并缓存）。
     * <p>表达式非法（空/空白/非法段/参数名不存在）时立即抛出 {@link IllegalArgumentException}，
     * 实现首次使用即校验（懒校验）；合法后按方法缓存，后续调用零解析开销。</p>
     *
     * @param method    方法
     * @param keyExpr   el 表达式
     * @return 解析器
     */
    public Resolver getResolver(Method method, String keyExpr) {

        // 先直接读，命中即返回，避免热路径每次创建 lambda 与重复并发读
        val cached = RESOLVER_CACHE.getIfPresent(method);
        if (null != cached) {
            return cached;
        }
        return RESOLVER_CACHE.get(method, k -> parse(method, keyExpr));
    }

    /**
     * 解析并校验表达式，生成可执行解析器
     */
    Resolver parse(Method method, String keyExpr) {

        if (null == keyExpr || keyExpr.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "el 表达式为空，方法: " + method);
        }

        val segments = keyExpr.split("\\.");
        if (segments.length == 0) {
            throw new IllegalArgumentException(
                "el 表达式非法: [" + keyExpr + "]，方法: " + method);
        }

        // 参数名 → 下标
        val paramName = segments[0];
        val paramIndex = findParamIndex(method, paramName);
        if (paramIndex < 0) {
            throw new IllegalArgumentException(
                "el 表达式参数名 [" + paramName + "] 不存在于方法参数: " + method);
        }

        // 属性链（跳过参数名自身）
        val propChain = new String[segments.length - 1];
        for (int i = 1; i < segments.length; i++) {
            val segment = segments[i];
            validateSegment(method, keyExpr, segment);
            propChain[i - 1] = segment;
        }

        return new Resolver(method, paramIndex, propChain);
    }

    private void validateSegment(Method method, String keyExpr, String segment) {
        if (segment.isEmpty() || !IDENTIFIER_PATTERN.matcher(segment).matches()) {
            throw new IllegalArgumentException(
                "el 表达式段非法: [" + segment + "]（整体: [" + keyExpr + "]），方法: " + method);
        }
    }

    private int findParamIndex(Method method, String paramName) {

        // 依赖编译期 -parameters（父 pom 已全局开启），Parameter#getName 返回真实形参名
        val parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (paramName.equals(parameters[i].getName())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 已编译的表达式解析器。运行期对一次调用入参执行属性链取值，并检测循环引用。
     */
    public static final class Resolver {

        private final Method method;
        private final int paramIndex;
        private final String[] propChain;

        Resolver(Method method, int paramIndex, String[] propChain) {
            this.method = method;
            this.paramIndex = paramIndex;
            this.propChain = propChain;
        }

        /**
         * 对方法入参求值。
         *
         * @param args 方法实参
         * @return 取值；参数为 null 或属性链中途某级为 null 时返回 null
         */
        @SneakyThrows
        public Object resolve(Object[] args) {

            if (null == args || args.length <= paramIndex || null == args[paramIndex]) {
                return null;
            }

            Object obj = args[paramIndex];

            // 无属性跳：仅取参数本身，无对象环可能，直接返回（最高频路径，零分配）
            if (propChain.length == 0) {
                return obj;
            }

            // 多级跳才可能形成对象环（环至少需两次跳转），单级跳直接取值走零分配快速路径
            if (propChain.length == 1) {
                return invokeGetter(obj, propChain[0]);
            }

            // 多级跳：单线程线性推进，用局部身份 set 去重（同一对象实例被重复访问即判定循环引用），
            // 仅在真正需要时才分配，避免高开销路径上的无谓分配
            Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
            visited.add(obj);

            for (val prop : propChain) {

                obj = invokeGetter(obj, prop);
                if (null == obj) {
                    return null;
                }
                if (!visited.add(obj)) {
                    throw new IllegalStateException(
                        "检测到 el 表达式循环引用（属性链中重复访问同一对象实例），方法: "
                            + method + "，重复对象类型: " + obj.getClass().getName());
                }
            }

            return obj;
        }

        /**
         * 按对象实际类型取属性 getter 值
         */
        @SneakyThrows
        private Object invokeGetter(Object obj, String prop) {
            val type = obj.getClass();
            val handle = CReflectUtils.getGetterHandleMap(type).get(prop);
            if (null == handle) {
                throw new IllegalStateException(
                    "el 表达式属性 [" + prop + "] 在类型 " + type.getName()
                        + " 上不可解析（方法: " + method + "），请检查表达式或参数类型");
            }
            return handle.invokeExact(obj);
        }
    }

}
