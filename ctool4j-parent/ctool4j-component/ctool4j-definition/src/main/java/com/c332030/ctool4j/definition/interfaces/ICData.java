package com.c332030.ctool4j.definition.interfaces;

import com.c332030.ctool4j.doc.annotation.CSchema;

/**
 * <p>
 * Description: ICData
 * </p>
 *
 * @see "doc/design/definition/ICData.adoc"
 * @since 2025/12/30
 */
public interface ICData<T> {

    /**
     * 获取数据
     * @return 数据
     */
    @CSchema("数据")
    T getData();

}
