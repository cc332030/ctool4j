package com.c332030.ctool4j.definition.enums.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: CClientTypeEnum
 * </p>
 *
 * @since 2026/6/25
 */
@Getter
@AllArgsConstructor
public enum CClientTypeEnum {

    WEB("网页"),

    OFFIACCOUNT("公众号"),
    MINI_PROGRAM("小程序"),

    APP("应用"),

    PC("PC"),

    ;

    /**
     * 描述
     */
    final String text;

}
