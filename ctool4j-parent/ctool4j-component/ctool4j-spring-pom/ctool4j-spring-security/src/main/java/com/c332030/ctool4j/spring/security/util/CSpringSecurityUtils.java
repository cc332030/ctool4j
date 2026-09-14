package com.c332030.ctool4j.spring.security.util;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.web.util.CServletUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * Description: CSpringSecurityUtils
 * </p>
 *
 * <p>Spring Security 工具类：获取/设置当前安全上下文与认证信息，获取当前主体/用户详情，以及以 JSON 形式输出认证错误。</p>
 *
 * <p>说明：基于 {@code SecurityContextHolder} 的静态工具；{@code getUserDetails()} 在 principal 非 {@code UserDetails} 时
 * 会因强转抛 {@code ClassCastException}，空上下文返回 null。</p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CSpringSecurityUtils}（{@code @UtilityClass}）提供 Spring Security 的静态访问入口：</p>
 * <ul>
 *   <li>{@code getAuthentication()} / {@code setAuthentication(Authentication)}：获取 / 设置当前安全上下文的认证信息</li>
 *   <li>认证错误输出：以 JSON 形式输出认证失败信息（供未认证 / 拒绝访问处理器复用），可指定自定义文案</li>
 *   <li>常量 {@code ROLE_ANONYMOUS}：匿名权限名（取值与 Spring Security 匿名认证一致，供权限比对与构造复用）</li>
 *   <li>常量 {@code ANONYMOUS_AUTHORITIES}：匿名权限，实例取自权限常量池 {@code CGrantedAuthorityUtils}，为不可修改集合</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>空安全上下文 / 未认证</td>
 *     <td>getAuthentication / getPrincipal / getUserDetails 返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>主体非 UserDetails（如字符串）</td>
 *     <td>getUserDetails 抛 ClassCastException</td>
 *   </tr>
 *   <tr>
 *     <td>认证错误输出</td>
 *     <td>以统一 JSON 结构输出，不抛异常</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>过滤器、异常处理器、认证入口等无容器注入的静态场景读取当前用户。</li>
 *   <li>认证失败 / 拒绝访问时输出统一 JSON 错误响应。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要区分主体形态的场景请先用 {@code getPrincipal} 自行判断，避免 {@code getUserDetails} 强转失败。</li>
 *   <li>异步 / 线程池场景需自行传递安全上下文（{@code SecurityContextHolder} 默认仅线程内可见）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 {@code SecurityContextHolder} 的线程可见性，异步线程中不自动传递（需显式策略）。</li>
 *   <li>空上下文与「未认证」返回一致（均为 null），调用方无法从返回值区分两者。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>基于 SecurityContextHolder</b></p>
 * <ul>
 *   <li>数据来源为 {@code SecurityContextHolder}（线程上下文），不依赖 Spring 容器，可在静态工具（过滤器、处理器）中直接使用。</li>
 *   <li>空上下文（未认证）时获取类方法一律返回 null，不抛异常。</li>
 * </ul>
 * <p><b>主体形态</b></p>
 * <ul>
 *   <li>主体类型由认证方式决定：{@code UsernamePasswordAuthenticationToken} 常为字符串主体，{@code UserDetails} 认证为 {@code UserDetails} 主体。</li>
 * </ul>
 * <p><b>认证错误输出的文案口径</b></p>
 * <ul>
 *   <li>响应体为统一结构 {@code CStrResult}，业务码取 HTTP 状态码；{@code message} 为空时回退为状态码默认文案（{@code ReasonPhrase}）。</li>
 *   <li>文案只由入参决定：不拼接请求路径（requestURI），路径与查询串均不进入响应体（需要时由调用方日志 / 网关掌握），
 *   故本方法不依赖请求对象、调用方无需传请求。</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/1/23
 * @version 1.1
 */
@UtilityClass
public class CSpringSecurityUtils {

    /**
     * 匿名权限名（Spring Security 匿名认证的内置权限取值）
     * <p>Spring Security 未对外提供该常量：其 {@code AnonymousAuthenticationFilter} 为内联字面量
     * {@code AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")}，{@code AuthorityUtils} 亦只有构造方法、无角色常量，
     * 官方也没有角色枚举。故按「值随使用它的类型走」的方式，在本类（匿名权限的归属类型）声明常量，
     * 取值与官方实现保持一致，避免字面量在调用方散落</p>
     */
    public static final String ROLE_ANONYMOUS = "ROLE_ANONYMOUS";

    /**
     * 匿名权限（ROLE_ANONYMOUS），用于构造匿名认证信息
     * <p>实例取自权限常量池（{@link CGrantedAuthorityUtils}），与反序列化得到的同权限为同一实例；
     * 集合经 {@code CList.of} 构造，<b>不可修改</b>（共享常量，请勿 add/remove）</p>
     * <p>注意：本常量不可直接作为无 setter 的 getter 返回值——Jackson 的 {@code USE_GETTERS_AS_SETTERS}
     * 会把 JSON 数据合并进 getter 的返回值，共享集合会因此报错或被污染；需要默认值时返回副本
     * （见 {@code ICSecuritySession#getAuthorities()}）。</p>
     */
    public static final List<GrantedAuthority> ANONYMOUS_AUTHORITIES =
        CList.of(CGrantedAuthorityUtils.get(ROLE_ANONYMOUS));

    /**
     * 获取安全上下文
     *
     * @return 安全上下文
     */
    public SecurityContext getContext() {
        return SecurityContextHolder.getContext();
    }

    /**
     * 获取当前认证信息
     *
     * @return 当前认证信息
     */
    public Authentication getAuthentication() {
        return getContext().getAuthentication();
    }

    /**
     * 设置当前认证信息
     *
     * @param authentication 认证信息
     */
    public void setAuthentication(Authentication authentication) {
        getContext().setAuthentication(authentication);
    }

    /**
     * 获取当前主体
     * <ul>
     *   <li>{@code getPrincipal()}：获取当前主体（未认证或空上下文返回 null）</li>
     * </ul>
     *
     * @param <T> 主体类型
     * @return 当前主体；未认证时返回 null
     */
    public <T> T getPrincipal() {

        val authentication = getAuthentication();
        if(authentication == null) {
            return null;
        }

        return CObjUtils.anyType(authentication.getPrincipal());
    }

    /**
     * 获取当前用户详情
     *
     * @param <T> 用户详情类型
     * @return 当前用户详情
     */
    public <T extends UserDetails> T getUserDetails() {
        // 显式 (Object) 强转，避免泛型 T（擦除为 Object）匹配到 anyType(CSupplier) 重载
        return CObjUtils.anyType((Object) getPrincipal());
    }

    /**
     * 以 JSON 形式输出认证错误
     *
     * @param httpStatus HTTP 状态码
     * @param response   响应
     */
    public void writeJsonError(
        HttpStatus httpStatus,
        HttpServletResponse response
    ) {
        writeJsonError(httpStatus, null, response);
    }

    /**
     * 以 JSON 形式输出认证错误，可指定错误信息
     *
     * @param httpStatus HTTP 状态码
     * @param message    错误信息，为空时取状态码默认文案
     * @param response   响应
     */
    public void writeJsonError(
        HttpStatus httpStatus,
        String message,
        HttpServletResponse response
    ) {

        message = StrUtil.blankToDefault(message, httpStatus.getReasonPhrase());
        val forbiddenResult = CStrResult.error(
            String.valueOf(httpStatus.value()),
            message
        );

        CServletUtils.writeJson(response, httpStatus, forbiddenResult);
    }

}
