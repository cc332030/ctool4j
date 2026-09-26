package com.c332030.ctool4j.model;

import com.c332030.ctool4j.interfaces.ICHttpSession;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Enumeration;
import java.util.Objects;

/**
 * <p>
 * Description: CHttpSession
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CHttpSession} 是 {@link ICHttpSession} 在 <b>jakarta</b> 侧的落地（适配器）：
 * 持有 {@code jakarta.servlet.http.HttpSession}，把接口声明的方法逐个转发给它。</p>
 * <p>创建入口：{@link #of(HttpSession)}，由 {@code CHttpServletRequest#getSession} 调用。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>纯转发、不加工</b>：每个方法只把调用交给底层会话，契约、边界与返回值语义完全沿用
 *   {@link ICHttpSession}（见该接口文档），此处不重复描述、不做兜底。</li>
 *   <li><b>由档位选源目录</b>：本份承 jakarta 包（{@code src/main/java-jakarta}），与同模块的另一侧源码目录
 *   一份同包同名、互为镜像；由 JDK 档位选用其一，使用方按自身容器引入对应档位产物。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>创建时校验底层会话非空（{@link #of(HttpSession)} 的 {@code null} 入参快速失败），
 *   故实例化后的方法调用不会因底层会话缺失而抛 {@link NullPointerException}。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>运行在 jakarta（Spring Boot 2.x / Servlet 4.0）容器下、需要面向 {@link ICHttpSession} 编写公共代码的场景。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>不暴露容器专有能力（{@code getServletContext}、已弃用的 {@code getValue} 系列），需要时直接取底层会话。</li>
 *   <li>会话失效后调用方法由容器按规范抛 {@code IllegalStateException}，本类不拦截、不改变该语义。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.1
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CHttpSession implements ICHttpSession {

    private final HttpSession session;

    /**
     * 以底层 jakarta 会话创建适配器
     *
     * @param session 底层会话，不可为 null
     * @return 适配器实例
     * @throws NullPointerException 底层会话为 null 时
     */
    public static CHttpSession of(HttpSession session) {
        Objects.requireNonNull(session, "session");
        return new CHttpSession(session);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return session.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCreationTime() {
        return session.getCreationTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLastAccessedTime() {
        return session.getLastAccessedTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxInactiveInterval() {
        return session.getMaxInactiveInterval();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaxInactiveInterval(int interval) {
        session.setMaxInactiveInterval(interval);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAttribute(String name) {
        return session.getAttribute(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<String> getAttributeNames() {
        return session.getAttributeNames();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String name, Object value) {
        session.setAttribute(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAttribute(String name) {
        session.removeAttribute(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invalidate() {
        session.invalidate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNew() {
        return session.isNew();
    }

}
