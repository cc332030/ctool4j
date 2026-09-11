package com.c332030.ctool4j.core.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

/**
 * <p>
 * Description: CAmountUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CAmountUtils} 为金额单位换算工具类，提供"分 → 元"的换算，支持 {@code Integer} / {@code Long} / {@code BigDecimal} 三种入参形态，统一返回 {@code BigDecimal}（元，四舍五入保留 2 位小数）。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>任一入参形态为 null</td>
 *     <td>返回 null</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>数据库/接口以"分"为单位存储金额、展示层需换算为"元"的场景。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅支持"分 → 元"单向换算，不提供"元 → 分"反向换算。</li>
 *   <li>结果为 BigDecimal，精度固定 2 位小数；不适合对元金额做超 2 位小数的精确运算（会四舍五入）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>四舍五入采用 HALF_UP 语义，与金融系统常见的四舍五入一致；需要其他舍入模式时不适用本类。</li>
 *   <li>入参 null 返回 null，与"空金额"语义保持一致。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义约定</b></p>
 * <ul>
 *   <li>换算规则：元 = 分 ÷ 100，结果四舍五入（HALF_UP）保留 2 位小数（复用 {@code CNumUtils.divide}）。</li>
 *   <li>入参为 null 返回 null，调用方据此可安全判断"金额为空"。</li>
 *   <li>三种入参形态重载，内部统一转为 {@code BigDecimal} 走同一换算路径，避免重复实现。</li>
 * </ul>
 * <p><b>实现方式</b></p>
 * <ul>
 *   <li>四舍五入。</li>
 * </ul>
 *
 * @since 2025/12/18
 * @version 1.0
 */
@UtilityClass
public class CAmountUtils {

    /**
     * 分转元
     * <ul>
     *   <li>{@code toYuan(Integer)}：整数分 → 元</li>
     *   <li>{@code toYuan(Long)}：长整型分 → 元</li>
     *   <li>{@code toYuan(BigDecimal)}：BigDecimal 分 → 元（支持带小数的"分"，如 123.456 分）</li>
     *   <li>{@code Integer}/{@code Long} 重载先判空，再经 {@code new BigDecimal(value)} 转 {@code BigDecimal}，委托 {@code toYuan(BigDecimal)}。</li>
     *   <li>{@code toYuan(BigDecimal)} 判空后委托 {@code CNumUtils.divide(value, ONE_HUNDRED, 2)} 完成除 100 与 2 位小数</li>
     * </ul>
     *
     * @param value 分
     * @return 元
     */
    public BigDecimal toYuan(Integer value) {

        if(null == value) {
            return null;
        }
        return toYuan(new BigDecimal(value));
    }

    /**
     * 分转元
     * @param value 分
     * @return 元
     */
    public BigDecimal toYuan(Long value) {

        if(null == value) {
            return null;
        }
        return toYuan(new BigDecimal(value));
    }

    /**
     * 分转元
     * @param value 分
     * @return 元
     */
    public BigDecimal toYuan(BigDecimal value) {

        if(null == value) {
            return null;
        }

        return CNumUtils.divide(value, CNumUtils.ONE_HUNDRED, 2);
    }

}
