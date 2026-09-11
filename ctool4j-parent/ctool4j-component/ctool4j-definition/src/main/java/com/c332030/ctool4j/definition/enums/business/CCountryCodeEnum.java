package com.c332030.ctool4j.definition.enums.business;

import com.c332030.ctool4j.definition.interfaces.ICValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: CCountryCodeEnum
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCountryCodeEnum} 为国家区号枚举，实现 {@code ICValue&lt;Integer&gt;}，当前含 CHN（86，中国），带 {@code value} 与 {@code text}。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>国家区号的标准化枚举。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>当前仅含中国，可扩展。</li>
 * </ul>
 *
 * @since 2026/3/17
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum CCountryCodeEnum implements ICValue<Integer> {

    CHN(86, "中国"),

    ;

    /**
     * 国家/地区电话区号（如中国为 86）
     */
    final Integer value;

    /**
     * 描述
     */
    final String text;

}
