package com.c332030.ctool4j.web.controller;

import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.model.CHttpServletRequest;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.util.CErrorUtils;
import lombok.CustomLog;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * Description: CErrorController
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CErrorController} 为统一错误处理 Controller，{@code @RestController} + {@code @ConditionalOnMissingBean(ErrorController.class)}， 容器中无其它 {@code ErrorController} 时生效，{@code @RequestMapping("/error")} 处理错误跳转。</p>
 * <p>核心方法 {@code error(HttpServletRequest request)}：</p>
 * <ul>
 *   <li>从 {@code CRequestUtils.getErrorStatusCode(request)} 取请求中携带的状态码</li>
 *   <li>存在异常属性（{@code RequestDispatcher.ERROR_EXCEPTION}）时记录 error 日志</li>
 *   <li>状态码解析失败（直接访问 {@code /error}、状态码为空/非法）时兜底 500（判定与组装见 {@link CErrorUtils}）</li>
 *   <li>返回 {@code CStrResult<Void>}（code 为状态码数字字符串，msg 为状态描述）</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>直接访问 {@code /error}（无 ERROR_STATUS_CODE）</td>
 *     <td>返回 500</td>
 *   </tr>
 *   <tr>
 *     <td>状态码非法/无法解析</td>
 *     <td>返回 500</td>
 *   </tr>
 *   <tr>
 *     <td>存在异常属性</td>
 *     <td>记录 error 日志（含堆栈）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>容器错误页统一重定向到 {@code /error} 时返回统一错误 JSON。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>业务方自定义 ErrorController 时不生效。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>返回结构固定为 {@code CStrResult&lt;Void&gt;}；状态码来自错误跳转携带的属性。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>条件装配</b></p>
 * <ul>
 *   <li>{@code @ConditionalOnMissingBean(ErrorController.class)}：业务方自定义 ErrorController 时本类不生效。</li>
 * </ul>
 * <p><b>只留容器相关部分</b></p>
 * <ul>
 *   <li>本类只做「取容器属性 + 包装为抽象层对象 + 记日志 + 委托」；状态码解析与兜底（含 500 兜底）
 *   收在 base 的 {@link CErrorUtils}，两侧不重复实现（见 {@code agent/AGENTS-PROJECT.MD} 的双栈规范：抽象层 + 两侧同名适配）。</li>
 * </ul>
 *
 * @since 2026/4/9
 * @version 1.1
 */
@CustomLog
@RestController
@ConditionalOnMissingBean(ErrorController.class)
public class CErrorController implements ErrorController {

    /**
     * 统一错误处理入口，按请求中携带的状态码返回错误结果
     *
     * @param request 请求
     * @return 错误结果
     */
    @RequestMapping("/error")
    public CStrResult<Void> error(HttpServletRequest request) {

        val statusCodeStr = CRequestUtils.getErrorStatusCode(CHttpServletRequest.of(request));
        val exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if(null != exception) {
            log.error("error with code: {}", statusCodeStr, exception);
        }

        // 状态码解析与兜底（直接访问 /error 时为空或非法 → 500）收在 base，两侧不重复实现
        return CErrorUtils.errorResult(statusCodeStr);
    }

}
