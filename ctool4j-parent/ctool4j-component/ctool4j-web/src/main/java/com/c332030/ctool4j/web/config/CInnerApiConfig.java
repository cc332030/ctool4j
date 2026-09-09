package com.c332030.ctool4j.web.config;

import com.c332030.ctool4j.core.util.CSet;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

/**
 * <p>
 * Description: 内部接口 IP 白名单配置
 * </p>
 *
 * <p>配置内部接口（标注 {@code @CInnerApi} 的 Controller/方法）允许访问的 IP/IP 段白名单，
 * 所有标注 {@code @CInnerApi} 的内部接口共用本配置。白名单为空时全部放行。</p>
 *
 * <p>规则支持单个 IPv4（如 {@code 192.168.1.5}）与 CIDR 网段（如 {@code 192.168.1.0/24}），
 * 多个用逗号分隔，由 {@code CIpUtils} 解析判断。</p>
 *
 * @author c332030
 * @since 2026/9/9
 * @see "doc/design/web/CInnerApiConfig.adoc"
 */
@Data
@ConfigurationProperties("inner-api")
public class CInnerApiConfig {

    /**
     * 内部接口允许访问的 IP/IP 段(CIDR)白名单，为空时全部放行
     */
    Set<String> allowedIps = CSet.of();

}
