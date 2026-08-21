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
 * @author c332030
 * @since 2024/5/8
 * @see "doc/design/web/CCorsConfig.adoc"
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

}
