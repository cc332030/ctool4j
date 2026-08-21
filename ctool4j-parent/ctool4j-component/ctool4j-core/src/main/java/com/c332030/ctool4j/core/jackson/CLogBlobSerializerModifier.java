package com.c332030.ctool4j.core.jackson;

import com.c332030.ctool4j.definition.annotation.CLogBlob;
import com.fasterxml.jackson.databind.JsonSerializer;

/**
 * <p>
 * Description: 长文本字段检测：标注 {@link CLogBlob} 的字段序列化时替换为固定占位符
 * </p>
 * <p>仅注册到日志专用 ObjectMapper（CJacksonUtils.OBJECT_MAPPER_LOG / CJsonUtils.toJsonLog），
 * 日志打印链路统一生效（toLogArgs 参数打印等）；全局 ObjectMapper 不注册，业务序列化输出真实内容</p>
 *
 * @since 2026/8/13
 * @see "doc/design/core/CLogBlobSerializerModifier.adoc"
 * @see "doc/design/core/CLogBlobSerializerModifierTests.adoc"
 */
public class CLogBlobSerializerModifier extends CLogFieldSerializerModifier<CLogBlob> {

    /**
     * 构建修改器
     */
    public CLogBlobSerializerModifier() {
        super(CLogBlob.class);
    }

    /**
     * 创建占位符序列化器：忽略注解参数，固定输出 {@link CLogBlobSerializer#BLOB_PLACEHOLDER}
     *
     * @param annotation 注解实例
     * @return 占位符序列化器
     */
    @Override
    protected JsonSerializer<Object> createSerializer(CLogBlob annotation) {
        return CLogBlobSerializer.INSTANCE;
    }

}
