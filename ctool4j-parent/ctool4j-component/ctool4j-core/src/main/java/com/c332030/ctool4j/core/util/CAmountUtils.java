package com.c332030.ctool4j.core.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

/**
 * <p>
 * Description: CAmountUtils
 * </p>
 *
 * @since 2025/12/18
 */
@UtilityClass
public class CAmountUtils {

    /**
     * 分转元
     * @param value 分
     * @return 元
     */
    public BigDecimal toCent(Integer value) {

        if(null == value) {
            return null;
        }
        return toCent(new BigDecimal(value));
    }

    /**
     * 分转元
     * @param value 分
     * @return 元
     */
    public BigDecimal toCent(Long value) {

        if(null == value) {
            return null;
        }
        return toCent(new BigDecimal(value));
    }

    /**
     * 分转元
     * @param value 分
     * @return 元
     */
    public BigDecimal toCent(BigDecimal value) {

        if(null == value) {
            return null;
        }

        return CNumUtils.divide(value, CNumUtils.ONE_HUNDRED, 2);
    }

}
