package com.c332030.ctool4j.feign.config;

import com.c332030.ctool4j.core.util.CSet;
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
@ConfigurationProperties("feign.client.log")
public class CFeignClientLogConfig {

    /**
     * 日志开关
     */
    Boolean enable = false;

    /**
     * 请求头日志开关
     */
    Boolean enableHeader = false;

    /**
     * 耗时日志开关
     */
    Boolean enableCost = true;

    /**
     * 全部日志开关
     */
    Boolean logAll = false;

    /**
     * 接口白名单
     */
    Set<String> apiWhiteList = CSet.of();

    /**
     * 接口黑名单
     */
    Set<String> apiBlackList = CSet.of();

    /**
     * host 白名单
     */
    Set<String> hostWhiteList = CSet.of();

    /**
     * host 黑名单
     */
    Set<String> hostBlackList = CSet.of();

    /**
     * path 白名单
     */
    Set<String> pathWhiteList = CSet.of();

    /**
     * path 黑名单
     */
    Set<String> pathBlackList = CSet.of();

}
