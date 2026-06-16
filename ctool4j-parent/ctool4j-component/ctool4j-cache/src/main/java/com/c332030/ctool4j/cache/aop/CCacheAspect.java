package com.c332030.ctool4j.cache.aop;

import com.c332030.ctool4j.cache.annotation.CCacheId;
import com.c332030.ctool4j.cache.annotation.CCacheable;
import com.c332030.ctool4j.cache.model.CCacheValue;
import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.classes.CClassUtils;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.util.CArrUtils;
import com.c332030.ctool4j.spring.util.CAspectUtils;
import lombok.CustomLog;
import lombok.val;
import lombok.var;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Description: CCacheAspect
 * </p>
 *
 * @since 2025/9/27
 */
@CustomLog
@Aspect
@Component
public class CCacheAspect {

    private static final CClassValue<Map<String, CCacheValue<Object>>> CLASS_CACHE_VALUE = CClassValue
        .of(e -> new ConcurrentHashMap<>());

    private static final CClassValue<ICCacheIdConverter<Object, Object>> CLASS_ID_CONVERTER = CClassValue
        .of(e -> CObjUtils.anyType(CReflectUtils.newInstance(e)));

    private static final CClassValue<Field> CACHE_ID_FIELD_CLASS_VALUE = CClassValue.of(type ->
        CReflectUtils.getAllFieldMap(type)
            .values()
            .stream()
            .filter(field -> field.isAnnotationPresent(CCacheId.class))
            .findFirst()
            .orElse(null)
    );

    /**
     * 缓存切面
     * @param joinPoint 切入点
     * @return 方法执行结果
     */
    @Around("@annotation(com.c332030.ctool4j.cache.annotation.CCacheable)")
    public Object cacheAspect(ProceedingJoinPoint joinPoint) {

        val method = CAspectUtils.getMethod(joinPoint);
        val cacheable = CReflectUtils.getAnnotationCached(method, CCacheable.class);
        try {
            if (cacheable.local()) {
                log.info("启用本地缓存");
                return getLocalCache(joinPoint, cacheable);
            }
        } catch (Throwable e) {
            log.error("获取缓存失败，cacheable: {}", cacheable);
        }

        return CAspectUtils.process(joinPoint);
    }

    /**
     * 生成缓存 key
     * @param object 方法参数
     * @param cacheable 缓存注解
     * @return 缓存 key
     */
    public String getCacheKey(
        Object object,
        CCacheable cacheable
    ) {

        val idConverter = CLASS_ID_CONVERTER.get(cacheable.idConverter());

        val objClass = object.getClass();
        val cacheIdField = CClassUtils.isJdkClass(objClass)
            ? null
            : CACHE_ID_FIELD_CLASS_VALUE.get(objClass);
        val cacheId = CObjUtils.convert(cacheIdField, f -> ((Field)f).get(object));

        return idConverter.apply(cacheId, object);
    }

    /**
     * 获取本地缓存
     * @param joinPoint 切入点
     * @param cacheable 缓存注解
     * @return 本地缓存或执行结果
     */
    public Object getLocalCache(
        ProceedingJoinPoint joinPoint,
        CCacheable cacheable
    ) {

        val namespace = cacheable.namespace();
        log.info("namespace: {}", namespace);

        val cacheMap = CLASS_CACHE_VALUE.get(namespace);
        log.info("cacheMap: {}", cacheMap);

        val args = joinPoint.getArgs();
        val argOne = CArrUtils.get(args, 0);
        log.info("argOne: {}", argOne);

        // TODO 无方法参数缓存
        if (null != argOne) {

            val currentMills = System.currentTimeMillis();

            val cacheKey = getCacheKey(argOne, cacheable);
            log.info("cacheKey: {}", cacheKey);

            var cacheValue = cacheMap.get(cacheKey);

            val expire = cacheable.expire();
            log.info("expire: {}", expire);
            if(null != cacheValue) {

                val passMills = currentMills - cacheValue.getCreateMills();
                log.info("passMills: {}", passMills);
                if(passMills <= expire * 1000L) {
                    log.info("未过期，取缓存");
                    return cacheValue.getValue();
                }
            }
            log.info("没有缓存或已过期");

            val valueNew = CAspectUtils.process(joinPoint);
            cacheValue = CCacheValue.builder()
                .value(valueNew)
                .createMills(currentMills)
                .build();

            log.info("新值 cacheValue： {}", cacheValue);
            cacheMap.put(cacheKey, cacheValue);

            return valueNew;
        }

        log.info("方法无参数，跳过缓存");
        return CAspectUtils.process(joinPoint);
    }

}
