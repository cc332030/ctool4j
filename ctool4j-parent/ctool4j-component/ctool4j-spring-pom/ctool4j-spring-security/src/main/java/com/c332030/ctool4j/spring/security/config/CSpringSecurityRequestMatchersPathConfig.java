package com.c332030.ctool4j.spring.security.config;

import com.c332030.ctool4j.core.util.CArrUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CSpringSecurityRequestMatchersPathConfig
 * </p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>配置允许访问的地址</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>放行路径配置</p>
 * <h2>不适用与边界场景</h2>
 * <p>实现 ICRequestMatchersConfig</p>
 *
 * @since 2026/1/24
 * @version 1.0
 */

@Data
@ConfigurationProperties("spring.security.request-matchers.path")
public class CSpringSecurityRequestMatchersPathConfig implements ICRequestMatchersConfig {

    /**
     * 允许的地址
     */
    String[] permits = CArrUtils.EMPTY_STR_ARR;

    /**
     * 禁止的地址
     */
    String[] denies = CArrUtils.EMPTY_STR_ARR;

}
