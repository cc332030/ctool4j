package com.c332030.ctool4j.redis.configuration;

import com.c332030.ctool4j.redis.util.CLockUtils;
import com.c332030.ctool4j.redis.util.CRedisUtils;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import com.c332030.ctool4j.spring.util.CAutowiredUtils;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: CRedisInit
 * </p>
 *
 * @since 2025/12/8
 */
@Component
@CAutowiredScan({
        CRedisUtils.class,
        CLockUtils.class
})
public class CRedisInit implements ICSpringInit {

    @Override
    public void onInit() {

    }

}
