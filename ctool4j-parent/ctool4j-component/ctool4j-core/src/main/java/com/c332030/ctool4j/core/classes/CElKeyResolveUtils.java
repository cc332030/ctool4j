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
 * <h2>能力目录</h2>
 * <p>{@code CElKeyResolveUtils} 将简单 el 表达式 {@code 参数名.属性名.属性名…} 编译为可复用解析器并执行取值。</p>
 * <p>表达式格式：{@code 参数名.属性名.属性名…}，如 {@code "user.id"}、{@code "req.userId"}，支持一级到任意深度，属性可以是嵌套对象。</p>
 * <p>核心能力：</p>
 * <ul>
 *   <li>{@code Resolver.resolve(Object[] args)}：对一次调用的实参求值，并检测循环引用。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>表达式空/空白</td>
 *     <td>解析期抛 IllegalArgumentException</td>
 *   </tr>
 *   <tr>
 *     <td>参数名不存在</td>
 *     <td>解析期抛 IllegalArgumentException</td>
 *   </tr>
 *   <tr>
 *     <td>非法段（连续点/非法字符）</td>
 *     <td>解析期抛 IllegalArgumentException</td>
 *   </tr>
 *   <tr>
 *     <td>参数为 null / 链中某级为 null</td>
 *     <td>resolve 返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>属性在某运行类型不可解析</td>
 *     <td>resolve 抛 IllegalStateException</td>
 *   </tr>
 *   <tr>
 *     <td>属性链循环引用（同实例重复访问）</td>
 *     <td>resolve 抛 IllegalStateException</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>缓存 key（{@code @CCacheable.key()}）取值，由 {@code CCacheAspect.resolveCacheKey} 调用。</li>
 *   <li>限流业务 id（{@code @CRateLimit.id()}）取值，由 {@code CRateLimitAspect} 调用。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不支持算术/方法调用/复杂 SpEL 语法，仅简单属性路径。</li>
 *   <li>参数名依赖编译期 {@code -parameters} 或 debug 符号，否则无法按参数名取值。</li>
 *   <li>校验为首次使用（懒）触发：某方法从未被调用则其表达式错误不会暴露（不做启动期全量扫描，换取启动速度）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>循环引用仅在"同一对象实例沿属性链被重复访问"时判定；不同对象即使值相等也不误报（引用去重）。</li>
 *   <li>校验期仅能做「表达式结构 + 参数名 + 段合法性」；属性在具体运行类型的可达性与循环引用需运行期按实际类型验证（接口/泛型无法静态推导）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语法与校验（首次使用即懒校验）</b></p>
 * <ul>
 *   <li>表达式为 null/空白 → {@code IllegalArgumentException}。</li>
 *   <li>按 {@code .} 分段，每段须为合法 Java 标识符（{@code [A-Za-z_$][A-Za-z0-9_$]*}），空段/非法字符 → 报错。</li>
 *   <li>首段为参数名，须能在方法形参中命中（否则报错）。</li>
 *   <li>参数名解析依赖编译期 {@code -parameters}（父 pom 已全局开启），直接读 {@code Parameter#getName()}。</li>
 *   <li>校验时机为「首次使用」：首次调用时解析校验并缓存；不牺牲启动时间（不做启动期全量扫描）。</li>
 * </ul>
 * <p><b>高性能</b></p>
 * <ul>
 *   <li>表达式只解析一次，按方法（{@code Method} 弱引用）缓存解析器；合法后后续调用命中缓存，运行期零字符串解析。</li>
 *   <li>解析器读取：先 {@code getIfPresent} 命中直接返回（不创建 lambda、不走并发 compute），热路径开销极低。</li>
 *   <li>属性 getter 复用 {@code CReflectUtils.getGetterHandleMap(type)} 按类缓存的 MethodHandle（含继承字段），运行期零反射查表、{@code handle.invokeExact} 执行（统一 {@code (Object)Object} 签名，无签名适配开销，为 MethodHandle 快速路径）。</li>
 *   <li>每级按对象实际类型取 getter，天然适配接口/父类/多态运行类型。</li>
 *   <li>无属性跳的一级表达式（{@code key="参数名"}）直接返回参数对象，跳过循环检测与集合分配，为最高频最快路径。</li>
 *   <li>单级跳（{@code 参数名.属性}）仅取一次属性、直接返回，不分配循环检测集合，为次快路径。</li>
 * </ul>
 * <p><b>运行期取值与循环引用检测</b></p>
 * <ul>
 *   <li>参数为 null、或属性链某级为 null → 返回 null（由调用方决定取值是否可用）。</li>
 *   <li>属性在某一实际类型不可解析（无该字段 getter）→ {@code IllegalStateException}（动态类型运行期兜底报错）。</li>
 *   <li>循环引用检测：仅在存在多级属性跳（属性链 ≥2 级）时进行；单线程线性推进，用局部引用去重 {@code Set}</li>
 *   <li>（{@code IdentityHashMap} 底层）记录已访问对象；若链中再次访问到同一对象实例，判定循环引用 → {@code IllegalStateException}。</li>
 *   <li>单级跳不可能形成环（环至少需两次跳转），走零分配快速路径跳过该检测。</li>
 *   <li>因求值在同一方法调用栈内顺序推进，用局部 {@code Set} 即可，无需 {@code ThreadLocal}。</li>
 * </ul>
 *
 * @since 2026/9/8
 * @version 1.0
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
     * <ul>
     *   <li>{@code getResolver(Method, String)}：解析并校验表达式，按方法缓存解析器。</li>
     * </ul>
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
