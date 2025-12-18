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
    public void clearEntity(ICId<?> entity) {
        entity.setId(null);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clearEntity(ICCreateTime entity) {
        entity.setCreateTime(null);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clearEntity(ICUpdateTime entity) {
        entity.setUpdateTime(null);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clearEntity(ICCreateUpdateTime entity) {
        clearEntity((ICCreateTime)entity);
        clearEntity((ICUpdateTime)entity);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clearEntity(ICCreateBy entity) {
        entity.setCreateBy(null);
        entity.setCreateById(null);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clearEntity(ICUpdateBy entity) {
        entity.setUpdateBy(null);
        entity.setUpdateById(null);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clearEntity(ICCreateUpdateBy entity) {
        clearEntity((ICCreateBy)entity);
        clearEntity((ICUpdateBy)entity);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clearEntity(ICCreateUpdateByAndTime entity) {
        clearEntity((ICCreateUpdateBy)entity);
        clearEntity((ICCreateUpdateTime)entity);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clearEntity(CBaseTimeEntity entity) {
        clearEntity((ICId<?>) entity);
        clearEntity((ICCreateUpdateTime)entity);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clearEntity(CBaseEntity entity) {
        clearEntity((ICId<?>) entity);
        clearEntity((ICCreateUpdateByAndTime)entity);
    }

}
