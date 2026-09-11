package com.c332030.ctool4j.definition.enums.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: CPlatformTypeEnum
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CPlatformTypeEnum} 为平台类型枚举，定义 WEB/ANDROID/IOS/HARMONY_OS/WINDOWS/MAC_OS/LINUX/WEARABLE，各带描述 {@code text}。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>平台类型的标准化枚举。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>描述为文本。</li>
 * </ul>
 *
 * @since 2026/6/25
 * @version 1.0
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
