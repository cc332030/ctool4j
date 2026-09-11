package com.c332030.ctool4j.feign.test.util;

import com.c332030.ctool4j.core.util.CSet;
import com.c332030.ctool4j.feign.config.CFeignClientHeaderConfig;
import com.c332030.ctool4j.feign.enums.CFeignClientHeaderPropagationModeEnum;
import com.c332030.ctool4j.feign.util.CFeignUtils;
import feign.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * CFeignUtils 测试
 *
 * 覆盖：addInterceptor / getApiType / intercept / newResponse / transferHeaders
 * 的正常路径、边界与异常路径
 *
 * <p>是 {@link com.c332030.ctool4j.feign.util.CFeignUtils} 的测试用例。</p>
 *
 * <h2>用例设计思路与依据</h2>
 * <ul>
 *   <li>覆盖 intercept（匹配/不匹配/空 map/无 target 抛 NPE）、getApiType、newResponse（有/无 body）、</li>
 *   <li>transferHeaders（null 配置/ALL/CUSTOM/NONE）。</li>
 * </ul>
 * <h2>intercept 拦截判断</h2>
 * <ul>
 *   <li>1.1 按接口类匹配（testInterceptMatchByClass）</li>
 *   <li>1.2 不匹配（testInterceptNotMatch）</li>
 *   <li>1.3 空拦截器 map（testInterceptEmptyInterceptorMap）</li>
 *   <li>1.4 模板无 target 抛 NPE（testInterceptTemplateNoTargetThrowsNpe）</li>
 * </ul>
 * <h2>getApiType</h2>
 * <ul>
 *   <li>2.1 获取接口类型（testGetApiType）</li>
 *   <li>2.2 空 target 抛 NPE（testGetApiTypeNullTargetThrowsNpe）</li>
 * </ul>
 * <h2>newResponse 响应重建</h2>
 * <ul>
 *   <li>3.1 带 body 重建（testNewResponse）</li>
 *   <li>3.2 null body（testNewResponseNullBody）</li>
 * </ul>
 * <h2>transferHeaders header 透传</h2>
 * <ul>
 *   <li>4.1 null 配置无操作（testTransferHeadersNullConfigNoOp）</li>
 *   <li>4.2 ALL 无传播 header 无操作（testTransferHeadersAllNoPropagationHeadersNoOp）</li>
 *   <li>4.3 ALL 复制来源 header（testTransferHeadersAllCopiesOrigin）</li>
 *   <li>4.4 CUSTOM 过滤（testTransferHeadersCustomFilters）</li>
 *   <li>4.5 NONE 不透传（testTransferHeadersNone）</li>
 * </ul>
 *
 * @author c332030
 * @since 1.0
 * @version 1.0
 */
class CFeignUtilsTests {

    /**
     * 被测类标记的接口
     */
    interface MarkerApi {
    }

    /**
     * 匹配的父接口
     */
    interface ParentApi {
    }

    /**
     * 直接匹配的类
     */
    static class MarkerImpl implements MarkerApi {
    }

    @BeforeEach
    void clearInterceptorMap() throws Exception {
        // INTERCEPTOR_MAP 为共享静态字段，逐用例清理，保证测试隔离
        Field field = CFeignUtils.class.getDeclaredField("INTERCEPTOR_MAP");
        field.setAccessible(true);
        ((Map<?, ?>) field.get(null)).clear();
    }

    @AfterEach
    void resetHeaderConfig() throws Exception {
        setHeaderConfig(null);
    }

    private static void setHeaderConfig(CFeignClientHeaderConfig config) throws Exception {
        Field field = CFeignUtils.class.getDeclaredField("headerConfig");
        field.setAccessible(true);
        field.set(null, config);
    }

    // ==================== addInterceptor / intercept ====================

    /**
     * 对应测试用例 1.1：按接口类匹配
     */
    @Test
    void testInterceptMatchByClass() {

        AtomicBoolean invoked = new AtomicBoolean(false);
        AtomicReference<RequestTemplate> got = new AtomicReference<>();
        RequestTemplate template = new RequestTemplate();

        CFeignUtils.addInterceptor(MarkerApi.class, t -> {
            invoked.set(true);
            got.set(t);
        });

        boolean matched = CFeignUtils.intercept(MarkerImpl.class, template);

        Assertions.assertTrue(matched);
        Assertions.assertTrue(invoked.get());
        Assertions.assertSame(template, got.get());
    }

    /**
     * 对应测试用例 1.2：不匹配
     */
    @Test
    void testInterceptNotMatch() {

        AtomicBoolean invoked = new AtomicBoolean(false);
        CFeignUtils.addInterceptor(MarkerApi.class, t -> invoked.set(true));

        boolean matched = CFeignUtils.intercept(Runnable.class, new RequestTemplate());

        Assertions.assertFalse(matched);
        Assertions.assertFalse(invoked.get());
    }

    /**
     * 对应测试用例 1.3：空拦截器 map
     */
    @Test
    void testInterceptEmptyInterceptorMap() {

        boolean matched = CFeignUtils.intercept(MarkerImpl.class, new RequestTemplate());
        Assertions.assertFalse(matched);
    }

    /**
     * 对应测试用例 1.4：模板无 target 抛 NPE
     */
    @Test
    void testInterceptTemplateNoTargetThrowsNpe() {

        // 未设置 feignTarget 的模板，getApiType 访问 null.feignTarget 抛 NPE
        Assertions.assertThrowsExactly(
            NullPointerException.class,
            () -> CFeignUtils.intercept(new RequestTemplate())
        );
    }

    /**
     * 对应测试用例 2.1：获取接口类型
     */
    @Test
    void testGetApiType() {

        Target<MarkerApi> target = new TestTarget<>(MarkerApi.class, "test");
        RequestTemplate template = new RequestTemplate();
        template.feignTarget(target);

        Assertions.assertSame(MarkerApi.class, CFeignUtils.getApiType(template));
    }

    /**
     * 对应测试用例 2.2：空 target 抛 NPE
     */
    @Test
    void testGetApiTypeNullTargetThrowsNpe() {

        Assertions.assertThrowsExactly(
            NullPointerException.class,
            () -> CFeignUtils.getApiType(new RequestTemplate())
        );
    }

    // ==================== newResponse ====================

    /**
     * 对应测试用例 3.1：带 body 重建
     */
    @Test
    void testNewResponse() throws java.io.IOException {

        Request request = buildRequest("http://localhost:8080/api");
        Map<String, Collection<String>> headers = new LinkedHashMap<>();
        headers.put("Content-Type", Collections.singletonList("application/json"));

        Response original = Response.builder()
            .status(200)
            .reason("OK")
            .request(request)
            .headers(headers)
            .body(new byte[]{1, 2, 3})
            .build();

        byte[] newBody = "new-body".getBytes(StandardCharsets.UTF_8);
        Response rebuilt = CFeignUtils.newResponse(original, newBody);

        Assertions.assertEquals(200, rebuilt.status());
        Assertions.assertEquals("OK", rebuilt.reason());
        Assertions.assertSame(request, rebuilt.request());
        Assertions.assertArrayEquals(newBody, Util.toByteArray(rebuilt.body().asInputStream()));
        // feign Response.builder().headers 内部将 header 名小写化，重建后 key 为小写
        // headers 值集合为 UnmodifiableCollection，equals 非普通 List 语义，逐项断言
        Collection<String> cts = rebuilt.headers().get("content-type");
        Assertions.assertEquals(1, cts.size());
        Assertions.assertEquals("application/json", cts.iterator().next());
    }

    /**
     * 对应测试用例 3.2：null body
     */
    @Test
    void testNewResponseNullBody() {

        Request request = buildRequest("http://localhost:8080/api");
        Response original = Response.builder()
            .status(204)
            .reason("No Content")
            .request(request)
            .body(new byte[0])
            .build();

        Response rebuilt = CFeignUtils.newResponse(original, null);

        Assertions.assertNull(rebuilt.body());
        Assertions.assertEquals(204, rebuilt.status());
    }

    // ==================== transferHeaders ====================

    /**
     * 对应测试用例 4.1：null 配置无操作
     */
    @Test
    void testTransferHeadersNullConfigNoOp() throws Exception {

        setHeaderConfig(null);
        RequestTemplate template = new RequestTemplate();
        template.header("X-Old", "v");

        CFeignUtils.transferHeaders(template);

        // null config 直接返回，模板 header 保持不变
        Assertions.assertEquals("v", template.headers().get("X-Old").iterator().next());
    }

    /**
     * 对应测试用例 4.2：ALL 无传播 header 无操作
     */
    @Test
    void testTransferHeadersAllNoPropagationHeadersNoOp() throws Exception {

        CFeignClientHeaderConfig config = new CFeignClientHeaderConfig();
        config.setPropagationMode(CFeignClientHeaderPropagationModeEnum.ALL);
        config.setPropagationRequestHeaders(null);
        setHeaderConfig(config);

        RequestTemplate template = new RequestTemplate();
        template.header("X-Old", "v");

        CFeignUtils.transferHeaders(template);

        // ALL + 空 propagationRequestHeaders -> 直接返回
        Assertions.assertEquals("v", template.headers().get("X-Old").iterator().next());
    }

    /**
     * 对应测试用例 4.3：ALL 复制来源 header
     */
    @Test
    void testTransferHeadersAllCopiesOrigin() throws Exception {

        CFeignClientHeaderConfig config = new CFeignClientHeaderConfig();
        config.setPropagationMode(CFeignClientHeaderPropagationModeEnum.ALL);
        config.setPropagationRequestHeaders(CSet.of("X-Trace"));
        setHeaderConfig(config);

        RequestTemplate template = new RequestTemplate();
        template.header("X-Old", "v1");

        CFeignUtils.transferHeaders(template);

        // ALL 模式下 originHeaders 全部并入 newHeaders 并写入模板
        Assertions.assertTrue(template.headers().containsKey("X-Old"));
    }

    /**
     * 对应测试用例 4.4：CUSTOM 过滤
     */
    @Test
    void testTransferHeadersCustomFilters() throws Exception {

        CFeignClientHeaderConfig config = new CFeignClientHeaderConfig();
        config.setPropagationMode(CFeignClientHeaderPropagationModeEnum.CUSTOM);
        config.setPropagationCustomHeaders(CSet.of("X-Keep", "X-Skip"));
        setHeaderConfig(config);

        RequestTemplate template = new RequestTemplate();
        Map<String, Collection<String>> originHeaders = new LinkedHashMap<>();
        originHeaders.put("X-Keep", Collections.singletonList("keep"));
        originHeaders.put("X-Skip", Collections.emptyList());   // 空集合不传播
        originHeaders.put("X-Other", Collections.singletonList("other")); // 不在自定义名单内
        template.headers(originHeaders);

        CFeignUtils.transferHeaders(template);

        Assertions.assertTrue(template.headers().containsKey("X-Keep"));
        Assertions.assertFalse(template.headers().containsKey("X-Skip"));
        Assertions.assertFalse(template.headers().containsKey("X-Other"));
    }

    /**
     * 对应测试用例 4.5：NONE 不透传
     */
    @Test
    void testTransferHeadersNone() throws Exception {

        CFeignClientHeaderConfig config = new CFeignClientHeaderConfig();
        config.setPropagationMode(CFeignClientHeaderPropagationModeEnum.NONE);
        setHeaderConfig(config);

        RequestTemplate template = new RequestTemplate();
        template.header("X-Old", "v");

        CFeignUtils.transferHeaders(template);

        // NONE 模式不传播自定义头
        Assertions.assertFalse(template.headers().containsKey("X-Old"));
    }

    // ==================== helper ====================

    private static Request buildRequest(String url) {
        // 5 参 create 在 feign 11 已弃用，传 null RequestTemplate 等价于旧行为
        return Request.create(
            Request.HttpMethod.GET,
            url,
            Collections.emptyMap(),
            new byte[0],
            StandardCharsets.UTF_8,
            null
        );
    }

    /**
     * feign.Target 的最小测试实现
     */
    private static final class TestTarget<T> implements Target<T> {

        private final Class<T> type;
        private final String name;

        private TestTarget(Class<T> type, String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public Class<T> type() {
            return type;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String url() {
            return "http://localhost:8080";
        }

        @Override
        public Request apply(RequestTemplate input) {
            return buildRequest(url() + input.path());
        }
    }
}
