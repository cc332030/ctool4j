package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
import lombok.CustomLog;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * Description: CExceptionHandler
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CExceptionHandler} 标注 {@code @RestControllerAdvice}，通过 {@code @ExceptionHandler(Exception.class)} 承接
 * <b>非 {@code CException} 体系</b>的通用异常（见 {@code CExceptionHandlerOrder} 的档位说明）：</p>
 * <ul>
 *   <li>文件上传超限（含按简单类名识别的容器私有类型）：返回业务码 413 与含上限的提示，判定与文案委托 {@code CFileUploadExceptionHandler}。</li>
 *   <li>其余未识别异常：返回业务码 500 与固定文案，日志以 {@code log.error} 记录堆栈。</li>
 *   <li>兜底优先级：{@code @Order(CExceptionHandlerOrder.EXCEPTION_FALLBACK)}（兜底区档位，见 {@code CExceptionHandlerOrder}）——位于
 *   {@code CException} 兜底档之后、{@code Throwable} 兜底档之前，故项目自身的一切具体类型处理器与 {@code CException} 处理器都先被咨询。</li>
 *   <li>通过 {@code @ConditionalOnMissingExceptionHandler(Exception.class)} 控制：容器存在其他同类型处理器时本处理器不生效。</li>
 *   <li>返回 {@code CStrResult&lt;Void&gt;} 错误结果（{@code CStrResult.error(...)}）。</li>
 *   <li>记录请求 URI 与异常堆栈（log），保证问题可追溯。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>上传超限的容器私有异常</td>
 *     <td>按简单类名识别后委托 {@code CFileUploadExceptionHandler}，返回业务码 413 + 泛化提示</td>
 *   </tr>
 *   <tr>
 *     <td>其余 {@code Exception} 子类（非 {@code CException}、非 {@code Error}）</td>
 *     <td>返回业务码 500 + 固定「未知异常」</td>
 *   </tr>
 *   <tr>
 *     <td>{@code Error} 及其子类（不属于 {@code Exception}）</td>
 *     <td>本处理器不匹配，由 {@code CThrowableHandler} 承接</td>
 *   </tr>
 *   <tr>
 *     <td>容器已存在同类型处理器</td>
 *     <td>@ConditionalOnMissingExceptionHandler 使本处理器不生效</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>Web 应用统一非 {@code CException} 体系异常的响应格式，使 {@code CThrowableHandler} 只保留 {@code Throwable} 兜底职责。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>{@code Error} 及其子类不在本处理器范围内（{@code Exception} 与 {@code Error} 无继承关系），仍由 {@code CThrowableHandler} 兜底。</li>
 *   <li>上传超限只按<b>简单类名</b>清单识别（见 {@code CFileUploadExceptionHandler#SIZE_EXCEEDED_SIMPLE_NAMES}），
 *   容器使用不同名类型时不命中该分支、走通用兜底。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 {@code CStrResult} 响应结构，与项目统一响应格式耦合。</li>
 *   <li>HTTP 语义缺失：通用兜底与上传超限均返回 200 + 业务码，无法按 5xx/413 触发告警（与同目录其余处理器一致）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>为什么是 {@code Exception} 而不是 {@code Throwable}</b></p>
 * <ul>
 *   <li>本处理器是「承接层」、{@code CThrowableHandler} 只保留「纯粹的返回」：{@code Exception} 覆盖一切业务可见的异常，
 *   {@code Error}（OOM、StackOverflow 等）留给最后一档，两档职责不重叠。</li>
 *   <li>若两档都用 {@code Throwable}，Spring 会因<b>同一 advice 内存在两个匹配精度相同的处理器</b>而在启动期抛
 *   {@code Ambiguous @ExceptionHandler method mapped}；用 {@code Exception} 划清边界同时也是让声明可判定的前提。</li>
 * </ul>
 * <p><b>为什么只留一个 {@code @ExceptionHandler(Exception.class)} 方法</b></p>
 * <ul>
 *   <li>同一 advice 内不允许两个 <b>异常类型相同</b> 的 {@code @ExceptionHandler} 方法（Spring 启动期即报歧义），
 *   故识别与兜底不能拆成两个同类型方法、也不能靠声明顺序区分。</li>
 *   <li>改用「声明更精确的类型」区分也走不通：声明 {@code Throwable} 的方法比 {@code Exception} 更宽泛，
 *   在方法解析里排在后面、永远不会被选中（最近匹配优先）。</li>
 *   <li>因此识别与兜底合并为一个方法，内部按需分流——这也是与同目录其他处理器一致的单方法形态。</li>
 * </ul>
 * <p><b>为什么委托上传处理器</b></p>
 * <ul>
 *   <li>文案与业务码由 {@code CFileUploadExceptionHandler#uploadSizeExceededResult} 单点提供，避免同一语义在多处漂移。</li>
 * </ul>
 *
 * @since 2026/9/21
 * @version 1.0
 */
@CustomLog
@Order(CExceptionHandlerOrder.EXCEPTION_FALLBACK)
@RestControllerAdvice
@ConditionalOnMissingExceptionHandler(Exception.class)
public class CExceptionHandler {

    /**
     * 处理非 {@code CException} 体系的异常：先识别上传超限，其余走通用兜底
     * <p>识别与兜底合并为一个方法：{@code @ExceptionHandler} 在同一个 advice 内不允许声明两个异常类型相同的方法
     * （Spring 启动期报 {@code Ambiguous @ExceptionHandler method mapped}），故不能拆成两个方法、也不能靠声明顺序区分
     * （见类 javadoc「设计要点」）。</p>
     *
     * @param e 异常（按本包 {@code @NonNullApi} 契约声明非空；Spring MVC 命中 {@code @ExceptionHandler} 时异常对象不为 null）
     * @return 错误结果（上传超限为业务码 413，其余为 500）
     */
    @ExceptionHandler(Exception.class)
    public CStrResult<Void> handle(Exception e) {

        // 上传超限的容器私有异常（Tomcat 等裸抛，如 FileSizeLimitExceededException）：按简单类名识别、委托上传处理器，
        // 给出业务码 413 + 明确提示；纯字符串比较，缺失该类也不报错。
        if (CFileUploadExceptionHandler.isUploadSizeExceeded(e)) {
            log.debug("handle upload size exceeded by class name，requestURI: {}",
                CRequestUtils.getRequestURIDefaultNull(), e);
            return CFileUploadExceptionHandler.uploadSizeExceededResult(e);
        }

        log.error("handle Exception，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e);
        return CStrResult.error(CThrowableHandler.UNKNOWN_MESSAGE);
    }

}
