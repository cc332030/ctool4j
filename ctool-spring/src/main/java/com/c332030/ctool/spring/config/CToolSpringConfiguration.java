package com.c332030.ctool.spring.config;

import com.c332030.ctool.core.constant.CToolConstants;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Description: CToolSpringConfiguration
 * </p>
 *
 * @since 2025/9/11
 */
@Configuration
@ComponentScan(basePackages = CToolConstants.BASE_PACKAGE)
public class CToolSpringConfiguration {

}
