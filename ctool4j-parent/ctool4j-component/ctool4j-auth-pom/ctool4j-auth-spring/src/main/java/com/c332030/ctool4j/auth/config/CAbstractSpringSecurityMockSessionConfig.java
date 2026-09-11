package com.c332030.ctool4j.auth.config;

import com.c332030.ctool4j.session.interfaces.ICSecuritySession;
import lombok.Data;

/**
 * <p>
 * Description: CAbstractSpringSecurityMockSessionConfig
 * </p>
 *
 * <p>Spring Security mock 会话配置抽象基类，业务子类继承并绑定具体会话类型，用于本地开发/测试注入固定会话免登录。
 * {@link #enable} 默认 false；{@code enable=true} 且 {@link #session} 非空时，{@code CAbstractAuthFilter}
 * 会注入该会话为已认证状态。<b>禁止在生产启用</b>。</p>
 *
 * <p>注意：类上 {@code @ConfigurationProperties} 当前被注释，配置键未固定，如需绑定请在子类或此处恢复注解并约定前缀。</p>
 *
 * @author c332030
 * @since 2026/4/7
 */
@Data
//@ConfigurationProperties("spring.security.mock.session")
public abstract class CAbstractSpringSecurityMockSessionConfig<SESSION extends ICSecuritySession> {

    /**
     * 是否启用 mock 会话（默认 false；生产禁止启用）
     */
    Boolean enable = false;

    /**
     * mock 会话对象（为空时不注入）
     */
    SESSION session;

}
