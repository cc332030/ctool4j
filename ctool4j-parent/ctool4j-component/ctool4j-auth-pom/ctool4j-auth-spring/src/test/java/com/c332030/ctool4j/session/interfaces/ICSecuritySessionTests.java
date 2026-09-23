package com.c332030.ctool4j.session.interfaces;

import com.c332030.ctool4j.core.util.CJsonUtils;
import com.c332030.ctool4j.spring.security.util.CSpringSecurityUtils;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * <p>
 * Description: ICSecuritySessionTests
 * </p>
 * <p>
 * 验证 {@link ICSecuritySession#getAuthorities()} 在 JSON 读写两种会话形态下的行为：
 * 无可写属性（走接口默认实现）与有可写属性（会话自身持久化权限）。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「只读派生属性 / 可写属性」两个维度组织：前者是线上故障场景，后者是必须不被误伤的正常场景。</li>
 *   <li>关键回归：Jackson 的 {@code MapperFeature.USE_GETTERS_AS_SETTERS}（默认开启）在属性无可写成员时走
 *   {@code SetterlessProperty}，会把 JSON 中的 {@code authorities} 合并进 <b>getter 的返回值</b>。
 *   默认实现若返回共享的不可修改常量，合并会抛 {@code UnsupportedOperationException}（线上 loadAuthentication error）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据线上堆栈：{@code SetterlessProperty.deserializeAndSet} → {@code CollectionDeserializer._deserializeFromArray}
 *   → {@code AbstractList.add} → {@code UnsupportedOperationException}，
 *   异常路径为 {@code SessionInfo["authorities"]->java.util.Collections$SingletonList[1]}。</li>
 *   <li>依据 Jackson 语义：{@code USE_GETTERS_AS_SETTERS} 只对 Collection/Map 属性生效，且仅在属性无可写成员时生效。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：默认实现（无 authorities 可写属性）读回不报错且权限仍为匿名；会话自带 authorities 可写属性时正常写入。</li>
 *   <li>未覆盖：覆盖了 {@code getAuthorities()} 但返回自定义实现的会话（本用例只固化读写契约）；真实 Redis 链路。</li>
 * </ul>
 * <h2>设计取舍</h2>
 * <ul>
 *   <li>桩类一律不覆盖 {@code getAuthorities()}（除了必要场景），以便真实走接口默认实现——这正是线上 {@code SessionInfo} 的形态。</li>
 *   <li>断言用「不抛异常 + 权限值」而非「容器是否被合并」：合并目标是无关紧要的中间产物，报错才是故障。</li>
 * </ul>
 * <h2>会话权限读写</h2>
 * <ul>
 *   <li>1 读回（对应测试方法 1.1-1.2）</li>
 * </ul>
 *
 * @since 2026/9/14
 * @version 1.0
 */
class ICSecuritySessionTests {

    /**
     * 与线上一致：authorities 为对象数组
     */
    private static final String SESSION_JSON =
        "{\"clientType\":\"MINI_PROGRAM\",\"createMillis\":null,"
            + "\"authorities\":[{\"authority\":\"ROLE_ANONYMOUS\"}]}";

    /**
     * 对应测试用例 1.1：无可写 authorities 属性的会话（走接口默认实现）读回不报错，权限仍为匿名
     */
    @Test
    void testGetAuthorities_setterlessProperty_readsWithoutError() {
        // 回归：默认实现返回的容器会被 Jackson 当作合并目标，必须是可填充且不共享的
        val session = CJsonUtils.fromJson(SESSION_JSON, AnonymousSessionStub.class);

        Assertions.assertNotNull(session);
        Assertions.assertEquals("MINI_PROGRAM", session.getClientType());

        val authorities = session.getAuthorities();
        Assertions.assertNotNull(authorities);
        Assertions.assertEquals(CSpringSecurityUtils.ROLE_ANONYMOUS, authorities.iterator().next().getAuthority());
    }

    /**
     * 对应测试用例 1.2：自带可写 authorities 属性的会话正常写入（修复不得误伤权限持久化）
     */
    @Test
    void testGetAuthorities_settableProperty_writesFromJson() {
        // 正例：有 setter 时走 setter 路径，不经 getter 合并
        val session = CJsonUtils.fromJson(
            "{\"authorities\":[{\"authority\":\"ROLE_ADMIN\"}]}",
            SettableSessionStub.class
        );

        Assertions.assertNotNull(session);
        Assertions.assertEquals(1, session.getAuthorities().size());
        Assertions.assertEquals("ROLE_ADMIN", session.getAuthorities().iterator().next().getAuthority());
    }

    /**
     * 匿名会话桩：不覆盖 {@code getAuthorities()}，即 authorities 无可写属性（复现线上 {@code SessionInfo} 结构）
     */
    @Data
    public static class AnonymousSessionStub implements ICSecuritySession {

        private String clientType;

        private Long createMillis;
    }

    /**
     * 持权会话桩：authorities 为可写属性（验证修复不误伤真正持久化权限的会话）
     */
    @Data
    public static class SettableSessionStub implements ICSecuritySession {

        private Collection<GrantedAuthority> authorities;
    }

}
