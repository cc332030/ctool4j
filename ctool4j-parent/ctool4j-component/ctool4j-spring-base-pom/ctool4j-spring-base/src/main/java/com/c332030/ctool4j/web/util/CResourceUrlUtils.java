package com.c332030.ctool4j.web.util;

import com.c332030.ctool4j.interfaces.CHttpRequest;
import com.c332030.ctool4j.interfaces.CHttpResponse;
import com.c332030.ctool4j.web.constant.CResourceUrlConstants;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpStatus;

/**
 * <p>
 * Description: CResourceUrlUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CResourceUrlUtils}（{@code @UtilityClass}）为静态资源忽略的<b>容器无关部分</b>：判定请求 URI 是否命中
 * 忽略集合（{@link CResourceUrlConstants#IGNORE_RESOURCE_URLS}），命中则以 204 结束。</p>
 * <ul>
 *   <li>{@code handleAndContinue(request, response)}：命中原样返回 true（继续）；命中忽略集合记 debug 日志、
 *   置 204、返回 false（不继续）</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>为什么单独抽一层</b>：忽略过滤器必须落在两侧适配模块（{@code ICFilter} 的方法签名是容器类型）；
 *   但「查忽略集合 + 置状态码」只用抽象层公共面——按项目规范（
 *   {@code agent/AGENTS-PROJECT.MD}「javax/jakarta 双栈」）无容器依赖的逻辑一律放 base，
 *   两侧同名过滤器只保留包装与放行判定。</li>
 *   <li><b>状态码取 Spring 常量</b>：用 {@link HttpStatus#NO_CONTENT} 而非某一侧 Servlet 包的 {@code SC_*} 常量，
 *   与 {@code CCorsUtils} 的口径一致。</li>
 *   <li><b>入参为抽象层类型</b>：只接受 {@link CHttpRequest} / {@link CHttpResponse}，不对容器表态。</li>
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
 *     <td>URI 不命中忽略集合</td>
 *     <td>返回 true（调用方继续后续处理），不设置状态码</td>
 *   </tr>
 *   <tr>
 *     <td>URI 为 null</td>
 *     <td>{@code contains(null)} 不命中，按未命中处理（返回 true）</td>
 *   </tr>
 *   <tr>
 *     <td>命中忽略集合</td>
 *     <td>记 debug 日志 + 置 204 + 返回 false</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>两侧静态资源过滤器（同名类）的忽略判定；任何需要「按忽略集合决定是否继续」的接入点。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要忽略集合之外的自定义规则时须自行判定（本层只认常量里的集合）。</li>
 *   <li>需要读出响应体、写 Cookie 等容器专有能力时不适用（抽象层不暴露）。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>忽略集合为编译期常量快照，新增忽略资源须改 {@link CResourceUrlConstants}（本层不提供动态注册）。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
@CustomLog
@UtilityClass
public class CResourceUrlUtils {

    /**
     * 处理静态资源忽略：命中忽略集合时记 debug 日志并以 204 结束
     *
     * @param request  请求
     * @param response 响应
     * @return true 表示继续后续处理；false 表示命中忽略资源（已以 204 结束，调用方不应再放行）
     */
    public boolean handleAndContinue(CHttpRequest request, CHttpResponse response) {

        val requestURI = request.getRequestURI();
        if(!CResourceUrlConstants.IGNORE_RESOURCE_URLS.contains(requestURI)) {
            return true;
        }

        log.debug("ignore: {}", requestURI);
        response.setStatus(HttpStatus.NO_CONTENT.value());
        return false;

    }

}
