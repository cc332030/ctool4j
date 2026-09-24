package com.c332030.ctool4j.spring.security.core;

import com.c332030.ctool4j.interfaces.CHttpRequest;
import com.c332030.ctool4j.interfaces.CHttpResponse;
import com.c332030.ctool4j.spring.security.interfaces.CForbiddenHandler;
import com.c332030.ctool4j.spring.security.util.CSecurityResponseUtils;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

/**
 * <p>
 * Description: CDefaultForbiddenHandler
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CDefaultForbiddenHandler} 是 {@link CForbiddenHandler} 的<b>默认实现</b>：
 * 以统一 JSON 结构输出 403。</p>
 * <ul>
 *   <li>{@code handle(request, response, accessDeniedException)}：输出 403 JSON</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>逻辑放在抽象层</b>：写出只用 {@link CHttpRequest}/{@link CHttpResponse}，与容器无关，
 *   故放在 base 做单一来源，两侧适配器直接委托本类、不复制这段逻辑。</li>
 *   <li><b>不暴露拒绝原因</b>：响应体只给状态码对应的标准文案，不回传被拒绝的资源或权限信息
 *   （访问拒绝属"已认证但无权限"，细节只进日志、不进响应）。</li>
 *   <li><b>不在此打日志</b>：base 模块只依赖 slf4j 之外的抽象层能力，日志由两侧适配器负责。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <p>响应体文案固定取 403 的标准描述，不随入参变化；写出失败按 {@code CSecurityResponseUtils} 的约定抛出。</p>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>作为访问拒绝处理器的默认实现被两侧适配器委托，或由使用方注入自定义实现替换。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要重定向、写 Cookie 或按资源粒度追加提示的场景不适用（抽象层公共面不含这些能力）。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>无状态、可复用：不含可变字段，可作为单例使用。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
public class CDefaultForbiddenHandler implements CForbiddenHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(
        CHttpRequest request,
        CHttpResponse response,
        AccessDeniedException accessDeniedException
    ) {
        CSecurityResponseUtils.writeJsonError(HttpStatus.FORBIDDEN, response);
    }

}
