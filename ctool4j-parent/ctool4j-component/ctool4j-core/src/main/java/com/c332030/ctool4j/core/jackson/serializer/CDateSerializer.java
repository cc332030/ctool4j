package com.c332030.ctool4j.core.jackson.serializer;

import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Date;

/**
 * <p>
 * Description: CDateSerializer
 * </p>
 *
 * @since 2025/4/14
 */
public class CDateSerializer extends JsonSerializer<Date> {

    /**
     * 单例实例
     */
    public static final CDateSerializer INSTANCE = new CDateSerializer();

    /**
     * 序列化为日期时间字符串
     *
     * @param value       日期
     * @param gen         生成器
     * @param serializers 序列化提供者
     */
    @Override
    public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(DateUtil.formatDateTime(value));
    }
}
