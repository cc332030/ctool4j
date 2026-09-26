package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.core.exception.CException;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.test.annotation.CTool4jSpringBootTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * <p>
 * Description: CCExceptionHandlerTests
 * </p>
 *
 * <p>覆盖 CCExceptionHandler.handle：统一返回默认 500 与异常消息；并经真实 MVC 链路验证
 * <b>不截走 @ExceptionHandler 更具体的异常（含其子类）</b>——{@code CException} 的子类只要另有具体处理器，
 * 就由该具体处理器命中，本处理器只在无更具体处理器时兜底。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>分类 1：直接实例化处理器调用 {@code handle}，验证兜底结果（错误码/消息）。</li>
 *   <li>分类 2：{@code @CTool4jSpringBootTest} + {@code @AutoConfigureMockMvc} 走真实接口（{@code CExceptionHandlerTestController}）：
 *   无更具体处理器的项目异常由本处理器命中；业务异常、未授权异常（及其子类）由各自具体处理器命中——
 *   以"业务错误码 9999"、"业务码 401"这类本处理器不可能产出的结果作为判据。</li>
 *   <li>子类覆盖用于验证按类型层级匹配：父类有具体处理器时，子类异常同样归该具体处理器，不得回落到本处理器。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对通用异常处理结果（错误码/消息）的约定。</li>
 *   <li>依据测试方法（正例/边界/分支覆盖）：兜底路径 + 各类具体异常的命中归属。</li>
 *   <li>依据 Spring 的 advice 解析行为：多个 {@code @RestControllerAdvice} 间按 advice 顺序取首个能匹配的处理器
 *   （单个 advice 内才按异常类型精确度/深度比较），故"本处理器不先于具体处理器被命中"需以真实链路用例固化
 *   （根因与优先级约定见 {@code ctool4j-web/README.md}）。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：见下方编号索引。</li>
 *   <li>未覆盖：{@code MethodArgumentNotValidException} / {@code MethodArgumentTypeMismatchException} 的子类（其构造依赖容器内部对象，测试中无法构造）。</li>
 * </ul>
 * <h2>通用异常处理</h2>
 * <ul>
 *   <li>1.1 handle：验证通用异常处理结果</li>
 *   <li>1.2 异常消息为 null：用异常类型简单名兜底，避免 {@code message: null}（handle_nullMessage）</li>
 *   <li>1.3 异常对象为 null（非预期入参）：返回固定「通用异常」、不抛 NPE（handle_nullException）</li>
 * </ul>
 * <h2>真实链路：兜底与"不截走更具体异常（含子类）"</h2>
 * <ul>
 *   <li>2.1 无更具体处理器的项目异常：本处理器兜底，code 500（plainException_fallbackByThisHandler）</li>
 *   <li>2.2 业务异常（带错误码）：由 CCBusinessExceptionHandler 命中，code 取异常错误码 9999（businessException_handledByBusinessHandler）</li>
 *   <li>2.3 业务异常子类：同上（businessExceptionSubclass_handledByBusinessHandler）</li>
 *   <li>2.4 未授权异常：由 CUnauthorizedExceptionHandler 命中，业务码 401（unauthorizedException_handledByUnauthorizedHandler）</li>
 *   <li>2.5 未授权异常子类：同上（unauthorizedExceptionSubclass_handledByUnauthorizedHandler）</li>
 * </ul>
 *
 * <p>`CCExceptionHandler` 的测试用例</p>
 * <p>被测依赖类（异常 / 序列化器 / 日志 / 服务 / 切面 / 拦截器等）无 builder，测试按常规直接 new 构造——属规范允许的取舍，依据与边界在此记录。</p>
 *
 * @since 2026/8/16
 * @version 1.2
 */
@AutoConfigureMockMvc
@CTool4jSpringBootTest
public class CCExceptionHandlerTests {

    private final CCExceptionHandler handler = new CCExceptionHandler();

    @Autowired
    private MockMvc mockMvc;

    /**
     * 对应测试用例 1.1：验证通用异常处理结果
     */
    @Test
    public void handle() {
        CStrResult<Void> result = handler.handle(new CException("boom"));

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("boom", result.getMessage());
    }

    /**
     * 对应测试用例 1.2：异常消息为 null 时用异常类型简单名兜底（避免返回 {@code message: null}）
     */
    @Test
    public void handle_nullMessage() {
        // 边界：消息为 null（非空消息原样返回，见 1.1；此处只覆盖 null）
        CStrResult<Void> result = handler.handle(new CException((String) null));

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals(CException.class.getSimpleName(), result.getMessage());
    }

    /**
     * 对应测试用例 1.3：异常对象为 null（非预期入参）返回固定「通用异常」、不抛 NPE
     * <p>Spring MVC 命中 {@code @ExceptionHandler} 时异常对象不为 null，该分支运行时不可达；
     * 用例固化"经容器显式传 null 也不 NPE"的兜底契约（兜底不取异常内容，故 message 为固定文案）。</p>
     */
    @Test
    public void handle_nullException() {
        // 边界：异常对象为 null
        CStrResult<Void> result = handler.handle(null);

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("通用异常", result.getMessage());
    }

    /**
     * 对应测试用例 2.1：无更具体处理器的项目异常由本处理器兜底（code 500 + 异常消息）
     */
    @Test
    public void plainException_fallbackByThisHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/plain-exception"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("500"))
            .andExpect(jsonPath("$.message").value("普通项目异常"));
    }

    /**
     * 对应测试用例 2.2：业务异常由 CCBusinessExceptionHandler 命中——code 取异常错误码 9999，而非本处理器的 500
     */
    @Test
    public void businessException_handledByBusinessHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/business"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("9999"));
    }

    /**
     * 对应测试用例 2.3：业务异常子类同样由 CCBusinessExceptionHandler 命中（code 取 9999）
     */
    @Test
    public void businessExceptionSubclass_handledByBusinessHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/business-sub"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("9999"));
    }

    /**
     * 对应测试用例 2.4：未授权异常由 CUnauthorizedExceptionHandler 命中（业务码 401）
     */
    @Test
    public void unauthorizedException_handledByUnauthorizedHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/unauthorized"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("401"))
            .andExpect(jsonPath("$.message").value("未授权"));
    }

    /**
     * 对应测试用例 2.5：未授权异常子类同样由 CUnauthorizedExceptionHandler 命中（业务码 401）
     */
    @Test
    public void unauthorizedExceptionSubclass_handledByUnauthorizedHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/unauthorized-sub"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("401"))
            .andExpect(jsonPath("$.message").value("未授权子类"));
    }

}
