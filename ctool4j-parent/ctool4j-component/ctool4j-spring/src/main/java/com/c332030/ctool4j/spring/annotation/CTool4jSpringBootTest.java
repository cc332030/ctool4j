package com.c332030.ctool4j.spring.annotation;

import com.c332030.ctool4j.spring.configuration.CSpringConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CTool4jSpringBootTest
 * </p>
 *
 * @since 2025/12/28
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

@SpringBootTest(classes = {CSpringConfiguration.class})
public @interface CTool4jSpringBootTest {

}
