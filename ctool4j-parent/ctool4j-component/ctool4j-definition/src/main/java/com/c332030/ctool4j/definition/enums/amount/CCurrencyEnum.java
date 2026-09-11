package com.c332030.ctool4j.definition.enums.amount;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: CCurrencyEnum
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCurrencyEnum} 为货币枚举，定义 17 种货币（CNY/USD/EUR/GBP/JPY/KRW/HKD/TWD/AUD/CAD/CHF/SGD/NZD/THB/ MYR/RUB/INR），各带中文描述 {@code text}。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>货币类型的标准化枚举。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>描述为中文文本。</li>
 * </ul>
 *
 * @since 2026/5/12
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum CCurrencyEnum {

    CNY("人民币"),

    USD("美元"),

    EUR("欧元"),

    GBP("英镑"),

    JPY("日元"),

    KRW("韩元"),

    HKD("港币"),

    TWD("新台币"),

    AUD("澳元"),

    CAD("加元"),

    CHF("瑞士法郎"),

    SGD("新加坡元"),

    NZD("新西兰元"),

    THB("泰铢"),

    MYR("马来西亚林吉特"),

    RUB("俄罗斯卢布"),

    INR("印度卢比"),

    ;

    /**
     * 描述
     */
    final String text;

}
