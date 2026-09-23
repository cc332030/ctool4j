package com.c332030.ctool4j.web.cors;

import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.core.util.CSet;
import com.c332030.ctool4j.definition.constant.CConstants;
import com.c332030.ctool4j.spring.annotation.CConfigurationProperties;
import lombok.Data;
import org.springframework.http.HttpHeaders;

import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Description: CCorsConfig
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCorsConfig} 为跨域（CORS）配置属性类，{@code @CConfigurationProperties("cors")} + {@code @Data}，
 * 由 Spring Boot 绑定 {@code cors.*} 前缀的配置项。</p>
 * <p>属性：</p>
 * <ul>
 *   <li>{@code enable}：跨域全局开关，默认 {@code false}（不开启跨域处理）</li>
 *   <li>{@code origins}：<b>按域名</b>的跨域配置，key 为<b>纯 host</b>（如 {@code a.example.com}，
 *   含点号时须写成中括号 {@code cors.origins[a.example.com]}），value 为 {@link CCorsOriginConfig}；
 *   <b>未在其中的域名一律不放行</b></li>
 *   <li>{@code allowedMethods}：允许的请求方法<b>默认值</b>，默认 {@code CSet.of(CConstants.STAR)}（即 {@code *}，允许全部）</li>
 *   <li>{@code allowedHeaders}：额外允许的请求报文头<b>默认值</b>，默认含 {@code Authorization}、{@code Content-Type}</li>
 *   <li>（注释明确不支持 {@code application/json} 触发预检的场景，默认只支持</li>
 *   <li>{@code application/x-www-form-urlencoded}、{@code multipart/form-data}、{@code text/plain}）</li>
 *   <li>{@code exposedHeaders}：暴露给浏览器脚本可读的响应报文头<b>默认值</b>，默认仅 {@code Authorization}</li>
 *   <li>（浏览器脚本默认仅可读简单响应头，{@code Authorization} 需显式暴露）</li>
 * </ul>
 * <p>通配取值取 {@link CConstants#STAR}：表示允许全部来源、方法或头。</p>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>未配置 {@code origins}</td>
 *     <td>空 Map → 无域名被允许，跨域实际不生效</td>
 *   </tr>
 *   <tr>
 *     <td>域名在 {@code origins} 中但未配置 {@code enable}</td>
 *     <td>视为 {@code false}，该域名不放行</td>
 *   </tr>
 *   <tr>
 *     <td>域名级未配置 {@code allowedMethods} / {@code allowedHeaders} / {@code exposedHeaders}</td>
 *     <td>回落到本类的同名默认值</td>
 *   </tr>
 *   <tr>
 *     <td>{@code enable=false}</td>
 *     <td>跨域处理直接跳过</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需跨域时通过 {@code cors.enable=true} + {@code cors.origins.<域名>.enable=true} 开启并指定该域名的放行范围。</li>
 *   <li>不同域名要求不同（方法、请求头、凭据、暴露响应头）时，逐域名在 {@code cors.origins} 下配置。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要支持 {@code application/json} 跨域请求时，浏览器会先发 OPTIONS 预检，需在 allowedHeaders</li>
 *   <li>中补充相应头（本类注释明确该限制）。</li>
 *   <li>域名未列入 {@code origins} 时不做任何跨域处理（本类不做通配来源）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>来源白名单与域名配置合并为 {@code origins} 单一来源：域名在 {@code origins} 中且该域名 {@code enable=true}
 *   才放行——不再另设 {@code allowedOrigins}，避免「同一事实两处来源」（改一处必漏另一处）。</li>
 *   <li>域名级配置不写默认值，默认值只在本类兜底，保证"删掉域名级配置即回到默认"。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>配置绑定</b></p>
 * <ul>
 *   <li>使用 {@code @CConfigurationProperties("cors")} 前缀绑定，全局 {@code enable} 默认即不开启跨域。</li>
 *   <li>域名级配置绑定 {@code cors.origins.<域名>}，由 {@link CCorsOriginConfig} 承载。</li>
 *   <li>绑定开关（未知键、非法值的处置）由 {@link CConfigurationProperties} 给定，需要强校验时按其文档改用元注解。</li>
 * </ul>
 * <p><b>默认值集中</b></p>
 * <ul>
 *   <li>方法/请求头/响应头的默认值只在本类声明一次，域名级未配置时回落至此（取值与兜底见
 *   {@link com.c332030.ctool4j.web.cors.util.CCorsUtils}）。</li>
 * </ul>
 *
 * @author c332030
 * @since 2024/5/8
 * @version 1.3
 */
@Data
@CConfigurationProperties("cors")
public class CCorsConfig {

    Boolean enable = false;

    Set<String> allowedMethods = CSet.of(CConstants.STAR);

    /**
     * 跨域额外允许的请求报文头
     */
    Set<String> allowedHeaders = CSet.of(
        HttpHeaders.AUTHORIZATION
        // 不支持：application/json，默认只支持：application/x-www-form-urlencoded、multipart/form-data、text/plain
        , HttpHeaders.CONTENT_TYPE
    );

    /**
     * 跨域暴露给浏览器脚本的响应报文头
     * <p>默认浏览器脚本仅可读取简单响应头（Cache-Control、Content-Language、Content-Length、
     * Content-Type、Expires、Last-Modified、Pragma），{@code Authorization} 响应头需通过本配置显式暴露。</p>
     */
    Set<String> exposedHeaders = CSet.of(
        HttpHeaders.AUTHORIZATION
    );

    /**
     * 按域名的跨域配置，key 为纯 host（如 {@code a.example.com}）
     * <p>含点号的域名在配置文件中须写成中括号形式（{@code cors.origins[a.example.com]}），否则会被当作层级分隔符；
     * key 不得含端口（详见 {@link CCorsOriginConfig} 的类注释）。</p>
     * <p>未在其中的域名一律不放行；域名级未配置的项回落到本类同名默认值。</p>
     */
    Map<String, CCorsOriginConfig> origins = CMap.of();

}
