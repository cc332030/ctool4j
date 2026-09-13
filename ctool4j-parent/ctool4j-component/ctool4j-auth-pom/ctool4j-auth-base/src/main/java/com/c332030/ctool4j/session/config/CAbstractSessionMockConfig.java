package com.c332030.ctool4j.session.config;

import com.c332030.ctool4j.session.interfaces.ICSession;
import lombok.Data;

/**
 * <p>
 * Description: CAbstractSessionMockConfig
 * </p>
 *
 * <p>mock 会话配置抽象基类，业务子类继承并绑定具体会话类型，用于本地开发/测试注入固定会话免登录。
 * {@link #enable} 默认 false；{@code enable=true} 且 {@link #session} 非空时，{@code CAbstractBaseAuthFilter}
 * 优先取该会话（不再读取真实会话），并由认证实现（如 auth-spring 的 {@code CAbstractAuthFilter}）按已认证注入安全上下文。
 * <b>禁止在生产启用</b>。</p>
 *
 * <p>注意：类上 {@code @ConfigurationProperties} 当前被注释，配置键未固定，如需绑定请在子类或此处恢复注解并约定前缀。</p>
 *
 * @author c332030
 * @since 2026/4/7
 */
@Data
//@ConfigurationProperties("session.mock")
public abstract class CAbstractSessionMockConfig<SESSION extends ICSession> {

    /**
     * 是否启用 mock 会话（默认 false；生产禁止启用）
     */
    Boolean enable = false;

    /**
     * mock 会话对象（为空时不注入）
     */
    SESSION session;

}
