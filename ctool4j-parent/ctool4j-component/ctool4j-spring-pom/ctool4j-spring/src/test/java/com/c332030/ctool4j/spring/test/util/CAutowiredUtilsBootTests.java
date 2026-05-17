package com.c332030.ctool4j.spring.test.util;

import com.c332030.ctool4j.spring.bean.CSpringConfigBeans;
import com.c332030.ctool4j.spring.test.annotation.CTool4jSpringBootTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CAutowiredUtilsBootTests
 * </p>
 *
 * @since 2025/12/28
 */
@CTool4jSpringBootTest
public class CAutowiredUtilsBootTests {

    @Test
    public void autowired() {

        Assertions.assertNotNull(CSpringConfigBeans.getSpringApplicationConfig());

    }

}
