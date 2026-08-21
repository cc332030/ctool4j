package com.c332030.ctool4j.redis.configuration;

import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: CRedisInit
 * </p>
 *
 * @see doc/design/redis/CRedisInit.adoc
 * @since 2025/12/8
 */
@Component
public class CRedisInit implements ICSpringInit {

    /**
     * Spring 启动初始化回调（当前无处理逻辑）
     */
    @Override
    public void onInit() {

    }

}
