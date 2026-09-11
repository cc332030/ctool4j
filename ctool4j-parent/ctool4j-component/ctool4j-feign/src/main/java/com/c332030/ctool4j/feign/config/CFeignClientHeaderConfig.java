package com.c332030.ctool4j.feign.config;

import com.c332030.ctool4j.core.util.CSet;
import com.c332030.ctool4j.feign.enums.CFeignClientHeaderPropagationModeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

/**
 * <p>
 * Description: CFeignClientHeaderConfig
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code propagationMode}：传播模式，默认 {@code ALL}。</li>
 *   <li>{@code propagationCustomHeaders}：自定义传播 headers。</li>
 *   <li>{@code propagationRequestHeaders}：传播的 request headers。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>前缀 {@code feign.client.header}，供配置文件绑定。</li>
 *   <li>集合默认 {@code CSet.of()}（空不可变集合）。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>未配置时用默认值（ALL 模式 + 空集合）。</li>
 * </ul>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>配置 Feign 请求头传播模式与自定义 headers（供 {@code CFeignUtils.transferHeaders} 使用）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>集合默认不可变，动态修改需重新 set。</li>
 * </ul>
 *
 * @since 2025/9/21
 * @version 1.0
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
