package com.c332030.ctool4j.feign.config;

import com.c332030.ctool4j.core.util.CMap;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * <p>
 * Description: CFeignConfig
 * </p>
 *
 * @see doc/design/feign/CFeignConfig.adoc
 * @since 2025/9/21
 */
@Data
@ConfigurationProperties(
    value = "feign",
    // 忽略格式不一样的字段
    ignoreInvalidFields = true
)
public class CFeignConfig {

    /**
     * 客户端信息
     */
    Map<String, ClientConfig> client = CMap.of();

}

/**
 * 客户端配置
 */
@Data
class ClientConfig {

    /**
     * 客户端地址
     */
    String url;

}
