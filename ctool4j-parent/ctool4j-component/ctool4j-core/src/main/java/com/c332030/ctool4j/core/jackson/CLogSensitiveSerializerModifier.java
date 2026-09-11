package com.c332030.ctool4j.core.jackson;

import com.c332030.ctool4j.definition.annotation.CLogSensitive;
import com.fasterxml.jackson.databind.JsonSerializer;

/**
 * <p>
 * Description: 敏感字段检测：标注 {@link CLogSensitive} 的字段序列化时脱敏输出
 * </p>
 * <p>仅注册到日志专用 ObjectMapper（CJacksonUtils.OBJECT_MAPPER_LOG / CJsonUtils.toJsonLog），
 * 日志打印链路统一生效（toLogArgs 参数打印等）；全局 ObjectMapper 不注册，业务序列化输出真实内容</p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CLogSensitiveSerializerModifier} 为敏感字段序列化修改器，继承 {@code CLogFieldSerializerModifier&lt;CLogSensitive&gt;}， 检测标注 {@code @CLogSensitive} 的字段，日志序列化时脱敏输出。</p>
 * <ul>
 *   <li>仅注册到日志专用 ObjectMapper；全局 mapper 输出真实内容。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>日志 mapper 序列化 @CLogSensitive 字段</td>
 *     <td>脱敏输出</td>
 *   </tr>
 *   <tr>
 *     <td>全局 mapper 序列化 @CLogSensitive 字段</td>
 *     <td>输出真实内容</td>
 *   </tr>
 *   <tr>
 *     <td>@CLogSensitive 字段为 null</td>
 *     <td>日志 mapper 不输出</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>敏感字段（手机号等）日志打印脱敏。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>仅日志 mapper 生效，业务输出真实内容。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>序列化器创建</b></p>
 * <ul>
 *   <li>{@code createSerializer} 按注解 {@code prefixKeep}/{@code suffixKeep} 创建 {@code CSensitiveSerializer}，保留位数可配。</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.0
 */
public class CLogSensitiveSerializerModifier extends CLogFieldSerializerModifier<CLogSensitive> {

    /**
     * 构建修改器
     */
    public CLogSensitiveSerializerModifier() {
        super(CLogSensitive.class);
    }

    /**
     * 创建脱敏序列化器：按注解保留位数配置
     *
     * @param annotation 注解实例
     * @return 脱敏序列化器
     */
    @Override
    protected JsonSerializer<Object> createSerializer(CLogSensitive annotation) {
        return new CSensitiveSerializer(annotation.prefixKeep(), annotation.suffixKeep());
    }

}
