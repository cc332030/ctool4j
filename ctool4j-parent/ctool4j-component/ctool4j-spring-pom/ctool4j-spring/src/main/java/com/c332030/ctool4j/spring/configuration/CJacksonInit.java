package com.c332030.ctool4j.spring.configuration;

import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import com.c332030.ctool4j.core.util.CBoolUtils;
import com.c332030.ctool4j.spring.config.CSpringJacksonConfig;
import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Description: CJacksonInit
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CJacksonInit}：Jackson 初始化。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>Spring 初始化时配置 Jackson</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>Jackson 初始化</p>
 * <h2>不适用与边界场景</h2>
 * <p>实现 ICSpringInit</p>
 * <h2>已知限制与取舍</h2>
 * <p>实现 ICSpringInit</p>
 *
 * @since 2026/4/8
 * @version 1.0
 */
@CustomLog
@Configuration
@AllArgsConstructor
public class CJacksonInit implements ICSpringInit {

    CSpringJacksonConfig jacksonConfig;

    ObjectMapper objectMapper;

    /**
     * Spring 启动初始化回调：开启 json5 时配置 ObjectMapper
     */
    @Override
    public void onInit() {

        if(CBoolUtils.isTrue(jacksonConfig.getJson5())) {
            log.debug("spring.jackson.json5 已开启，配置 ObjectMapper");
            CJacksonUtils.configure(objectMapper);
        }

    }

}
