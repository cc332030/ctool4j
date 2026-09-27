package com.c332030.ctool4j.spring.security.util;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.exception.CServletException;
import com.c332030.ctool4j.interfaces.CHttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 * Description: CSecurityResponseUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CSecurityResponseUtils}（{@code @UtilityClass}）为安全侧的错误响应写出工具：
 * 以抽象层响应 {@link CHttpResponse} 为出口，输出与全项目一致的 JSON 错误体。</p>
 * <ul>
 *   <li>{@code writeJsonError(httpStatus, response)}：按状态码默认文案输出</li>
 *   <li>{@code writeJsonError(httpStatus, message, response)}：自定义文案输出（为空时回退状态码默认文案）</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>写抽象层而非 Servlet</b>：原先的写出依赖具体容器的 {@code HttpServletResponse}，使安全处理器必须分
 *   javax/jakarta 两套；本工具只依赖 {@link CHttpResponse}，两侧适配器共用一份写出逻辑。</li>
 *   <li><b>响应体结构统一</b>：复用全项目的 {@link CStrResult}，业务码取 HTTP 状态码，与业务接口的错误体一致，
 *   前端无需为安全错误单独分支。</li>
 *   <li><b>文案只由入参决定</b>：不拼接请求路径与查询串（需要时由调用方日志或网关掌握），故本工具不依赖请求对象——
 *   与既有的 {@code CSpringSecurityUtils#writeJsonError} 口径一致。</li>
 *   <li><b>编码显式声明</b>：写出前设置 {@code application/json} 与 UTF-8，避免中文文案乱码。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>message 为空或空白</td>
 *     <td>回退为状态码默认文案（{@code ReasonPhrase}）</td>
 *   </tr>
 *   <tr>
 *     <td>序列化或写出失败</td>
 *     <td>包装为 {@link CServletException} 抛出，不静默吞掉</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>未认证（401）、访问被拒绝（403）等安全类错误的统一响应输出。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>响应已提交时写出会抛容器异常（{@code IllegalStateException} 一类），本工具不拦截、不改变该语义。</li>
 *   <li>需要输出非 JSON（HTML 登录页、重定向）的场景不适用。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>{@link ObjectMapper} 为工具内自持的静态实例（只读使用、线程安全），不参与 Spring 的 Jackson 定制——
 *   安全错误体结构固定，无需承载业务方的序列化定制。</li>
 *   <li>只设置状态码、内容类型与编码：不额外写业务性响应头（如需暴露响应头由 CORS 层负责）。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
@UtilityClass
public class CSecurityResponseUtils {

    /**
     * JSON 序列化器（只读使用，线程安全）
     */
    private final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 以 JSON 形式输出安全错误，文案取状态码默认值
     *
     * @param httpStatus HTTP 状态码
     * @param response   响应
     */
    public void writeJsonError(HttpStatus httpStatus, CHttpResponse response) {
        writeJsonError(httpStatus, null, response);
    }

    /**
     * 以 JSON 形式输出安全错误，可指定错误信息
     *
     * @param httpStatus HTTP 状态码
     * @param message    错误信息；为空或空白时取状态码默认文案
     * @param response   响应
     * @throws CServletException 序列化响应体或写出响应失败时
     */
    public void writeJsonError(HttpStatus httpStatus, String message, CHttpResponse response) {

        val text = StringUtils.hasText(message) ? message : httpStatus.getReasonPhrase();
        val errorResult = CStrResult.error(String.valueOf(httpStatus.value()), text);

        response.setStatus(httpStatus.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try {
            response.getWriter().write(OBJECT_MAPPER.writeValueAsString(errorResult));
        } catch (IOException e) {
            throw new CServletException(e);
        }

    }

}
