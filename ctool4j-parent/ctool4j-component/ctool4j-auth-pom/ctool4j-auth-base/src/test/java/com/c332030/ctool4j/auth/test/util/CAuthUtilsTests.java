package com.c332030.ctool4j.auth.test.util;

import com.c332030.ctool4j.auth.config.CAuthConfig;
import com.c332030.ctool4j.auth.interfaces.ICJwtInfo;
import com.c332030.ctool4j.auth.util.CAuthUtils;
import com.c332030.ctool4j.web.util.CJwtUtils;
import lombok.CustomLog;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;

/**
 * <p>
 * Description: CAuthUtilsTests
 * </p>
 * <p>`com.c332030.ctool4j.auth.util.CAuthUtils`（CAuthUtils）的测试用例</p>
 *
 * <p>覆盖依赖配置的 jwt 能力：由 jwt 校验并解析 token（getTokenByJwt）、为 jwt body 生成 jwt 并写入响应头（setJwt）；
 * token 前缀与请求头/请求属性读写等纯逻辑见 ctool4j-web 的 CTokenUtils 测试。</p>
 *
 * <p><b>用例设计思路</b>：按「getTokenByJwt / setJwt」两个维度组织，覆盖正常、带前缀、空值、校验失败与配置缺失等路径。</p>
 * <p><b>设计依据</b>：依据功能设计对 jwt 校验兜底、配置密钥使用的约定；异常路径按实现精确断言异常类型
 * （密钥空白 IllegalArgumentException、非请求上下文 IllegalArgumentException、配置未注入 NPE）。</p>
 * <p><b>覆盖场景</b>：getTokenByJwt 正常/带前缀/空白/null/非法 jwt/载荷无 token 字段/配置未注入；
 * setJwt 生成并写入响应头且结果可 verify/密钥空白/非请求上下文/配置未注入。</p>
 * <p><b>未覆盖</b>：无。</p>
 *
 * <p><b>用例编号索引</b>：1 getTokenByJwt（1.1-1.6）；2 setJwt（2.1-2.4）。各测试方法 javadoc 标注其编号与说明。</p>
 *
 * @since 2026/9/11
 */
@CustomLog
public class CAuthUtilsTests {

    private static final String SECRET = "test-secret-key-12345";

    /**
     * 清理测试状态：重置请求上下文与注入的 authConfig，避免状态残留污染同 JVM 的其他用例
     */
    @AfterEach
    public void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        CAuthUtils.setAuthConfig(null);
    }

    // ---------- getTokenByJwt ----------

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void getTokenByJwt_valid() {
        // 正例：校验通过并解析出载荷中的 token
        setAuthConfig();

        val jwt = CJwtUtils.create(Collections.singletonMap("token", "t-1"), SECRET);
        Assertions.assertEquals("t-1", CAuthUtils.getTokenByJwt(jwt));
    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void getTokenByJwt_withPrefix() {
        // 正例：带 Bearer 前缀的 jwt 先移除前缀再解析
        setAuthConfig();

        val jwt = CJwtUtils.create(Collections.singletonMap("token", "t-2"), SECRET);
        Assertions.assertEquals("t-2", CAuthUtils.getTokenByJwt("Bearer " + jwt));
    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void getTokenByJwt_blank_returnsNull() {
        // 边界：jwt 为空返回 null
        Assertions.assertNull(CAuthUtils.getTokenByJwt(null));
        Assertions.assertNull(CAuthUtils.getTokenByJwt(""));
        Assertions.assertNull(CAuthUtils.getTokenByJwt("   "));
    }

    /**
     * 对应测试用例 1.4
     */
    @Test
    public void getTokenByJwt_invalid_returnsNull() {
        // 反例：非法 jwt 校验失败返回 null
        setAuthConfig();
        Assertions.assertNull(CAuthUtils.getTokenByJwt("not-a-jwt"));
    }

    /**
     * 对应测试用例 1.5
     */
    @Test
    public void getTokenByJwt_noTokenField_returnsNull() {
        // 边界：校验通过但载荷中无 token 字段时返回 null
        setAuthConfig();

        val jwt = CJwtUtils.create(Collections.singletonMap("name", "tom"), SECRET);
        Assertions.assertNull(CAuthUtils.getTokenByJwt(jwt));
    }

    /**
     * 对应测试用例 1.6
     */
    @Test
    public void getTokenByJwt_withoutAuthConfig_returnsNull() {
        // 兜底：authConfig 未注入时取密钥的 NPE 被捕获，静默返回 null（不向上抛）
        val jwt = CJwtUtils.create(Collections.singletonMap("token", "t-3"), SECRET);
        Assertions.assertNull(CAuthUtils.getTokenByJwt(jwt));
    }

    // ---------- setJwt ----------

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void setJwt_writesAuthorizationHeader() {
        // 正例：由 ICJwtInfo 生成 jwt 并写入响应 Authorization 头，结果可 verify
        setAuthConfig();

        // setJwt 经 CTokenUtils.setHeaderToken 写入当前响应，须先绑定请求上下文
        val response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(new MockHttpServletRequest(), response)
        );

        val info = new JwtInfoStub();
        info.setName("tom");

        CAuthUtils.setJwt(info);

        val header = response.getHeader(HttpHeaders.AUTHORIZATION);
        Assertions.assertNotNull(header);
        Assertions.assertTrue(header.startsWith("Bearer "));
        Assertions.assertTrue(CJwtUtils.verify(header.substring("Bearer ".length()), SECRET));
    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void setJwt_blankSecret_throws() {
        // 异常路径：密钥未配置（空白）时 CJwtUtils.create 抛 IllegalArgumentException（setJwt 不做兜底）
        val config = new CAuthConfig();
        config.setJwtSecret("");
        CAuthUtils.setAuthConfig(config);

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CAuthUtils.setJwt(new JwtInfoStub())
        );
    }

    /**
     * 对应测试用例 2.3
     */
    @Test
    public void setJwt_withoutRequestContext_throws() {
        // 异常路径：非请求上下文写入响应头时 CRequestUtils 抛 IllegalArgumentException
        setAuthConfig();

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CAuthUtils.setJwt(new JwtInfoStub())
        );
    }

    /**
     * 对应测试用例 2.4
     */
    @Test
    public void setJwt_withoutAuthConfig_throws() {
        // 异常路径：authConfig 未注入时取密钥抛 NPE（setJwt 不做兜底）
        Assertions.assertThrowsExactly(
            NullPointerException.class,
            () -> CAuthUtils.setJwt(new JwtInfoStub())
        );
    }

    private void setAuthConfig() {
        val config = new CAuthConfig();
        config.setJwtSecret(SECRET);
        CAuthUtils.setAuthConfig(config);
    }

    // 内部测试用 ICJwtInfo 桩（字段即 jwt 载荷）
    @Data
    public static class JwtInfoStub implements ICJwtInfo {
        private String name;
    }

}
