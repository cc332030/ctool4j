package com.c332030.ctool4j.spring.security.jackson.deserializer;

import com.c332030.ctool4j.spring.security.util.CGrantedAuthorityUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;

/**
 * <p>
 * Description: CGrantedAuthorityDeserializer
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CGrantedAuthorityDeserializer} 为 {@link GrantedAuthority} 的 Jackson 反序列化器：</p>
 * <ul>
 *   <li>对象形态 {@code {"authority":"ROLE_XXX"}} → 权限</li>
 *   <li>字符串形态 {@code "ROLE_XXX"} → 权限（兼容简写）</li>
 *   <li>结果统一取自权限常量池（{@link CGrantedAuthorityUtils}），相同 authority 复用同一实例</li>
 * </ul>
 * <p>提供单例 {@code INSTANCE}（模块中同时注册到 {@link GrantedAuthority} 与 {@link SimpleGrantedAuthority}）。</p>
 *
 * <h2>设计要点</h2>
 * <p><b>存在的原因</b></p>
 * <ul>
 *   <li>{@link GrantedAuthority} 是接口，序列化后 JSON 不含类型信息，Jackson 反序列化时无法推断具体实现类，
 *   会抛 {@code InvalidDefinitionException}（abstract types either need to be mapped to concrete types...）。
 *   会话类（如实现 {@code ICSecuritySession} 的类）含 {@code authorities} 字段并需持久化到 Redis 再读回时必然触发。</li>
 *   <li>序列化侧无需自研（Jackson 默认按 {@code getAuthority()} 输出同上形态），本类只补反序列化侧。</li>
 * </ul>
 * <p><b>实现约定</b></p>
 * <ul>
 *   <li>用 {@code readValueAsTree} 一次读尽当前节点，对象/字符串两种形态在同一处归一，避免依赖 token 顺序。</li>
 *   <li>authority 取不到或为空白即返回 null，不构造非法权限
 *   （{@link SimpleGrantedAuthority} 构造对空白入参会抛 {@code IllegalArgumentException}）。</li>
 *   <li>不采用 Spring 官方 {@code CoreJackson2Module}：其依赖 default typing（JSON 带 {@code @class}），
 *   与既有数据形态不兼容（详见 {@code CSecurityJacksonModule}）。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr><th>场景</th><th>兜底行为</th></tr>
 *   <tr><td>JSON null</td><td>返回 null</td></tr>
 *   <tr><td>字符串形态为空白</td><td>返回 null</td></tr>
 *   <tr><td>对象无 authority 字段、或值为 null/空白</td><td>返回 null</td></tr>
 *   <tr><td>既非对象也非文本的节点（数字、布尔、数组）</td><td>取不到 authority，返回 null（不抛异常）</td></tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>会话/用户对象中 {@link GrantedAuthority} 集合的 JSON 反序列化（Redis 会话读回等）。</li>
 *   <li>字段/集合声明为 {@link GrantedAuthority} 或 {@link SimpleGrantedAuthority} 均可（模块对两者注册同一反序列化器）。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不保留权限原文之外的信息：JSON 仅含 authority 时其余字段无处安放。</li>
 *   <li>字符串形态与对象形态混用同一集合时，两种形态均按各自规则归一，不做一致性校验。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>仅还原为 {@link SimpleGrantedAuthority}（实例来自常量池）；自定义 {@link GrantedAuthority} 实现无法通过本反序列化器还原，
 *   如需保留自定义类型应在业务侧用 {@code @JsonTypeInfo} 标注。</li>
 *   <li>authority <b>不 trim</b>：{@code " ROLE_A"} 原样还原（与 Spring {@link SimpleGrantedAuthority} 的语义一致），
 *   不静默改写权限值。</li>
 * </ul>
 *
 * @since 2026/9/14
 * @version 1.0
 */
public class CGrantedAuthorityDeserializer extends JsonDeserializer<GrantedAuthority> {

    /**
     * 单例实例
     */
    public static final CGrantedAuthorityDeserializer INSTANCE = new CGrantedAuthorityDeserializer();

    /**
     * authority 字段名
     */
    private static final String FIELD_AUTHORITY = "authority";

    /**
     * 反序列化权限：对象取 authority 字段、字符串直接作为 authority，null/空白返回 null
     *
     * @param parser  解析器
     * @param context 反序列化上下文
     * @return 权限；无有效 authority 时返回 null
     */
    @Override
    public GrantedAuthority deserialize(JsonParser parser, DeserializationContext context) throws IOException {

        JsonNode node = parser.readValueAsTree();
        if(null == node || node.isNull()) {
            return null;
        }

        String authority = node.isTextual()
            ? node.asText()
            : node.path(FIELD_AUTHORITY).asText(null);

        // 统一经权限常量池构造：同值复用同一实例；null/空白在此返回 null
        return CGrantedAuthorityUtils.get(authority);
    }

}
