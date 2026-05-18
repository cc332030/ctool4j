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

@Data
class ClientConfig {

    String url;

}
