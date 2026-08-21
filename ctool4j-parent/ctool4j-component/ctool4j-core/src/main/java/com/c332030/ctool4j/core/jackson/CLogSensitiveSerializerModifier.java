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
 * @since 2026/8/16
 * @see doc/design/core/CLogSensitiveSerializerModifier.adoc
 * @see doc/design/core/CLogSensitiveSerializerModifierTests.adoc
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
