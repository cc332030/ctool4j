package com.c332030.ctool4j.core.jackson;

import com.c332030.ctool4j.definition.annotation.CLogBlob;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * <p>
 * Description: 长文本字段序列化器，按值规模决定打印真实内容或按类型输出占位符
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CLogBlobSerializer} 为长文本/大对象字段序列化器，扩展 {@code JsonSerializer&lt;Object&gt;}，按规模阈值决定输出： 规模可评估且不超过阈值时打印真实内容，超过则输出占位符并附规模信息（如 {@code &lt;BLOB:chars=1024&gt;}、{@code &lt;BLOB:list=500&gt;}）。</p>
 * <ul>
 *   <li>阈值由构造参数传入，来源于 {@code @CLogBlob.maxSize()}；无参构造用 {@code CLogBlob.DEFAULT_MAX_SIZE}、单例 {@code INSTANCE} 同。</li>
 *   <li>占位符只暴露"形状 + 规模"，不泄露真实内容。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>输出按规模与值类型生成</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>输出</th>
 *   </tr>
 *   <tr>
 *     <td>规模 ≤ 阈值（含等于）</td>
 *     <td>真实内容（原始 JSON 形状）</td>
 *   </tr>
 *   <tr>
 *     <td>{@code CharSequence} 规模 &gt; 阈值</td>
 *     <td>{@code &lt;BLOB:chars=字符数&gt;}</td>
 *   </tr>
 *   <tr>
 *     <td>{@code Collection} 规模 &gt; 阈值</td>
 *     <td>{@code &lt;BLOB:list=元素数&gt;}</td>
 *   </tr>
 *   <tr>
 *     <td>{@code Map} 规模 &gt; 阈值</td>
 *     <td>{@code &lt;BLOB:map=条目数&gt;}</td>
 *   </tr>
 *   <tr>
 *     <td>{@code byte[]} 规模 &gt; 阈值</td>
 *     <td>{@code &lt;BLOB:bytes=字节数&gt;}</td>
 *   </tr>
 *   <tr>
 *     <td>其他数组规模 &gt; 阈值</td>
 *     <td>{@code &lt;BLOB:array=元素数&gt;}</td>
 *   </tr>
 *   <tr>
 *     <td>规模无法评估（null、流、自定义对象）</td>
 *     <td>{@code &lt;BLOB&gt;}</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>长文本/大对象字段（字符串、集合、Map、数组、字节数组）的日志序列化。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>牺牲大内容的可见性换取日志安全（仅保留规模信息）；规模本身仍会出现在日志中。</li>
 *   <li>不超过阈值时输出真实内容：阈值调大即等于放宽日志体积，需业务方自行权衡。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>规模判定与类型美化</b></p>
 * <ul>
 *   <li>规模为 O(1) 读取（{@code length()}/{@code size()}），不遍历内容、不改动原值；无法评估返回 -1，按占位处理。</li>
 *   <li>超过阈值时容器类型若统一输出 {@code &lt;BLOB&gt;} 会丢失形状信息（如集合字段在日志里与普通字符串无区别），
 *   故按类型附规模：既不刷屏，又能判断"是不是集合/Map/数组、有多大"。</li>
 *   <li>不超过阈值时经 {@code SerializerProvider#defaultSerializeValue} 输出真实内容，保持原始 JSON 形状（集合仍是数组）。</li>
 * </ul>
 *
 * @since 2026/8/13
 * @version 1.2
 */
public class CLogBlobSerializer extends JsonSerializer<Object> {

    /**
     * 长文本/大对象字段打印时的固定占位符（规模无法评估时使用）
     */
    public static final String BLOB_PLACEHOLDER = "<BLOB>";

    /**
     * 带规模信息的占位符前缀，完整格式 {@code <BLOB:类型=规模>}
     */
    public static final String BLOB_SIZE_PREFIX = "<BLOB:";

    /**
     * 默认阈值单例（等价于 {@code new CLogBlobSerializer(CLogBlob.DEFAULT_MAX_SIZE)}）
     */
    public static final CLogBlobSerializer INSTANCE = new CLogBlobSerializer();

    /**
     * 规模阈值（含）：规模不超过则打印真实内容，超过则输出占位符
     */
    private final int maxSize;

    /**
     * 构建序列化器（使用 {@link CLogBlob#DEFAULT_MAX_SIZE} 默认阈值）
     */
    public CLogBlobSerializer() {
        this(CLogBlob.DEFAULT_MAX_SIZE);
    }

    /**
     * 构建序列化器
     *
     * @param maxSize 规模阈值（含）
     */
    public CLogBlobSerializer(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * 按规模输出真实内容或占位符
     *
     * @param value       原始值
     * @param gen         生成器
     * @param serializers 序列化提供者
     */
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

        // 规模可评估且不超过阈值：打印真实内容（小数据便于排障）；否则输出占位符（附规模）
        if (printContent(value)) {
            serializers.defaultSerializeValue(value, gen);
            return;
        }

        gen.writeString(placeholder(value));
    }

    /**
     * 判断是否打印真实内容：规模可评估且不超过阈值
     *
     * @param value 原始值
     * @return 是否打印真实内容
     */
    private boolean printContent(Object value) {
        int size = sizeOf(value);
        return 0 <= size && size <= maxSize;
    }

    /**
     * 计算值的规模：{@code CharSequence} 字符数、{@code Collection} 元素数、{@code Map} 条目数、数组长度
     *
     * @param value 原始值（可为 null）
     * @return 规模，无法评估（null、流、自定义对象等）返回 -1
     */
    public static int sizeOf(Object value) {

        if (null == value) {
            return -1;
        }
        if (value instanceof byte[]) {
            return ((byte[]) value).length;
        }
        if (value instanceof CharSequence) {
            return ((CharSequence) value).length();
        }
        if (value instanceof Collection) {
            return ((Collection<?>) value).size();
        }
        if (value instanceof Map) {
            return ((Map<?, ?>) value).size();
        }
        if (value instanceof Object[]) {
            return ((Object[]) value).length;
        }

        return -1;
    }

    /**
     * 按值类型生成占位符：只暴露形状与规模，不输出真实内容
     *
     * @param value 原始值（可为 null）
     * @return 占位符（无规模可算时为 {@link #BLOB_PLACEHOLDER}）
     */
    public static String placeholder(Object value) {

        if (null == value) {
            return BLOB_PLACEHOLDER;
        }
        if (value instanceof byte[]) {
            return sizePlaceholder("bytes", ((byte[]) value).length);
        }
        if (value instanceof CharSequence) {
            return sizePlaceholder("chars", ((CharSequence) value).length());
        }
        if (value instanceof Collection) {
            return sizePlaceholder("list", ((Collection<?>) value).size());
        }
        if (value instanceof Map) {
            return sizePlaceholder("map", ((Map<?, ?>) value).size());
        }
        if (value instanceof Object[]) {
            return sizePlaceholder("array", ((Object[]) value).length);
        }

        return BLOB_PLACEHOLDER;
    }

    /**
     * 拼接带规模信息的占位符
     *
     * @param type 类型标识（chars/list/map/bytes/array）
     * @param size 规模
     * @return 占位符
     */
    private static String sizePlaceholder(String type, int size) {
        return BLOB_SIZE_PREFIX + type + "=" + size + ">";
    }

}
