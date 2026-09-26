package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.core.exception.CUnauthorizedException;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.test.annotation.CTool4jSpringBootTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * <p>
 * Description: CUnauthorizedExceptionHandlerTests
 * </p>
 *
 * <p>覆盖 {@code CUnauthorizedExceptionHandler.handle}：业务码固定 401、消息取异常消息（为空回退 401 原因短语），
 * 不设置 HTTP 响应状态（未授权对外以响应体业务码 401 为准），且真实 MVC 链路中由本处理器而非通用处理器命中。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>直接实例化处理器调用 handle（与同目录其他处理器用例一致），另经 {@code @CTool4jSpringBootTest} + {@code @AutoConfigureMockMvc} 走真实接口。</li>
 *   <li>业务码随响应体断言；并断言方法未声明 {@code @ResponseStatus}——HTTP 状态不由注解设置（与模块内其余处理器同一口径，防回改）。</li>
 *   <li>真实链路用测试 Controller 抛 {@code CUnauthorizedException}，断言由本处理器命中（业务码 401）：容器内并存通用处理器
 *   {@code CCExceptionHandler}（处理基类 {@code CException}），Spring 按 advice 顺序取首个匹配、不跨 advice 比较精确度，故须固定优先级。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：有消息、无消息、HTTP 状态不由注解设置、真实 MVC 链路命中本处理器。</li>
 *   <li>未覆盖：容器装配条件（{@code @ConditionalOnMissingExceptionHandler} 语义由该注解自身用例覆盖）。</li>
 * </ul>
 * <h2>handle 分支输出</h2>
 * <ul>
 *   <li>1.1 消息非空：code "401" + 原消息（handle_withMessage）</li>
 *   <li>1.2 消息为空：message 回退 401 状态原因短语（handle_withoutMessage）</li>
 *   <li>1.3 未声明 HTTP 响应状态（handle_noResponseStatusAnnotation）</li>
 *   <li>1.4 真实 MVC 链路：由本处理器命中而非通用处理器，HTTP 200 + 业务码 401（handle_viaMvc_businessCode401）</li>
 *   <li>1.5 容器装配：本处理器与通用处理器同时注册（未见装配条件误禁用）（handler_and_genericHandlerBothRegistered）</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/17
 * @version 1.3
 * @see CUnauthorizedExceptionHandler
 * @see "doc/design/web/unauthorized-401.adoc"
 */
@AutoConfigureMockMvc
@CTool4jSpringBootTest
class CUnauthorizedExceptionHandlerTests {

    private final CUnauthorizedExceptionHandler handler = new CUnauthorizedExceptionHandler();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * <p>对应测试用例 1.1：消息非空时透传消息，业务码固定 401</p>
     */
    @Test
    void handle_withMessage() {
        CStrResult<Void> result = handler.handle(new CUnauthorizedException("未授权"));

        Assertions.assertEquals("401", result.getCode());
        Assertions.assertEquals("未授权", result.getMessage());
        Assertions.assertNull(result.getData());
    }

    /**
     * <p>对应测试用例 1.2：消息为空时回退 401 状态原因短语</p>
     */
    @Test
    void handle_withoutMessage() {
        CStrResult<Void> result = handler.handle(new CUnauthorizedException());

        Assertions.assertEquals("401", result.getCode());
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), result.getMessage());
    }

    /**
     * <p>对应测试用例 1.3：不声明 HTTP 响应状态——HTTP 状态保持容器默认（HTTP 200），未授权只由响应体业务码 401 表意</p>
     */
    @Test
    void handle_noResponseStatusAnnotation() throws NoSuchMethodException {
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(
            CUnauthorizedExceptionHandler.class.getMethod("handle", CUnauthorizedException.class),
            ResponseStatus.class);

        Assertions.assertNull(responseStatus, "不得声明 @ResponseStatus：HTTP 状态须与模块内其余处理器一致（HTTP 200 + 响应体业务码）");
    }

    /**
     * <p>对应测试用例 1.4：真实 MVC 链路中未授权异常由本处理器命中（而非通用处理器 {@code CCExceptionHandler}），
     * HTTP 状态 200、响应体业务码 401</p>
     */
    @Test
    void handle_viaMvc_businessCode401() throws Exception {

        mockMvc.perform(get("/c-exception-handler/unauthorized"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("401"))
            .andExpect(jsonPath("$.message").value("未授权"));
    }

    /**
     * <p>对应测试用例 1.5：容器中本处理器与通用处理器 {@code CCExceptionHandler} 同时注册——
     * 装配条件 {@code @ConditionalOnMissingExceptionHandler} 按精确类型判断，不会因基类处理器存在而禁用本处理器；
     * 二者并存时命中归属由 advice 优先级决定（见用例 1.4）</p>
     */
    @Test
    void handler_and_genericHandlerBothRegistered() {

        Assertions.assertNotNull(applicationContext.getBean(CUnauthorizedExceptionHandler.class),
            "专用处理器必须被装配：装配条件按精确类型判断，不受基类处理器影响");
        Assertions.assertNotNull(applicationContext.getBean(CCExceptionHandler.class),
            "通用处理器（基类 CException）同时在容器中");
    }

}
