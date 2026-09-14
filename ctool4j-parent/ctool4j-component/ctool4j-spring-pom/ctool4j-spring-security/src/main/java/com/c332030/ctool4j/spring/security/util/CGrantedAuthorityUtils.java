package com.c332030.ctool4j.spring.security.util;

import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CMapUtils;
import com.c332030.ctool4j.core.validation.CValidUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Description: CGrantedAuthorityUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CGrantedAuthorityUtils} 为 {@link GrantedAuthority} 常量池（intern 池）：</p>
 * <ul>
 *   <li>{@code get(String authority)}：按 authority 取权限，相同 authority 全局复用同一实例</li>
 *   <li>{@code get(Collection&lt;String&gt; authorities)}：批量取权限（保持顺序，过滤无效项）</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <p><b>存在的原因</b></p>
 * <ul>
 *   <li>权限是高频、值域有限的枚举式数据（{@code ROLE_ADMIN} 等）。会话每次从 Redis 读回都会新建
 *   {@link SimpleGrantedAuthority}，热路径上会产生大量同值对象、加重 GC；常量池保证同值唯一实例。</li>
 * </ul>
 * <p><b>池化实现</b></p>
 * <ul>
 *   <li>以 authority 字符串为键、{@code ConcurrentHashMap} 承载；{@code computeIfAbsent} 保证同一键只创建
 *   一次（并发安全：同一 authority 必然拿到同一实例）。</li>
 *   <li>{@link SimpleGrantedAuthority} 不可变（final 字段、无 setter），实例共享无副作用。</li>
 *   <li>池内只承载 {@link SimpleGrantedAuthority}：值唯一与实现类唯一是一致的取舍（JSON 场景本也无法保留实现类）。</li>
 *   <li>构造入口统一收敛到本类：反序列化（{@code CGrantedAuthorityDeserializer}）、匿名权限
 *   （{@code CSpringSecurityUtils.ANONYMOUS_AUTHORITIES}）均取自本池。</li>
 * </ul>
 * <p><b>键的匹配语义</b></p>
 * <ul>
 *   <li>按 authority <b>原文精确匹配</b>：不做 trim、区分大小写。故 {@code "ROLE_A"}、{@code "role_a"}、
 *   {@code " ROLE_A"} 是三个不同条目，且还原出的 authority 与入参逐字符一致（不改变数据，只做复用）。</li>
 *   <li>不归一化是有意取舍：归一化会改变权限值本身（Spring 的 {@link SimpleGrantedAuthority} 同样不归一化），
 *   权限判定依赖精确字符串，静默改写风险高于省下的那点内存。</li>
 *   <li>null/空白一律不入池并返回 null：{@link SimpleGrantedAuthority} 构造对空白入参抛
 *   {@code IllegalArgumentException}（内部断言），本类在入口处拦掉，避免把非法权限构造异常抛给调用方。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr><th>场景</th><th>兜底行为</th></tr>
 *   <tr><td>authority 为 null/空白</td><td>返回 null（不入池，不触发构造断言）</td></tr>
 *   <tr><td>批量入参为 null/空集合</td><td>返回空 List</td></tr>
 *   <tr><td>批量入参含 null/空白项</td><td>跳过该项，不影响其他项与顺序</td></tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>权限对象的构造入口：会话读回反序列化、匿名权限、业务侧按角色名构造权限。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>自定义 {@link GrantedAuthority} 实现不适用：池内实例统一为 {@link SimpleGrantedAuthority}。</li>
 *   <li>需要携带额外状态（如带数据范围的权限）的对象不适用。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>池无上限、无淘汰：权限名值域有限时长期驻留内存可接受；但池的写入源包含<b>反序列化数据</b>，
 *   若该数据不可信（会话被篡改）或权限码按用户动态生成，值域由数据源决定，需调用方自行控制。</li>
 *   <li>以「值唯一」换取「实现类唯一」：无法保留自定义权限实现类（JSON 反序列化场景本也无法保留）。</li>
 * </ul>
 *
 * @since 2026/9/14
 * @version 1.0
 */
@UtilityClass
public class CGrantedAuthorityUtils {

    /**
     * 权限常量池：authority -> 权限实例
     */
    private final Map<String, GrantedAuthority> AUTHORITY_POOL = new ConcurrentHashMap<>();

    /**
     * 获取权限（常量池，同值唯一）
     *
     * <p>authority 按原文入池：不 trim、区分大小写；null/空白返回 null 且不入池。</p>
     *
     * @param authority 权限名
     * @return 权限；authority 为 null/空白时返回 null
     */
    public GrantedAuthority get(String authority) {

        if(CValidUtils.isNotValid(authority)) {
            return null;
        }

        return CMapUtils.computeIfAbsent(AUTHORITY_POOL, authority, () -> new SimpleGrantedAuthority(authority));
    }

    /**
     * 批量获取权限（常量池，同值唯一，保持入参顺序）
     *
     * <p>无效项（null/空白）被跳过而非报错：权限集合中偶发的脏数据不应导致整批构造失败。</p>
     *
     * @param authorities 权限名集合
     * @return 权限列表；入参为 null/空时返回空 List
     */
    public List<GrantedAuthority> get(Collection<String> authorities) {

        if(CValidUtils.isNotValid(authorities)) {
            return CList.of();
        }

        // 显式声明元素类型：newList 的泛型无目标类型可推导（val 会推成 List<Object>）
        List<GrantedAuthority> list = CCollUtils.newList(authorities.size());
        for (val authority : authorities) {
            CCollUtils.addIgnoreNull(list, get(authority));
        }

        return list;
    }

}
