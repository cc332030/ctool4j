package com.c332030.ctool4j.core.util;

import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.definition.entity.base.*;
import com.c332030.ctool4j.definition.function.CConsumer;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.reflect.Method;
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

    private static final List<Method> CLEAR_METHODS = CReflectUtils.getMethodsByName(
            CEntityUtils.class, "clear"
    );

    private static final CClassValue<CConsumer<Object>> CLEAN_ENTITY_CONSUMER = CClassValue.of(type -> {

        for (int i = CLEAR_METHODS.size() - 1; i >= 0; i--) {

            val method = CLEAR_METHODS.get(i);
            val param0 = method.getParameterTypes()[0];
            if(param0 != Object.class && param0.isAssignableFrom(type)) {
                return e -> method.invoke(null, e);
            }
        }

        return CConsumer.empty();
    });

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
    public void clear(CBaseTimeEntity entity) {
        clear((ICId<?>) entity);
        clear((ICCreateUpdateTime)entity);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clear(CBaseEntity entity) {
        clear((ICId<?>) entity);
        clear((ICCreateUpdateByAndTime)entity);
    }

}
