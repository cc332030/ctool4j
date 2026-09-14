package com.c332030.ctool4j.spring.security.jackson.deserializer;

import com.c332030.ctool4j.core.util.CJsonUtils;
import com.c332030.ctool4j.spring.security.util.CGrantedAuthorityUtils;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * <p>
 * Description: CGrantedAuthorityDeserializerTests
 * </p>
 * <p>
 * 通过真实 ObjectMapper 反序列化含权限字段的会话对象，验证 GrantedAuthority 接口类型的还原能力。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「会话对象 / 对象与字符串两种形态 / 空白与非法节点兜底 / 常量池复用」多个维度组织。</li>
 *   <li>关键回归：GrantedAuthority 为接口且 JSON 无类型信息，无对应反序列化器时会话对象反序列化必然抛异常
 *   （线上 "loadAuthentication error" 的根因）。</li>
 *   <li>全程不手工注册任何模块：能力由 {@code META-INF/services} + {@code findAndRegisterModules()} 提供
 *   （SPI 自动发现本身由 {@code CSecurityJacksonModuleTests} 覆盖）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据线上报错日志：{@code Cannot construct instance of GrantedAuthority (no Creators...)};
 *   参考 JSON 结构 {@code {"authorities":[{"authority":"ROLE_ANONYMOUS"}]}}。</li>
 *   <li>依据 {@code SimpleGrantedAuthority} 构造对空白入参抛 {@code IllegalArgumentException} 的既有约定，
 *   故空白一律在反序列化器入口拦掉，不把构造异常抛给调用方。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：会话对象还原、对象/字符串形态、空白与 null 边界、非对象非文本节点的兜底、多权限顺序、常量池复用。</li>
 *   <li>未覆盖：自定义 GrantedAuthority 实现类（本反序列化器统一还原为 SimpleGrantedAuthority）；
 *   具体实现类声明类型（由 {@code CSecurityJacksonModuleTests} 覆盖，属模块注册契约）。</li>
 * </ul>
 * <h2>设计取舍</h2>
 * <ul>
 *   <li>断言以「权限值 + 实例同一性」为主：常量池是核心契约，仅比值会漏掉复用失效。</li>
 *   <li>非法节点（数字/布尔/数组）要求返回 null 而非抛异常：会话数据脏化时不应让整个会话读回失败。</li>
 * </ul>
 * <h2>权限反序列化</h2>
 * <ul>
 *   <li>1 形态与正常路径（对应测试方法 1.1-1.4）</li>
 *   <li>2 边界与兜底（对应测试方法 2.1-2.4）</li>
 * </ul>
 *
 * @since 2026/9/14
 * @version 1.0
 */
class CGrantedAuthorityDeserializerTests {

    /**
     * 与线上报错一致的 JSON 结构（含 authorities 为对象数组）
     */
    private static final String SESSION_JSON =
        "{\"unionid\":\"omZEc5hKUmIYxlgY4_JB5uiuqRAI\",\"clientType\":\"MINI_PROGRAM\","
            + "\"authorities\":[{\"authority\":\"ROLE_ANONYMOUS\"}],\"createMillis\":null}";

    /**
     * 对应测试用例 1.1：全局预置 mapper 反序列化会话成功且权限还原
     */
    @Test
    void testDeserialize_session_success() {
        // 正例：CJacksonUtils 预置 mapper 在构建时已 findAndRegisterModules，authorities（接口集合）
        // 还原为 SimpleGrantedAuthority，全程无手工注册
        val session = CJsonUtils.fromJson(SESSION_JSON, SessionStub.class);

        Assertions.assertNotNull(session);
        Assertions.assertEquals("MINI_PROGRAM", session.getClientType());

        val authorities = session.getAuthorities();
        Assertions.assertNotNull(authorities);
        Assertions.assertEquals(1, authorities.size());
        Assertions.assertEquals("ROLE_ANONYMOUS", authorities.iterator().next().getAuthority());
    }

    /**
     * 对应测试用例 1.2：对象形态 {"authority":"ROLE_A"}
     */
    @Test
    void testDeserialize_objectForm() {
        // 正例：对象形态取 authority 字段
        val authority = CJsonUtils.fromJson("{\"authority\":\"ROLE_A\"}", GrantedAuthority.class);
        Assertions.assertEquals("ROLE_A", authority.getAuthority());
    }

    /**
     * 对应测试用例 1.3：字符串形态 "ROLE_B"（兼容简写）
     */
    @Test
    void testDeserialize_stringForm() {
        // 正例：字符串形态直接作为 authority
        val authority = CJsonUtils.fromJson("\"ROLE_B\"", GrantedAuthority.class);
        Assertions.assertEquals("ROLE_B", authority.getAuthority());
    }

    /**
     * 对应测试用例 1.4：多权限集合顺序保持，且重复权限复用同一实例
     */
    @Test
    void testDeserialize_multiAuthorities_keepsOrderAndPooled() {
        // 正例：顺序与入参 JSON 一致；重复出现的 ROLE_1 两次还原为同一实例
        val json = "{\"authorities\":[{\"authority\":\"ROLE_1\"},{\"authority\":\"ROLE_2\"},{\"authority\":\"ROLE_1\"}]}";
        val session = CJsonUtils.fromJson(json, SessionStub.class);

        Assertions.assertNotNull(session);
        val iterator = session.getAuthorities().iterator();

        Assertions.assertSame(CGrantedAuthorityUtils.get("ROLE_1"), iterator.next());
        Assertions.assertSame(CGrantedAuthorityUtils.get("ROLE_2"), iterator.next());
        Assertions.assertSame(CGrantedAuthorityUtils.get("ROLE_1"), iterator.next());
        Assertions.assertFalse(iterator.hasNext());
    }

    /**
     * 对应测试用例 2.1：对象无 authority 字段返回 null
     */
    @Test
    void testDeserialize_missingAuthority_returnsNull() {
        // 边界：对象缺 authority 字段时返回 null，不构造非法权限
        Assertions.assertNull(CJsonUtils.fromJson("{}", GrantedAuthority.class));
    }

    /**
     * 对应测试用例 2.2：JSON null 返回 null
     */
    @Test
    void testDeserialize_null_returnsNull() {
        // 边界：JSON null 返回 null
        Assertions.assertNull(CJsonUtils.fromJson("null", GrantedAuthority.class));
    }

    /**
     * 对应测试用例 2.3：空白 authority（字符串形态或对象字段）返回 null，不触发构造断言异常
     */
    @Test
    void testDeserialize_blankAuthority_returnsNull() {
        // 边界：空白一律拦在入口——SimpleGrantedAuthority 构造对空白入参会抛 IllegalArgumentException
        Assertions.assertNull(CJsonUtils.fromJson("\"\"", GrantedAuthority.class));
        Assertions.assertNull(CJsonUtils.fromJson("\"   \"", GrantedAuthority.class));
        Assertions.assertNull(CJsonUtils.fromJson("{\"authority\":\"   \"}", GrantedAuthority.class));
    }

    /**
     * 对应测试用例 2.4：非对象非文本节点（数字/布尔/数组）取不到 authority，返回 null 而不抛异常
     */
    @Test
    void testDeserialize_nonAuthorityNode_returnsNull() {
        // 边界：脏数据不应让整个会话读回失败
        Assertions.assertNull(CJsonUtils.fromJson("123", GrantedAuthority.class));
        Assertions.assertNull(CJsonUtils.fromJson("true", GrantedAuthority.class));
        Assertions.assertNull(CJsonUtils.fromJson("[\"ROLE_A\"]", GrantedAuthority.class));
    }

    /**
     * 对应测试用例 2.5：对象形态与字符串形态还原出的同一权限为常量池同一实例
     */
    @Test
    void testDeserialize_reusesPoolInstance() {
        // 正例：两种形态归一后复用同一实例，池化不因入口形态不同而失效
        val objectForm = CJsonUtils.fromJson("{\"authority\":\"ROLE_POOL\"}", GrantedAuthority.class);
        val stringForm = CJsonUtils.fromJson("\"ROLE_POOL\"", GrantedAuthority.class);

        Assertions.assertSame(CGrantedAuthorityUtils.get("ROLE_POOL"), objectForm);
        Assertions.assertSame(objectForm, stringForm);
    }

    /**
     * 会话对象桩：含接口类型字段 authorities（复现线上结构）
     */
    @Data
    public static class SessionStub {

        private String unionid;

        private String clientType;

        private Collection<? extends GrantedAuthority> authorities;

        private Long createMillis;
    }

}
