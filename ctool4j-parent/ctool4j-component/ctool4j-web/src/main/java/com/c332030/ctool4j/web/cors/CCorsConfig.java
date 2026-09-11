package com.c332030.ctool4j.web.cors;

import com.c332030.ctool4j.core.util.CSet;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;

import java.util.Collections;
import java.util.Set;

/**
 * <p>
 * Description: CCorsConfig
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCorsConfig} 为跨域（CORS）配置属性类，{@code @ConfigurationProperties("cors")} + {@code @Data}， 由 Spring Boot 绑定 {@code cors.*} 前缀的配置项。</p>
 * <p>属性：</p>
 * <ul>
 *   <li>{@code enable}：跨域开关，默认 {@code false}（不开启跨域处理）</li>
 *   <li>{@code allowedOrigins}：允许的来源集合，默认空集合</li>
 *   <li>{@code allowedMethods}：允许的请求方法，默认 {@code CSet.of(ALL)}（即 {@code *}，允许全部）</li>
 *   <li>{@code allowedHeaders}：额外允许的请求报文头，默认含 {@code Authorization}、{@code Content-Type}</li>
 *   <li>（注释明确不支持 {@code application/json} 触发预检的场景，默认只支持</li>
 *   <li>{@code application/x-www-form-urlencoded}、{@code multipart/form-data}、{@code text/plain}）</li>
 *   <li>{@code exposedHeaders}：跨域暴露给浏览器脚本可读的响应报文头，默认仅 {@code Authorization}</li>
 *   <li>（浏览器脚本默认仅可读简单响应头，{@code Authorization} 需显式暴露）</li>
 * </ul>
 * <p>常量 {@code ALL = "*"}：表示允许全部来源、方法或头。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>未配置 {@code allowedOrigins}</td>
 *     <td>空集合 → 无来源被允许，跨域实际不生效</td>
 *   </tr>
 *   <tr>
 *     <td>未配置 {@code allowedMethods}</td>
 *     <td>默认 {@code *} 全部允许</td>
 *   </tr>
 *   <tr>
 *     <td>{@code exposedHeaders} 为空</td>
 *     <td>不设置 {@code Access-Control-Expose-Headers}（浏览器脚本仅可读简单响应头）</td>
 *   </tr>
 *   <tr>
 *     <td>{@code enable=false}</td>
 *     <td>跨域处理直接跳过</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需跨域时通过 {@code cors.*} 配置开启并指定允许的来源/方法/头。</li>
 *   <li>前端脚本需读取 {@code Authorization} 等非简单响应头时，通过 {@code exposedHeaders} 显式暴露。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要支持 {@code application/json} 跨域请求时，浏览器会先发 OPTIONS 预检，需在 allowedHeaders</li>
 *   <li>中补充相应头（本类注释明确该限制）。</li>
 *   <li>{@code exposedHeaders} 若配置 {@code *} 且同时 {@code allowCredentials=true}，部分浏览器会拒绝（</li>
 *   <li>{@code Access-Control-Allow-Credentials} 不能与 {@code Access-Control-Expose-Headers: *} 共用通配）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>{@code allowedHeaders} 默认不含自定义头，跨域请求带自定义头时需显式配置，否则预检失败。</li>
 *   <li>{@code exposedHeaders} 默认仅暴露 {@code Authorization}；其它响应头需显式加入集合。</li>
 *   <li>{@code ALL} 常量与 Spring 的 {@code *} 通配语义对应。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>配置绑定</b></p>
 * <ul>
 *   <li>使用 {@code @ConfigurationProperties("cors")} 前缀绑定，默认值即不开启跨域。</li>
 * </ul>
 * <p><b>默认方法/头</b></p>
 * <ul>
 *   <li>方法默认全部允许；头默认白名单方式（Authorization/Content-Type），需要更多头时在配置中追加。</li>
 * </ul>
 *
 * @author c332030
 * @since 2024/5/8
 * @version 1.0
 */
@Data
@ConfigurationProperties("cors")
public class CCorsConfig {

    /**
     * 通配符，表示允许全部来源、方法或头
     */
    public static final String ALL = "*";

    Boolean enable = false;

    Set<String> allowedOrigins = Collections.emptySet();

    Set<String> allowedMethods = CSet.of(ALL);

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

}
