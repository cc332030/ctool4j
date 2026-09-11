package com.c332030.ctool4j.csv.util;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CStrUtils;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * <p>
 * Description: CCsvUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCsvUtils}（{@code @UtilityClass}）提供 CSV 单元格字符串的处理能力：</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>入参为 null</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>空串 / 纯空白 / 仅退格</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>普通字符串</td>
 *     <td>去除两端空白与退格后返回</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>CSV 单元格值的标准化（去空白、去退格）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不处理字符串内部的常规空白（仅去除退格与两端空白）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>退格移除使用正则 {@code \b}，对退格字符 {@code \u0008} 生效。</li>
 *   <li>空串返回 null 的约定需调用方注意区分"空单元格"与"缺失列"。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>实现方式</b></p>
 * <ul>
 *   <li>依赖 {@code CStrUtils.trim} 与 hutool 的 {@code StrUtil.isBlank}。</li>
 *   <li>通过 {@code replaceAll("\b", "")} 移除退格字符。</li>
 * </ul>
 *
 * @since 2026/1/14
 * @version 1.0
 */
@UtilityClass
public class CCsvUtils {

    /**
     * 去除字符串两端空白，空字符串返回 null
     *
     * <h2>trim 语义</h2>
     * <ul>
     *   <li>先调用 {@code CStrUtils.trim} 去除两端空白。</li>
     *   <li>空白（blank）字符串返回 {@code null}（空串、纯空白、仅退格均视为 blank）。</li>
     *   <li>非空串去除所有退格字符（{@code \b}）后返回。</li>
     * </ul>
     * <ul>
     *   <li>{@code trim(String)}：去除字符串两端空白，空白字符串返回 {@code null}；并移除字符串中的退格字符（{@code \b}）。</li>
     * </ul>
     *
     * @param str 原始字符串
     * @return 去除空白后的字符串；空白字符串返回 null*/
    public String trim(String str) {

        val strNew = CStrUtils.trim(str);
        if(StrUtil.isBlank(strNew)) {
            return null;
        }

        return strNew.replaceAll("\b", "");
    }

}
