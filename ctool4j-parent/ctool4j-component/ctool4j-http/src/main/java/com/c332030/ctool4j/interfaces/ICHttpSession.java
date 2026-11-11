package com.c332030.ctool4j.interfaces;

import java.util.Enumeration;

/**
 * <p>
 * Description: ICHttpSession
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICHttpSession} 是会话的<b>容器无关</b>契约，方法按 Servlet 规范分组：</p>
 * <ul>
 *   <li><b>标识</b>：{@code getId}</li>
 *   <li><b>时限</b>：{@code getCreationTime} / {@code getLastAccessedTime} / {@code getMaxInactiveInterval} /
 *   {@code setMaxInactiveInterval}</li>
 *   <li><b>属性</b>：{@code getAttribute} / {@code getAttributeNames} / {@code setAttribute} / {@code removeAttribute}</li>
 *   <li><b>生命周期</b>：{@code invalidate}（失效）/ {@code isNew}（新建）</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>用包装类型消除跨包差异</b>：{@code javax.servlet.http.HttpSession} 与
 *   {@code jakarta.servlet.http.HttpSession} 是两套包里的<b>不同类</b>，无法出现在同一方法签名上；
 *   本接口把它们统一为自有类型，使用方（{@code CHttpRequest#getSession}）零 Servlet 依赖，切容器时调用方与 import 均不变。</li>
 *   <li><b>不含 {@code getServletContext}</b>：其返回类型指向应用级 {@code ServletContext}，与请求级抽象层的层次不符，
 *   不在纳入范围（需要应用级能力时应另立抽象，而非挂在会话上）。</li>
 *   <li><b>不含已弃用方法</b>：{@code getSessionContext} / {@code getValue} / {@code getValueNames} / {@code putValue} /
 *   {@code removeValue} 均为弃用 API，不进本接口。</li>
 *   <li><b>属性值为 {@code Object}</b>：沿用 Servlet 语义，不做类型约束、不做兜底；键为 {@code String}。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>本接口不提供兜底：语义与 Servlet 规范一致（属性不存在返回 {@code null}、无属性名时返回空 {@code Enumeration}）；
 *   失效后再调用方法由容器按规范抛 {@code IllegalStateException}，本接口不拦截、不改变该语义。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要在 javax / jakarta 两套容器间共用的会话读写代码：{@code CHttpRequest#getSession} 的返回值类型。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>无状态认证场景（如本项目 auth 的 "Authorization 头 → token → Redis"）不应使用容器会话，
 *   容器会话只在需要与容器或第三方组件共享会话状态时使用。</li>
 *   <li>需要会话 ID 来源判定（Cookie / URL 重写）时用 {@code CHttpRequest} 上的对应方法，不在本接口。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>两侧适配器需各自把容器 {@code HttpSession} 包装为本接口实现（类型不同，包装代码无法下沉 base），包装是纯转发、两侧同构。</li>
 *   <li>不提供"会话不存在时返回 {@code null}"语义的重载：是否需要创建由 {@code CHttpRequest#getSession(boolean)} 决定。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
public interface ICHttpSession {

    /**
     * 取会话标识
     *
     * @return 会话 ID
     */
    String getId();

    /**
     * 取创建时间（自纪元起的毫秒数）
     *
     * @return 创建时间
     */
    long getCreationTime();

    /**
     * 取最后访问时间（自纪元起的毫秒数）
     *
     * @return 最后访问时间
     */
    long getLastAccessedTime();

    /**
     * 取最大空闲间隔（秒）
     *
     * @return 秒数；不大于 0 表示永不失效
     */
    int getMaxInactiveInterval();

    /**
     * 设置最大空闲间隔（秒）
     *
     * @param interval 秒数；不大于 0 表示永不失效
     */
    void setMaxInactiveInterval(int interval);

    /**
     * 取会话属性
     *
     * @param name 属性名
     * @return 属性值；不存在时返回 null
     */
    Object getAttribute(String name);

    /**
     * 取全部会话属性名
     *
     * @return 属性名枚举；无属性时返回空枚举
     */
    Enumeration<String> getAttributeNames();

    /**
     * 设置会话属性
     *
     * @param name  属性名
     * @param value 属性值
     */
    void setAttribute(String name, Object value);

    /**
     * 移除会话属性
     *
     * @param name 属性名
     */
    void removeAttribute(String name);

    /**
     * 使会话失效
     */
    void invalidate();

    /**
     * 判断会话是否由本次请求新建
     *
     * @return true 表示新建
     */
    boolean isNew();

}
