package com.c332030.ctool4j.auth.configuration;

import com.c332030.ctool4j.auth.filter.CAbstractAuthFilter;
import com.c332030.ctool4j.session.interfaces.ICSecuritySession;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * <p>
 * Description: CAbstractAuthConfiguration
 * </p>
 *
 * @author c332030
 * @since 2026/9/14
 * @version 1.0
 */
//@Configuration
public abstract class CAbstractAuthConfiguration<T extends ICSecuritySession> extends CAbstractAuthBaseConfiguration<T> {

    abstract boolean isAuthAnonymous(T session);

    @Bean
    @ConditionalOnMissingBean(CAbstractAuthFilter.class)
    public CAbstractAuthFilter<T> cAuthFilter() {
        return new CAbstractAuthFilter<T>() {
            @Override
            public boolean isAnonymous(T session) {
                return isAuthAnonymous(session);
            }
        };
    }

}
