package com.c332030.ctool4j.core.util;

import com.c332030.ctool4j.definition.entity.base.*;
import lombok.experimental.UtilityClass;

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
