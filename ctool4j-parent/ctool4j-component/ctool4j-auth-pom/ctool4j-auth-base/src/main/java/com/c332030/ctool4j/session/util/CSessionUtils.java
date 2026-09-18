package com.c332030.ctool4j.session.util;

import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.exception.CUnauthorizedException;
import com.c332030.ctool4j.session.interfaces.ICSession;
import com.c332030.ctool4j.session.service.CAbstractBaseSessionService;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * Description: CSessionUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CSessionUtils}（{@code @UtilityClass} + {@code @CAutowiredScan}）为会话读取的静态门面，转发到容器内的 {@link CAbstractBaseSessionService}：</p>
 * <ul>
 *   <li>{@link #load(HttpServletRequest)}：按请求加载会话（取请求内 token），无会话返回 null、不抛异常。</li>
 *   <li>{@link #getDefaultNull()}：取当前会话，无会话返回 null（{@code @Nullable}）。</li>
 *   <li>{@link #get()}：取当前会话，无会话抛 {@link CUnauthorizedException}。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>无会话 + 调 {@link #load(HttpServletRequest)} / {@link #getDefaultNull()}</td>
 *     <td>返回 null（由调用方决定后续处理，如保持未认证状态）</td>
 *   </tr>
 *   <tr>
 *     <td>无会话 + 调 {@link #get()}</td>
 *     <td>抛 {@link CUnauthorizedException}（与 {@link CAbstractBaseSessionService#get()} 同语义）</td>
 *   </tr>
 *   <tr>
 *     <td>会话服务未被注入（如未启用 {@code @CAutowiredScan}）</td>
 *     <td>空指针（快速失败，不静默返回 null）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>在过滤器、工具类或业务代码中读取会话、而不想在每个调用方注入会话服务的场景。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>泛型返回值按调用方声明的类型自动适配、<b>不做校验</b>（{@code CObjUtils.anyType} 直转）：
 *   声明类型与实际会话类型不符时，在使用点抛 {@code ClassCastException}。</li>
 *   <li>"无会话是否算异常"由调用哪个方法决定：{@link #load(HttpServletRequest)} / {@link #getDefaultNull()} 返回 null，
 *   {@link #get()} 抛异常——调用方按场景选择，本类不额外判定。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>静态注入（{@code @CAutowiredScan} + {@code @CAutowired}）与项目其他 C 工具类一致，避免调用方到处注入会话服务。</li>
 *   <li>方法名不再重复 {@code Session} 后缀（类名已表意），语义与其在 {@link CAbstractBaseSessionService} 上的对应方法一致，
 *   本类只做转发与泛型适配（含 {@code getDefaultNull} 的 {@code @Nullable} 传递）。</li>
 * </ul>
 *
 * @since 2026/9/18
 * @version 1.3
 */
@UtilityClass
@CAutowiredScan
public class CSessionUtils {

    /**
     * 会话服务（由容器按类型注入）
     */
    @Setter
    @CAutowired
    CAbstractBaseSessionService<? extends ICSession> sessionService;

    /**
     * 按请求加载会话（取请求内 token）
     *
     * @param request 当前请求
     * @param <T>     会话类型（由调用方声明，不做校验）
     * @return 会话；未携带 token / 解析失败 / 查不到会话时返回 null
     */
    public <T extends ICSession> T load(HttpServletRequest request) {
        return CObjUtils.anyType(sessionService.loadSession(request));
    }

    /**
     * 获取当前会话（未授权返回 null，不抛异常）
     *
     * @param <T> 会话类型（由调用方声明，不做校验）
     * @return 当前会话；未授权返回 null
     */
    @Nullable
    public <T extends ICSession> T getDefaultNull() {
        return CObjUtils.anyType(sessionService.getDefaultNull());
    }

    /**
     * 获取当前会话（未授权抛异常）
     *
     * @param <T> 会话类型（由调用方声明，不做校验）
     * @return 当前会话
     * @throws CUnauthorizedException 未授权（当前请求无有效会话）
     */
    public <T extends ICSession> T get() {
        return CObjUtils.anyType(sessionService.get());
    }

}
