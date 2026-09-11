package com.c332030.ctool4j.core.exception;

import lombok.experimental.StandardException;

/**
 * <p>
 * Description: CException
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CException} 为项目基础异常，继承 {@code RuntimeException}，标注 {@code @StandardException} （Lombok 生成完整构造方法集合：无参/消息/原因/消息+原因）。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>无参构造</td>
 *     <td>message/cause 为 null</td>
 *   </tr>
 *   <tr>
 *     <td>带消息构造</td>
 *     <td>消息正确设置</td>
 *   </tr>
 *   <tr>
 *     <td>带原因构造</td>
 *     <td>cause 正确设置</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>项目自定义运行时异常的基类。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>业务异常应使用 {@code CBusinessException}（带错误码）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>继承 RuntimeException，支持标准异常构造，便于业务异常复用。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>构造方法</b></p>
 * <ul>
 *   <li>由 {@code @StandardException} 生成标准异常构造集合：</li>
 *   <li>无参构造：message/cause 均为 null</li>
 *   <li>{@code (String message)}、{@code (Throwable cause)}、{@code (String message, Throwable cause)}</li>
 * </ul>
 * <p><b>基类定位</b></p>
 * <ul>
 *   <li>作为项目运行时异常的基类，业务异常 {@code CBusinessException} 继承自它。</li>
 * </ul>
 *
 * @since 2025/10/27
 * @version 1.0
 */
@StandardException
public class CException extends RuntimeException{

    private static final long serialVersionUID = 1;

}
