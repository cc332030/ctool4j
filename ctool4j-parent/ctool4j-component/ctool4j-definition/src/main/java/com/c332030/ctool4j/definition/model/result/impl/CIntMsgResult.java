package com.c332030.ctool4j.definition.model.result.impl;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.definition.model.result.ICIntMsgResult;
import com.c332030.ctool4j.definition.model.result.ICIntResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

/**
 * <p>
 * Description: CIntMsgResult
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CIntMsgResult&lt;DATA&gt;} 为结果封装，实现 {@code ICIntResult&lt;DATA&gt;} 与 {@code ICIntMsgResult&lt;DATA&gt;}，含 {@code code}/{@code msg}/{@code data} 字段，提供：</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>error message 为 null</td>
 *     <td>使用 500 原因短语</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>服务统一结果封装（code/msg/data）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>与 CResult 类似，msg 字段命名。</li>
 * </ul>
 *
 * @since 2025/5/13
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CIntMsgResult<DATA> implements ICIntResult<DATA>, ICIntMsgResult<DATA> {

    Integer code;

    String msg;

    DATA data;

    /**
     * 构造结果
     * <ul>
     *   <li>{@code newInstance(code, message, data)}：构造</li>
     * </ul>
     *
     * @param code    状态码
     * @param message 消息
     * @param data    数据
     * @param <DATA>  数据类型
     * @return 结果
     */
    public static <DATA> CIntMsgResult<DATA> newInstance(Integer code, String message, DATA data) {
        return CIntMsgResult.<DATA>builder()
                .code(code)
                .msg(message)
                .data(data)
                .build();
    }

    /**
     * 成功结果（无数据）
     * <ul>
     *   <li>{@code success()} / {@code success(data)}：成功结果（200）</li>
     * </ul>
     *
     * @param <DATA> 数据类型
     * @return 成功结果
     */
    public static <DATA> CIntMsgResult<DATA> success() {
        return success(null);
    }

    /**
     * 成功结果
     *
     * @param data   数据
     * @param <DATA> 数据类型
     * @return 成功结果
     */
    public static <DATA> CIntMsgResult<DATA> success(DATA data) {
        return newInstance(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), data);
    }

    /**
     * 失败结果（默认 500）
     * <ul>
     *   <li>{@code error(message)} / {@code error(code, message)} 等：失败结果（默认 500）</li>
     *   <li>{@code success} 用 {@code HttpStatus.OK}（200）；{@code error(message)} 默认 500，message 为 null 用 500 原因短语。</li>
     * </ul>
     *
     * @param message 消息
     * @param <DATA>  数据类型
     * @return 失败结果
     */
    public static <DATA> CIntMsgResult<DATA> error(String message) {
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                StrUtil.nullToDefault(message, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        );
    }

    /**
     * 失败结果
     *
     * @param code    状态码
     * @param message 消息
     * @param <DATA>  数据类型
     * @return 失败结果
     */
    public static <DATA> CIntMsgResult<DATA> error(Integer code, String message) {
        return newInstance(code, message, null);
    }

}
