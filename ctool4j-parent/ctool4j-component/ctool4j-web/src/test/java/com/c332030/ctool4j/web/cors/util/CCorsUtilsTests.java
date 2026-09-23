package com.c332030.ctool4j.web.cors.util;

import com.c332030.ctool4j.definition.constant.CConstants;
import com.c332030.ctool4j.web.cors.CCorsConfig;
import com.c332030.ctool4j.web.cors.CCorsOriginConfig;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>
 * Description: CCorsUtilsTests
 * </p>
 *
 * <p>覆盖 CCorsUtils 的 handleOptions/handle/handleDo 跨域逻辑，含按域名精细化配置、
 * 域名级独立开关与默认值回落，不依赖 Spring 容器，通过静态 setter 注入 CCorsConfig</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按 CCorsUtils.handleOptions/handle/handleDo：跨域校验与响应头设置 的处理路径/边界组织分类，逐一覆盖正例、反例与边界。</li>
 *   <li>使用 MockHttpServletRequest/MockHttpServletResponse 构造真实请求场景，贴近真实使用，不依赖外部服务。</li>
 *   <li>白盒视角：handleDo 的校验链（Origin 空 → 同源 → 域名未配置 → 域名未启用 → 方法不允许）逐节点配对正反例；
 *   各域名级开关（enable/credentials/exposeHeaders）取值 null/true/false 三态分别覆盖，验证"未配置取默认"。</li>
 *   <li>黑盒视角：按配置项取出入参取值（域名、方法、头集合的空/非空/含通配），取代表性值覆盖等价类与边界。</li>
 *   <li>测试数据贴近真实：Origin 用真实域名，HOST 用本机地址，域名配置 key 与 handleDo 的归一化口径
 *   （{@code CUrlUtils.getHostWithPort}）一致。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：见下方编号索引。</li>
 *   <li>未覆盖：Spring 配置绑定（{@code cors.origins.<域名>.*} → 对象）由 Spring Boot 绑定机制保证，
 *   本类直接构造配置对象；依赖外部 Servlet 容器/Spring 全容器装配的集成场景由集成测试覆盖。</li>
 * </ul>
 * <h2>CCorsUtils.handleOptions/handle/handleDo：跨域校验与响应头设置</h2>
 * <ul>
 *   <li>1.1 handleOptions_whenDisable（handleOptions_whenDisable）</li>
 *   <li>1.2 handleOptions_whenEnableAndOptions（handleOptions_whenEnableAndOptions）</li>
 *   <li>1.3 handleOptions_whenEnableAndNotOptions（handleOptions_whenEnableAndNotOptions）</li>
 *   <li>1.4 handleOptions_whenOptionsIgnoreCase（handleOptions_whenOptionsIgnoreCase）</li>
 *   <li>1.5 handleOptions_whenConfigNull（handleOptions_whenConfigNull）</li>
 *   <li>1.6 handle_whenEnable（handle_whenEnable）</li>
 *   <li>1.7 handle_whenDisable（handle_whenDisable）</li>
 *   <li>1.8 handleDo_whenNoOrigin（handleDo_whenNoOrigin）</li>
 *   <li>1.9 handleDo_whenSameOrigin（handleDo_whenSameOrigin）</li>
 *   <li>1.10 handleDo_whenOriginNotConfigured（handleDo_whenOriginNotConfigured）</li>
 *   <li>1.11 handleDo_whenOriginEnableNotConfigured（handleDo_whenOriginEnableNotConfigured）</li>
 *   <li>1.12 handleDo_whenOriginEnableFalse（handleDo_whenOriginEnableFalse）</li>
 *   <li>1.13 handleDo_whenMethodNotAllowed（handleDo_whenMethodNotAllowed）</li>
 *   <li>1.14 handleDo_whenAllowedHeadersAll（handleDo_whenAllowedHeadersAll）</li>
 *   <li>1.15 handleDo_whenAllowedHeadersSpecific（handleDo_whenAllowedHeadersSpecific）</li>
 *   <li>1.16 handleDo_whenCredentialsDefaultDisabled（handleDo_whenCredentialsDefaultDisabled）</li>
 *   <li>1.17 handleDo_whenCredentialsEnabled（handleDo_whenCredentialsEnabled）</li>
 *   <li>1.18 handleDo_whenCredentialsFalse（handleDo_whenCredentialsFalse）</li>
 *   <li>1.19 handleDo_whenExposeHeadersDefaultDisabled（handleDo_whenExposeHeadersDefaultDisabled）</li>
 *   <li>1.20 handleDo_whenExposeHeadersEnabledDefaultSet（handleDo_whenExposeHeadersEnabledDefaultSet）</li>
 *   <li>1.21 handleDo_whenExposeHeadersEnabledAll（handleDo_whenExposeHeadersEnabledAll）</li>
 *   <li>1.22 handleDo_whenExposeHeadersEnabledEmpty（handleDo_whenExposeHeadersEnabledEmpty）</li>
 *   <li>1.23 handle_whenEnable_shouldExposeHeaders（handle_whenEnable_shouldExposeHeaders）</li>
 *   <li>1.24 handleDo_whenExposeHeadersEnabledMultiple（handleDo_whenExposeHeadersEnabledMultiple）</li>
 *   <li>1.25 handleDo_whenExposeHeadersEnabledNull（handleDo_whenExposeHeadersEnabledNull）</li>
 *   <li>1.26 handleDo_whenOriginsEmpty（handleDo_whenOriginsEmpty）</li>
 *   <li>1.27 handleDo_whenOriginsNull（handleDo_whenOriginsNull）</li>
 *   <li>1.28 handleDo_whenMethodsDefault（handleDo_whenMethodsDefault）</li>
 *   <li>1.29 handleDo_whenAllowedHeadersDefault（handleDo_whenAllowedHeadersDefault）</li>
 *   <li>1.30 handleDo_whenExposeHeadersNotEnabledWithHeaders（handleDo_whenExposeHeadersNotEnabledWithHeaders）</li>
 *   <li>1.31 handleDo_whenOriginHostWithPort（handleDo_whenOriginHostWithPort）</li>
 *   <li>1.32 handleDo_whenMultiOrigins（handleDo_whenMultiOrigins）</li>
 *   <li>1.33 getOriginConfig_whenExactHostWithPort（getOriginConfig_whenExactHostWithPort）</li>
 *   <li>1.34 getOriginConfig_whenHostFallback（getOriginConfig_whenHostFallback）</li>
 *   <li>1.35 getOriginConfig_whenNotConfigured（getOriginConfig_whenNotConfigured）</li>
 *   <li>1.36 handleDo_whenMethodsEmpty（handleDo_whenMethodsEmpty）</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.1
 */
public class CCorsUtilsTests {

    private static final String ORIGIN = "https://example.com";

    private static final String HOST = "localhost:8080";

    private CCorsConfig config;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    /**
     * 每个用例执行前的准备
     */
    @BeforeEach
    public void setUp() {
        config = new CCorsConfig();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    /**
     * 每个用例执行后的清理
     */
    @AfterEach
    public void tearDown() {
        // 还原静态 config，避免污染其他用例
        CCorsUtils.setConfig(null);
    }

    /**
     * 启用全局跨域并注入配置
     */
    private void enable() {
        config.setEnable(Boolean.TRUE);
        CCorsUtils.setConfig(config);
    }

    /**
     * 构造域名级配置并挂到 config.origins 上（key 为归一化后的 host）
     *
     * @param originEnable  域名开关
     * @param credentials   是否允许凭据
     * @param exposeHeaders 是否暴露响应头
     * @return 域名级配置
     */
    private CCorsOriginConfig originConfig(boolean originEnable, Boolean credentials, Boolean exposeHeaders) {
        val originConfig = new CCorsOriginConfig();
        originConfig.setEnable(originEnable);
        originConfig.setCredentials(credentials);
        originConfig.setExposeHeaders(exposeHeaders);
        config.setOrigins(Collections.singletonMap("example.com", originConfig));
        return originConfig;
    }

    /**
     * 构造"域名已启用 + 默认凭据/暴露关闭"的域名级配置（多数正例的基准配置）
     *
     * @return 域名级配置
     */
    private CCorsOriginConfig enabledOrigin() {
        return originConfig(true, null, null);
    }

    /**
     * 构造带 Origin 与 HOST 的 GET 请求（跨域场景基准请求）
     */
    private void corsRequest() {
        request.setMethod("GET");
        request.addHeader(HttpHeaders.ORIGIN, ORIGIN);
        request.addHeader(HttpHeaders.HOST, HOST);
    }

    // ---------- handleOptions ----------

    /**
     * 对应测试用例 1.1：handleOptions_whenDisable
     */
    @Test
    public void handleOptions_whenDisable() {
        // 反例：未开启跨域时，OPTIONS 请求不处理
        CCorsUtils.setConfig(config);
        request.setMethod("OPTIONS");

        val handled = CCorsUtils.handleOptions(request, response);

        Assertions.assertFalse(handled);
        Assertions.assertEquals(200, response.getStatus());
    }

    /**
     * 对应测试用例 1.2：handleOptions_whenEnableAndOptions
     */
    @Test
    public void handleOptions_whenEnableAndOptions() {
        // 正例：开启跨域且为 OPTIONS 请求时，返回 204 并视为已处理
        enable();
        request.setMethod("OPTIONS");

        val handled = CCorsUtils.handleOptions(request, response);

        Assertions.assertTrue(handled);
        Assertions.assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
    }

    /**
     * 对应测试用例 1.3：handleOptions_whenEnableAndNotOptions
     */
    @Test
    public void handleOptions_whenEnableAndNotOptions() {
        // 反例：开启跨域但非 OPTIONS 请求时，不处理
        enable();
        request.setMethod("GET");

        val handled = CCorsUtils.handleOptions(request, response);

        Assertions.assertFalse(handled);
    }

    /**
     * 对应测试用例 1.4：handleOptions_whenOptionsIgnoreCase
     */
    @Test
    public void handleOptions_whenOptionsIgnoreCase() {
        // 边界：方法名大小写不敏感时仍视为 OPTIONS
        enable();
        request.setMethod("options");

        val handled = CCorsUtils.handleOptions(request, response);

        Assertions.assertTrue(handled);
    }

    /**
     * 对应测试用例 1.5：handleOptions_whenConfigNull
     */
    @Test
    public void handleOptions_whenConfigNull() {
        // 异常：config 为 null 时异常被吞掉并返回 false
        request.setMethod("OPTIONS");

        val handled = CCorsUtils.handleOptions(request, response);

        Assertions.assertFalse(handled);
    }

    // ---------- handle ----------

    /**
     * 对应测试用例 1.6：handle_whenEnable
     */
    @Test
    public void handle_whenEnable() {
        // 正例：开启跨域 + 域名已启用时委托 handleDo 设置跨域响应头
        enable();
        enabledOrigin();
        corsRequest();

        CCorsUtils.handle(request, response);

        Assertions.assertEquals(ORIGIN, response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    /**
     * 对应测试用例 1.7：handle_whenDisable
     */
    @Test
    public void handle_whenDisable() {
        // 反例：未开启跨域时，不设置任何跨域响应头
        CCorsUtils.setConfig(config);
        enabledOrigin();
        corsRequest();

        CCorsUtils.handle(request, response);

        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    // ---------- handleDo ----------

    /**
     * 对应测试用例 1.8：handleDo_whenNoOrigin
     */
    @Test
    public void handleDo_whenNoOrigin() {
        // 反例：无 Origin 请求头时，不做跨域处理
        enable();
        enabledOrigin();
        request.setMethod("GET");
        request.addHeader(HttpHeaders.HOST, HOST);

        CCorsUtils.handleDo(request, response);

        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    /**
     * 对应测试用例 1.9：handleDo_whenSameOrigin
     */
    @Test
    public void handleDo_whenSameOrigin() {
        // 反例：同源请求（HOST 与 Origin 主机一致）时，不设置跨域响应头
        enable();
        enabledOrigin();
        request.setMethod("GET");
        request.addHeader(HttpHeaders.ORIGIN, "http://localhost:8080");
        request.addHeader(HttpHeaders.HOST, HOST);

        CCorsUtils.handleDo(request, response);

        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    /**
     * 对应测试用例 1.10：handleDo_whenOriginNotConfigured
     */
    @Test
    public void handleDo_whenOriginNotConfigured() {
        // 反例：域名未配置在 origins 中时，不设置跨域响应头
        enable();
        enabledOrigin();
        request.setMethod("GET");
        request.addHeader(HttpHeaders.ORIGIN, "https://other.com");
        request.addHeader(HttpHeaders.HOST, HOST);

        CCorsUtils.handleDo(request, response);

        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    /**
     * 对应测试用例 1.11：handleDo_whenOriginEnableNotConfigured
     */
    @Test
    public void handleDo_whenOriginEnableNotConfigured() {
        // 反例：域名已配置但未配置 enable（视为 false）时，不设置跨域响应头
        enable();
        val originConfig = new CCorsOriginConfig();
        config.setOrigins(Collections.singletonMap("example.com", originConfig));
        corsRequest();

        CCorsUtils.handleDo(request, response);

        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    /**
     * 对应测试用例 1.12：handleDo_whenOriginEnableFalse
     */
    @Test
    public void handleDo_whenOriginEnableFalse() {
        // 反例：域名级 enable 显式为 false 时，不设置跨域响应头
        enable();
        originConfig(false, Boolean.TRUE, Boolean.TRUE);
        corsRequest();

        CCorsUtils.handleDo(request, response);

        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
    }

    /**
     * 对应测试用例 1.13：handleDo_whenMethodNotAllowed
     */
    @Test
    public void handleDo_whenMethodNotAllowed() {
        // 反例：请求方法不在允许方法列表中时，不设置跨域响应头
        enable();
        val originConfig = enabledOrigin();
        originConfig.setAllowedMethods(new LinkedHashSet<String>(Collections.singletonList("GET")));
        request.setMethod("DELETE");
        request.addHeader(HttpHeaders.ORIGIN, ORIGIN);
        request.addHeader(HttpHeaders.HOST, HOST);

        CCorsUtils.handleDo(request, response);

        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    /**
     * 对应测试用例 1.14：handleDo_whenAllowedHeadersAll
     */
    @Test
    public void handleDo_whenAllowedHeadersAll() {
        // 正例：允许全部请求头时，ALLOW_HEADERS 为通配符
        enable();
        val originConfig = enabledOrigin();
        originConfig.setAllowedHeaders(Collections.singleton(CConstants.STAR));
        corsRequest();

        CCorsUtils.handleDo(request, response);

        Assertions.assertEquals(ORIGIN, response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        Assertions.assertEquals(CConstants.STAR, response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS));
        Assertions.assertEquals("GET", response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
    }

    /**
     * 对应测试用例 1.15：handleDo_whenAllowedHeadersSpecific
     */
    @Test
    public void handleDo_whenAllowedHeadersSpecific() {
        // 正例：允许指定请求头时，ALLOW_HEADERS 为逗号拼接的列表
        enable();
        val originConfig = enabledOrigin();
        originConfig.setAllowedMethods(Collections.singleton("POST"));

        Set<String> allowedHeaders = new LinkedHashSet<String>();
        allowedHeaders.add(HttpHeaders.AUTHORIZATION);
        allowedHeaders.add(HttpHeaders.CONTENT_TYPE);
        originConfig.setAllowedHeaders(allowedHeaders);

        request.setMethod("POST");
        request.addHeader(HttpHeaders.ORIGIN, ORIGIN);
        request.addHeader(HttpHeaders.HOST, HOST);

        CCorsUtils.handleDo(request, response);

        Assertions.assertEquals(ORIGIN, response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        Assertions.assertEquals("Authorization,Content-Type",
            response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS));
        Assertions.assertEquals("POST", response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
    }

    /**
     * 对应测试用例 1.16：handleDo_whenCredentialsDefaultDisabled
     */
    @Test
    public void handleDo_whenCredentialsDefaultDisabled() {
        // 正例（默认值）：未配置 credentials 时默认禁用，不设置 ALLOW_CREDENTIALS
        enable();
        enabledOrigin();
        corsRequest();

        CCorsUtils.handleDo(request, response);

        Assertions.assertEquals(ORIGIN, response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    /**
     * 对应测试用例 1.17：handleDo_whenCredentialsEnabled
     */
    @Test
    public void handleDo_whenCredentialsEnabled() {
        // 正例：显式开启 credentials 时，设置 ALLOW_CREDENTIALS 为 true
        enable();
        originConfig(true, Boolean.TRUE, null);
        corsRequest();

        CCorsUtils.handleDo(request, response);

        Assertions.assertEquals(ORIGIN, response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        Assertions.assertEquals("true", response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    /**
     * 对应测试用例 1.18：handleDo_whenCredentialsFalse
     */
    @Test
    public void handleDo_whenCredentialsFalse() {
        // 反例：显式配置 credentials=false 时，不设置 ALLOW_CREDENTIALS
        enable();
        originConfig(true, Boolean.FALSE, null);
        corsRequest();

        CCorsUtils.handleDo(request, response);

        Assertions.assertEquals(ORIGIN, response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    /**
     * 对应测试用例 1.19：handleDo_whenExposeHeadersDefaultDisabled
     */
    @Test
    public void handleDo_whenExposeHeadersDefaultDisabled() {
        // 正例（默认值）：未配置 exposeHeaders 时默认禁用，不设置 EXPOSE_HEADERS
        enable();
        enabledOrigin();
        corsRequest();

        CCorsUtils.handleDo(request, response);

        Assertions.assertEquals(ORIGIN, response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
    }

    /**
     * 对应测试用例 1.20：handleDo_whenExposeHeadersEnabledDefaultSet
     */
    @Test
    public void handleDo_whenExposeHeadersEnabledDefaultSet() {
        // 正例：显式开启 exposeHeaders 且未配置集合时，暴露默认 Authorization
        enable();
        originConfig(true, null, Boolean.TRUE);
        corsRequest();

        CCorsUtils.handleDo(request, response);

        Assertions.assertEquals(HttpHeaders.AUTHORIZATION,
            response.getHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
    }

    /**
     * 对应测试用例 1.21：handleDo_whenExposeHeadersEnabledAll
     */
    @Test
    public void handleDo_whenExposeHeadersEnabledAll() {
        // 正例：exposeHeaders 开启且集合含 STAR 时，EXPOSE_HEADERS 使用通配符
        enable();
        val originConfig = originConfig(true, null, Boolean.TRUE);
        originConfig.setExposedHeaders(Collections.singleton(CConstants.STAR));
        corsRequest();

        CCorsUtils.handleDo(request, response);

        Assertions.assertEquals(CConstants.STAR,
            response.getHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
    }

    /**
     * 对应测试用例 1.22：handleDo_whenExposeHeadersEnabledEmpty
     */
    @Test
    public void handleDo_whenExposeHeadersEnabledEmpty() {
        // 边界：exposeHeaders 开启但集合为空时不设置 EXPOSE_HEADERS
        enable();
        val originConfig = originConfig(true, null, Boolean.TRUE);
        originConfig.setExposedHeaders(Collections.emptySet());
        corsRequest();

        CCorsUtils.handleDo(request, response);

        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
    }

    /**
     * 对应测试用例 1.23：handle_whenEnable_shouldExposeHeaders
     */
    @Test
    public void handle_whenEnable_shouldExposeHeaders() {
        // 正例：普通（非 OPTIONS）实际响应在开启暴露后同样输出 EXPOSE_HEADERS，前端 JS 才能读取响应头
        enable();
        originConfig(true, null, Boolean.TRUE);
        corsRequest();

        CCorsUtils.handle(request, response);

        Assertions.assertEquals(HttpHeaders.AUTHORIZATION,
            response.getHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
    }

    /**
     * 对应测试用例 1.24：handleDo_whenExposeHeadersEnabledMultiple
     */
    @Test
    public void handleDo_whenExposeHeadersEnabledMultiple() {
        // 正例：exposeHeaders 开启且集合含多个头时，EXPOSE_HEADERS 为逗号拼接的列表
        enable();
        val originConfig = originConfig(true, null, Boolean.TRUE);

        Set<String> exposedHeaders = new LinkedHashSet<String>();
        exposedHeaders.add(HttpHeaders.AUTHORIZATION);
        exposedHeaders.add("X-TOKEN");
        originConfig.setExposedHeaders(exposedHeaders);
        corsRequest();

        CCorsUtils.handleDo(request, response);

        Assertions.assertEquals("Authorization,X-TOKEN",
            response.getHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
    }

    /**
     * 对应测试用例 1.25：handleDo_whenExposeHeadersEnabledNull
     */
    @Test
    public void handleDo_whenExposeHeadersEnabledNull() {
        // 边界：exposeHeaders 开启但集合为 null 时回落到默认 Authorization（空安全）
        enable();
        val originConfig = originConfig(true, null, Boolean.TRUE);
        originConfig.setExposedHeaders(null);
        corsRequest();

        CCorsUtils.handleDo(request, response);

        Assertions.assertEquals(HttpHeaders.AUTHORIZATION,
            response.getHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
    }

    /**
     * 对应测试用例 1.26：handleDo_whenOriginsEmpty
     */
    @Test
    public void handleDo_whenOriginsEmpty() {
        // 反例：origins 为空 Map 时，任何域名都不放行
        enable();
        config.setOrigins(Collections.emptyMap());
        corsRequest();

        CCorsUtils.handleDo(request, response);

        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    /**
     * 对应测试用例 1.27：handleDo_whenOriginsNull
     */
    @Test
    public void handleDo_whenOriginsNull() {
        // 异常：origins 为 null 时异常被吞掉，不设置跨域响应头
        enable();
        config.setOrigins(null);
        corsRequest();

        CCorsUtils.handleDo(request, response);

        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    /**
     * 对应测试用例 1.28：handleDo_whenMethodsDefault
     */
    @Test
    public void handleDo_whenMethodsDefault() {
        // 正例（默认值）：域名级未配置 allowedMethods 时回落到默认 `*`，任意方法放行
        enable();
        enabledOrigin();
        request.setMethod("DELETE");
        request.addHeader(HttpHeaders.ORIGIN, ORIGIN);
        request.addHeader(HttpHeaders.HOST, HOST);

        CCorsUtils.handleDo(request, response);

        Assertions.assertEquals(ORIGIN, response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        Assertions.assertEquals("DELETE", response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
    }

    /**
     * 对应测试用例 1.29：handleDo_whenAllowedHeadersDefault
     */
    @Test
    public void handleDo_whenAllowedHeadersDefault() {
        // 正例（默认值）：域名级未配置 allowedHeaders 时回落到默认 Authorization,Content-Type
        enable();
        enabledOrigin();
        corsRequest();

        CCorsUtils.handleDo(request, response);

        Assertions.assertEquals("Authorization,Content-Type",
            response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS));
    }

    /**
     * 对应测试用例 1.30：handleDo_whenExposeHeadersNotEnabledWithHeaders
     */
    @Test
    public void handleDo_whenExposeHeadersNotEnabledWithHeaders() {
        // 反例：配了 exposedHeaders 但未开启 exposeHeaders 开关时，仍不暴露（配置了≠启用了）
        enable();
        val originConfig = new CCorsOriginConfig();
        originConfig.setEnable(Boolean.TRUE);
        originConfig.setExposedHeaders(Collections.singleton(HttpHeaders.AUTHORIZATION));
        config.setOrigins(Collections.singletonMap("example.com", originConfig));
        corsRequest();

        CCorsUtils.handleDo(request, response);

        Assertions.assertEquals(ORIGIN, response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
    }

    /**
     * 对应测试用例 1.31：handleDo_whenOriginHostWithPort
     */
    @Test
    public void handleDo_whenOriginHostWithPort() {
        // 边界：Origin 带非默认端口时归一化为 host:port，域名配置以纯 host 为 key 也能命中
        enable();
        val originConfig = new CCorsOriginConfig();
        originConfig.setEnable(Boolean.TRUE);
        config.setOrigins(Collections.singletonMap("example.com", originConfig));

        request.setMethod("GET");
        request.addHeader(HttpHeaders.ORIGIN, "https://example.com:8081");
        request.addHeader(HttpHeaders.HOST, HOST);

        CCorsUtils.handleDo(request, response);

        Assertions.assertEquals("https://example.com:8081",
            response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    /**
     * 对应测试用例 1.32：handleDo_whenMultiOrigins
     */
    @Test
    public void handleDo_whenMultiOrigins() {
        // 正例：多个域名各自独立配置，仅命中的域名按自己的开关输出
        enable();

        val enabled = new CCorsOriginConfig();
        enabled.setEnable(Boolean.TRUE);
        enabled.setCredentials(Boolean.TRUE);

        val disabled = new CCorsOriginConfig();
        disabled.setEnable(Boolean.FALSE);

        val origins = new LinkedHashMap<String, CCorsOriginConfig>();
        origins.put("example.com", enabled);
        origins.put("other.com", disabled);
        config.setOrigins(origins);

        request.setMethod("GET");
        request.addHeader(HttpHeaders.ORIGIN, "https://other.com");
        request.addHeader(HttpHeaders.HOST, HOST);
        CCorsUtils.handleDo(request, response);
        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));

        val otherResponse = new MockHttpServletResponse();
        request.removeHeader(HttpHeaders.ORIGIN);
        request.addHeader(HttpHeaders.ORIGIN, ORIGIN);
        CCorsUtils.handleDo(request, otherResponse);
        Assertions.assertEquals(ORIGIN, otherResponse.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        Assertions.assertEquals("true", otherResponse.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    // ---------- getOriginConfig ----------

    /**
     * 对应测试用例 1.33：getOriginConfig_whenExactHostWithPort
     */
    @Test
    public void getOriginConfig_whenExactHostWithPort() {
        // 正例：origins 存在 host:port 形态的 key 时优先精确命中
        CCorsUtils.setConfig(config);
        val exact = new CCorsOriginConfig();
        config.setOrigins(Collections.singletonMap("example.com:8081", exact));

        val result = CCorsUtils.getOriginConfig("example.com:8081");

        Assertions.assertSame(exact, result);
    }

    /**
     * 对应测试用例 1.34：getOriginConfig_whenHostFallback
     */
    @Test
    public void getOriginConfig_whenHostFallback() {
        // 正例：无 host:port 精确 key 时回落到纯 host（配置文件的域名 key 形态）
        CCorsUtils.setConfig(config);
        val byHost = new CCorsOriginConfig();
        config.setOrigins(Collections.singletonMap("example.com", byHost));

        val result = CCorsUtils.getOriginConfig("example.com:8081");

        Assertions.assertSame(byHost, result);
    }

    /**
     * 对应测试用例 1.35：getOriginConfig_whenNotConfigured
     */
    @Test
    public void getOriginConfig_whenNotConfigured() {
        // 反例：两种形态都未配置时返回 null
        CCorsUtils.setConfig(config);
        config.setOrigins(Collections.singletonMap("example.com", new CCorsOriginConfig()));

        val result = CCorsUtils.getOriginConfig("other.com:8081");

        Assertions.assertNull(result);
    }

    /**
     * 对应测试用例 1.36：handleDo_whenMethodsEmpty
     */
    @Test
    public void handleDo_whenMethodsEmpty() {
        // 反例：域名级显式配置空方法集合时不放行（空集合即不允许任何方法，不回落成"全部允许"）
        enable();
        val originConfig = enabledOrigin();
        originConfig.setAllowedMethods(Collections.emptySet());
        corsRequest();

        CCorsUtils.handleDo(request, response);

        Assertions.assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

}
