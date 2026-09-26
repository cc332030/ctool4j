package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.test.annotation.CTool4jSpringBootTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * <p>
 * Description: CExceptionHandlerTests
 * </p>
 *
 * <p>覆盖 CExceptionHandler 之内两条分支（上传超限识别、通用兜底）与它在真实 MVC 链路中的档位归属：直接实例化处理器调用
 * {@code handle}，并经真实接口（{@code CExceptionHandlerTestController}）断言「哪条分支承接了哪类异常」这一对外可观测结果。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>分类 1：直接实例化处理器，验证上传超限分支与通用兜底分支的返回值。</li>
 *   <li>分类 2：{@code @CTool4jSpringBootTest} + {@code @AutoConfigureMockMvc} 走真实接口，验证分支归属——
 *   上传超限（含按类名识别的容器私有类型）返回 413；未识别的 {@code Exception} 返回「未知异常」；
 *   {@code Error} 与更具体类型不被本处理器截走。</li>
 *   <li>档位与条件装配的约定值由 {@code CExceptionHandlerOrderTests} 固化，本类只验证可观测的命中归属。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计：本处理器按 {@code Exception} 承接非 {@code CException} 体系的通用异常，上传超限委托 {@code CFileUploadExceptionHandler}。</li>
 *   <li>依据测试方法（正例/分支覆盖）：识别分支命中与不命中、通用兜底；上传超限与普通异常各为正例。</li>
 *   <li>依据 Spring 的 advice 解析：单个 advice 内不允许两个异常类型相同的 {@code @ExceptionHandler} 方法（启动期报歧义），
 *   故识别与兜底合并为一个方法（见类 javadoc「设计要点」）。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：见下方编号索引。</li>
 *   <li>未覆盖：容器私有超限类型的真实类（Tomcat 的 {@code FileSizeLimitExceededException}）——测试用同名简单类的替身固化「按类名识别」这一判据。</li>
 * </ul>
 * <h2>直接调用分支</h2>
 * <ul>
 *   <li>1.1 handle：上传超限（Spring 类型）返回业务码 413 + 含上限的提示（uploadSizeExceeded_springType）</li>
 *   <li>1.2 handle：按简单类名识别的容器私有超限类型同样返回 413 + 泛化提示（uploadSizeExceeded_byClassName）</li>
 *   <li>1.3 handle：非超限异常走通用兜底，返回业务码 500 + 固定「未知异常」（handle）</li>
 *   <li>1.4 识别入口对 null 安全：CFileUploadExceptionHandler.isUploadSizeExceeded(null) 为 false（isUploadSizeExceeded_nullSafe）</li>
 *   <li>1.5 通用兜底文案与 CThrowableHandler 同源：两档共用 CThrowableHandler.UNKNOWN_MESSAGE（unknownMessage_sharedWithThrowableHandler）</li>
 * </ul>
 * <h2>真实链路：命中归属</h2>
 * <ul>
 *   <li>2.1 上传超限（Spring 类型）由本处理器识别分支承接（413 + 含上限提示）</li>
 *   <li>2.2 上传超限（容器私有类型名）由本处理器识别分支承接（413 + 泛化提示）</li>
 *   <li>2.3 未识别运行时异常由本处理器通用兜底分支承接（500 + 未知异常）</li>
 *   <li>2.4 普通检查异常子类由本处理器通用兜底分支承接</li>
 *   <li>2.5 {@code Exception} 本身由本处理器通用兜底分支承接</li>
 *   <li>2.6 JDK 检查异常（IOException）由本处理器通用兜底分支承接</li>
 *   <li>2.7 未识别错误（Error）不由本处理器承接，由 CThrowableHandler 兜底</li>
 *   <li>2.8 项目自身业务异常（code 9999）不被本处理器覆盖，由业务处理器承接</li>
 *   <li>2.9 未认证（响应体业务码 401）不被本处理器覆盖，由 CUnauthorizedExceptionHandler 承接</li>
 * </ul>
 *
 * <p>`CExceptionHandler` 的测试用例</p>
 *
 * @since 2026/9/21
 * @version 1.2
 */
@AutoConfigureMockMvc
@CTool4jSpringBootTest
public class CExceptionHandlerTests {

    private final CExceptionHandler handler = new CExceptionHandler();

    @Autowired
    private MockMvc mockMvc;

    /**
     * 对应测试用例 1.1：上传超限（Spring 类型）返回业务码 413 与含上限的提示
     */
    @Test
    public void uploadSizeExceeded_springType() {
        CStrResult<Void> result = handler.handle(new MaxUploadSizeExceededException(1048576L));

        Assertions.assertEquals("413", result.getCode());
        Assertions.assertEquals("文件大小超出限制（单个文件最大 1MB），请压缩后重试", result.getMessage());
    }

    /**
     * 对应测试用例 1.2：按简单类名识别的容器私有超限类型返回 413 与泛化提示（读不到上限）
     */
    @Test
    public void uploadSizeExceeded_byClassName() {
        CStrResult<Void> result = handler.handle(
            new CExceptionHandlerTestController.FileSizeLimitExceededException("exceeds")
        );

        Assertions.assertEquals("413", result.getCode());
        Assertions.assertEquals("文件大小超出限制，请压缩后重试", result.getMessage());
    }

    /**
     * 对应测试用例 1.3：非超限异常走通用兜底，返回业务码 500 与固定「未知异常」
     */
    @Test
    public void handle() {
        CStrResult<Void> result = handler.handle(new RuntimeException("boom"));

        Assertions.assertEquals("500", result.getCode());
        Assertions.assertEquals("未知异常", result.getMessage());
    }

    /**
     * 对应测试用例 1.4：识别入口对 null 安全——{@code isUploadSizeExceeded(null)} 为 false（按「非超限」判定）
     */
    @Test
    public void isUploadSizeExceeded_nullSafe() {
        Assertions.assertFalse(CFileUploadExceptionHandler.isUploadSizeExceeded(null));
    }

    /**
     * 对应测试用例 1.5：通用兜底的文案与 {@code CThrowableHandler} 共用一个常量（同一语义只有一处取值）
     * <p>本次把「未知异常」提为 {@code CThrowableHandler.UNKNOWN_MESSAGE} 供两档共用；用例固化两档取值同源，
     * 避免日后其中一档改文案、另一档漂移（两档的响应对外都是「未识别异常的兜底」）。</p>
     */
    @Test
    public void unknownMessage_sharedWithThrowableHandler() {

        CStrResult<Void> result = handler.handle(new RuntimeException("boom"));

        Assertions.assertEquals(CThrowableHandler.UNKNOWN_MESSAGE, result.getMessage());
    }

    /**
     * 对应测试用例 2.1：上传超限（Spring 类型）由本处理器识别分支承接（413 + 含上限提示）
     */
    @Test
    public void uploadSizeExceeded_handledByThisHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/upload-size-exceeded"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("413"))
            .andExpect(jsonPath("$.message").value("文件大小超出限制（单个文件最大 1MB），请压缩后重试"));
    }

    /**
     * 对应测试用例 2.2：上传超限（容器私有类型名）由本处理器识别分支承接（413 + 泛化提示）
     */
    @Test
    public void uploadSizeExceededByClassName_handledByThisHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/upload-size-exceeded-by-name"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("413"))
            .andExpect(jsonPath("$.message").value("文件大小超出限制，请压缩后重试"));
    }

    /**
     * 对应测试用例 2.3：未识别运行时异常由本处理器通用兜底分支承接
     */
    @Test
    public void unknownRuntimeException_fallbackByThisHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/unknown-runtime"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("500"))
            .andExpect(jsonPath("$.message").value("未知异常"));
    }

    /**
     * 对应测试用例 2.4：普通检查异常子类由本处理器通用兜底分支承接
     */
    @Test
    public void checkedExceptionSubclass_fallbackByThisHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/exception-sub"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("500"))
            .andExpect(jsonPath("$.message").value("未知异常"));
    }

    /**
     * 对应测试用例 2.5：{@code Exception} 本身由本处理器通用兜底分支承接
     */
    @Test
    public void exception_fallbackByThisHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/exception"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("500"))
            .andExpect(jsonPath("$.message").value("未知异常"));
    }

    /**
     * 对应测试用例 2.6：JDK 检查异常（{@code IOException}）由本处理器通用兜底分支承接
     */
    @Test
    public void ioException_fallbackByThisHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/io-exception"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("500"))
            .andExpect(jsonPath("$.message").value("未知异常"));
    }

    /**
     * 对应测试用例 2.7：未识别错误（{@code Error}）不由本处理器承接——{@code Error} 不属 {@code Exception}，由 CThrowableHandler 兜底
     */
    @Test
    public void unknownError_fallbackByThrowableHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/unknown-error"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("500"))
            .andExpect(jsonPath("$.message").value("未知异常"));
    }

    /**
     * 对应测试用例 2.8：项目自身业务异常不被本处理器覆盖（由业务处理器承接，错误码 9999）
     */
    @Test
    public void businessException_handledByBusinessHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/business"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("9999"))
            .andExpect(jsonPath("$.message").value("业务失败"));
    }

    /**
     * 对应测试用例 2.9：未认证（响应体业务码 401）不被本处理器覆盖（由 CUnauthorizedExceptionHandler 承接）
     */
    @Test
    public void unauthorizedException_handledByUnauthorizedHandler() throws Exception {
        mockMvc.perform(get("/c-exception-handler/unauthorized"))
            .andExpect(jsonPath("$.code").value("401"));
    }

}
