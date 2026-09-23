package com.c332030.ctool4j.session.util;

import com.c332030.ctool4j.core.exception.CUnauthorizedException;
import com.c332030.ctool4j.session.interfaces.ICSession;
import com.c332030.ctool4j.session.service.CAbstractBaseSessionService;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * Description: CSessionUtilsTests
 * </p>
 * <p>{@code CSessionUtils} 的测试用例</p>
 *
 * <p>覆盖静态门面透传的全部方法：{@code get(token)} / {@code load(request)} / {@code getDefaultNull()} / {@code getByJwt(jwt)} 的"查不到返回 null"
 * （透传服务层语义），{@code get()} / {@code check()} 的"无会话即未授权"，以及 {@code save(token, session)} / {@code remove(token)} 的原样透传。
 * 门面只做透传与泛型适配，故以会话服务替身的返回值为唯一变量驱动断言。</p>
 *
 * <p><b>用例设计思路</b>：替身继承 {@link CAbstractBaseSessionService}（自 {@code session.service} 包跨包继承，
 * 与门面所在包不同）并覆写 {@code loadSession(request)} / {@code getDefaultNull()} / {@code get(token)} / {@code getSessionByJwt(jwt)} /
 * {@code save(token, session)} / {@code remove(token)}，门面各方法走真实实现、不额外判定；</p>
 * <ul>
 *   <li>null 语义组（{@code load} / {@code getDefaultNull} / {@code getByJwt}）断言"查不到返回 null"，并断言请求/jwt 按原样透传给服务；</li>
 *   <li>透传组（{@code get(String)} / {@code save} / {@code remove}）断言入参（token/会话）原样交给服务，返回值由服务决定（查不到返回 null，含空白 token）；</li>
 *   <li>未授权语义组（{@code get()} / {@code check()}）按实现精确断言异常类型（{@link CUnauthorizedException}）与消息；</li>
 *   <li>静态门面状态在用例后清理（{@code @AfterEach}），避免静态字段逃逸到其他测试类。</li>
 * </ul>
 *
 * <p><b>设计依据</b>：依据 {@code CSessionUtils} javadoc 的方法语义（透传：{@code load} / {@code getDefaultNull} / {@code get(String)} / {@code getByJwt} 返回 null、
 * {@code get()} / {@code check()} 无会话抛未授权；{@code save} / {@code remove} 原样透传）与等价类/边界值（有会话/无会话、token 有值/空白）。</p>
 *
 * <p><b>覆盖场景</b>：{@code get(String)} 命中/查不到/空白 token；{@code save} / {@code remove} 入参透传；{@code getByJwt} 命中/查不到；
 * {@code load} 有会话/无会话；{@code check()} 有会话/无会话；{@code getDefaultNull} / {@code get()} 有/无会话；会话服务未注入时的快速失败。</p>
 * <p><b>未覆盖</b>：泛型声明类型与实际会话类型不符时使用点的 {@code ClassCastException}（{@code CObjUtils.anyType} 直转，
 * 属调用方误用，见 {@code CSessionUtils} 的「已知限制与取舍」）。</p>
 *
 * <h2>加载会话（null 语义）</h2>
 * <ul>
 *   <li>1.1 有会话：load() 返回会话并把请求透传给服务（load_withSession_returnsSessionAndPassesRequest）</li>
 *   <li>1.2 无会话：load() 返回 null 且不抛异常（load_withoutSession_returnsNull）</li>
 *   <li>1.3 有会话：getDefaultNull() 返回当前会话（getDefaultNull_withSession_returnsSession）</li>
 *   <li>1.4 无会话：getDefaultNull() 返回 null（getDefaultNull_withoutSession_returnsNull）</li>
 * </ul>
 *
 * <h2>取当前会话（未授权语义）</h2>
 * <ul>
 *   <li>2.1 有会话：get() 返回当前会话（get_withCurrentSession_returnsSession）</li>
 *   <li>2.2 无会话：get() 抛 CUnauthorizedException（get_withoutCurrentSession_throws）</li>
 * </ul>
 *
 * <h2>按 token 取会话（null 语义，透传）</h2>
 * <ul>
 *   <li>3.1 命中：get(token) 返回会话并把 token 透传给服务（get_byToken_returnsSessionAndPassesToken）</li>
 *   <li>3.2 查不到：get(token) 返回 null、不抛异常（get_byToken_missing_returnsNull）</li>
 *   <li>3.3 空白 token：原样透传给服务并返回 null（本类不做有效性判定）（get_byToken_blank_passesThrough）</li>
 * </ul>
 *
 * <h2>校验已授权（未授权语义）</h2>
 * <ul>
 *   <li>4.1 有会话：check() 不抛异常（check_withSession_passes）</li>
 *   <li>4.2 无会话：check() 抛 CUnauthorizedException（check_withoutSession_throws）</li>
 * </ul>
 *
 * <h2>写入与删除（透传）</h2>
 * <ul>
 *   <li>5.1 save() 把 token 与会话原样透传给服务（save_passesTokenAndSessionThrough）</li>
 *   <li>5.2 remove() 把 token 原样透传给服务（remove_passesTokenThrough）</li>
 * </ul>
 *
 * <h2>由 jwt 取会话（null 语义）</h2>
 * <ul>
 *   <li>6.1 命中：getByJwt(jwt) 返回会话并把 jwt 透传给服务（getByJwt_withSession_returnsSessionAndPassesJwt）</li>
 *   <li>6.2 查不到：getByJwt(jwt) 返回 null、不抛异常（getByJwt_withoutSession_returnsNull）</li>
 * </ul>
 *
 * <h2>会话服务未注入</h2>
 * <ul>
 *   <li>7.1 未注入：getDefaultNull() 快速失败（NullPointerException）（getDefaultNull_serviceNotInjected_throwsNullPointer）</li>
 * </ul>
 *
 * @since 2026/9/18
 * @version 1.0
 * @see CSessionUtils
 */
class CSessionUtilsTests {

    /**
     * 注入静态门面的会话服务替身
     */
    private SessionServiceStub sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new SessionServiceStub();
        CSessionUtils.setSessionService(sessionService);
    }

    /**
     * 清理注入到静态门面的会话服务：静态状态不随用例结束回收，避免逃逸到其他测试类
     */
    @AfterEach
    void tearDown() {
        CSessionUtils.setSessionService(null);
    }

    // ---------- 加载会话（null 语义） ----------

    /**
     * 对应测试用例 1.1：有会话时 load() 返回会话，并把请求透传给会话服务
     */
    @Test
    void load_withSession_returnsSessionAndPassesRequest() {

        // 正例：门面按请求加载会话，服务收到同一请求实例
        val request = new MockHttpServletRequest();
        val session = new SessionStub();
        sessionService.loadedSession = session;

        Assertions.assertSame(session, CSessionUtils.load(request));
        Assertions.assertSame(request, sessionService.lastRequest);
        Assertions.assertEquals(1, sessionService.loadSessionCount);

    }

    /**
     * 对应测试用例 1.2：无会话时 load() 返回 null、不抛异常
     */
    @Test
    void load_withoutSession_returnsNull() {

        // 边界：未携带 token / 解析失败 / 查不到会话 → null，交由调用方判定
        Assertions.assertNull(CSessionUtils.load(new MockHttpServletRequest()));
        Assertions.assertEquals(1, sessionService.loadSessionCount);

    }

    /**
     * 对应测试用例 1.3：有会话时 getDefaultNull() 返回当前会话
     */
    @Test
    void getDefaultNull_withSession_returnsSession() {

        // 正例：返回的就是替身给出的当前会话实例
        val session = new SessionStub();
        sessionService.currentSession = session;

        Assertions.assertSame(session, CSessionUtils.getDefaultNull());

    }

    /**
     * 对应测试用例 1.4：无会话时 getDefaultNull() 返回 null
     */
    @Test
    void getDefaultNull_withoutSession_returnsNull() {

        Assertions.assertNull(CSessionUtils.getDefaultNull());

    }

    // ---------- 取当前会话（未授权语义） ----------

    /**
     * 对应测试用例 2.1：有会话时 get() 返回当前会话
     */
    @Test
    void get_withCurrentSession_returnsSession() {

        val session = new SessionStub();
        sessionService.currentSession = session;

        Assertions.assertSame(session, CSessionUtils.get());

    }

    /**
     * 对应测试用例 2.2：无会话时 get() 抛 CUnauthorizedException
     */
    @Test
    void get_withoutCurrentSession_throws() {

        // 无会话 → 未授权快速失败（异常类型与消息契约）
        val exception = Assertions.assertThrows(CUnauthorizedException.class, () -> CSessionUtils.get());
        Assertions.assertEquals("未授权", exception.getMessage());

    }

    // ---------- 按 token 取会话（未授权语义） ----------

    /**
     * 对应测试用例 3.1：按 token 命中时返回会话，并把 token 透传给服务
     */
    @Test
    void get_byToken_returnsSessionAndPassesToken() {

        val session = new SessionStub();
        sessionService.sessionByToken = session;

        Assertions.assertSame(session, CSessionUtils.get("token-1"));
        Assertions.assertEquals("token-1", sessionService.lastToken);

    }

    /**
     * 对应测试用例 3.2：按 token 查不到会话时返回 null、不抛异常
     */
    @Test
    void get_byToken_missing_returnsNull() {

        // 透传：服务替身未配置 sessionByToken → 查不到会话 → null，交由调用方判定
        Assertions.assertNull(CSessionUtils.get("token-1"));
        Assertions.assertEquals("token-1", sessionService.lastToken);

    }

    /**
     * 对应测试用例 3.3：空白 token 同样原样透传给会话服务并返回 null（本类不做有效性判定）
     */
    @Test
    void get_byToken_blank_passesThrough() {

        // 透传：空白 token 也交给服务，返回值由服务决定（替身查不到 → null）
        Assertions.assertNull(CSessionUtils.get(" "));
        Assertions.assertEquals(" ", sessionService.lastToken);

    }

    // ---------- 校验已授权（未授权语义） ----------

    /**
     * 对应测试用例 4.1：有会话时 check() 不抛异常
     */
    @Test
    void check_withSession_passes() {

        // 服务层 check() 委托 get()（真实实现）→ 会话存在 → 正常返回
        sessionService.currentSession = new SessionStub();

        Assertions.assertDoesNotThrow(() -> CSessionUtils.check());

    }

    /**
     * 对应测试用例 4.2：无会话时 check() 抛 CUnauthorizedException
     */
    @Test
    void check_withoutSession_throws() {

        val exception = Assertions.assertThrows(CUnauthorizedException.class, () -> CSessionUtils.check());
        Assertions.assertEquals("未授权", exception.getMessage());

    }

    // ---------- 写入与删除（透传） ----------

    /**
     * 对应测试用例 5.1：save() 把 token 与会话原样透传给服务
     */
    @Test
    void save_passesTokenAndSessionThrough() {

        // 透传：门面不设默认过期时间、不做校验，token 与会话原样交给服务
        val session = new SessionStub();
        CSessionUtils.save("token-1", session);

        Assertions.assertEquals("token-1", sessionService.savedToken);
        Assertions.assertSame(session, sessionService.savedSession);

    }

    /**
     * 对应测试用例 5.2：remove() 把 token 原样透传给服务
     */
    @Test
    void remove_passesTokenThrough() {

        CSessionUtils.remove("token-1");

        Assertions.assertEquals("token-1", sessionService.removedToken);

    }

    // ---------- 由 jwt 取会话（null 语义） ----------

    /**
     * 对应测试用例 6.1：getByJwt() 命中时返回会话，并把 jwt 透传给服务
     */
    @Test
    void getByJwt_withSession_returnsSessionAndPassesJwt() {

        val session = new SessionStub();
        sessionService.sessionByJwt = session;

        Assertions.assertSame(session, CSessionUtils.getByJwt("jwt-1"));
        Assertions.assertEquals("jwt-1", sessionService.lastJwt);

    }

    /**
     * 对应测试用例 6.2：getByJwt() 查不到会话时返回 null、不抛异常
     */
    @Test
    void getByJwt_withoutSession_returnsNull() {

        // 透传：jwt 中 token 为空 / 按 token 查不到 → null，交由调用方判定
        Assertions.assertNull(CSessionUtils.getByJwt("jwt-1"));
        Assertions.assertEquals("jwt-1", sessionService.lastJwt);

    }

    // ---------- 会话服务未注入 ----------

    /**
     * 对应测试用例 7.1：会话服务未注入时快速失败（NullPointerException），不静默返回 null
     */
    @Test
    void getDefaultNull_serviceNotInjected_throwsNullPointer() {

        CSessionUtils.setSessionService(null);

        Assertions.assertThrows(NullPointerException.class, () -> CSessionUtils.getDefaultNull());

    }

    /**
     * 会话替身：{@code ICSession} 的最小实现，用于断言"返回的是哪个实例"
     */
    @Data
    public static class SessionStub implements ICSession {

        private String token;

    }

    /**
     * 会话服务测试替身：跨包继承 {@link CAbstractBaseSessionService}，覆写门面用到的三个读取方法并记录入参
     */
    private static class SessionServiceStub extends CAbstractBaseSessionService<SessionStub> {

        /**
         * {@code getDefaultNull()} 的返回（null 表示当前无会话）
         */
        SessionStub currentSession;

        /**
         * {@code loadSession(request)} 的返回（null 表示未携带 token / 查不到会话）
         */
        SessionStub loadedSession;

        /**
         * {@code get(token)} 的返回（null 表示按 token 查不到会话）
         */
        SessionStub sessionByToken;

        /**
         * {@code loadSession} 收到的请求（null 表示未被调用）
         */
        HttpServletRequest lastRequest;

        /**
         * {@code get(token)} 收到的 token（null 表示未被调用）
         */
        String lastToken;

        /**
         * {@code loadSession} 调用次数
         */
        int loadSessionCount;

        /**
         * {@code getSessionByJwt(jwt)} 的返回（null 表示查不到会话）
         */
        SessionStub sessionByJwt;

        /**
         * {@code getSessionByJwt(jwt)} 收到的 jwt（null 表示未被调用）
         */
        String lastJwt;

        /**
         * {@code save(token, session)} 收到的 token（null 表示未被调用）
         */
        String savedToken;

        /**
         * {@code save(token, session)} 收到的会话（null 表示未被调用）
         */
        SessionStub savedSession;

        /**
         * {@code remove(token)} 收到的 token（null 表示未被调用）
         */
        String removedToken;

        @Override
        public SessionStub loadSession(HttpServletRequest request) {

            loadSessionCount++;
            lastRequest = request;

            return loadedSession;

        }

        @Override
        public SessionStub getDefaultNull() {
            return currentSession;
        }

        @Override
        public SessionStub get(String token) {

            lastToken = token;
            return sessionByToken;

        }

        @Override
        public SessionStub getSessionByJwt(String jwt) {

            lastJwt = jwt;
            return sessionByJwt;

        }

        @Override
        public void save(String token, SessionStub session) {

            savedToken = token;
            savedSession = session;

        }

        @Override
        public void remove(String token) {
            removedToken = token;
        }

    }

}
