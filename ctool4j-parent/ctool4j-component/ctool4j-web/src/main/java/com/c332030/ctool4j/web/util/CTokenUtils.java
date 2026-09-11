package com.c332030.ctool4j.web.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.validation.CAssert;
import com.c332030.ctool4j.definition.interfaces.ICToken;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: CTokenUtils
 * </p>
 *
 * <p>token 工具类，提供两类 token 读写能力：</p>
 * <ul>
 *   <li><b>请求/响应头</b>：{@link #TOKEN_PREFIX} 默认前缀 "Bearer"，{@link #removePrefix(String)} 移除前缀，
 *       {@link #getHeaderToken()} 系列从请求 Authorization 头取 token，{@link #setHeaderToken(String)} 系列设置响应 Authorization 头；</li>
 *   <li><b>请求属性</b>：{@link ICToken#TOKEN} 为属性名，{@link #getToken()}/{@link #setToken(HttpServletRequest, String)}
 *       读写当前请求属性中的 token，{@link #getTokenOrNew()} 缺省生成。</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>{@link #removePrefix(String)}：token 空白返回 null；长度不大于前缀或未以前缀开头时原样返回；否则去掉前缀。</li>
 *   <li>{@link #getHeaderToken(HttpServletRequest, String)}：request 为 null 返回 null；Authorization 头为空、
 *   未以前缀开头、长度不大于前缀时返回 null；否则返回前缀后的 token。</li>
 *   <li>{@link #setHeaderToken(String, String, HttpServletResponse)}：设置 {@code Authorization: {prefix} {token}} 响应头。</li>
 *   <li>请求属性：{@link #getToken()} 依赖当前请求上下文，非请求环境由 {@code CRequestUtils} 抛 {@link IllegalArgumentException}；{@link #getTokenOrNew()} 在无 token 时返回 UUID。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr><th>场景</th><th>兜底行为</th></tr>
 *   <tr><td>removePrefix token 空白</td><td>返回 null</td></tr>
 *   <tr><td>removePrefix 未以前缀开头</td><td>原样返回</td></tr>
 *   <tr><td>getHeaderToken request null / 头无效</td><td>返回 null</td></tr>
 *   <tr><td>getHeaderToken 长度等于前缀</td><td>返回 null</td></tr>
 *   <tr><td>getToken 非请求环境</td><td>抛 IllegalArgumentException</td></tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>适用：Bearer token 的请求解析与响应设置；请求属性中的 token 读写。</li>
 *   <li>不适用：依赖 jwt 与配置的 token 解析不在本类，见 ctool4j-auth-base 的 {@code CAuthUtils#getTokenByJwt}。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>前缀匹配大小写敏感；基于 Bearer 前缀约定。</li>
 *   <li>前缀与 token 之间按「跳过 1 个字符」截断（约定为空格）：无空格时会丢弃 1 个字符（如 {@code "Bearerabc"} → {@code "bc"}），
 *   仅前缀加空格时得到空串。</li>
 *   <li>前缀参数不额外兜底：{@link #getHeaderToken(HttpServletRequest, String)} 的 prefix 为 null 且请求头非空时抛 NPE。</li>
 * </ul>
 *
 * <p>注意：依赖当前请求上下文的方法（无参 {@code getHeaderToken}/{@code setHeaderToken}/{@code getToken}/{@code setToken(String)}/{@code getTokenOrNew}）
 * 在非请求线程调用时，因 {@code CRequestUtils} 取不到请求/响应而抛 {@link IllegalArgumentException}；
 * 接收 {@code request}/{@code response} 入参的重载不依赖上下文。{@link ICToken#TOKEN} 为请求属性名，与 jwt 载荷字段名的语义区分见
 * ctool4j-auth-base 的 {@code CAuthUtils#getTokenByJwt}。</p>
 *
 * @author c332030
 * @since 2026/3/16
 */
@CustomLog
@UtilityClass
public class CTokenUtils {

    /**
     * token 前缀
     */
    public final String TOKEN_PREFIX = "Bearer";

    // ---------- 请求/响应头 ----------

    /**
     * 移除前缀
     * @param token token
     * @return token
     */
    public String removePrefix(String token) {

        if(StrUtil.isBlank(token)) {
            return null;
        }

        if(token.length() <= TOKEN_PREFIX.length() ||
            !token.startsWith(TOKEN_PREFIX)) {
            return token;
        }

        return token.substring(TOKEN_PREFIX.length() + 1);
    }

    /**
     * 获取请求头 token
     * @return token
     */
    public String getHeaderToken() {
        return getHeaderToken(TOKEN_PREFIX);
    }

    /**
     * 获取请求头 token
     * @param prefix 前缀
     * @return token
     */
    public String getHeaderToken(String prefix) {
        return getHeaderToken(CRequestUtils.getRequest(), prefix);
    }

    /**
     * 获取请求头 token
     * @param request 请求
     * @return token
     */
    public String getHeaderToken(HttpServletRequest request) {
        return getHeaderToken(request, TOKEN_PREFIX);
    }

    /**
     * 获取请求头 token
     * @param request 请求
     * @param prefix 前缀
     * @return token
     */
    public String getHeaderToken(HttpServletRequest request, String prefix) {

        if(null == request) {
            return null;
        }

        val authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(StrUtil.isEmpty(authorization)
            || !authorization.startsWith(prefix)
            ||  authorization.length() <= prefix.length()
        ) {
            return null;
        }

        return authorization.substring(prefix.length() + 1);

    }

    /**
     * 设置响应头 token
     * @param token token
     */
    public void setHeaderToken(String token) {
        setHeaderToken(token, TOKEN_PREFIX);
    }

    /**
     * 设置响应头 token
     * @param token token
     * @param prefix 前缀
     */
    public void setHeaderToken(String token, String prefix) {
        setHeaderToken(token, prefix, CRequestUtils.getResponse());
    }

    /**
     * 设置响应头 token
     * @param token token
     * @param response 响应
     */
    public void setHeaderToken(String token, HttpServletResponse response) {
        setHeaderToken(token, TOKEN_PREFIX, response);
    }

    /**
     * 设置响应头 token
     * @param token token
     * @param prefix 前缀
     * @param response 响应
     */
    public void setHeaderToken(String token, String prefix, HttpServletResponse response) {
        response.setHeader(HttpHeaders.AUTHORIZATION, prefix + " " + token);
    }

    // ---------- 请求属性 ----------

    /**
     * 将 token 写入当前请求属性
     *
     * @param token token
     */
    public void setToken(String token) {
        setToken(CRequestUtils.getRequest(), token);
    }

    /**
     * 将 token 写入指定请求属性
     *
     * @param request 请求
     * @param token   token
     */
    public void setToken(HttpServletRequest request, String token) {
        request.setAttribute(ICToken.TOKEN, token);
    }

    /**
     * 读取当前请求属性中的 token
     *
     * @return token；非请求环境由 {@code CRequestUtils.getRequest()} 抛 {@link IllegalArgumentException}
     */
    public String getToken() {

        val request = CRequestUtils.getRequest();
        CAssert.notNull(request, "非请求环境");
        return (String) request.getAttribute(ICToken.TOKEN);
    }

    /**
     * 读取 token，不存在则生成新 token
     *
     * @return token
     */
    public String getTokenOrNew() {
        val token = getToken();
        if(StrUtil.isNotEmpty(token)) {
            return token;
        }
        return IdUtil.fastUUID();
    }

}
