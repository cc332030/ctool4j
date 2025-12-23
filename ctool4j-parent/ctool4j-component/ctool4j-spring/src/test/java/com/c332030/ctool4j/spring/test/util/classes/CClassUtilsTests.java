package com.c332030.ctool4j.spring.test.util.classes;

import com.c332030.ctool4j.core.classes.CClassUtils;
import com.c332030.ctool4j.definition.constant.CToolConstants;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.bean.CSpringConfigBeans;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CClassUtilsTests
 * </p>
 *
 * @since 2025/12/23
 */
public class CClassUtilsTests {

    @Test
    public void listAnnotatedClass() {

        val classes = CClassUtils.listAnnotatedClass(CAutowired.class, CToolConstants.BASE_PACKAGE);
        Assertions.assertTrue(classes.contains(CSpringConfigBeans.class));

    }

}
