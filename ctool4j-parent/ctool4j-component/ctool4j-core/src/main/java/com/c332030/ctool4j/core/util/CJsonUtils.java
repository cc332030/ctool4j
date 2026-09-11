package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.jackson.CJacksonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Description: CJsonUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>序列化：{@code toJson} / {@code toJsonSnakeCase} / {@code toJsonNonNull} / {@code toJsonLog}</li>
 *   <li>{@code fromJsonStringValue}</li>
 *   <li>对象转换：{@code convert} / {@code convertSnakeCase}</li>
 *   <li>转 Map：{@code toMap} / {@code toMapSnakeCase} / {@code toMapStringValue} / {@code toMapStringValueSnakeCase}</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>toJson(null)</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>fromJson null/空/空白</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>toJsonNonNull(null)</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>toMap(null)</td>
 *     <td>返回 null</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>对象与 JSON 互转、驼峰/下划线互转、Long 安全序列化、日志输出（toJsonLog）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>基于 Jackson 默认/配置 ObjectMapper，复杂自定义序列化需传入 ObjectMapper 重载。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>Long 序列化为字符串防止前端精度丢失，是核心取舍。</li>
 *   <li>toMap 保留 Long 数值类型（区别于 toMapStringValue 的字符串值）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>序列化语义</b></p>
 * <ul>
 *   <li>{@code toJson}：null 入参返回 null；<b>Long 序列化为字符串</b>（{@code "id":"1"}），避免前端溢出（易错点）。</li>
 *   <li>{@code toJsonSnakeCase}：驼峰字段转下划线（{@code userName} → {@code user_name}）。</li>
 *   <li>{@code toJsonNonNull}：null 字段不输出。</li>
 * </ul>
 * <p><b>反序列化语义</b></p>
 * <ul>
 *   <li>{@code fromJson}：null/空/空白入参返回 null；非法结构抛 Jackson 异常（MismatchedInputException 等）。</li>
 *   <li>{@code fromJsonSnakeCase}：下划线字段转驼峰。</li>
 *   <li>{@code fromJsonList}：解析为 {@code List&lt;Map&lt;String,Object&gt;&gt;}；{@code fromJsonStringValue}：值为 String 的 Map。</li>
 * </ul>
 * <p><b>对象转换与转 Map</b></p>
 * <ul>
 *   <li>{@code convert}：对象经 JSON 中转（bean→map/bean）。</li>
 *   <li>{@code toMap}：对象转 {@code Map&lt;String,Object&gt;}，Long 保留数值类型（Q11 修复，不再经 JSON 转 String）。</li>
 *   <li>{@code toMapStringValue}：值统一转 String（Long → {@code "1"}）。</li>
 * </ul>
 *
 * @author c332030
 * @since 2024/3/4
 * @version 1.0
 */
@UtilityClass
public class CJsonUtils {

    /**
     * 转 json
     *
     * @param object 对象
     * @param objectMapper 对象映射器
     * @return json
     */
    @SneakyThrows
    public String toJson(Object object, ObjectMapper objectMapper){
        if(null == object) {
            return null;
        }
        return objectMapper.writeValueAsString(object);
    }

    /**
     * 转 json
     *
     * @param object 源对象
     * @return json
     */
    public String toJson(Object object){
        if(null == object) {
            return null;
        }
        return toJson(object, CJacksonUtils.OBJECT_MAPPER);
    }

    /**
     * 转 json，驼峰会转成下划线
     * @param object 源对象
     * @return json
     */
    public String toJsonSnakeCase(Object object){
        return toJson(object, CJacksonUtils.OBJECT_MAPPER_SNAKE_CASE);
    }

    /**
     * 转 json，null值不参与转换
     * @param object 源对象
     * @return json
     */
    public String toJsonNonNull(Object object) {
        return toJson(object, CJacksonUtils.OBJECT_MAPPER_NON_NULL);
    }

    /**
     * 转 json（日志专用）
     * <p>不序列化 null 值；标注 CLogBlob 的字段输出 &lt;BLOB&gt; 占位符，避免长文本刷屏日志。
     * 仅日志打印链路使用（CLogUtils.toLogArgs），全局 mapper 无该行为</p>
     * @param object 源对象
     * @return json
     */
    public String toJsonLog(Object object) {
        return toJson(object, CJacksonUtils.OBJECT_MAPPER_LOG);
    }

    /**
     * 从 json 转为对象
     * <ul>
     *   <li>反序列化：{@code fromJson(json, Class|TypeReference)} / {@code fromJsonSnakeCase} / {@code fromJsonList} /</li>
     * </ul>
     *
     * @param json json
     * @param tClass 目标对象类型
     * @param objectMapper 对象映射器
     * @return 目标对象
     * @param <T> 目标对象泛型
     */
    @SneakyThrows
    public <T> T fromJson(String json, Class<T> tClass, ObjectMapper objectMapper) {
        if(StrUtil.isBlank(json)) {
            return null;
        }
        return objectMapper.readValue(json, tClass);
    }

    /**
     * 从 json 转为对象
     * @param json json
     * @param typeReference 目标对象类型
     * @param objectMapper 映射器
     * @return 目标对象
     * @param <T> 目标对象泛型
     */
    @SneakyThrows
    public <T> T fromJson(String json, TypeReference<T> typeReference, ObjectMapper objectMapper) {
        if(StrUtil.isBlank(json)) {
            return null;
        }
        return objectMapper.readValue(json, typeReference);
    }

    /**
     * 从 json 转为对象
     * @param json json
     * @param tClass 源对象类型
     * @return 目标对象
     * @param <T> 目标对象泛型
     */
    public <T> T fromJson(String json, Class<T> tClass) {
        return fromJson(json, tClass, CJacksonUtils.OBJECT_MAPPER);
    }

    /**
     * 从 json 转为对象
     * @param json json
     * @param typeReference 源对象类型
     * @return 源对象
     * @param <T> 源对象泛型
     */
    public <T> T fromJson(String json, TypeReference<T> typeReference) {
        return fromJson(json, typeReference, CJacksonUtils.OBJECT_MAPPER);
    }

    /**
     * 从 json 转为对象
     * @param json json
     * @param tClass 源对象类型
     * @return 源对象
     * @param <T> 源对象泛型
     */
    public <T> T fromJsonSnakeCase(String json, Class<T> tClass){
        return fromJson(json, tClass, CJacksonUtils.OBJECT_MAPPER_SNAKE_CASE);
    }

    /**
     * 从 json 转为对象
     * @param json json
     * @param typeReference 源对象类型
     * @return 源对象
     * @param <T> 源对象泛型
     */
    public <T> T fromJsonSnakeCase(String json, TypeReference<T> typeReference){
        return fromJson(json, typeReference, CJacksonUtils.OBJECT_MAPPER_SNAKE_CASE);
    }

    /**
     * 对象转换
     * @param object 源对象
     * @param typeReference 目标类型
     * @param <T> 目标泛型
     * @return 目标对象
     */
    public <T> T convert(Object object, TypeReference<T> typeReference) {
        return fromJson(toJson(object), typeReference);
    }

    /**
     * 对象转换
     * @param object 源对象
     * @param typeReference 目标类型
     * @param <T> 目标泛型
     * @return 目标对象
     */
    public <T> T convertSnakeCase(Object object, TypeReference<T> typeReference) {
        return fromJsonSnakeCase(toJsonSnakeCase(object), typeReference);
    }

    /**
     * 对象转换
     * @param object 源对象
     * @param tClass 目标类型
     * @param <T> 目标泛型
     * @return 目标对象
     */
    public <T> T convert(Object object, Class<T> tClass) {
        return fromJson(toJson(object), tClass);
    }

    /**
     * 对象转换，驼峰转下划线
     * @param object 源对象
     * @param tClass 目标类型
     * @param <T> 目标泛型
     * @return 目标对象
     */
    public <T> T convertSnakeCase(Object object, Class<T> tClass) {
        return fromJsonSnakeCase(toJsonSnakeCase(object), tClass);
    }

    /**
     * 转 map
     * <p>使用 OBJECT_MAPPER_NATIVE：Long/BigDecimal 保留数值类型，避免经 JSON 中转后变 String
     * （与 CBeanUtils.toMap 的类型语义一致）；其余 JSON 语义（属性名、@JsonIgnore、日期格式）与 OBJECT_MAPPER 一致</p>
     * @param object 源对象
     * @return map
     */
    public Map<String, Object> toMap(Object object) {
        return fromJson(
            toJson(object, CJacksonUtils.OBJECT_MAPPER_NATIVE),
            CMapUtils.MAP_STRING_OBJECT_TYPE_REFERENCE,
            CJacksonUtils.OBJECT_MAPPER_NATIVE
        );
    }

    /**
     * 转 map，驼峰转下划线
     * @param object 源对象
     * @return map
     */
    public Map<String, Object> toMapSnakeCase(Object object) {
        return fromJson(toJsonSnakeCase(object));
    }

    /**
     * 从 json 转为 map，下划线转驼峰
     * @param json json
     * @return map
     */
    public Map<String, Object> fromJson(String json) {
        return fromJson(json, CMapUtils.MAP_STRING_OBJECT_TYPE_REFERENCE);
    }

    /**
     * 从 json 转为 list map
     * @param json json
     * @return list map
     */
    public List<Map<String, Object>> fromJsonList(String json) {
        return fromJson(json, CMapUtils.LIST_MAP_STRING_OBJECT_TYPE_REFERENCE);
    }

    /**
     * 转 map string value，下划线转驼峰
     * @param object 源对象
     * @return map string value
     */
    public Map<String, String> toMapStringValue(Object object) {
        return fromJsonStringValue(toJson(object));
    }

    /**
     * 转 map string value，驼峰转下划线
     * @param object 源对象
     * @return map string value
     */
    public Map<String, String> toMapStringValueSnakeCase(Object object) {
        return fromJsonStringValue(toJsonSnakeCase(object));
    }

    /**
     * 从 json 转为 map string value
     * @param json json
     * @return map string value
     */
    public Map<String, String> fromJsonStringValue(String json) {
        return fromJson(json, CMapUtils.MAP_STRING_STRING_TYPE_REFERENCE);
    }

}
