package com.c332030.ctool4j.model;

import com.c332030.ctool4j.interfaces.ICCookie;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * Description: CCookie
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCookie} 是 {@link ICCookie} 的默认实现（可变值对象），承载两侧容器 Cookie 的公共字段。</p>
 * <ul>
 *   <li>{@code of(name, value)}：以名称与值快速创建</li>
 *   <li>链式访问器：{@code setPath} / {@code setDomain} / {@code setMaxAge} / {@code setSecure} /
 *   {@code setHttpOnly} / {@code setVersion} / {@code setComment}（返回 {@code this}）</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>实现类不依赖容器</b>：本类只有 JDK 字段，放在 base 层，两侧适配器各自把容器 {@code Cookie}
 *   的字段搬进来即可，映射逻辑同构、语义一致。</li>
 *   <li><b>链式访问器</b>：{@code @Accessors(chain = true)} 让 setter 返回 {@code this}，便于一行构造；
 *   接口 {@link ICCookie} 不声明 setter，故与"只读契约"不冲突。</li>
 *   <li><b>默认值与 Servlet 规范一致</b>：{@code maxAge = -1}（会话 Cookie）、{@code version = 0}，
 *   {@code path}/{@code domain}/{@code comment} 默认 {@code null}。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>无兜底：字段为空即返回 {@code null}，与 Servlet 规范一致。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>两侧适配器把容器 Cookie 映射为抽象层对象；业务代码在抽象层构造 Cookie 并写回响应。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>可变值对象、非线程安全；作为请求/响应处理过程中的临时载体使用即可。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
@Data
@Accessors(chain = true)
public class CCookie implements ICCookie {

    /**
     * 名称
     */
    private String name;

    /**
     * 值
     */
    private String value;

    /**
     * 作用路径
     */
    private String path;

    /**
     * 作用域
     */
    private String domain;

    /**
     * 最大存活秒数（默认 -1，即会话 Cookie）
     */
    private int maxAge = -1;

    /**
     * 是否仅 HTTPS 传输
     */
    private boolean secure;

    /**
     * 是否禁止脚本访问
     */
    private boolean httpOnly;

    /**
     * 协议版本（默认 0）
     */
    private int version;

    /**
     * 注释
     */
    private String comment;

    /**
     * 以名称与值创建 Cookie
     *
     * @param name  名称
     * @param value 值
     * @return Cookie 实例
     */
    public static CCookie of(String name, String value) {
        return new CCookie().setName(name).setValue(value);
    }

}
