package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.definition.interfaces.ICRes;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * <p>
 * Description: CResUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CResUtils} 为响应信息格式化工具类，提供：</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>formatMessage res 为 null</td>
 *     <td>返回 msgExtend</td>
 *   </tr>
 *   <tr>
 *     <td>formatMessage msgExtend 为空</td>
 *     <td>返回 res.getMsg()</td>
 *   </tr>
 *   <tr>
 *     <td>formatResMessage res 为 null</td>
 *     <td>返回 msgExtend</td>
 *   </tr>
 *   <tr>
 *     <td>formatResMessage msgExtend 为空</td>
 *     <td>返回 {@code [code] msg}（不追加）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>统一格式化接口/异常响应信息，便于日志与错误提示拼接。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅支持 {@code ICRes} 接口（getCode/getMsg）；其他响应类型需自行适配。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>空值语义统一（res null 返回扩展信息、扩展空返回 msg），调用方无需重复判空。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义约定</b></p>
 * <ul>
 *   <li>{@code formatMessage}：res 为 null 时返回 msgExtend；msgExtend 为空（null/空白）时返回 res.getMsg()；</li>
 *   <li>否则返回 {@code res.getMsg() + ": " + msgExtend}。</li>
 *   <li>{@code formatResMessage}：res 为 null 时返回 msgExtend；否则返回 {@code "[code] msg}，msgExtend 非空时</li>
 *   <li>追加 {@code ": " + msgExtend}。</li>
 * </ul>
 *
 * @since 2025/10/24
 * @version 1.0
 */
@UtilityClass
public class CResUtils {

    /**
     * 格式化响应信息
     * <ul>
     *   <li>{@code formatMessage(ICRes)} / {@code formatMessage(ICRes, String msgExtend)}：格式化响应信息</li>
     * </ul>
     *
     * @param res 响应
     * @return 响应信息
     */
    public String formatMessage(ICRes<?> res) {
        return formatMessage(res, null);
    }

    /**
     * 格式化响应信息
     * @param res 响应
     * @param msgExtend 扩展信息
     * @return 响应信息
     */
    public String formatMessage(ICRes<?> res, String msgExtend) {

        if(res == null) {
            return msgExtend;
        }

        if(StrUtil.isEmpty(msgExtend)){
            return res.getMsg();
        }

        return res.getMsg() + ": " + msgExtend;
    }

    /**
     * 格式化响应信息-带错误码
     * <ul>
     *   <li>{@code formatResMessage(ICRes)} / {@code formatResMessage(ICRes, String msgExtend)}：带错误码格式化响应信息</li>
     * </ul>
     *
     * @param res 响应
     * @return 响应信息-带错误码
     */
    public String formatResMessage(ICRes<?> res) {
        return formatResMessage(res, null);
    }

    /**
     * 格式化响应信息-带错误码
     * @param res 响应
     * @param msgExtend 扩展信息
     * @return 响应信息-带错误码
     */
    public String formatResMessage(ICRes<?> res, String msgExtend) {

        if(res == null) {
            return msgExtend;
        }

        val sb = new StringBuilder();
        sb.append("[");
        sb.append(res.getCode());
        sb.append("] ");
        sb.append(res.getMsg());

        if(StrUtil.isNotEmpty(msgExtend)) {
            sb.append(": ");
            sb.append(msgExtend);
        }

        return sb.toString();
    }

}
