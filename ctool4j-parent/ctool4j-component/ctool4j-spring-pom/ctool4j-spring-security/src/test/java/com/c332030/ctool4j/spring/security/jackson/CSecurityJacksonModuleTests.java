package com.c332030.ctool4j.spring.security.jackson;

import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import com.c332030.ctool4j.core.util.CJsonUtils;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.spring.security.util.CGrantedAuthorityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * Description: CSecurityJacksonModuleTests
 * </p>
 * <p>
 * 验证本模块提供的 Jackson 能力：模块经 SPI 自动发现、{@link GrantedAuthority} 的序列化形态、
 * 按声明类型（接口 / 具体实现类）反序列化，以及序列化往返。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>序列化无自研实现（Jackson 原生即可），故用测试固化「形态契约」：{@code {"authority":"ROLE_XXX"}}，
 *   且<b>不带</b> {@code @class} 类型信息（与线上既有数据一致）。</li>
 *   <li>反序列化能力由 SPI 自动发现提供：全程不手工注册模块，SPI 声明缺失时用例直接失败。</li>
 *   <li>分「接口声明 / 具体类声明 / 预置 mapper 全量」三个维度验证，覆盖 Jackson 按声明类型查找反序列化器的特性
 *   与 {@code copy()} 派生 mapper 的传播假设。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>线上报错 JSON：{@code "authorities":[{"authority":"ROLE_ANONYMOUS"}]}（无类型信息）。</li>
 *   <li>Spring 官方 {@code CoreJackson2Module} 走 default typing（带 {@code @class}），与本形态不兼容，故不采用。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：SPI 自动发现、原生序列化形态、集合序列化、会话对象形态、具体实现类声明反序列化、
 *   5 个预置 mapper 全量可用、序列化往返。</li>
 *   <li>未覆盖：自定义 {@link GrantedAuthority} 实现、多态类型信息（本模块不启用 default typing）、
 *   shade 打包后 SPI 文件是否保留（属构建期问题，不在单测范围）。</li>
 * </ul>
 * <h2>设计取舍</h2>
 * <ul>
 *   <li>会话形态断言用「包含子串 + 不含 {@code @class}」而非全量字符串相等：字段顺序非契约，形态才是契约。</li>
 *   <li>构造输入优先用常量池实例（口径与生产一致）；序列化用例额外覆盖一次未入池实例（{@code new}），
 *   证明序列化形态不依赖常量池。</li>
 * </ul>
 * <h2>模块能力</h2>
 * <ul>
 *   <li>1 SPI 自动发现（对应测试方法 1.1）</li>
 *   <li>2 序列化（对应测试方法 2.1-2.3）</li>
 *   <li>3 按声明类型反序列化（对应测试方法 3.1-3.3）</li>
 *   <li>4 序列化往返（对应测试方法 4.1）</li>
 * </ul>
 *
 * @since 2026/9/14
 * @version 1.0
 */
class CSecurityJacksonModuleTests {

    /**
     * 对应测试用例 1.1：新建 mapper 调用 findAndRegisterModules 后即具备权限反序列化能力
     */
    @SneakyThrows
    @Test
    void testFindAndRegisterModules_discoversModule() {
        // 正例：SPI 自动发现——未手工注册任何模块，findAndRegisterModules 后即可还原权限
        val objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        val authority = objectMapper.readValue("{\"authority\":\"ROLE_A\"}", GrantedAuthority.class);
        Assertions.assertEquals("ROLE_A", authority.getAuthority());
    }

    /**
     * 对应测试用例 2.1：Jackson 原生即可序列化单个权限（无需本模块、无需自定义序列化器）
     */
    @SneakyThrows
    @Test
    void testSerialize_singleAuthority_nativeSupport() {
        // 正例：原生 mapper 直接输出 {"authority":"ROLE_A"}，与全局 mapper（含 Long→String 等定制）结果一致
        val nativeMapper = new ObjectMapper();

        Assertions.assertEquals(
            "{\"authority\":\"ROLE_A\"}",
            nativeMapper.writeValueAsString(new SimpleGrantedAuthority("ROLE_A"))
        );
        Assertions.assertEquals(
            "{\"authority\":\"ROLE_A\"}",
            CJsonUtils.toJson(new SimpleGrantedAuthority("ROLE_A"))
        );
    }

    /**
     * 对应测试用例 2.2：集合序列化形态为对象数组（顺序保持）
     */
    @Test
    void testSerialize_authorityList() {
        // 正例：集合序列化为 [{"authority":"..."}]，与线上 authorities 结构一致
        val json = CJsonUtils.toJson(CList.of(
            CGrantedAuthorityUtils.get("ROLE_A"),
            CGrantedAuthorityUtils.get("ROLE_B")
        ));

        Assertions.assertEquals("[{\"authority\":\"ROLE_A\"},{\"authority\":\"ROLE_B\"}]", json);
    }

    /**
     * 对应测试用例 2.3：会话对象序列化形态与线上一致（不带类型信息）
     */
    @Test
    void testSerialize_sessionShape() {
        // 正例：会话对象按 {"authorities":[{"authority":"ROLE_ANONYMOUS"}]} 输出
        val session = new SessionStub();
        session.setClientType("MINI_PROGRAM");
        session.setAuthorities(CList.of(CGrantedAuthorityUtils.get("ROLE_ANONYMOUS")));

        val json = CJsonUtils.toJson(session);

        Assertions.assertTrue(json.contains("\"clientType\":\"MINI_PROGRAM\""));
        Assertions.assertTrue(json.contains("\"authorities\":[{\"authority\":\"ROLE_ANONYMOUS\"}]"));
        // 关键：不引入 default typing（无 @class），保证与既有持久化数据形态兼容
        Assertions.assertFalse(json.contains("@class"));
    }

    /**
     * 对应测试用例 3.1：变量声明为具体实现类 SimpleGrantedAuthority 时也能读回，且实例来自常量池
     */
    @Test
    void testDeserialize_simpleGrantedAuthorityType_pooled() {
        // 正例：Jackson 按声明类型查找反序列化器，具体实现类与接口注册的是同一反序列化器
        val authority = CJsonUtils.fromJson("{\"authority\":\"ROLE_CONCRETE\"}", SimpleGrantedAuthority.class);

        Assertions.assertNotNull(authority);
        Assertions.assertEquals("ROLE_CONCRETE", authority.getAuthority());
        Assertions.assertSame(CGrantedAuthorityUtils.get("ROLE_CONCRETE"), authority);
    }

    /**
     * 对应测试用例 3.2：集合元素声明为具体实现类时同样可读回，且元素来自常量池
     */
    @Test
    void testDeserialize_concreteTypeList_pooled() {
        // 正例：List<SimpleGrantedAuthority> 字段按元素声明类型命中同一反序列化器
        val json = "{\"authorities\":[{\"authority\":\"ROLE_C1\"},{\"authority\":\"ROLE_C2\"}]}";
        val session = CJsonUtils.fromJson(json, ConcreteAuthorityStub.class);

        Assertions.assertNotNull(session);

        val authorities = session.getAuthorities();
        Assertions.assertEquals(2, authorities.size());
        Assertions.assertSame(CGrantedAuthorityUtils.get("ROLE_C1"), authorities.get(0));
        Assertions.assertSame(CGrantedAuthorityUtils.get("ROLE_C2"), authorities.get(1));
    }

    /**
     * 对应测试用例 3.3：5 个预置 mapper 均具备权限反序列化能力
     */
    @SneakyThrows
    @Test
    void testDeserialize_presetMappers_allSupport() {
        // 回归：能力依赖 findAndRegisterModules 在预置 mapper 构建/派生（copy）时生效，
        // 逐个验证避免只有 OBJECT_MAPPER 可用、其余 mapper 静默失效
        val objectMappers = CList.of(
            CJacksonUtils.OBJECT_MAPPER,
            CJacksonUtils.OBJECT_MAPPER_NON_NULL,
            CJacksonUtils.OBJECT_MAPPER_SNAKE_CASE,
            CJacksonUtils.OBJECT_MAPPER_NATIVE,
            CJacksonUtils.OBJECT_MAPPER_LOG
        );

        for (val objectMapper : objectMappers) {
            val authority = objectMapper.readValue("{\"authority\":\"ROLE_A\"}", GrantedAuthority.class);
            Assertions.assertEquals("ROLE_A", authority.getAuthority());
        }
    }

    /**
     * 对应测试用例 4.1：序列化 → 反序列化 → 再序列化，权限值稳定且实例来自常量池
     */
    @Test
    void testRoundTrip_session_keepsAuthorities() {
        // 正例：会话 JSON 可反复读回（Redis 场景），权限值不变、形态不变
        val json = "{\"clientType\":\"MINI_PROGRAM\",\"authorities\":[{\"authority\":\"ROLE_X\"}]}";

        val session = CJsonUtils.fromJson(json, SessionStub.class);
        Assertions.assertNotNull(session);

        val authorities = session.getAuthorities();
        Assertions.assertEquals(1, authorities.size());

        val authority = authorities.iterator().next();
        Assertions.assertEquals("ROLE_X", authority.getAuthority());
        // 反序列化实例取自常量池：同值复用
        Assertions.assertSame(CGrantedAuthorityUtils.get("ROLE_X"), authority);

        Assertions.assertTrue(CJsonUtils.toJson(session).contains("\"authorities\":[{\"authority\":\"ROLE_X\"}]"));
    }

    /**
     * 会话对象桩：authorities 声明为接口类型（复现线上结构）
     */
    @Data
    public static class SessionStub {

        private String clientType;

        private Collection<? extends GrantedAuthority> authorities;
    }

    /**
     * 会话对象桩：authorities 元素声明为具体实现类（验证按声明类型命中反序列化器）
     */
    @Data
    public static class ConcreteAuthorityStub {

        private List<SimpleGrantedAuthority> authorities;
    }

}
