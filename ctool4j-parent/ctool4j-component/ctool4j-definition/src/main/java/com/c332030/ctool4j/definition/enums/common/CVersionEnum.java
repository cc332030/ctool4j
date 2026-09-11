package com.c332030.ctool4j.definition.enums.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: CVersionEnum
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CVersionEnum} 为版本枚举，定义 V1/V2/V3，各带描述 {@code text}。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>版本类型的标准化枚举。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>描述与枚举名一致（V1/V2/V3）。</li>
 * </ul>
 *
 * @since 2026/6/6
 * @version 1.0
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
