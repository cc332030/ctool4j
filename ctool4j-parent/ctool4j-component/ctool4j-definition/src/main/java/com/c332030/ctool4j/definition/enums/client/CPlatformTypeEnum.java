package com.c332030.ctool4j.definition.enums.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: CPlatformTypeEnum
 * </p>
 *
 * @since 2026/6/25
 */
@Getter
@AllArgsConstructor
public enum CPlatformTypeEnum {

    WEB("网页"),

    ANDROID("安卓"),
    IOS("iOS"),
    HARMONY_OS("HarmonyOS"),

    WINDOWS("Windows"),
    MAC_OS("MacOS"),
    LINUX("Linux"),

    WEARABLE("Wearable"),

    ;

    /**
     * 描述
     */
    final String text;

}
