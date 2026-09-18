package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.core.util.CSet;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
import lombok.CustomLog;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.Set;

/**
 * <p>
 * Description: CFileUploadExceptionHandler
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CFileUploadExceptionHandler} 标注 {@code @RestControllerAdvice}，通过 {@code @ExceptionHandler} 处理文件上传（multipart）异常：</p>
 * <ul>
 *   <li>文件超限（{@code MaxUploadSizeExceededException}）：返回业务码 413 与<b>含上限的明确提示</b>（如"单个文件最大 10MB"），并提示压缩后重试。</li>
 *   <li>其他 multipart 解析失败（{@code MultipartException}）：返回业务码 400 与解析失败提示。</li>
 *   <li>容器私有超限异常（Tomcat 等<b>裸抛</b>、未经 Spring 包装）：由兜底处理器 {@code CThrowableHandler} 按<b>简单类名</b>识别后委托本处理器处理（见 {@link #isUploadSizeExceeded(Throwable)}）。</li>
 *   <li>异常按 {@code log.debug} 打印（含请求 URI 与堆栈），不把堆栈或原始异常文本返给调用方。</li>
 *   <li>通过 {@code @ConditionalOnMissingExceptionHandler(MultipartException.class)} 控制：容器存在其他同类型处理器时本处理器不生效。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>{@code MaxUploadSizeExceededException} 且上限未知（≤0）</td>
 *     <td>返回泛化提示"文件大小超出限制，请压缩后重试"</td>
 *   </tr>
 *   <tr>
 *     <td>其他 {@code MultipartException}</td>
 *     <td>返回业务码 400 + 上传解析失败提示</td>
 *   </tr>
 *   <tr>
 *     <td>裸抛的容器私有超限异常</td>
 *     <td>兜底处理器按简单类名识别后委托本处理器，返回业务码 413 + 泛化提示</td>
 *   </tr>
 *   <tr>
 *     <td>类名清单中的类型在运行环境不存在</td>
 *     <td>纯字符串比较，不解析类，<b>不报错</b></td>
 *   </tr>
 *   <tr>
 *     <td>容器已存在同类型处理器</td>
 *     <td>@ConditionalOnMissingExceptionHandler 使本处理器不生效</td>
 *   </tr>
 *   <tr>
 *     <td>异常对象为 null</td>
 *     <td>按泛化提示返回</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>Web 应用文件上传（multipart）异常的响应格式统一：超限给出可理解的提示，解析失败给出统一错误码。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>{@code @ExceptionHandler} 只声明 Spring 的公共类型（{@code MultipartException} 族），<b>不引用任何容器私有类</b>：
 *   Tomcat 的 {@code FileSizeLimitExceededException} 等在 Jetty/Undertow 下不存在，直接声明会导致类加载失败。</li>
 *   <li>容器私有超限类型按<b>简单类名</b>识别（清单见 {@link #SIZE_EXCEEDED_SIMPLE_NAMES}）：只覆盖同名类型；
 *   其他容器若使用不同名类型，需把其简单类名加入清单。</li>
 *   <li>裸抛的容器私有异常读不到上限（上限由 Spring 的 {@code MaxUploadSizeExceededException} 提供），故用泛化提示。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>{@code MaxUploadSizeExceededException} 继承 {@code MultipartException}，两个方法分别按"超限"与"其他解析失败"给出不同错误码与提示。</li>
 *   <li>提示文案与错误码由 {@link #uploadSizeExceededResult(Throwable)} 单点提供，兜底处理器委托调用，避免文案多处漂移。</li>
 *   <li>用 {@code CRequestUtils.getRequestURIDefaultNull()} 记录请求 URI 用于日志。</li>
 * </ul>
 *
 * @since 2026/9/18
 * @version 1.1
 */
@CustomLog
@Order(CExceptionHandlerOrder.CONCRETE)
@RestControllerAdvice
@ConditionalOnMissingExceptionHandler(MultipartException.class)
public class CFileUploadExceptionHandler {

    /**
     * 上传超限异常的<b>简单类名</b>清单（按字符串识别，不引用容器私有类：该类不存在也不报错）
     * <p>Spring：{@code MaxUploadSizeExceededException}；Tomcat：{@code FileSizeLimitExceededException}（单文件）、
     * {@code SizeLimitExceededException}（整个请求）；其他容器/客户端库使用同名类型时同样命中</p>
     */
    public static final Set<String> SIZE_EXCEEDED_SIMPLE_NAMES = CSet.of(
        "MaxUploadSizeExceededException",
        "FileSizeLimitExceededException",
        "SizeLimitExceededException"
    );

    /**
     * 异常 cause 链遍历的深度上限（防御自引用/环形 cause）
     */
    private static final int MAX_CAUSE_DEPTH = 32;

    /**
     * 1MB 的字节数
     */
    private static final long MEGABYTE = 1024L * 1024L;

    /**
     * 1KB 的字节数
     */
    private static final long KILOBYTE = 1024L;

    /**
     * 上限未知时的泛化提示
     */
    private static final String TOO_LARGE_MESSAGE = "文件大小超出限制，请压缩后重试";

    /**
     * 其他 multipart 解析失败提示
     */
    private static final String INVALID_MESSAGE = "文件上传解析失败，请重试";

    /**
     * 处理文件超限异常：返回 413 + 含上限的提示
     *
     * @param e 文件超限异常
     * @return 错误结果
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public CStrResult<Void> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {

        log.debug("handle MaxUploadSizeExceededException，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e);
        return uploadSizeExceededResult(e);
    }

    /**
     * 处理其他 multipart 异常（解析失败等）：返回 400 + 解析失败提示
     *
     * @param e multipart 异常
     * @return 错误结果
     */
    @ExceptionHandler(MultipartException.class)
    public CStrResult<Void> handleMultipart(MultipartException e) {

        log.debug("handle MultipartException，requestURI: {}", CRequestUtils.getRequestURIDefaultNull(), e);
        return CStrResult.error(HttpStatus.BAD_REQUEST, INVALID_MESSAGE);
    }

    /**
     * 上传超限的错误结果：上限可得时附上限，否则用泛化提示
     * <p>供 {@code CThrowableHandler} 在处理"裸抛的容器私有超限异常"时委托调用，保证文案与业务码单点提供</p>
     *
     * @param e 异常（可为 null）
     * @return 错误结果（业务码 413）
     */
    public static CStrResult<Void> uploadSizeExceededResult(Throwable e) {
        return CStrResult.error(HttpStatus.PAYLOAD_TOO_LARGE, tooLargeMessage(e));
    }

    /**
     * 判断异常（含 cause 链）是否为上传超限：按<b>简单类名</b>比较
     * <p>只做字符串比较、不解析也不依赖容器私有类，故在缺失该类的环境（如 Jetty 无 Tomcat 类）同样安全</p>
     *
     * @param e 异常（可为 null）
     * @return 是否为上传超限
     */
    public static boolean isUploadSizeExceeded(Throwable e) {

        Throwable current = e;
        for (int depth = 0; null != current && depth < MAX_CAUSE_DEPTH; depth++, current = current.getCause()) {
            if (SIZE_EXCEEDED_SIMPLE_NAMES.contains(current.getClass().getSimpleName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 组装超限提示：上限可得时附上（如"单个文件最大 10MB"），否则用泛化提示
     *
     * @param e 异常（可为 null）
     * @return 提示文案
     */
    private static String tooLargeMessage(Throwable e) {

        if (!(e instanceof MaxUploadSizeExceededException)) {
            return TOO_LARGE_MESSAGE;
        }

        long maxUploadSize = ((MaxUploadSizeExceededException) e).getMaxUploadSize();
        if (0 >= maxUploadSize) {
            return TOO_LARGE_MESSAGE;
        }

        return "文件大小超出限制（单个文件最大 " + formatSize(maxUploadSize) + "），请压缩后重试";
    }

    /**
     * 字节数转可读大小：≥1MB 用 MB、≥1KB 用 KB、否则用 B（取整展示）
     *
     * @param bytes 字节数
     * @return 可读大小（如 {@code 10MB}）
     */
    private static String formatSize(long bytes) {

        if (MEGABYTE <= bytes) {
            return bytes / MEGABYTE + "MB";
        }
        if (KILOBYTE <= bytes) {
            return bytes / KILOBYTE + "KB";
        }

        return bytes + "B";
    }

}
