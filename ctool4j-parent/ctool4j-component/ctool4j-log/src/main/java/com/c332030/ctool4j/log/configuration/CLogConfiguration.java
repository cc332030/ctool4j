package com.c332030.ctool4j.log.configuration;

import com.c332030.ctool4j.log.advice.CLogRequestBodyAdvice;
import com.c332030.ctool4j.log.advice.CLogResponseBodyAdvice;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Description: CLogConfiguration
 * </p>
 *
 * @author c332030
 * @since 2025/12/20
 */
@Configuration
public class CLogConfiguration {

    @Bean
    public CLogRequestBodyAdvice cLogRequestBodyAdvice() {
        return new CLogRequestBodyAdvice();
    }

    @Bean
    public CLogResponseBodyAdvice cLogResponseBodyAdvice() {
        return new CLogResponseBodyAdvice();
    }

}
