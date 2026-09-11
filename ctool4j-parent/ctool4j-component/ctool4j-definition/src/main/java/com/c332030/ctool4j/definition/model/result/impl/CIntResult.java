package com.c332030.ctool4j.definition.model.result.impl;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.definition.model.result.ICIntResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

/**
 * <p>
 * Description: CIntResult
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CIntResult&lt;DATA&gt;} 为结果封装，实现 {@code ICIntResult&lt;DATA&gt;}，含 {@code code}/{@code message}/{@code data} 字段，提供：</p>
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
 *   <li>服务统一结果封装（Integer 状态码）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>与 CResult 类似，但用 message 字段命名。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>成功/失败工厂</b></p>
 * <ul>
 *   <li>{@code success} 用 {@code HttpStatus.OK}（200），可指定状态与数据。</li>
 * </ul>
 *
 * @since 2025/5/13
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CIntResult<DATA> implements ICIntResult<DATA> {

    Integer code;

    String message;

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
    public static <DATA> CIntResult<DATA> newInstance(Integer code, String message, DATA data) {
        return CIntResult.<DATA>builder()
            .code(code)
            .message(message)
            .data(data)
            .build();
    }

    /**
     * 成功结果（无数据）
     * <ul>
     *   <li>{@code success()} / {@code success(httpStatus, data)} / {@code success(data)}：成功结果</li>
     * </ul>
     *
     * @param <DATA> 数据类型
     * @return 成功结果
     */
    public static <DATA> CIntResult<DATA> success() {
        return success(null);
    }

    /**
     * 成功结果（指定状态）
     *
     * @param httpStatus 状态
     * @param data       数据
     * @param <DATA>     数据类型
     * @return 成功结果
     */
    public static <DATA> CIntResult<DATA> success(@NonNull HttpStatus httpStatus, DATA data) {
        return newInstance(
            httpStatus.value(),
            httpStatus.getReasonPhrase(),
            data
        );
    }

    /**
     * 成功结果
     *
     * @param data   数据
     * @param <DATA> 数据类型
     * @return 成功结果
     */
    public static <DATA> CIntResult<DATA> success(DATA data) {
        return success(
            HttpStatus.OK,
            data
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
    public static <DATA> CIntResult<DATA> error(Integer code, String message) {
        return newInstance(code, message, null);
    }

    /**
     * 失败结果（指定状态）
     *
     * @param httpStatus 状态
     * @param message    消息
     * @param <DATA>     数据类型
     * @return 失败结果
     */
    public static <DATA> CIntResult<DATA> error(@NonNull HttpStatus httpStatus, String message) {
        return error(
            httpStatus.value(),
            StrUtil.nullToDefault(message, httpStatus.getReasonPhrase())
        );
    }

    /**
     * 失败结果（仅状态）
     *
     * @param httpStatus 状态
     * @param <DATA>     数据类型
     * @return 失败结果
     */
    public static <DATA> CIntResult<DATA> error(@NonNull HttpStatus httpStatus) {
        return error(
            httpStatus,
            null
        );
    }

    /**
     * 失败结果（默认 500）
     *
     * @param message 消息
     * @param <DATA>  数据类型
     * @return 失败结果
     */
    public static <DATA> CIntResult<DATA> error(String message) {
        return error(
            HttpStatus.INTERNAL_SERVER_ERROR,
            message
        );
    }

}
