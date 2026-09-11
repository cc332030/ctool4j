package com.c332030.ctool4j.core.util;

import cn.hutool.core.codec.Base62;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

/**
 * <p>
 * Description: CBase62Utils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CBase62Utils} 为 Base62 编解码工具类，基于 hutool 的 {@code Base62} 封装，提供字节数组与 Base62 字符串的双向转换。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>encode 传入空字节数组/null</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>decode 传入空字符串/null</td>
 *     <td>返回 null</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>二进制数据的紧凑文本表示（URL 安全场景可选用 Base62 字符集）。</li>
 *   <li>需要与 Base62 互操作的数据传输/存储。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅支持字节数组↔字符串互转，不提供字节数组↔数字等其他语义。</li>
 *   <li>非法 Base62 字符串（含非 Base62 字符）解码行为由底层 hutool 决定，本类不额外校验。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>空入参返回 null 与 hutool 底层行为解耦，由本类显式控制，保证空值语义稳定。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义约定</b></p>
 * <ul>
 *   <li>空入参（encode 的空字节数组/null、decode 的空字符串/null）统一返回 null，而非抛异常或</li>
 *   <li>返回空结果，调用方据此可安全判断"未编码/未解码"。</li>
 *   <li>编码与解码互为逆操作：{@code decode(encode(bytes))} 恢复原字节数组。</li>
 * </ul>
 * <p><b>实现方式</b></p>
 * <ul>
 *   <li>直接委托 hutool {@code Base62.encode/decode}，本项目不重复实现字符集转换，避免引入与既有库</li>
 *   <li>不一致的编码结果。</li>
 *   <li>入参空值判断前置，避免空入参传入底层库产生非预期行为。</li>
 * </ul>
 *
 * @since 2026/1/4
 * @version 1.0
 */
@UtilityClass
public class CBase62Utils {

    /**
     * 字节数组 Base62 编码
     * <ul>
     *   <li>{@code encode(byte[])}：字节数组编码为 Base62 字符串；空数组/null 返回 null</li>
     * </ul>
     *
     * @param bytes 字节数组
     * @return Base62 字符串，字节数组为空时返回 null
     */
    public String encode(byte[] bytes) {

        if(ArrayUtil.isEmpty(bytes)) {
            return null;
        }
        return Base62.encode(bytes);
    }

    /**
     * Base62 字符串解码
     * <ul>
     *   <li>{@code decode(String)}：Base62 字符串解码为字节数组；空字符串/null 返回 null</li>
     * </ul>
     *
     * @param value Base62 字符串
     * @return 字节数组，字符串为空时返回 null
     */
    public byte[] decode(String value) {
        if(StrUtil.isEmpty(value)) {
            return null;
        }
        return Base62.decode(value);
    }

}
