package com.c332030.ctool4j.web.cors;

import com.c332030.ctool4j.definition.constant.CConstants;
import lombok.Data;

import java.util.Set;

/**
 * <p>
 * Description: CCorsOriginConfig
 * </p>
 *
 * <p>{@code CCorsOriginConfig} 为<b>单个域名</b>的跨域（CORS）配置，作为 {@code CCorsConfig.origins} 的 value，
 * 由 Spring Boot 绑定 {@code cors.origins[域名].*} 下的配置项。不同域名的要求差异较大（有的只需放行来源，
 * 有的还要带凭据、暴露响应头），故每个域名一条配置、每个操作一个独立开关。</p>
 *
 * <p><b>域名 key 的写法约束（易踩坑）</b>：key 为<b>纯 host</b>（如 {@code a.example.com}），含点号时
 * <b>必须用中括号形式</b> {@code cors.origins[a.example.com].enable} —— 点号形式
 * {@code cors.origins.a.example.com.enable} 会被 Spring 当作层级分隔符、绑不到 Map 的 key 上（实测 {@code origins} 为空）；
 * key <b>不得含端口</b>（{@code [a.example.com:8081]} 实测被规范化成 {@code cexamplecom}，既绑不上、
 * 不同域名之间还会互相覆盖）。请求侧带非默认端口时按纯 host 命中该配置，匹配口径见
 * {@link com.c332030.ctool4j.web.cors.util.CCorsUtils#getOriginConfig}。</p>
 *
 * <h2>能力目录</h2>
 * <p>属性（<b>均不在本类写默认值</b>，未配置时由 {@link com.c332030.ctool4j.web.cors.util.CCorsUtils} 在取值处兜底，
 * 避免"域名配置里写了默认值、删掉配置反而不生效"）：</p>
 * <ul>
 *   <li>{@code enable}：本域名跨域配置总开关，<b>必须显式配置为 {@code true}</b> 该域名才生效；未配置视为 {@code false}</li>
 *   <li>{@code allowedMethods}：本域名允许的请求方法，未配置取默认（{@code *} 允许全部）</li>
 *   <li>{@code allowedHeaders}：本域名额外允许的请求报文头，未配置取默认（{@code Authorization}、{@code Content-Type}）</li>
 *   <li>{@code credentials}：是否允许携带凭据（{@code Access-Control-Allow-Credentials}），<b>默认禁用</b>，需显式开启</li>
 *   <li>{@code exposeHeaders}：是否暴露响应报文头（{@code Access-Control-Expose-Headers}），<b>默认禁用</b>，需显式开启</li>
 *   <li>{@code exposedHeaders}：暴露给浏览器脚本的响应报文头集合，仅在 {@code exposeHeaders} 开启时生效，
 *   未配置取默认（{@code Authorization}）</li>
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
 *     <td>未配置 {@code enable}</td>
 *     <td>视为 {@code false}，该域名不做任何跨域处理</td>
 *   </tr>
 *   <tr>
 *     <td>未配置 {@code credentials}</td>
 *     <td>视为 {@code false}，不设置 {@code Access-Control-Allow-Credentials}</td>
 *   </tr>
 *   <tr>
 *     <td>未配置 {@code exposeHeaders}</td>
 *     <td>视为 {@code false}，不设置 {@code Access-Control-Expose-Headers}</td>
 *   </tr>
 *   <tr>
 *     <td>未配置 {@code allowedMethods} / {@code allowedHeaders} / {@code exposedHeaders}</td>
 *     <td>取默认值（方法 {@code *}；请求头 {@code Authorization}、{@code Content-Type}；响应头 {@code Authorization}）</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要按域名差异化放行方法、请求头、凭据与暴露响应头的跨域场景。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>{@code enable=false}（含未配置）时该域名其余配置项一律不生效——「配置了"不等于"启用了"（有意设计，
 *   避免删掉配置后仍残留行为）。</li>
 *   <li>{@code exposeHeaders=true} 但 {@code exposedHeaders} 为空集合时，等效于不暴露（不设置该响应头）。</li>
 *   <li>{@code exposedHeaders} 配 {@code *} 且同时开启 {@code credentials} 时，部分浏览器会拒绝
 *   （{@code Access-Control-Allow-Credentials} 不能与 {@code Access-Control-Expose-Headers: *} 共用通配）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>{@code enable} 为 {@link Boolean} 而非 {@code boolean}：保持"未配置"与"显式 false"可区分（配置绑定要求），
 *   对使用方而言两者等效为关闭。</li>
 *   <li>域名级字段一律不写默认值，默认值集中由处理逻辑兜底，保证"删掉配置即回到默认"。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>粒度</b></p>
 * <ul>
 *   <li>一个域名一条配置：新增域名只在 {@code cors.origins} 下追加一条，不影响其它域名。</li>
 * </ul>
 * <p><b>默认值落点</b></p>
 * <ul>
 *   <li>默认值不写在本类字段上：未配置即保持 {@code null}/空，由 {@link com.c332030.ctool4j.web.cors.util.CCorsUtils}
 *   在取值时统一兜底（默认值只在一处维护）。</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/18
 * @version 1.0
 */
@Data
public class CCorsOriginConfig {

    /**
     * 本域名跨域配置总开关
     * <p>必须显式配置为 {@code true} 本域名才生效；未配置视为 {@code false}（不写默认值，保持 null 与"显式 false"可区分）。</p>
     */
    Boolean enable;

    /**
     * 本域名允许的请求方法
     * <p>未配置时取默认值（含通配 {@link CConstants#STAR}，即允许全部）。</p>
     */
    Set<String> allowedMethods;

    /**
     * 本域名跨域额外允许的请求报文头
     * <p>未配置时取默认值（{@code Authorization}、{@code Content-Type}）。</p>
     */
    Set<String> allowedHeaders;

    /**
     * 是否允许携带凭据（{@code Access-Control-Allow-Credentials}）
     * <p>默认禁用：未配置视为 {@code false}，需显式配置为 {@code true} 才设置该响应头。</p>
     */
    Boolean credentials;

    /**
     * 是否暴露响应报文头（{@code Access-Control-Expose-Headers}）
     * <p>默认禁用：未配置视为 {@code false}，需显式配置为 {@code true} 才设置该响应头。</p>
     */
    Boolean exposeHeaders;

    /**
     * 暴露给浏览器脚本可读的响应报文头
     * <p>仅在 {@link #exposeHeaders} 开启时生效；未配置时取默认值（{@code Authorization}）。</p>
     */
    Set<String> exposedHeaders;

}
