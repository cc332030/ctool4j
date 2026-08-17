package com.c332030.ctool4j.core.util;

import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.definition.entity.base.*;
import com.c332030.ctool4j.definition.function.CConsumer;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Description: CEntityUtils
 * </p>
 *
 * @since 2025/12/18
 */
@UtilityClass
public class CEntityUtils {

    /**
     * 清除方法
     */
    private static final List<Method> CLEAR_METHODS = CReflectUtils.getAllMethodsByName(
            CEntityUtils.class, "clear"
    );

    /**
     * 各实体类清除方法缓存
     */
    private static final CClassValue<CConsumer<Object>> CLEAN_ENTITY_CONSUMER = CClassValue.of(type -> {

        // getDeclaredMethods() 返回顺序不保证，不能依赖声明顺序选择最具体方法
        val candidates = new ArrayList<Method>();
        for (Method method : CLEAR_METHODS) {

            val param0 = method.getParameterTypes()[0];
            if(param0 != Object.class && param0.isAssignableFrom(type)) {
                candidates.add(method);
            }
        }

        // 选出参数类型最具体的重载（不存在更具体的可接收 type 的重载）
        for (Method candidate : candidates) {

            val param = candidate.getParameterTypes()[0];
            val mostSpecific = candidates.stream()
                    .noneMatch(other -> other != candidate
                            && other.getParameterTypes()[0] != param
                            && param.isAssignableFrom(other.getParameterTypes()[0]));
            if(mostSpecific) {
                return e -> {
                    try {
                        candidate.invoke(null, e);
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        throw new IllegalStateException(ex);
                    }
                };
            }
        }

        return CConsumer.empty();
    });

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clear(Object entity) {
        CLEAN_ENTITY_CONSUMER.get(entity.getClass())
                .accept(entity);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clear(ICId<?> entity) {
        entity.setId(null);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clear(ICCreateTime entity) {
        entity.setCreateTime(null);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clear(ICUpdateTime entity) {
        entity.setUpdateTime(null);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clear(ICCreateUpdateTime entity) {
        clear((ICCreateTime)entity);
        clear((ICUpdateTime)entity);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clear(ICCreateBy entity) {
        entity.setCreateBy(null);
        entity.setCreateById(null);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clear(ICUpdateBy entity) {
        entity.setUpdateBy(null);
        entity.setUpdateById(null);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clear(ICCreateUpdateBy entity) {
        clear((ICCreateBy)entity);
        clear((ICUpdateBy)entity);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clear(ICCreateUpdateByAndTime entity) {
        clear((ICCreateUpdateBy)entity);
        clear((ICCreateUpdateTime)entity);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clear(CBaseTimeEntity<?> entity) {
        clear((ICId<?>) entity);
        clear((ICCreateUpdateTime)entity);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clear(CBaseEntity<?> entity) {
        clear((ICId<?>) entity);
        clear((ICCreateUpdateByAndTime)entity);
    }

}
