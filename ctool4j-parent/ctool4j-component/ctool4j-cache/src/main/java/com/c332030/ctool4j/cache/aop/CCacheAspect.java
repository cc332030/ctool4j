package com.c332030.ctool4j.cache.aop;

import com.c332030.ctool4j.cache.annotation.CCacheId;
import com.c332030.ctool4j.cache.annotation.CCacheable;
import com.c332030.ctool4j.cache.model.CCacheValue;
import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.util.CArrUtils;
import com.c332030.ctool4j.spring.util.CAspectUtils;
import lombok.val;
import lombok.var;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

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
@Aspect
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
        if (cacheable.local()) {
            return getLocalCache(joinPoint, cacheable);
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

        val cacheIdField = CACHE_ID_FIELD_CLASS_VALUE.get(object.getClass());
        val cacheId = CObjUtils.convert(cacheIdField, f -> f.get(object));

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
        val cacheMap = CLASS_CACHE_VALUE.get(namespace);

        val args = joinPoint.getArgs();
        val argOne = CArrUtils.get(args, 0);

        // TODO 无方法参数缓存
        if (null != argOne) {

            val currentMills = System.currentTimeMillis();

            val cacheKey = getCacheKey(argOne, cacheable);
            var cacheValue = cacheMap.get(cacheKey);
            if(null != cacheValue
                && currentMills - cacheValue.getCreateMills() < cacheable.expire() * 1000
            ) {
                return cacheValue.getValue();
            }

            val valueNew = CAspectUtils.process(joinPoint);
            cacheValue = CCacheValue.builder()
                .value(null)
                .createMills(currentMills)
                .build();
            cacheMap.put(cacheKey, cacheValue);

            return valueNew;
        }

        return CAspectUtils.process(joinPoint);
    }

}
