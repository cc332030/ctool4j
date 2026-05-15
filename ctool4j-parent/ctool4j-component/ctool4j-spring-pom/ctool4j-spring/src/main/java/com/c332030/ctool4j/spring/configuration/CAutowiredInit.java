package com.c332030.ctool4j.spring.configuration;

import cn.hutool.extra.spring.SpringUtil;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import com.c332030.ctool4j.spring.util.CAutowiredUtils;
import com.c332030.ctool4j.spring.util.CSpringUtils;
import lombok.CustomLog;
import lombok.val;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CAutowiredInit
 * </p>
 *
 * @author c332030
 * @since 2026/5/15
 */
@CustomLog
@Component
public class CAutowiredInit implements ICSpringInit {

    @Override
    public void onInit() {

        val start = System.currentTimeMillis();
        log.info("CAutowiredScan start");

        val beans = CSpringUtils.getBeansWithAnnotation(CAutowiredScan.class);
        beans.values()
                .stream()
                .map(bean -> bean.getClass().getAnnotation(CAutowiredScan.class))
                .map(CAutowiredScan::value)
                .flatMap(Arrays::stream)
                .forEach(type -> {
                    log.info("CAutowiredScan type: {}", type);
                    CAutowiredUtils.autowired(type);
                });

        val cost = System.currentTimeMillis() - start;
        log.info("CAutowiredScan end, cost: {}ms", cost);

    }

}
