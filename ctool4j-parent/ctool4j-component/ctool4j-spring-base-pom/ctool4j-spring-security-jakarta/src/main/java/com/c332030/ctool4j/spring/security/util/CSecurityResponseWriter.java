package com.c332030.ctool4j.spring.security.util;

import com.c332030.ctool4j.model.CHttpServletResponse;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: CSecurityResponseWriter
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CSecurityResponseWriter}（{@code @UtilityClass}）是安全侧错误响应的<b>jakarta 侧入口</b>：
 * 收 jakarta 的 {@link HttpServletResponse}，包装成抽象层响应后委托
 * {@link CSecurityResponseUtils} 完成写出。</p>
 * <ul>
 *   <li>{@code writeJsonError(httpStatus, response)}：按状态码默认文案输出</li>
 *   <li>{@code writeJsonError(httpStatus, message, response)}：自定义文案输出</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>只做适配，逻辑不重复</b>：响应体结构、状态码、编码与文案回退都在
 *   {@link CSecurityResponseUtils}（抽象层），本类只有「包装 + 委托」两行，jakarta/javax 两侧各一份同名类。</li>
 *   <li><b>从 {@code CSpringSecurityUtils} 抽出来</b>：那个类同时承载与容器无关的安全上下文能力与这段 servlet 写出，
 *   整体上移到两侧会反向依赖业务模块（它引用 {@code CGrantedAuthorityUtils}）；
 *   抽出后「与容器无关的留原地、带 Servlet 参数的落两侧」，各自只在需要的地方存在。</li>
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
 *     <td>由 {@code CSecurityResponseUtils} 回退状态码默认文案</td>
 *   </tr>
 *   <tr>
 *     <td>序列化或写出失败</td>
 *     <td>抛 {@code CServletException}，不静默吞掉</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>jakarta（Spring Boot 2.x / Servlet 4.0）工程中，过滤器、处理器等只有 jakarta 响应对象、
 *   却需要输出统一 JSON 错误体的场景。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>javax（Servlet 5.0+）工程须改用 javax 侧的同名类，否则参数类型不匹配。</li>
 *   <li>需要输出非 JSON（HTML、重定向）的场景不适用。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>不接收请求对象：错误体不拼接请求路径与查询串，避免把用户可控内容写进响应。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
@UtilityClass
public class CSecurityResponseWriter {

    /**
     * 以 JSON 形式输出安全错误，文案取状态码默认值
     *
     * @param httpStatus HTTP 状态码
     * @param response   jakarta 响应
     */
    public void writeJsonError(HttpStatus httpStatus, HttpServletResponse response) {
        writeJsonError(httpStatus, null, response);
    }

    /**
     * 以 JSON 形式输出安全错误，可指定错误信息
     *
     * @param httpStatus HTTP 状态码
     * @param message    错误信息；为空或空白时取状态码默认文案
     * @param response   jakarta 响应
     */
    public void writeJsonError(HttpStatus httpStatus, String message, HttpServletResponse response) {
        CSecurityResponseUtils.writeJsonError(httpStatus, message, CHttpServletResponse.of(response));
    }

}
