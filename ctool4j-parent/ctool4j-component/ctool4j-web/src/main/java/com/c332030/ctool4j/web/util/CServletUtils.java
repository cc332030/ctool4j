package com.c332030.ctool4j.web.util;

import com.c332030.ctool4j.core.util.CCharsets;
import com.c332030.ctool4j.core.util.CJsonUtils;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: CServletUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CServletUtils}（{@code @UtilityClass}）为 Servlet 响应写出工具类。</p>
 * <p>核心方法：</p>
 * <p>{@code writeJson} 实现：</p>
 * <ul>
 *   <li>设置 {@code Content-Type: application/json}、UTF-8 字符编码</li>
 *   <li>设置 HTTP 状态码</li>
 *   <li>写入 JSON 体并 flush/close</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>序列化异常 / IO 异常</td>
 *     <td>被 {@code @SneakyThrows} 抛出（包装为运行时异常）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>过滤器、拦截器等非 Controller 场景需要直接写出 JSON 响应。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>{@code writer.close()} 在写出后立即关闭，调用方不应再写响应。</li>
 *   <li>依赖 {@code CJsonUtils.toJson} 序列化。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>统一 JSON 响应</b></p>
 * <ul>
 *   <li>便捷地以 JSON + 指定状态码写出响应，供过滤器/拦截器等场景复用。</li>
 * </ul>
 * <p><b>@SneakyThrows</b></p>
 * <ul>
 *   <li>用 {@code @SneakyThrows} 处理 IO 异常，调用方无需捕获。</li>
 * </ul>
 *
 * @since 2025/9/25
 * @version 1.0
 */
@UtilityClass
public class CServletUtils {

    /**
     * 以 JSON 形式写出响应，对象序列化为 JSON
     *
     * @param response   响应
     * @param httpStatus HTTP 状态码
     * @param body       响应体对象
     */
    @SneakyThrows
    public void writeJson(
        HttpServletResponse response,
        HttpStatus httpStatus,
        Object body
    ) {
        writeJson(response, httpStatus, CJsonUtils.toJson(body));
    }

    /**
     * 以 JSON 形式写出响应
     *
     * @param response   响应
     * @param httpStatus HTTP 状态码
     * @param jsonBody   JSON 字符串响应体
     */
    @SneakyThrows
    public void writeJson(
        HttpServletResponse response,
        HttpStatus httpStatus,
        String jsonBody
    ) {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(CCharsets.UTF_8.name());
        response.setStatus(httpStatus.value());

        val writer = response.getWriter();
        writer.write(jsonBody);
        writer.flush();
        writer.close();

    }

}
