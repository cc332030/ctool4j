package com.c332030.ctool4j.core.util;

import lombok.experimental.UtilityClass;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 * Description: CCharsets
 * </p>
 *
 * <p>
 * 项目全局统一字符集常量，各模块共用，避免每处重复指定字符集或依赖平台默认字符集导致跨环境不一致
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCharsets} 为全局字符集常量类，提供：</p>
 * <ul>
 *   <li>{@code UTF_8}：全局统一编码，值为 {@code StandardCharsets.UTF_8}</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>需要字符集常量</td>
 *     <td>统一引用 {@code CCharsets.UTF_8}</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>各模块编解码、文件读写、网络传输需要指定字符集时统一使用。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅提供 UTF-8 常量；其他字符集需求使用 JDK {@code StandardCharsets} 或显式指定。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>作为纯常量类（@UtilityClass），仅承载全局统一编码约定，无运行时行为。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>统一编码约定</b></p>
 * <ul>
 *   <li>项目全局统一使用 UTF-8，避免各模块重复指定字符集或依赖平台默认字符集导致跨环境不一致。</li>
 *   <li>常量直接引用 {@code StandardCharsets.UTF_8}，无冗余定义。</li>
 * </ul>
 *
 * @since 2026/8/15
 * @version 1.0
 */
@UtilityClass
public class CCharsets {

    /**
     * 全局统一编码：UTF-8
     */
    public static final Charset UTF_8 = StandardCharsets.UTF_8;

}
