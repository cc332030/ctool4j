package com.c332030.ctool4j.definition.interfaces;

import com.c332030.ctool4j.doc.annotation.CSchema;

/**
 * <p>
 * Description: ICCreateMillis
 * </p>
 *
 * <p>创建时间字段接口：提供创建时间（毫秒时间戳）的读取能力，默认返回 {@code null}，
 * 由实现类按需覆写。</p>
 *
 * @since 2026/9/11
 * @version 1.0
 */
public interface ICCreateMillis {

    /**
     * 获取创建时间（毫秒时间戳）
     *
     * @return 创建时间毫秒时间戳；未覆写时返回 null
     */
    @CSchema("创建时间")
    default Long getCreateMillis() {
        return null;
    }

}
