package com.c332030.ctool4j.web.test.interceptor;

import com.c332030.ctool4j.core.util.CSet;
import com.c332030.ctool4j.definition.annotation.CInnerApi;
import com.c332030.ctool4j.web.config.CInnerApiConfig;
import com.c332030.ctool4j.web.interceptor.CInnerApiInterceptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

/**
 * <p>
 * Description: CInnerApiInterceptorTests
 * </p>
 *
 * <p>覆盖 CInnerApiInterceptor.preHandle：非 HandlerMethod 放行、无注解放行、
 * 类/方法级注解识别、白名单为空放行、命中放行、未命中拒绝(403)。</p>
 *
 * <p>被测依赖类（异常 / 序列化器 / 日志 / 服务 / 切面 / 拦截器等）无 builder，测试按常规直接 new 构造——属规范允许的取舍，依据与边界在此记录。</p>
 *
 * <p><b>用例设计思路</b>：按拦截器判断链路的各分支覆盖——handler 类型判定 → 注解识别（类/方法级）→ 白名单空/命中/未命中；
 * 通过 Controller 桩类标注/不标注 {@code @CInnerApi}，配合 {@code HandlerMethod} 驱动 {@code preHandle}，
 * 用 {@code MockHttpServletRequest}/{@code MockHttpServletResponse} 模拟请求与响应。</p>
 * <p><b>设计依据</b>：依据功能设计对 {@code preHandle} 的约定（非内部接口放行、白名单空放行、命中放行、未命中返回 false 且写 403）；
 * 依据分支/边界覆盖（非 HandlerMethod、无注解、类级/方法级注解、白名单空、命中、未命中）。</p>
 * <p><b>覆盖场景</b>：非 HandlerMethod 放行；无注解放行；类级/方法级 {@code @CInnerApi} 识别；{@code @Inherited} 父类类级继承；
 * 白名单空放行；命中白名单放行（含 X-Forwarded-For 首段命中）；X-Forwarded-For 首段未命中拒绝；未命中拒绝并响应 403。</p>
 * <p><b>未覆盖</b>：真实 Spring MVC 容器集成（由集成用例另行覆盖）、{@code @Inherited} 在真实代理（如 CGLIB）下的场景。</p>
 *
 * <p><b>用例编号索引</b>：1 IP 白名单拦截（1.1-1.10），各测试方法 javadoc 标注其编号与说明。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按拦截器判断链路的各分支覆盖：handler 类型判定 → 注解识别（类/方法级）→ 白名单空/命中/未命中。</li>
 *   <li>通过内部 Controller 桩类标注/不标注 {@code @CInnerApi}，配合 {@code HandlerMethod} 驱动 {@code preHandle}，用 {@code MockHttpServletRequest}/{@code MockHttpServletResponse} 模拟请求与响应。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 {@code preHandle} 的约定：非内部接口放行、白名单空放行、命中放行、未命中返回 false 且写 403。</li>
 *   <li>依据边界/分支覆盖：非 HandlerMethod、无注解、类级注解、方法级注解、白名单空、命中（含 X-Forwarded-For 首段）、未命中。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：非 HandlerMethod 放行；无注解放行；类级/方法级 {@code @CInnerApi} 识别；{@code @Inherited} 父类类级继承；白名单空放行；命中白名单放行（含 X-Forwarded-For 首段命中）；X-Forwarded-For 首段未命中拒绝；未命中拒绝并响应 403。</li>
 *   <li>未覆盖：真实 Spring MVC 容器集成（由集成用例另行覆盖）、{@code @Inherited} 在真实代理(如 CGLIB)下的场景。</li>
 * </ul>
 * <h2>IP 白名单拦截</h2>
 * <ul>
 *   <li>1.1 非 HandlerMethod（如静态资源）：直接放行</li>
 *   <li>1.2 类/方法均无 {@code @CInnerApi}：放行</li>
 *   <li>1.3 标记 {@code @CInnerApi} 但白名单为空：放行</li>
 *   <li>1.4 方法级 {@code @CInnerApi} 命中白名单：放行</li>
 *   <li>1.5 类级 {@code @CInnerApi} 命中白名单：放行</li>
 *   <li>1.6 标记 {@code @CInnerApi} 未命中白名单：拒绝且响应 HTTP 403</li>
 *   <li>1.7 方法级 {@code @CInnerApi} 经 X-Forwarded-For 首段命中：放行</li>
 *   <li>1.8 方法级 {@code @CInnerApi} 的 X-Forwarded-For 首段未命中白名单：拒绝且响应 HTTP 403（仅信任首段）</li>
 *   <li>1.9 子类继承 {@code @CInnerApi} 标注父类类（@Inherited 类级继承）命中白名单：放行</li>
 *   <li>1.10 子类继承 {@code @CInnerApi} 标注父类类未命中白名单：拒绝且响应 HTTP 403</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/9
 * @version 1.0
 */
class CInnerApiInterceptorTests {

    /**
     * 无内部接口标记的普通 Controller 桩
     */
    static class OpenController {
        public String open() {
            return "open";
        }
    }

    /**
     * 方法级标记 @CInnerApi 的 Controller 桩
     */
    static class MethodAnnotatedController {
        @CInnerApi
        public String inner() {
            return "inner";
        }

        public String open() {
            return "open";
        }
    }

    /**
     * 类级标记 @CInnerApi 的 Controller 桩
     */
    @CInnerApi
    static class ClassAnnotatedController {
        public String any() {
            return "any";
        }
    }

    /**
     * 父类类级标记 @CInnerApi 的基类桩
     */
    @CInnerApi
    static class AnnotatedBaseController {
        public String any() {
            return "any";
        }
    }

    /**
     * 继承标注父类类的子类 Controller 桩（@Inherited 使类级注解被子类继承）
     */
    static class InheritChildController extends AnnotatedBaseController {
    }

    /**
     * 对应测试用例 1.1：非 HandlerMethod（如静态资源）：直接放行
     * 非 HandlerMethod（如静态资源）直接放行
     */
    @Test
    void preHandle_notHandlerMethod_pass() {
        CInnerApiConfig config = new CInnerApiConfig();
        config.setAllowedIps(CSet.of("192.168.1.0/24"));
        CInnerApiInterceptor interceptor = new CInnerApiInterceptor(config);

        boolean result = interceptor.preHandle(
            new MockHttpServletRequest(),
            new MockHttpServletResponse(),
            new Object()
        );

        Assertions.assertTrue(result);
    }

    /**
     * 对应测试用例 1.2：类/方法均无 {@code @CInnerApi}：放行
     * 类与方法均无 @CInnerApi 时放行
     */
    @Test
    void preHandle_noAnnotation_pass() throws Exception {
        CInnerApiConfig config = new CInnerApiConfig();
        config.setAllowedIps(CSet.of("192.168.1.0/24"));
        CInnerApiInterceptor interceptor = new CInnerApiInterceptor(config);

        Object bean = new OpenController();
        HandlerMethod handler = new HandlerMethod(bean, bean.getClass().getMethod("open"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("8.8.8.8");

        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), handler);

        Assertions.assertTrue(result);
    }

    /**
     * 对应测试用例 1.3：标记 {@code @CInnerApi} 但白名单为空：放行
     * 白名单为空（未配置 allowed-ips）时，即使标记 @CInnerApi 也全部放行
     */
    @Test
    void preHandle_annotated_butWhitelistEmpty_pass() throws Exception {
        CInnerApiConfig config = new CInnerApiConfig();
        config.setAllowedIps(CSet.of());
        CInnerApiInterceptor interceptor = new CInnerApiInterceptor(config);

        Object bean = new ClassAnnotatedController();
        HandlerMethod handler = new HandlerMethod(bean, bean.getClass().getMethod("any"));

        boolean result = interceptor.preHandle(
            new MockHttpServletRequest(),
            new MockHttpServletResponse(),
            handler
        );

        Assertions.assertTrue(result);
    }

    /**
     * 对应测试用例 1.4：方法级 {@code @CInnerApi} 命中白名单：放行
     * 方法级 @CInnerApi + 命中白名单放行
     */
    @Test
    void preHandle_methodAnnotated_hit_pass() throws Exception {
        CInnerApiConfig config = new CInnerApiConfig();
        config.setAllowedIps(CSet.of("192.168.1.0/24"));
        CInnerApiInterceptor interceptor = new CInnerApiInterceptor(config);

        Object bean = new MethodAnnotatedController();
        HandlerMethod handler = new HandlerMethod(bean, bean.getClass().getMethod("inner"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.5");

        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), handler);

        Assertions.assertTrue(result);
    }

    /**
     * 对应测试用例 1.5：类级 {@code @CInnerApi} 命中白名单：放行
     * 类级 @CInnerApi + 命中白名单放行
     */
    @Test
    void preHandle_classAnnotated_hit_pass() throws Exception {
        CInnerApiConfig config = new CInnerApiConfig();
        config.setAllowedIps(CSet.of("127.0.0.1", "192.168.1.0/24"));
        CInnerApiInterceptor interceptor = new CInnerApiInterceptor(config);

        Object bean = new ClassAnnotatedController();
        HandlerMethod handler = new HandlerMethod(bean, bean.getClass().getMethod("any"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.5");

        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), handler);

        Assertions.assertTrue(result);
    }

    /**
     * 对应测试用例 1.6：标记 {@code @CInnerApi} 未命中白名单：拒绝且响应 HTTP 403
     * 类级 @CInnerApi + 未命中白名单：拒绝并返回 403
     */
    @Test
    void preHandle_annotated_notHit_reject403() throws Exception {
        CInnerApiConfig config = new CInnerApiConfig();
        config.setAllowedIps(CSet.of("192.168.1.0/24"));
        CInnerApiInterceptor interceptor = new CInnerApiInterceptor(config);

        Object bean = new ClassAnnotatedController();
        HandlerMethod handler = new HandlerMethod(bean, bean.getClass().getMethod("any"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("8.8.8.8");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, handler);

        Assertions.assertFalse(result);
        Assertions.assertEquals(MockHttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }

    /**
     * 对应测试用例 1.7：方法级 {@code @CInnerApi} 经 X-Forwarded-For 首段命中：放行
     * 方法级 @CInnerApi 命中 X-Forwarded-For 首段地址：放行
     */
    @Test
    void preHandle_methodAnnotated_forwardedForHit_pass() throws Exception {
        CInnerApiConfig config = new CInnerApiConfig();
        config.setAllowedIps(CSet.of("10.0.0.0/8"));
        CInnerApiInterceptor interceptor = new CInnerApiInterceptor(config);

        Object bean = new MethodAnnotatedController();
        HandlerMethod handler = new HandlerMethod(bean, bean.getClass().getMethod("inner"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "10.1.2.3, 8.8.8.8");

        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), handler);

        Assertions.assertTrue(result);
    }

    /**
     * 对应测试用例 1.8：方法级 {@code @CInnerApi} 的 X-Forwarded-For 首段未命中白名单：拒绝且响应 HTTP 403（仅信任首段）
     * 方法级 @CInnerApi 的 X-Forwarded-For 多段，仅首段被信任：
     * 首段未命中白名单即拒绝（取首段语义，后续段不参与）
     */
    @Test
    void preHandle_methodAnnotated_forwardedForFirstNotHit_reject403() throws Exception {
        CInnerApiConfig config = new CInnerApiConfig();
        config.setAllowedIps(CSet.of("10.0.0.0/8"));
        CInnerApiInterceptor interceptor = new CInnerApiInterceptor(config);

        Object bean = new MethodAnnotatedController();
        HandlerMethod handler = new HandlerMethod(bean, bean.getClass().getMethod("inner"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        // 首段 8.8.8.8 未命中，虽后续 10.1.2.3 命中 10.0.0.0/8，仍应拒绝（信任首段）
        request.addHeader("X-Forwarded-For", "8.8.8.8, 10.1.2.3");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, handler);

        Assertions.assertFalse(result);
        Assertions.assertEquals(MockHttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }

    /**
     * 对应测试用例 1.9：子类继承 {@code @CInnerApi} 标注父类类（@Inherited 类级继承）命中白名单：放行
     * 子类继承 @CInnerApi 标注父类类（@Inherited 类级继承），命中白名单放行
     */
    @Test
    void preHandle_inheritedClassAnnotation_hit_pass() throws Exception {
        CInnerApiConfig config = new CInnerApiConfig();
        config.setAllowedIps(CSet.of("192.168.1.0/24"));
        CInnerApiInterceptor interceptor = new CInnerApiInterceptor(config);

        Object bean = new InheritChildController();
        // 方法声明于父类，bean 类型为子类；@Inherited 使子类 isAnnotationPresent 命中父类注解
        HandlerMethod handler = new HandlerMethod(
            bean,
            AnnotatedBaseController.class.getMethod("any")
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.5");

        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), handler);

        Assertions.assertTrue(result);
    }

    /**
     * 对应测试用例 1.10：子类继承 {@code @CInnerApi} 标注父类类未命中白名单：拒绝且响应 HTTP 403
     * 子类继承 @CInnerApi 标注父类类，未命中白名单拒绝(403)
     */
    @Test
    void preHandle_inheritedClassAnnotation_notHit_reject403() throws Exception {
        CInnerApiConfig config = new CInnerApiConfig();
        config.setAllowedIps(CSet.of("192.168.1.0/24"));
        CInnerApiInterceptor interceptor = new CInnerApiInterceptor(config);

        Object bean = new InheritChildController();
        HandlerMethod handler = new HandlerMethod(
            bean,
            AnnotatedBaseController.class.getMethod("any")
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("8.8.8.8");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, handler);

        Assertions.assertFalse(result);
        Assertions.assertEquals(MockHttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }

}
