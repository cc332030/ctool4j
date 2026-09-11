package com.c332030.ctool4j.core.exception;

import com.c332030.ctool4j.core.util.CResUtils;
import com.c332030.ctool4j.definition.interfaces.ICRes;
import lombok.Getter;

/**
 * <p>
 * Description: CBusinessException
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CBusinessException} 为业务异常，继承 {@code CException}，携带错误码定义、附加信息与原因。</p>
 * <p>提供字段：</p>
 * <ul>
 *   <li>{@code error}（ICRes 错误码定义）</li>
 *   <li>{@code msgExtend}（附加信息）</li>
 * </ul>
 * <p>构造重载：</p>
 * <ul>
 *   <li>{@code (ICRes error)}、{@code (ICRes error, Throwable cause)}、{@code (ICRes error, String msgExtend)}、</li>
 *   <li>{@code (ICRes error, String msgExtend, Throwable cause)}</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>error 为 null</td>
 *     <td>消息仅含 msgExtend</td>
 *   </tr>
 *   <tr>
 *     <td>msgExtend 为空</td>
 *     <td>消息为 {@code [code] msg}</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>业务校验/处理失败，携带错误码便于统一处理。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>error 为 null 时无法携带错误码，仅作普通消息异常。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>统一经 CResUtils 格式化消息，保证错误码展示一致。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>消息格式化</b></p>
 * <ul>
 *   <li>消息经 {@code CResUtils.formatResMessage(error, msgExtend)} 生成：{@code [code] msg}，msgExtend 非空追加</li>
 *   <li>{@code : msgExtend}。</li>
 *   <li>error 为 null 时返回 msgExtend。</li>
 * </ul>
 *
 * @since 2025/9/14
 * @version 1.0
 */
@Getter
public class CBusinessException extends CException {

    private static final long serialVersionUID = 1L;

    private final ICRes<?> error;

    private final String msgExtend;

    /**
     * 构造业务异常
     *
     * @param error 错误码定义
     */
    public CBusinessException(ICRes<?> error) {
        this(error, (Throwable) null);
    }

    /**
     * 构造业务异常
     *
     * @param error 错误码定义
     * @param cause 异常原因
     */
    public CBusinessException(ICRes<?> error, Throwable cause) {
        this(error, null, cause);
    }

    /**
     * 构造业务异常
     *
     * @param error     错误码定义
     * @param msgExtend 附加信息
     */
    public CBusinessException(ICRes<?> error, String msgExtend) {
        this(error, msgExtend, null);
    }

    /**
     * 构造业务异常
     *
     * @param error     错误码定义
     * @param msgExtend 附加信息
     * @param cause     异常原因
     */
    public CBusinessException(ICRes<?> error, String msgExtend, Throwable cause) {
        super(CResUtils.formatResMessage(error, msgExtend), cause);
        this.error = error;
        this.msgExtend = msgExtend;
    }

}
