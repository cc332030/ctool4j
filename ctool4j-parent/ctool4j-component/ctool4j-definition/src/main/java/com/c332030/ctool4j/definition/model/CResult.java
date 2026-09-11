package com.c332030.ctool4j.definition.model;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.definition.model.result.ICIntMsgResult;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

/**
 * <p>
 * Description: CResult
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CResult&lt;DATA&gt;} 为通用结果封装，实现 {@code ICIntMsgResult&lt;DATA&gt;}，含 {@code code}/{@code msg}/{@code data} 字段，提供：</p>
 * <ul>
 *   <li>{@code getMessage()}：委托 msg</li>
 * </ul>
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
 *   <tr>
 *     <td>error message 为空</td>
 *     <td>保持空消息</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>接口/服务统一结果封装。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>code 为 Integer，msg 为消息，data 为负载。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>成功/失败工厂</b></p>
 * <ul>
 *   <li>{@code success} 用 {@code HttpStatus.OK}（200）。</li>
 * </ul>
 *
 * @since 2025/5/13
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class CResult<DATA> implements ICIntMsgResult<DATA> {

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
    public static <DATA> CResult<DATA> newInstance(Integer code, String message, DATA data) {
        return CResult.<DATA>builder()
            .code(code)
            .msg(message)
            .data(data)
            .build();
    }

    /**
     * 成功结果（无数据）
     * <ul>
     *   <li>{@code success()} / {@code success(data)}：成功结果（HttpStatus.OK=200）</li>
     * </ul>
     *
     * @param <DATA> 数据类型
     * @return 成功结果
     */
    public static <DATA> CResult<DATA> success() {
        return success(null);
    }

    /**
     * 成功结果
     *
     * @param data   数据
     * @param <DATA> 数据类型
     * @return 成功结果
     */
    public static <DATA> CResult<DATA> success(DATA data) {
        return newInstance(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), data);
    }

    /**
     * 失败结果（默认 500）
     *
     * @param message 消息
     * @param <DATA>  数据类型
     * @return 失败结果
     */
    public static <DATA> CResult<DATA> error(String message) {
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
    public static <DATA> CResult<DATA> error(Integer code, String message) {
        return newInstance(code, message, null);
    }

}
