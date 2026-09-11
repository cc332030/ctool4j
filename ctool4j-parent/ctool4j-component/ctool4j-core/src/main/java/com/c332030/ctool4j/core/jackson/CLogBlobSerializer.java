package com.c332030.ctool4j.core.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * <p>
 * Description: 长文本字段序列化器，打印时跳过真实内容，输出固定占位符
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CLogBlobSerializer} 为长文本字段序列化器，扩展 {@code JsonSerializer&lt;Object&gt;}，序列化时跳过真实内容， 输出固定占位符 {@code BLOB_PLACEHOLDER = "&lt;BLOB&gt;"}。提供单例 {@code INSTANCE}。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>value 非空/空/null</td>
 *     <td>均输出 {@code &lt;BLOB&gt;} 占位符</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>长文本字段（如日志/请求体）序列化时隐藏真实内容，输出占位符。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>牺牲真实内容可见性换取日志安全（占位符）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>脱敏占位</b></p>
 * <ul>
 *   <li>无论 value 是否为 null/真实长文本，均输出 {@code &lt;BLOB&gt;}，避免长文本真实内容出现在日志中。</li>
 * </ul>
 *
 * @since 2026/8/13
 * @version 1.0
 */
public class CLogBlobSerializer extends JsonSerializer<Object> {

    /**
     * 长文本字段打印时的固定占位符
     */
    public static final String BLOB_PLACEHOLDER = "<BLOB>";

    /**
     * 单例实例
     */
    public static final CLogBlobSerializer INSTANCE = new CLogBlobSerializer();

    /**
     * 序列化为固定占位符，避免长文本真实内容出现在日志中
     *
     * @param value       原始值（内容被忽略）
     * @param gen         生成器
     * @param serializers 序列化提供者
     */
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(BLOB_PLACEHOLDER);
    }

}
