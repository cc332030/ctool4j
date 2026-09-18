package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.test.annotation.CTool4jSpringBootTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * <p>
 * Description: CThrowableHandlerTests
 * </p>
 *
 * <p>覆盖 CThrowableHandler.handle：兜底返回默认 500 与固定消息；并经真实 MVC 链路验证
 * <b>不截走任何有具体处理器的异常（含其子类）</b>，只兜底未识别异常（含 {@code Error}）。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>分类 1：直接实例化处理器调用 {@code handle}，验证兜底结果（错误码/消息）。</li>
 *   <li>分类 2：{@code @CTool4jSpringBootTest} + {@code @AutoConfigureMockMvc} 走真实接口（{@code CExceptionHandlerTestController}），
 *   对每类异常（及子类）断言"由对应具体处理器命中"的可观测结果——具体处理器的码/消息（或 {@code void} 处理器的空响应体），
 *   而非本兜底处理器的 {@code 未知异常}。</li>
 *   <li>分类 2 覆盖两类兜底：无具体处理器的运行时异常、{@code Error}（经容器包装后仍由 {@code Throwable} 兜底返回 200）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对未识别异常处理结果（错误码/消息）的约定。</li>
 *   <li>依据测试方法（正例/边界/分支覆盖）：兜底路径 + 各类具体异常的命中归属；子类覆盖用于验证处理器按类型层级匹配。</li>
 *   <li>依据 {@code @ExceptionHandler(Throwable.class)} 的匹配范围：非 {@code CException} 体系的一切异常与错误（其他 {@code Exception}/{@code Throwable} 子类）均无具体处理器，一律由本处理器兜底。</li>
 *   <li>Spring 的 advice 解析：多个 {@code @RestControllerAdvice} 间按 advice 顺序取首个能匹配的处理器，
 *   故"本处理器不先于具体处理器被命中"需以真实链路用例固化（根因与优先级约定见 {@code ctool4j-web/README.md}）。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：见下方编号索引。</li>
 *   <li>未覆盖：{@code MethodArgumentNotValidException} / {@code MethodArgumentTypeMismatchException} 的子类（其构造依赖容器内部对象，测试中无法构造）；
 *   {@code ClientAbortException} 为 final，无子类用例。</li>
 * </ul>
 * <h2>未识别异常处理</h2>
 * <ul>
 *   <li>1.1 handle：验证未识别异常处理结果</li>
 *   <li>1.2 handle(null)：异常对象为 null（非预期入参）返回固定「未知异常」、不抛 NPE（handle_nullThrowable）</li>
 * </ul>
 * <h2>真实链路：兜底与"不截走具体异常"（含子类）</h2>
 * <ul>
 *   <li>2.1 未识别运行时异常：本处理器兜底，code 500 + {@code 未知异常}（ArithmeticException）</li>
 *   <li>2.2 未识别错误：本处理器兜底，code 500 + {@code 未知异常}（Error）</li>
 *   <li>2.3 客户端连接中断：由 CClientAbortExceptionHandler 命中（void，空响应体）</li>
 *   <li>2.4 请求体不可读：由 CHttpMessageNotReadableExceptionHandler 命中</li>
 *   <li>2.5 请求体不可读子类：同上</li>
 *   <li>2.6 响应体不可写：由 CHttpMessageNotWritableExceptionHandler 命中（void，空响应体）</li>
 *   <li>2.7 响应体不可写子类：同上</li>
 *   <li>2.8 HTTP 方法不支持：由 CHttpRequestMethodNotSupportedExceptionHandler 命中</li>
 *   <li>2.9 HTTP 方法不支持子类：同上</li>
 *   <li>2.10 非法参数：由 CIllegalArgumentExceptionHandler 命中</li>
 *   <li>2.11 数字格式错误（IllegalArgumentException 的 JDK 子类）：同上</li>
 *   <li>2.12 非法状态：由 CIllegalStateExceptionHandler 命中</li>
 *   <li>2.13 非法状态子类：同上</li>
 *   <li>2.14 缺必填参数（自然触发）：由 CMissingServletRequestParameterExceptionHandler 命中</li>
 *   <li>2.15 缺必填参数子类：同上</li>
 *   <li>2.16 参数类型不匹配（自然触发）：由 CIllegalArgumentExceptionHandler 按 cause 命中（不回落到本处理器）</li>
 *   <li>2.17 参数校验失败（自然触发）：由 CMethodArgumentNotValidExceptionHandler 命中</li>
 *   <li>2.18 普通异常（Exception 本身，非 CException 体系）：本处理器兜底</li>
 *   <li>2.19 普通检查异常子类（extends Exception）：本处理器兜底</li>
 *   <li>2.20 普通运行时异常子类（extends RuntimeException）：本处理器兜底</li>
 *   <li>2.21 JDK 检查异常（IOException）：本处理器兜底</li>
 *   <li>2.22 JDK 错误子类（AssertionError）：本处理器兜底</li>
 * </ul>
 *
 * <p>`com.c332030.ctool4j.web.exception.handler.CThrowableHandler`（CThrowableHandler）的测试用例</p>
 *
 * @since 2026/8/16
 * @version 1.3
 */
@AutoConfigureMockMvc
@CTool4jSpringBootTest
public class CThrowableHandlerTests {

    private final CThrowableHandler handler = new CThrowableHandler();

    @Autowired
    private MockMvc mockMvc;

    /**
     * 对应测试用例 1.1：验证未识别异常处理结果
     */
    @Test
    public void handle() {
        CStrResult<Void> result = handler.handle(new RuntimeException("boom"));

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("未知异常", result.getMessage());
    }

    /**
     * 对应测试用例 1.2：异常对象为 null（非预期入参）返回固定「未知异常」、不抛 NPE
     * <p>Spring MVC 命中 {@code @ExceptionHandler} 时异常对象不为 null，该分支运行时不可达；
     * 用例固化"经容器显式传 null 也不 NPE"的兜底契约（并覆盖委托上传处理器分支前的 null 守卫；旧实现在此处 NPE）。</p>
     */
    @Test
    public void handle_nullThrowable() {
        // 边界：异常对象为 null
        CStrResult<Void> result = handler.handle(null);

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("未知异常", result.getMessage());
    }

    /**
     * 对应测试用例 2.1：未识别运行时异常由本处理器兜底（code 500 + 未知异常）
     */
    @Test
    public void unknownRuntimeException_fallbackByThisHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/unknown-runtime"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("500"))
            .andExpect(jsonPath("$.message").value("未知异常"));
    }

    /**
     * 对应测试用例 2.2：未识别错误（Error）经容器包装后仍由本处理器兜底返回 200 + 未知异常
     */
    @Test
    public void unknownError_fallbackByThisHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/unknown-error"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("500"))
            .andExpect(jsonPath("$.message").value("未知异常"));
    }

    /**
     * 对应测试用例 2.3：客户端连接中断由 CClientAbortExceptionHandler 命中（void → 空响应体）
     */
    @Test
    public void clientAbort_handledByClientAbortHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/client-abort"))
            .andExpect(status().isOk())
            .andExpect(content().string(""));
    }

    /**
     * 对应测试用例 2.4：请求体不可读由 CHttpMessageNotReadableExceptionHandler 命中
     */
    @Test
    public void notReadable_handledByNotReadableHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/not-readable"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("请求体缺失或格式不正确"));
    }

    /**
     * 对应测试用例 2.5：请求体不可读子类同样由 CHttpMessageNotReadableExceptionHandler 命中
     */
    @Test
    public void notReadableSubclass_handledByNotReadableHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/not-readable-sub"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("请求体缺失或格式不正确"));
    }

    /**
     * 对应测试用例 2.6：响应体不可写由 CHttpMessageNotWritableExceptionHandler 命中（void → 空响应体）
     */
    @Test
    public void notWritable_handledByNotWritableHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/not-writable"))
            .andExpect(status().isOk())
            .andExpect(content().string(""));
    }

    /**
     * 对应测试用例 2.7：响应体不可写子类同样由 CHttpMessageNotWritableExceptionHandler 命中（void → 空响应体）
     */
    @Test
    public void notWritableSubclass_handledByNotWritableHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/not-writable-sub"))
            .andExpect(status().isOk())
            .andExpect(content().string(""));
    }

    /**
     * 对应测试用例 2.8：HTTP 方法不支持由 CHttpRequestMethodNotSupportedExceptionHandler 命中（消息取异常消息）
     */
    @Test
    public void methodNotSupported_handledByNotSupportedHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/method-not-supported"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Request method 'PUT' not supported"));
    }

    /**
     * 对应测试用例 2.9：HTTP 方法不支持子类同样由 CHttpRequestMethodNotSupportedExceptionHandler 命中
     */
    @Test
    public void methodNotSupportedSubclass_handledByNotSupportedHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/method-not-supported-sub"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Request method 'PATCH' not supported"));
    }

    /**
     * 对应测试用例 2.10：非法参数由 CIllegalArgumentExceptionHandler 命中（消息取异常消息）
     */
    @Test
    public void illegalArgument_handledByIllegalArgumentHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/illegal-argument"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("非法参数"));
    }

    /**
     * 对应测试用例 2.11：数字格式异常（IllegalArgumentException 的 JDK 子类）同样由 CIllegalArgumentExceptionHandler 命中
     */
    @Test
    public void numberFormatSubclass_handledByIllegalArgumentHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/number-format"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("数字格式错误"));
    }

    /**
     * 对应测试用例 2.12：非法状态由 CIllegalStateExceptionHandler 命中（消息取异常消息）
     */
    @Test
    public void illegalState_handledByIllegalStateHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/illegal-state"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("非法状态"));
    }

    /**
     * 对应测试用例 2.13：非法状态子类同样由 CIllegalStateExceptionHandler 命中
     */
    @Test
    public void illegalStateSubclass_handledByIllegalStateHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/illegal-state-sub"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("非法状态子类"));
    }

    /**
     * 对应测试用例 2.14：缺必填参数（自然触发）由 CMissingServletRequestParameterExceptionHandler 命中
     */
    @Test
    public void missingParameter_handledByMissingParameterHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/missing-param"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("缺少必填参数：name"));
    }

    /**
     * 对应测试用例 2.15：缺必填参数子类同样由 CMissingServletRequestParameterExceptionHandler 命中
     */
    @Test
    public void missingParameterSubclass_handledByMissingParameterHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/missing-param-sub"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("缺少必填参数：id"));
    }

    /**
     * 对应测试用例 2.16：参数类型不匹配（自然触发）由 CIllegalArgumentExceptionHandler 按 cause 命中，不由本兜底处理器命中
     * <p>该异常由转换失败产生，cause 为 {@code NumberFormatException}（IAE 子类）：Spring 在单个 advice 内无精确类型匹配时
     * 按 cause 回退匹配，故消息取异常的 message（即 cause 消息），而非 {@code CMethodArgumentTypeMismatchExceptionHandler} 的
     * "参数类型不正确：X"（该分支在此类场景不可达，见 {@code ctool4j-web/README.md} 命中回退说明与待办）。</p>
     */
    @Test
    public void typeMismatch_handledByIllegalArgumentHandlerByCause() throws Exception {
        mockMvc.perform(get("/c-exception-handler/type-mismatch").param("id", "abc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("For input string: \"abc\""));
    }

    /**
     * 对应测试用例 2.17：参数校验失败（自然触发）由 CMethodArgumentNotValidExceptionHandler 命中（字段名 + 约束消息）
     */
    @Test
    public void notValid_handledByNotValidHandler() throws Exception {
        mockMvc.perform(post("/c-exception-handler/not-valid")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("name 不能为空"));
    }

    /**
     * 对应测试用例 2.18：普通异常（{@code Exception} 本身，非 CException 体系）无具体处理器，由本处理器兜底
     */
    @Test
    public void exception_fallbackByThisHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/exception"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("500"))
            .andExpect(jsonPath("$.message").value("未知异常"));
    }

    /**
     * 对应测试用例 2.19：普通检查异常子类（直接继承 {@code Exception}）由本处理器兜底
     */
    @Test
    public void checkedExceptionSubclass_fallbackByThisHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/exception-sub"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("500"))
            .andExpect(jsonPath("$.message").value("未知异常"));
    }

    /**
     * 对应测试用例 2.20：普通运行时异常子类（直接继承 {@code RuntimeException}）由本处理器兜底
     */
    @Test
    public void runtimeExceptionSubclass_fallbackByThisHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/runtime-exception"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("500"))
            .andExpect(jsonPath("$.message").value("未知异常"));
    }

    /**
     * 对应测试用例 2.21：JDK 检查异常（{@code IOException}）由本处理器兜底
     */
    @Test
    public void ioException_fallbackByThisHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/io-exception"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("500"))
            .andExpect(jsonPath("$.message").value("未知异常"));
    }

    /**
     * 对应测试用例 2.22：JDK 错误子类（{@code AssertionError}）经容器包装后仍由本处理器兜底（200 + 未知异常）
     */
    @Test
    public void assertionError_fallbackByThisHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/assertion-error"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("500"))
            .andExpect(jsonPath("$.message").value("未知异常"));
    }

}
