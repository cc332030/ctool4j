package com.c332030.ctool4j.nacos.discovery.configuration;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Map;

/**
 * <p>
 * Description: FeignConfig
 * </p>
 *
 * @since 2024/12/9
 */
@Data
@ConfigurationProperties("feign.client.local-instance")
@ConditionalOnProperty(prefix = "feign.client.local-instance", value = "enabled", havingValue = "true")
public class CFeignLocalClientConfig {

    Map<String, String> urls = Collections.emptyMap();

}
