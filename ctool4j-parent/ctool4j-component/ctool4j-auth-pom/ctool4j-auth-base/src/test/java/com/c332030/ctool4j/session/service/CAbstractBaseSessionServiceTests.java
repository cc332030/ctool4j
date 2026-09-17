package com.c332030.ctool4j.session.service;

import com.c332030.ctool4j.core.exception.CUnauthorizedException;
import com.c332030.ctool4j.session.interfaces.ICSession;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CAbstractBaseSessionServiceTests
 * </p>
 * <p>{@code com.c332030.ctool4j.session.service.CAbstractBaseSessionService}（CAbstractBaseSessionService）的测试用例</p>
 *
 * <p>覆盖「当前会话」链路：子类扩展点 {@code getDefaultNull()} 与基于它的 {@code get()} / {@code check()}；
 * {@code getDefaultNull()} 为 {@code public}，跨包继承见 {@code CAbstractBaseAuthFilterTests} 中的会话服务替身
 * （位于 {@code com.c332030.ctool4j.auth.filter} 包，其可编译即证明任意包可继承）。</p>
 *
 * <p><b>用例设计思路</b>：以「子类实现 {@code getDefaultNull()} 的返回值」为唯一变量驱动 {@code get()} / {@code check()}
 * 两条路径——有会话（返回该会话 / 不抛异常）与无会话（未授权快速失败），并断言钩子按调用即时取值（非缓存）；</p>
 * <ul>
 *   <li>替身继承被测类并实现 {@code getDefaultNull()}，被测类的 {@code get()} / {@code check()} 走真实实现，不覆写；</li>
 *   <li>无会话路径按实现精确断言异常类型（{@link CUnauthorizedException}，未授权语义，由 {@code get()} 抛出）。</li>
 * </ul>
 *
 * <p><b>设计依据</b>：依据 {@code CAbstractBaseSessionService} javadoc 对 {@code getDefaultNull} 为子类扩展点、
 * {@code get()} 未授权快速失败的约定；依据等价类/边界值（有会话/无会话）与分支覆盖。</p>
 *
 * <p><b>覆盖场景</b>：{@code get()} 有会话、无会话；{@code check()} 有会话、无会话；扩展点按调用即时取值。</p>
 * <p><b>未覆盖</b>：依赖 Redis 与 jwt 的 {@code get(String)} / {@code save} / {@code remove} /
 * {@code getSessionByJwt} / {@code loadSession}（需 redis 与 jwt 配置，另行用例覆盖）；泛型解析
 * {@code getGenericClass()}（由 {@code IGenericType} 的用例覆盖）。</p>
 *
 * <h2>当前会话</h2>
 * <ul>
 *   <li>1.1 有会话：get() 返回扩展点给出的会话（get_withSession_returnsSession）</li>
 *   <li>1.2 无会话：get() 抛 CUnauthorizedException（get_withoutSession_throws）</li>
 *   <li>1.3 有会话：check() 不抛异常（check_withSession_passes）</li>
 *   <li>1.4 无会话：check() 抛 CUnauthorizedException（check_withoutSession_throws）</li>
 *   <li>1.5 扩展点按调用即时取值：先无会话后有会话（get_hookEvaluatedPerCall）</li>
 * </ul>
 *
 * @since 2026/9/13
 * @version 1.1
 * @see CAbstractBaseSessionService
 */
class CAbstractBaseSessionServiceTests {

    /**
     * 对应测试用例 1.1：get() 返回扩展点给出的会话
     */
    @Test
    void get_withSession_returnsSession() {

        // 正例：子类 getDefaultNull 返回会话 → get() 原样返回该会话
        val session = new SessionStub();
        val service = new SessionServiceStub();
        service.session = session;

        Assertions.assertSame(session, service.get());

    }

    /**
     * 对应测试用例 1.2：get() 未授权抛 CUnauthorizedException
     */
    @Test
    void get_withoutSession_throws() {

        // 异常路径：扩展点返回 null（未授权）→ get() 快速失败，异常类型精确断言
        val service = new SessionServiceStub();

        Assertions.assertThrowsExactly(CUnauthorizedException.class, service::get);

    }

    /**
     * 对应测试用例 1.3：check() 有会话时不抛异常
     */
    @Test
    void check_withSession_passes() {

        // 正例：有会话 → check() 委托 get() 且不抛异常
        val service = new SessionServiceStub();
        service.session = new SessionStub();

        Assertions.assertDoesNotThrow(service::check);

    }

    /**
     * 对应测试用例 1.4：check() 未授权抛 CUnauthorizedException
     */
    @Test
    void check_withoutSession_throws() {

        // 异常路径：无会话 → check() 委托 get() 快速失败
        val service = new SessionServiceStub();

        Assertions.assertThrowsExactly(CUnauthorizedException.class, service::check);

    }

    /**
     * 对应测试用例 1.5：扩展点按调用即时取值，不缓存
     */
    @Test
    void get_hookEvaluatedPerCall() {

        // 边界：同一实例先无会话（失败）后设置会话（成功），证明每次 get() 都重新向扩展点取值
        val session = new SessionStub();
        val service = new SessionServiceStub();

        Assertions.assertThrowsExactly(CUnauthorizedException.class, service::get);

        service.session = session;

        Assertions.assertSame(session, service.get());

    }

    /**
     * 会话测试替身（{@link ICSession} 实现，不依赖 Spring Security）
     */
    @Data
    public static class SessionStub implements ICSession {

        private String token;

    }

    /**
     * 会话服务替身：实现扩展点 {@code getDefaultNull()} 返回可控会话，其余能力走被测类真实实现
     */
    private static class SessionServiceStub extends CAbstractBaseSessionService<SessionStub> {

        /**
         * 扩展点返回值（null 表示无当前会话）
         */
        SessionStub session;

        @Override
        public SessionStub getDefaultNull() {
            return session;
        }

    }

}
