package com.c332030.ctool4j.spring.security.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

/**
 * <p>
 * Description: CSpringSecurityUtilsTests
 * </p>
 * <p>
 * `com.c332030.ctool4j.spring.security.util.CSpringSecurityUtils` 的测试用例，仅测试基于 SecurityContextHolder 的纯逻辑方法，
 * 不依赖 Spring 容器。
 * </p>
 *
 * <p><b>用例设计思路</b>：验证获取安全上下文、当前用户/用户详情的各条路径与边界。</p>
 * <p><b>设计依据</b>：依据 CSpringSecurityUtils 对获取安全上下文、当前用户的约定，及等价类/边界/分支覆盖。</p>
 * <p><b>覆盖场景</b>：获取认证信息、主体（字符串/UserDetails/null 主体）、用户详情（UserDetails/字符串强转异常/空上下文）。</p>
 * <p><b>未覆盖</b>：真实容器/框架集成场景。</p>
 *
 * <p><b>用例编号索引</b>：1 Security 工具（1.1-1.9），各测试方法 javadoc 标注其编号与说明。</p>
 *
 * @author c332030
 * @since 2026/8/14
 */
class CSpringSecurityUtilsTests {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

        /**
     * 1.1 空上下文获取认证信息返回 null
     */
    @Test
    void testGetAuthentication_emptyContext_returnsNull() {
        SecurityContextHolder.clearContext();
        Assertions.assertNull(CSpringSecurityUtils.getAuthentication());
    }

        /**
     * 1.2 空上下文获取主体返回 null
     */
    @Test
    void testGetPrincipal_emptyContext_returnsNull() {
        SecurityContextHolder.clearContext();
        Assertions.assertNull(CSpringSecurityUtils.getPrincipal());
    }

        /**
     * 1.3 设置后获取认证信息返回同一对象
     */
    @Test
    void testSetAndGetAuthentication() {
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", null);
        CSpringSecurityUtils.setAuthentication(auth);
        Assertions.assertEquals(auth, CSpringSecurityUtils.getAuthentication());
    }

        /**
     * 1.4 主体为字符串时获取主体返回该字符串
     */
    @Test
    void testGetPrincipal_stringPrincipal() {
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", null);
        CSpringSecurityUtils.setAuthentication(auth);
        Assertions.assertEquals("admin", CSpringSecurityUtils.getPrincipal());
    }

        /**
     * 1.5 主体为 UserDetails 时获取主体返回该对象
     */
    @Test
    void testGetPrincipal_userDetailsPrincipal() {
        UserDetails userDetails = new User("admin", "pass", Collections.emptyList());
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null);
        CSpringSecurityUtils.setAuthentication(auth);
        Assertions.assertEquals(userDetails, CSpringSecurityUtils.getPrincipal());
    }

        /**
     * 1.6 主体为 null 时获取主体返回 null
     */
    @Test
    void testGetPrincipal_nullPrincipal() {
        Authentication auth = new UsernamePasswordAuthenticationToken(null, null);
        CSpringSecurityUtils.setAuthentication(auth);
        Assertions.assertNull(CSpringSecurityUtils.getPrincipal());
    }

        /**
     * 1.7 主体为 UserDetails 时获取用户详情返回该对象
     */
    @Test
    void testGetUserDetails_userDetailsPrincipal() {
        // 正例：principal 为 UserDetails 时原样返回（anyType((Object)...) 消除重载歧义）
        UserDetails userDetails = new User("admin", "pass", Collections.emptyList());
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null);
        CSpringSecurityUtils.setAuthentication(auth);
        Assertions.assertEquals(userDetails, CSpringSecurityUtils.getUserDetails());
    }

        /**
     * 1.8 主体非 UserDetails（字符串）时强转失败，抛 ClassCastException
     */
    @Test
    void testGetUserDetails_stringPrincipal() {
        // 反例：principal 非 UserDetails（如字符串）时强转失败，运行期抛 ClassCastException
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", null);
        CSpringSecurityUtils.setAuthentication(auth);
        Assertions.assertThrowsExactly(
            ClassCastException.class,
            CSpringSecurityUtils::getUserDetails
        );
    }

        /**
     * 1.9 空上下文获取用户详情返回 null
     */
    @Test
    void testGetUserDetails_emptyContext_returnsNull() {
        // 边界：空上下文时 getPrincipal 返回 null，anyType((Object)null) 返回 null
        SecurityContextHolder.clearContext();
        Assertions.assertNull(CSpringSecurityUtils.getUserDetails());
    }
}
