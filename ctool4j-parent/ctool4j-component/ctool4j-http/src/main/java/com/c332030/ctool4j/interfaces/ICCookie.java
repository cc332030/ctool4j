package com.c332030.ctool4j.interfaces;

/**
 * <p>
 * Description: ICCookie
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICCookie} 是 Cookie 的<b>容器无关</b>契约：字段与 Servlet 规范对齐，只暴露读取能力。</p>
 * <ul>
 *   <li>{@code getName} / {@code getValue}：名称与值</li>
 *   <li>{@code getPath} / {@code getDomain}：作用域</li>
 *   <li>{@code getMaxAge} / {@code isSecure} / {@code isHttpOnly}：生命周期与安全属性</li>
 *   <li>{@code getVersion} / {@code getComment}：协议版本与注释</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>用值对象消除跨包差异</b>：{@code javax.servlet.http.Cookie} 与 {@code jakarta.servlet.http.Cookie}
 *   是两套包里的<b>不同类</b>，无法出现在同一方法签名上；本接口把它们统一为自有类型，
 *   使用方（{@code CHttpRequest#getCookies}、{@code CHttpResponse#addCookie}）零 Servlet 依赖，
 *   切容器时调用方与 import 均不变。</li>
 *   <li><b>不声明 set 方法</b>：纯字段接口只暴露读取契约，写入由实现类
 *   （{@code com.c332030.ctool4j.model.CCookie} 的链式访问器）承担，避免 {@code void setter}
 *   挡掉实现类的链式访问器。</li>
 *   <li><b>语义沿用 Servlet 规范</b>：{@code maxAge} 未设置返回 {@code -1}、{@code version} 未设置返回 {@code 0}、
 *   {@code path}/{@code domain}/{@code comment} 未设置返回 {@code null}，本接口不新增语义、不做兜底。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>抽象层读写 Cookie 的场景：作为 {@code CHttpRequest#getCookies} 的元素类型与
 *   {@code CHttpResponse#addCookie} 的入参。</li>
 *   <li>需要在 javax / jakarta 两套容器间共用的 Cookie 处理代码。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要容器特有属性（jakarta 侧的 {@code getAttribute}、{@code SameSite} 等）时不适用，本接口只覆盖两边公共字段。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>两侧适配器需各自把容器 {@code Cookie} 映射为本接口实现（类型不同，映射代码无法下沉 base），
 *   映射是纯字段搬运、两侧同构。</li>
 *   <li>本类型是<b>值对象</b>，不承载"写回响应"的行为；写回由 {@code CHttpResponse#addCookie} 完成。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
public interface ICCookie {

    /**
     * 取 Cookie 名
     *
     * @return 名称
     */
    String getName();

    /**
     * 取 Cookie 值
     *
     * @return 值
     */
    String getValue();

    /**
     * 取作用路径
     *
     * @return 路径；未设置时返回 null
     */
    String getPath();

    /**
     * 取作用域
     *
     * @return 域名；未设置时返回 null
     */
    String getDomain();

    /**
     * 取最大存活秒数
     *
     * @return 秒数；未设置（会话 Cookie）时返回 -1
     */
    int getMaxAge();

    /**
     * 是否仅 HTTPS 传输
     *
     * @return true 表示仅 HTTPS
     */
    boolean isSecure();

    /**
     * 是否禁止脚本访问
     *
     * @return true 表示禁止脚本访问
     */
    boolean isHttpOnly();

    /**
     * 取协议版本
     *
     * @return 版本；未设置时返回 0
     */
    int getVersion();

    /**
     * 取注释
     *
     * @return 注释；未设置时返回 null
     */
    String getComment();

}
