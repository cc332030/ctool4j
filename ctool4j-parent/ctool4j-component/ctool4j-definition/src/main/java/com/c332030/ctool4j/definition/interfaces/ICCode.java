package com.c332030.ctool4j.definition.interfaces;

import com.c332030.ctool4j.doc.annotation.CSchema;

/**
 * <p>
 * Description: ICCode
 * </p>
 *
 * @see "doc/design/definition/ICCode.adoc"
 * @since 2025/12/30
 */
public interface ICCode<T> {

    /**
     * 获取编码
     * @return 编码
     */
    @CSchema("状态码")
    T getCode();

}
