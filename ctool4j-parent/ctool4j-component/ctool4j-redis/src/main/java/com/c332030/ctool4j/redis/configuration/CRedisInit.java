package com.c332030.ctool4j.redis.configuration;

import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
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

//        CSpringUtils.wireBean(CObjectValueRedisService.class, CRedisUtils::setRedisService);
//        CSpringUtils.wireBean(CLockService.class, CLockUtils::setLockService);

    }

}
