package com.c332030.ctool4j.session.service;

import com.c332030.ctool4j.session.interfaces.ICSecuritySession;
import com.c332030.ctool4j.spring.security.util.CSpringSecurityUtils;
import lombok.CustomLog;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Description: CAbstractSessionService
 * </p>
 *
 * <p>Spring Security 会话服务抽象基类：继承 auth-base 的 {@link CAbstractBaseSessionService}（会话存取/删除、
 * 按 jwt 取会话、按请求加载会话），只需把「当前会话」的来源绑定到安全上下文——
 * {@link CSpringSecurityUtils#getPrincipal()}。</p>
 *
 * <p>说明：{@code get()}/{@code check()} 依赖 Spring Security 上下文，仅请求线程可用。</p>
 *
 * <p>构造路径：无参构造按本子类泛型实参解析会话类型（业务直接继承时使用）；显式 {@code Class} 构造供
 * "泛型实参在创建点仍是类型变量"的场景（如配置类里的匿名 bean，由配置从业务子类解析后传入）。</p>
 *
 * @author c332030
 * @since 2026/9/10
 * @version 1.1
 */
@CustomLog
@NoArgsConstructor
public abstract class CAbstractSessionService<SESSION extends ICSecuritySession> extends CAbstractBaseSessionService<SESSION> {

    /**
     * 显式指定会话类型构造：供"泛型实参在创建点仍是类型变量"的场景使用（如配置类里的匿名 bean），
     * 由调用方从绑定具体类型的类（如业务配置子类）解析后传入；无参构造由 lombok {@code @NoArgsConstructor}
     * 生成（会话类型按本子类泛型实参解析）。详见基类同参构造
     *
     * @param sessionClass 会话类型（不可为 null）
     */
    public CAbstractSessionService(Class<SESSION> sessionClass) {
        super(sessionClass);
    }

    /**
     * 获取当前会话（未授权返回 null，不抛异常）
     *
     * @return 当前会话；未授权返回 null
     */
    @Override
    public SESSION getDefaultNull() {
        return CSpringSecurityUtils.getPrincipal();
    }

}
