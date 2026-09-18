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
 * <h2>能力目录</h2>
 * <p>{@code CLogBlobSerializerModifier} 为长文本字段序列化修改器，继承 {@code CLogFieldSerializerModifier&lt;CLogBlob&gt;}， 检测标注 {@code @CLogBlob} 的字段，日志序列化时替换为占位符（按值类型附规模，见 {@code CLogBlobSerializer}）。</p>
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
 *     <td>日志 mapper 序列化 @CLogBlob 字段，规模不超过 {@code maxSize}</td>
 *     <td>打印真实内容</td>
 *   </tr>
 *   <tr>
 *     <td>日志 mapper 序列化 @CLogBlob 字段，规模超过 {@code maxSize}</td>
 *     <td>输出占位符：{@code &lt;BLOB&gt;}（规模无法评估）或 {@code &lt;BLOB:list=5&gt;}（字符串/集合/Map/数组按类型附规模）</td>
 *   </tr>
 *   <tr>
 *     <td>全局 mapper 序列化 @CLogBlob 字段</td>
 *     <td>输出真实内容</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>长文本字段日志打印脱敏。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>仅日志 mapper 生效，业务输出真实内容（占位符仅用于日志）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>序列化器创建</b></p>
 * <ul>
 *   <li>{@code createSerializer} 按注解 {@code maxSize()} 创建 {@code CLogBlobSerializer}：规模不超过阈值打印真实内容，超过则输出占位符。</li>
 * </ul>
 *
 * @since 2026/8/13
 * @version 1.2
 */
public class CLogBlobSerializerModifier extends CLogFieldSerializerModifier<CLogBlob> {

    /**
     * 构建修改器
     */
    public CLogBlobSerializerModifier() {
        super(CLogBlob.class);
    }

    /**
     * 创建序列化器：按注解 {@code maxSize} 阈值决定是否打印真实内容
     * <p>每个字段按其注解阈值创建（不可共用单例：阈值随注解配置）</p>
     *
     * @param annotation 注解实例
     * @return 字段序列化器
     */
    @Override
    protected JsonSerializer<Object> createSerializer(CLogBlob annotation) {
        return new CLogBlobSerializer(annotation.maxSize());
    }

}
