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
 * @author c332030
 * @since 2026/9/9
 * @see "doc/design/web/CInnerApiInterceptor.adoc"
  * <p>被测依赖类（异常 / 序列化器 / 日志 / 服务 / 切面 / 拦截器等）无 builder，测试按常规直接 new 构造——属规范允许的取舍，依据与边界在此记录。</p>
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
