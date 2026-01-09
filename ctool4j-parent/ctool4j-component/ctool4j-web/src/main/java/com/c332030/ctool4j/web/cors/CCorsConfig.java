package com.c332030.ctool4j.web.cors;

import com.c332030.ctool4j.core.util.CSet;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Set;

/**
 * <p>
 * Description: CORSConfig
 * </p>
 *
 * @author c332030
 * @since 2024/5/8
 */
@Data
@ConfigurationProperties("cors")
public class CCorsConfig {

    public static final String ALL = "*";

    Boolean enabled = false;

    Set<String> allowedOrigins = Collections.emptySet();

    Set<String> allowedMethods = CSet.of(ALL);

    Set<String> allowedHeaders = CSet.of(ALL);

}
