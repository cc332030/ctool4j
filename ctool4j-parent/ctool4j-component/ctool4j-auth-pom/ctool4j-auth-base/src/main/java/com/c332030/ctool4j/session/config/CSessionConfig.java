package com.c332030.ctool4j.session.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * <p>
 * Description: CSessionConfig
 * </p>
 *
 * <p>会话配置属性类，绑定 {@code session.*}。会话写入 Redis 时以 {@link #expire} 作为过期时间；
 * 全局唯一过期时间，不支持按会话类型/用户差异化。</p>
 *
 * @author c332030
 * @since 2026/9/10
 */
@Data
@ConfigurationProperties("session")
public class CSessionConfig {

    /**
     * token 过期时间，默认 16 小时（注意单位写法，如 30m、16h）
     */
    Duration expire = Duration.ofHours(16);

}
