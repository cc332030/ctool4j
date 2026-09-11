package com.c332030.ctool4j.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CPageConfig
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CPageConfig} 为分页配置类，标注 {@code @ConfigurationProperties("page")}，提供属性：</p>
 * <ul>
 *   <li>{@code defaultPageSize}：默认分页大小，默认 100</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>未配置 page.defaultPageSize</td>
 *     <td>使用默认 100</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>Spring Boot 分页默认大小配置注入。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需配合 {@code @EnableConfigurationProperties} 或组件扫描启用配置绑定。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>简单配置属性类，默认值保证未配置可用。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>配置绑定</b></p>
 * <ul>
 *   <li>通过 {@code @ConfigurationProperties("page")} 绑定 {@code page.defaultPageSize} 配置项。</li>
 *   <li>默认值 100，未配置时生效。</li>
 * </ul>
 *
 * @since 2025/12/9
 * @version 1.0
 */
@Data
@ConfigurationProperties("page")
public class CPageConfig {

    Integer defaultPageSize = 100;

}
