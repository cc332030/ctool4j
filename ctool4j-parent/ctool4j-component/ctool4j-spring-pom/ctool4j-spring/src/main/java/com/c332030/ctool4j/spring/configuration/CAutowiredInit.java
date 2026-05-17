package com.c332030.ctool4j.spring.configuration;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.util.CArrUtils;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import com.c332030.ctool4j.spring.util.CAutowiredUtils;
import com.c332030.ctool4j.spring.util.CSpringUtils;
import lombok.CustomLog;
import lombok.val;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
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
        log.debug("CAutowiredScan start");

        val beans = CSpringUtils.getBeansWithAnnotation(CAutowiredScan.class);
        val fields = beans.values()
                .stream()
                .map(bean -> bean.getClass().getAnnotation(CAutowiredScan.class))
                .map(CAutowiredScan::value)
                .flatMap(Arrays::stream)
                .map(Class::getDeclaredFields)
                .flatMap(Arrays::stream)
                .filter(CReflectUtils::isStatic)
                .filter(field -> field.isAnnotationPresent(CAutowired.class))
                .collect(Collectors.toList());

        val typeGroupMap = CCollUtils.groupingBy(fields, Field::getType);

        typeGroupMap.forEach((argType, list) -> {

            val bean = SpringUtil.getBean(argType);
            list.forEach(field -> CReflectUtils.setValue(null, field, bean));

        });

        val cost = System.currentTimeMillis() - start;
        log.info("CAutowired {} fields cost: {}ms", fields.size(), cost);

    }

}
