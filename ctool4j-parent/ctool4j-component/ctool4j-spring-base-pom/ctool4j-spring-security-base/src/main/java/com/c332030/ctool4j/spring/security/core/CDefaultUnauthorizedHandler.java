package com.c332030.ctool4j.spring.security.core;

import com.c332030.ctool4j.interfaces.CHttpRequest;
import com.c332030.ctool4j.interfaces.CHttpResponse;
import com.c332030.ctool4j.spring.security.interfaces.CUnauthorizedHandler;
import com.c332030.ctool4j.spring.security.util.CSecurityResponseUtils;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

/**
 * <p>
 * Description: CDefaultUnauthorizedHandler
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CDefaultUnauthorizedHandler} 是 {@link CUnauthorizedHandler} 的<b>默认实现</b>：
 * 按认证异常类型生成提示文案，并以统一 JSON 结构输出 401。</p>
 * <ul>
 *   <li>{@code handle(request, response, authenticationException)}：判定文案 → 输出 401 JSON</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>逻辑放在抽象层</b>：文案判定与 401 写出与容器无关（只用 {@link CHttpRequest}/{@link CHttpResponse}），
 *   故放在 base 做<b>单一来源</b>，两侧适配器直接继承本类、不复制这段逻辑。</li>
 *   <li><b>文案口径</b>：{@code AuthenticationCredentialsNotFoundException} → "无有效登录用户"；
 *   {@code BadCredentialsException} → 取异常自带消息；其余异常 → 交由写出工具回退状态码默认文案
 *   （即返回 null，不自行编造文案）。</li>
 *   <li><b>不在此打日志</b>：base 模块不引入项目的日志设施；需要记录认证失败原因的调用方（两侧适配器）
 *   自行打日志后再委托本类。</li>
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
 *     <td>异常类型不在已知分支内</td>
 *     <td>文案返回 null，由 {@code CSecurityResponseUtils} 回退为 401 的标准文案</td>
 *   </tr>
 *   <tr>
 *     <td>{@code BadCredentialsException} 自身消息为空</td>
 *     <td>同上，回退标准文案</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>作为未认证处理器的默认实现被两侧适配器继承，或由使用方直接组合进自有实现。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要重定向登录页、写 Cookie 等容器专有行为的场景不适用（抽象层公共面不含这些能力）。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>文案为项目统一口径，不读取请求路径或查询串——避免把用户可控内容写进错误响应体。</li>
 *   <li>无状态、可复用：不含可变字段，可作为单例使用。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
public class CDefaultUnauthorizedHandler implements CUnauthorizedHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(
        CHttpRequest request,
        CHttpResponse response,
        AuthenticationException authenticationException
    ) {
        CSecurityResponseUtils.writeJsonError(
            HttpStatus.UNAUTHORIZED,
            resolveMessage(authenticationException),
            response
        );
    }

    /**
     * 按认证异常类型解析提示文案
     *
     * @param authenticationException 认证异常
     * @return 文案；无法判定时返回 null（由写出工具回退标准文案）
     */
    private String resolveMessage(AuthenticationException authenticationException) {

        if (authenticationException instanceof AuthenticationCredentialsNotFoundException) {
            return "无有效登录用户";
        }
        if (authenticationException instanceof BadCredentialsException) {
            return authenticationException.getMessage();
        }

        return null;

    }

}
