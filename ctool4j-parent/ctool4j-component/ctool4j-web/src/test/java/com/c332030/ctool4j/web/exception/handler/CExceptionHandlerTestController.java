package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.core.exception.CBusinessException;
import com.c332030.ctool4j.core.exception.CException;
import com.c332030.ctool4j.core.exception.CUnauthorizedException;
import com.c332030.ctool4j.definition.interfaces.ICRes;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;

/**
 * <p>
 * Description: CExceptionHandlerTestController：异常处理器命中矩阵的测试用 Controller
 * </p>
 *
 * <h2>功能说明</h2>
 * <p>为异常处理器集成用例提供真实接口入口：每个端点抛出（或自然触发）一类异常（含其子类），
 * 由容器内装配的异常处理器链路处理，用于断言"哪个处理器命中"这一对外可观测结果。</p>
 * <ul>
 *   <li>{@code GET /c-exception-handler/{type}}：抛出对应异常（子类端点抛子类实例）。</li>
 *   <li>{@code POST /c-exception-handler/not-valid}：{@code @Valid @RequestBody} 自然触发 {@code MethodArgumentNotValidException}。</li>
 *   <li>{@code GET /c-exception-handler/type-mismatch}：{@code @RequestParam Integer} 传非数字自然触发 {@code MethodArgumentTypeMismatchException}。</li>
 *   <li>{@code GET /c-exception-handler/missing-param}：缺必填 {@code @RequestParam} 自然触发 {@code MissingServletRequestParameterException}。</li>
 * </ul>
 * <h2>适用场景</h2>
 * <ul>
 *   <li>{@code CCExceptionHandlerTests}（通用处理器视角：不截走子类/更具体异常）与
 *   {@code CThrowableHandlerTests}（兜底处理器视角：不截走具体异常、只兜底未识别异常）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>测试辅助类；端点仅用于触发异常，不承载业务语义。</li>
 *   <li>子类以本类内静态内部类承载（仅子类命中用例需要，不代表业务模型）。</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/18
 * @version 1.1
 */
@RestController
public class CExceptionHandlerTestController {

    /** 业务异常用例的错误码（区别于默认 500，用于证明由业务处理器而非通用处理器命中） */
    private static final String BUSINESS_ERROR_CODE = "9999";

    /**
     * 抛业务异常（无具体错误码，走消息分支）
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/plain-exception")
    public String plainException() {
        throw new CTestException("普通项目异常");
    }

    /**
     * 抛业务异常（带错误码）
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/business")
    public String business() {
        throw new CBusinessException(error());
    }

    /**
     * 抛业务异常子类（带错误码）
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/business-sub")
    public String businessSub() {
        throw new CTestBusinessException(error());
    }

    /**
     * 抛未授权异常
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/unauthorized")
    public String unauthorized() {
        throw new CUnauthorizedException("未授权");
    }

    /**
     * 抛未授权异常子类
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/unauthorized-sub")
    public String unauthorizedSub() {
        throw new CTestUnauthorizedException("未授权子类");
    }

    /**
     * 抛客户端连接中断异常（处理器为 void，仅记录日志）
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/client-abort")
    public String clientAbort() throws ClientAbortException {
        throw new ClientAbortException("客户端中断");
    }

    /**
     * 抛请求体不可读异常
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/not-readable")
    public String notReadable() {
        throw new HttpMessageNotReadableException("请求体不可读");
    }

    /**
     * 抛请求体不可读异常子类
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/not-readable-sub")
    public String notReadableSub() {
        throw new CTestHttpMessageNotReadableException("请求体不可读子类");
    }

    /**
     * 抛响应体不可写异常（处理器为 void，仅记录日志）
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/not-writable")
    public String notWritable() {
        throw new HttpMessageNotWritableException("响应体不可写");
    }

    /**
     * 抛响应体不可写异常子类
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/not-writable-sub")
    public String notWritableSub() {
        throw new CTestHttpMessageNotWritableException("响应体不可写子类");
    }

    /**
     * 抛 HTTP 方法不支持异常
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/method-not-supported")
    public String methodNotSupported() throws HttpRequestMethodNotSupportedException {
        throw new HttpRequestMethodNotSupportedException("PUT");
    }

    /**
     * 抛 HTTP 方法不支持异常子类
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/method-not-supported-sub")
    public String methodNotSupportedSub() throws HttpRequestMethodNotSupportedException {
        throw new CTestHttpRequestMethodNotSupportedException("PATCH");
    }

    /**
     * 非法参数异常
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/illegal-argument")
    public String illegalArgument() {
        throw new IllegalArgumentException("非法参数");
    }

    /**
     * 数字格式异常（{@code IllegalArgumentException} 的 JDK 子类）
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/number-format")
    public String numberFormat() {
        throw new NumberFormatException("数字格式错误");
    }

    /**
     * 非法状态异常
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/illegal-state")
    public String illegalState() {
        throw new IllegalStateException("非法状态");
    }

    /**
     * 非法状态异常子类
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/illegal-state-sub")
    public String illegalStateSub() {
        throw new CTestIllegalStateException("非法状态子类");
    }

    /**
     * 缺必填参数异常（自然触发）
     *
     * @param name 必填参数（请求中不传）
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/missing-param")
    public String missingParam(@RequestParam String name) {
        throw new IllegalStateException("不会执行到此：name=" + name);
    }

    /**
     * 抛缺必填参数异常子类
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/missing-param-sub")
    public String missingParamSub() throws MissingServletRequestParameterException {
        throw new CTestMissingServletRequestParameterException("id", "String");
    }

    /**
     * 参数类型不匹配异常（自然触发：{@code id} 传非数字）
     * <p>该异常由转换失败产生，其 cause 为 {@code NumberFormatException}（{@code IllegalArgumentException} 子类），
     * Spring 在单个 advice 内无精确类型匹配时按 cause 回退匹配，故由 {@code CIllegalArgumentExceptionHandler} 命中
     * （消息取该异常的 message，即 cause 的消息）；{@code CMethodArgumentTypeMismatchExceptionHandler} 的
     * "参数类型不正确：X" 消息分支在含 IAE 系 cause 的场景下不可达，见 {@code ctool4j-web/README.md} 命中回退说明。</p>
     *
     * @param id 数字参数
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/type-mismatch")
    public String typeMismatch(@RequestParam Integer id) {
        throw new IllegalStateException("不会执行到此：id=" + id);
    }

    /**
     * 参数校验异常（自然触发：{@code @Valid} + {@code @NotNull}）
     *
     * @param dto 请求体
     * @return 不返回
     */
    @PostMapping("/c-exception-handler/not-valid")
    public String notValid(@Valid @RequestBody CNotValidDTO dto) {
        throw new IllegalStateException("不会执行到此：dto=" + dto);
    }

    /**
     * 未识别运行时异常（无具体处理器，由兜底处理器处理）
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/unknown-runtime")
    public String unknownRuntime() {
        throw new ArithmeticException("未识别运行时异常");
    }

    /**
     * 未识别错误（{@code Error}，无具体处理器，由兜底处理器处理）
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/unknown-error")
    public String unknownError() {
        throw new CTestError("未识别错误");
    }

    /**
     * 普通异常（{@code Exception} 本身，非 CException 体系，无具体处理器）
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/exception")
    public String exception() throws Exception {
        throw new Exception("普通异常");
    }

    /**
     * 普通检查异常子类（直接继承 {@code Exception}）
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/exception-sub")
    public String exceptionSub() throws Exception {
        throw new CTestCheckedException("普通检查异常子类");
    }

    /**
     * 普通运行时异常子类（直接继承 {@code RuntimeException}）
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/runtime-exception")
    public String runtimeException() {
        throw new CTestRuntimeException("普通运行时异常子类");
    }

    /**
     * JDK 检查异常（{@code IOException}）
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/io-exception")
    public String ioException() throws IOException {
        throw new IOException("IO 异常");
    }

    /**
     * JDK 错误子类（{@code AssertionError}）
     *
     * @return 不返回
     */
    @GetMapping("/c-exception-handler/assertion-error")
    public String assertionError() {
        throw new AssertionError("断言错误");
    }

    /**
     * 构造带错误码的业务错误（错误码区别于默认 500）
     *
     * @return 统一返回体错误码载体
     */
    private static ICRes<?> error() {
        return new CTestRes(BUSINESS_ERROR_CODE, "业务失败");
    }

    /**
     * 业务错误定义（仅测试用）：错误码区别于默认 500，用于证明由业务处理器命中
     */
    @Getter
    @RequiredArgsConstructor
    static class CTestRes implements ICRes<String> {

        private final String code;
        private final String msg;

    }

    /**
     * 参数校验用 DTO：{@code name} 必填
     */
    @Getter
    @Setter
    public static class CNotValidDTO {

        /**
         * 必填名称
         */
        @NotNull(message = "不能为空")
        private String name;
    }

    /**
     * 普通项目异常子类（无对应具体处理器，应由通用处理器处理）
     */
    public static class CTestException extends CException {

        /**
         * 构造
         *
         * @param message 消息
         */
        public CTestException(String message) {
            super(message);
        }
    }

    /**
     * 业务异常子类
     */
    public static class CTestBusinessException extends CBusinessException {

        /**
         * 构造
         *
         * @param error 错误
         */
        public CTestBusinessException(ICRes<?> error) {
            super(error);
        }
    }

    /**
     * 未授权异常子类
     */
    public static class CTestUnauthorizedException extends CUnauthorizedException {

        /**
         * 构造
         *
         * @param message 消息
         */
        public CTestUnauthorizedException(String message) {
            super(message);
        }
    }

    /**
     * 请求体不可读异常子类
     */
    public static class CTestHttpMessageNotReadableException extends HttpMessageNotReadableException {

        /**
         * 构造
         *
         * @param message 消息
         */
        public CTestHttpMessageNotReadableException(String message) {
            super(message);
        }
    }

    /**
     * 响应体不可写异常子类
     */
    public static class CTestHttpMessageNotWritableException extends HttpMessageNotWritableException {

        /**
         * 构造
         *
         * @param message 消息
         */
        public CTestHttpMessageNotWritableException(String message) {
            super(message);
        }
    }

    /**
     * HTTP 方法不支持异常子类
     */
    public static class CTestHttpRequestMethodNotSupportedException extends HttpRequestMethodNotSupportedException {

        /**
         * 构造
         *
         * @param method 不支持的 HTTP 方法
         */
        public CTestHttpRequestMethodNotSupportedException(String method) {
            super(method);
        }
    }

    /**
     * 非法状态异常子类
     */
    public static class CTestIllegalStateException extends IllegalStateException {

        /**
         * 构造
         *
         * @param message 消息
         */
        public CTestIllegalStateException(String message) {
            super(message);
        }
    }

    /**
     * 缺必填参数异常子类
     */
    public static class CTestMissingServletRequestParameterException extends MissingServletRequestParameterException {

        /**
         * 构造
         *
         * @param parameterName 参数名
         * @param parameterType 参数类型
         */
        public CTestMissingServletRequestParameterException(String parameterName, String parameterType) {
            super(parameterName, parameterType);
        }
    }

    /**
     * 未识别错误子类（{@code Error}，验证兜底处理器对 Error 的处理）
     */
    public static class CTestError extends Error {

        /**
         * 构造
         *
         * @param message 消息
         */
        public CTestError(String message) {
            super(message);
        }
    }

    /**
     * 普通检查异常子类（非 CException 体系，应由兜底处理器处理）
     */
    public static class CTestCheckedException extends Exception {

        /**
         * 构造
         *
         * @param message 消息
         */
        public CTestCheckedException(String message) {
            super(message);
        }
    }

    /**
     * 普通运行时异常子类（非 CException 体系，应由兜底处理器处理）
     */
    public static class CTestRuntimeException extends RuntimeException {

        /**
         * 构造
         *
         * @param message 消息
         */
        public CTestRuntimeException(String message) {
            super(message);
        }
    }

}
