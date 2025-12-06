package com.c332030.ctool4j.feign.config;

import com.c332030.ctool4j.core.util.CSet;
import com.c332030.ctool4j.feign.enums.CFeignClientHeaderPropagationModeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

/**
 * <p>
 * Description: CFeignClientLogConfig
 * </p>
 *
 * @since 2025/9/21
 */
@Data
@ConfigurationProperties("feign.client.header")
public class CFeignClientHeaderConfig {

    /**
     * header 传播模式
     */
    CFeignClientHeaderPropagationModeEnum propagationMode = CFeignClientHeaderPropagationModeEnum.ALL;

    /**
     * 自定义传播 headers
     */
    Set<String> propagationCustomHeaders = CSet.of();

    /**
     * 传播的 request headers
     */
    Set<String> propagationRequestHeaders = CSet.of();

}
