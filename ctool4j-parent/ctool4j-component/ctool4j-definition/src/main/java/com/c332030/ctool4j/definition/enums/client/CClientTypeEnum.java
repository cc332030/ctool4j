package com.c332030.ctool4j.definition.enums.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: CClientTypeEnum
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CClientTypeEnum} 为客户端类型枚举，定义 WEB/OFFICIAL_ACCOUNT/MINI_PROGRAM/APP/PC，各带中文描述 {@code text}。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>客户端类型的标准化枚举。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>描述为中文文本。</li>
 * </ul>
 *
 * @since 2026/6/25
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum CClientTypeEnum {

    WEB("网页"),

    OFFICIAL_ACCOUNT("公众号"),
    MINI_PROGRAM("小程序"),

    APP("应用"),

    PC("PC"),

    ;

    /**
     * 描述
     */
    final String text;

}
