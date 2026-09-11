package com.c332030.ctool4j.redis.util;

import com.c332030.ctool4j.core.classes.CElKeyResolveUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

/**
 * <p>
 * Description: CRedisKeyUtilsTests
 * </p>
 * <p>
 * 测试 {@link CRedisKeyUtils}：业务 id el 表达式解析（{@code resolveBizId}）、
 * Redis key 构建（{@code buildKey}）与业务 id 判空（{@code isBlankSpecKey}）。
 * 不依赖 Spring 容器与真实 Redis。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>{@code buildKey}/{@code isBlankSpecKey} 为纯逻辑方法，直接断言结果。</li>
 *   <li>{@code resolveBizId} 依赖方法参数名（编译期 {@code -parameters}），通过反射取测试类方法验证 el 表达式求值；</li>
 *   <li>非法表达式/属性不可解析等场景断言抛对应异常。</li>
 *   <li>不依赖 Spring 容器与真实 Redis。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：id 表达式为空/空白/取参数本身/取属性链/参数为 null/参数名不存在/属性不可解析的 {@code resolveBizId}；</li>
 *   <li>key 含方法名与业务 id/无业务 id/空白业务 id/去方法名段/去方法名段含业务 id 的 {@code buildKey}；</li>
 *   <li>null/空白/非空白/非字符串的 {@code isBlankSpecKey}。</li>
 *   <li>未覆盖：真实 Redis 集成（纯工具逻辑，无外部依赖）。</li>
 * </ul>
 * <h2>CRedisKeyUtils.resolveBizId（业务 id 解析）</h2>
 * <ul>
 *   <li>1.1 id 表达式为空：返回 null（resolveBizId_emptyExpr_returnsNull）</li>
 *   <li>1.2 id 表达式为空白：返回 null（resolveBizId_blankExpr_returnsNull）</li>
 *   <li>1.3 取参数本身（resolveBizId_paramExpr_resolves）</li>
 *   <li>1.4 取参数属性链（resolveBizId_propChain_resolves）</li>
 *   <li>1.5 参数为 null：返回 null（resolveBizId_nullParam_returnsNull）</li>
 *   <li>1.6 表达式参数名不存在：抛 IllegalArgumentException（resolveBizId_unknownParam_throws）</li>
 *   <li>1.7 属性在类型上不可解析：抛 IllegalStateException（resolveBizId_unknownProp_throws）</li>
 * </ul>
 * <h2>CRedisKeyUtils.buildKey（key 构建）</h2>
 * <ul>
 *   <li>2.1 含方法名与业务 id（buildKey_withMethodAndBizId）</li>
 *   <li>2.2 无业务 id（null）省略末段（buildKey_noBizId）</li>
 *   <li>2.3 业务 id 为空白字符串省略末段（buildKey_blankBizId）</li>
 *   <li>2.4 useMethodName=false 省略方法名段（buildKey_withoutMethodName）</li>
 *   <li>2.5 useMethodName=false 且含业务 id（buildKey_withoutMethodName_bizId）</li>
 * </ul>
 * <h2>CRedisKeyUtils.isBlankSpecKey（业务 id 判空）</h2>
 * <ul>
 *   <li>3.1 null 视为空（isBlankSpecKey_null）</li>
 *   <li>3.2 空白字符串视为空（isBlankSpecKey_blankString）</li>
 *   <li>3.3 非空字符串不为空（isBlankSpecKey_nonBlank）</li>
 *   <li>3.4 非字符串对象不为空（isBlankSpecKey_nonString）</li>
 * </ul>
 *
 * @since 2026/9/9
 * @version 1.0
 */
class CRedisKeyUtilsTests {

    // ===== 供反射取方法的样例 =====

    @SuppressWarnings("unused")
    private static String sample(Long userId) {
        return "ok";
    }

    @SuppressWarnings("unused")
    private static String sampleOrder(OrderRequest req) {
        return "ok";
    }

    static class OrderRequest {
        private final Long userId;

        OrderRequest(Long userId) {
            this.userId = userId;
        }

        @SuppressWarnings("unused")
        public Long getUserId() {
            return userId;
        }
    }

    private Method method(String name) {
        for (Method m : CRedisKeyUtilsTests.class.getDeclaredMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        throw new IllegalStateException("method not found: " + name);
    }

    // ===== resolveBizId =====

    /**
     * 对应测试用例 1.1：id 表达式为空返回 null（无业务维度）
     */
    @Test
    void resolveBizId_emptyExpr_returnsNull() {
        Method m = method("sample");
        Assertions.assertNull(CRedisKeyUtils.resolveBizId(new Object[] { 1L }, m, ""));
    }

    /**
     * 对应测试用例 1.2：id 表达式为空白返回 null
     */
    @Test
    void resolveBizId_blankExpr_returnsNull() {
        Method m = method("sample");
        Assertions.assertNull(CRedisKeyUtils.resolveBizId(new Object[] { 1L }, m, "   "));
    }

    /**
     * 对应测试用例 1.3：id 表达式取参数本身
     */
    @Test
    void resolveBizId_paramExpr_resolves() {
        Method m = method("sample");
        Assertions.assertEquals(10L, CRedisKeyUtils.resolveBizId(new Object[] { 10L }, m, "userId"));
    }

    /**
     * 对应测试用例 1.4：id 表达式取参数属性链
     */
    @Test
    void resolveBizId_propChain_resolves() {
        Method m = method("sampleOrder");
        Assertions.assertEquals(10L,
            CRedisKeyUtils.resolveBizId(new Object[] { new OrderRequest(10L) }, m, "req.userId"));
    }

    /**
     * 对应测试用例 1.5：参数为 null 返回 null
     */
    @Test
    void resolveBizId_nullParam_returnsNull() {
        Method m = method("sample");
        Assertions.assertNull(CRedisKeyUtils.resolveBizId(new Object[] { null }, m, "userId"));
    }

    /**
     * 对应测试用例 1.6：表达式参数名不存在抛 IllegalArgumentException
     */
    @Test
    void resolveBizId_unknownParam_throws() {
        Method m = method("sample");
        Assertions.assertThrowsExactly(IllegalArgumentException.class,
            () -> CRedisKeyUtils.resolveBizId(new Object[] { 1L }, m, "unknown"));
    }

    /**
     * 对应测试用例 1.7：属性在类型上不可解析抛 IllegalStateException
     */
    @Test
    void resolveBizId_unknownProp_throws() {
        Method m = method("sampleOrder");
        Assertions.assertThrowsExactly(IllegalStateException.class,
            () -> CRedisKeyUtils.resolveBizId(new Object[] { new OrderRequest(10L) }, m, "req.noSuch"));
    }

    // ===== buildKey =====

    /**
     * 对应测试用例 2.1：含方法名与业务 id
     */
    @Test
    void buildKey_withMethodAndBizId() {
        Assertions.assertEquals("grp:Svc:do:10",
            CRedisKeyUtils.buildKey("grp", "Svc", "do", true, 10L));
    }

    /**
     * 对应测试用例 2.2：无业务 id（null）省略末段
     */
    @Test
    void buildKey_noBizId() {
        Assertions.assertEquals("grp:Svc:do",
            CRedisKeyUtils.buildKey("grp", "Svc", "do", true, null));
    }

    /**
     * 对应测试用例 2.3：业务 id 为空白字符串省略末段
     */
    @Test
    void buildKey_blankBizId() {
        Assertions.assertEquals("grp:Svc:do",
            CRedisKeyUtils.buildKey("grp", "Svc", "do", true, "  "));
    }

    /**
     * 对应测试用例 2.4：useMethodName=false 省略方法名段
     */
    @Test
    void buildKey_withoutMethodName() {
        Assertions.assertEquals("grp:Svc",
            CRedisKeyUtils.buildKey("grp", "Svc", "do", false, null));
    }

    /**
     * 对应测试用例 2.5：useMethodName=false 且含业务 id
     */
    @Test
    void buildKey_withoutMethodName_bizId() {
        Assertions.assertEquals("grp:Svc:10",
            CRedisKeyUtils.buildKey("grp", "Svc", "do", false, 10L));
    }

    // ===== isBlankSpecKey =====

    /**
     * 对应测试用例 3.1：null 视为空
     */
    @Test
    void isBlankSpecKey_null() {
        Assertions.assertTrue(CRedisKeyUtils.isBlankSpecKey(null));
    }

    /**
     * 对应测试用例 3.2：空白字符串视为空
     */
    @Test
    void isBlankSpecKey_blankString() {
        Assertions.assertTrue(CRedisKeyUtils.isBlankSpecKey("  "));
    }

    /**
     * 对应测试用例 3.3：非空字符串不为空
     */
    @Test
    void isBlankSpecKey_nonBlank() {
        Assertions.assertFalse(CRedisKeyUtils.isBlankSpecKey("userId"));
    }

    /**
     * 对应测试用例 3.4：非字符串对象不为空
     */
    @Test
    void isBlankSpecKey_nonString() {
        Assertions.assertFalse(CRedisKeyUtils.isBlankSpecKey(10L));
    }

}
