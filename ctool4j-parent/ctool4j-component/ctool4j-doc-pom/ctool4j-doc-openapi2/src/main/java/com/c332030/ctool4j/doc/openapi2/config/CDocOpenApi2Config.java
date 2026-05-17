package com.c332030.ctool4j.doc.openapi2.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CDocOpenApi2Config
 * </p>
 *
 * @since 2026/1/6
 */
@Data
@ConfigurationProperties("c-doc.openapi2")
public class CDocOpenApi2Config {

    /**
     * 路径映射，一般是 nginx 做了反向代理，knife4j 检测不到
     */
    String pathMapping = "/";

}
