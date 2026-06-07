package com.c332030.ctool4j.definition.enums.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: CVersionEnum
 * </p>
 *
 * @since 2026/6/6
 */
@Getter
@AllArgsConstructor
public enum CVersionEnum {

    V1("V1"),

    V2("V2"),

    V3("V3"),

    ;

    /**
     * 描述
     */
    final String text;

}
