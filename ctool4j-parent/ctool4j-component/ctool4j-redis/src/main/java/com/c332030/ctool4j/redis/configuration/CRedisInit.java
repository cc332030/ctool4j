package com.c332030.ctool4j.redis.configuration;

import com.c332030.ctool4j.redis.service.impl.CLockService;
import com.c332030.ctool4j.redis.service.impl.CObjectValueRedisService;
import com.c332030.ctool4j.redis.util.CLockUtils;
import com.c332030.ctool4j.redis.util.CRedisUtils;
import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import com.c332030.ctool4j.spring.util.CSpringUtils;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: CRedisInit
 * </p>
 *
 * @since 2025/12/8
 */
@Component
public class CRedisInit implements ICSpringInit {

    @Override
    public void onInit() {

        CSpringUtils.wireBean(CObjectValueRedisService.class, CRedisUtils::setRedisService);
        CSpringUtils.wireBean(CLockService.class, CLockUtils::setLockService);

    }

}
