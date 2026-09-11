package com.c332030.ctool4j.definition.interfaces;

/**
 * <p>
 * Description: ICCreateMillis
 * </p>
 *
 * <p>创建时间字段接口：提供创建时间（毫秒时间戳）的读取能力，默认返回 {@code null}，
 * 由实现类按需覆写。</p>
 *
 * @since 2026/9/11
 */
public interface ICCreateMillis {

    /**
     * 获取创建时间（毫秒时间戳）
     *
     * @return 创建时间毫秒时间戳；未覆写时返回 null
     */
    default Long getCreateMillis() {
        return null;
    }

}
