package com.c332030.ctool4j.web.util;

import com.c332030.ctool4j.core.util.CNumUtils;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpStatus;

import java.util.Optional;

/**
 * <p>
 * Description: CErrorUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CErrorUtils}（{@code @UtilityClass}）为统一错误页的<b>容器无关部分</b>：把错误状态码字符串
 * 解析为统一错误响应。</p>
 * <ul>
 *   <li>{@code errorResult(statusCodeStr)}：状态码字符串 → {@link CStrResult}（业务码取状态码数字、文案取状态描述）；
 *   为空或非法时兜底 500</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>为什么单独抽一层</b>：错误页控制器必须落在两侧适配模块（{@code @RequestMapping} 处理器方法的入参是
 *   {@code HttpServletRequest}，且 {@code RequestDispatcher.ERROR_*} 常量两侧取值不同）；
 *   但「状态码解析 + 兜底 + 组装返回体」不含任何容器类型——按项目规范（
 *   {@code agent/AGENTS-PROJECT.MD}「javax/jakarta 双栈」）无容器依赖的逻辑一律放 base，
 *   两侧同名控制器只保留取属性、记日志与包装。</li>
 *   <li><b>兜底归本层</b>：解析失败（直接访问错误页、状态码为空或非法）一律 {@code 500}，
 *   避免两侧各写一份判断分支造成口径漂移。</li>
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
 *     <td>状态码字符串为 null / 空 / 非法</td>
 *     <td>兜底 {@code HttpStatus.INTERNAL_SERVER_ERROR}（500）</td>
 *   </tr>
 *   <tr>
 *     <td>状态码合法但 Spring 无法解析（{@code HttpStatus.resolve} 返回 null）</td>
 *     <td>同样兜底 500</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>两侧 {@code CErrorController}（同名类）的统一错误响应组装。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要按业务异常映射错误码的场景不适用（本层只认 HTTP 状态码字符串）。</li>
 *   <li>需要写出响应（设状态码、写响应体）的场景不适用：本层只返回结果对象，写出由调用方（Spring MVC）完成。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>返回体结构固定为 {@code CStrResult<Void>}：与全项目统一返回体一致，前端无需为错误页单独分支。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
@UtilityClass
public class CErrorUtils {

    /**
     * 把错误状态码字符串解析为统一错误响应；为空或非法时兜底 500
     *
     * @param statusCodeStr 错误状态码字符串；可为空
     * @return 错误结果（业务码为状态码数字字符串，消息为状态描述）
     */
    public CStrResult<Void> errorResult(String statusCodeStr) {
        val httpStatus = Optional.ofNullable(CNumUtils.parseIntDefaultNull(statusCodeStr))
            .map(HttpStatus::resolve)
            .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
        return CStrResult.error(httpStatus);
    }

}
