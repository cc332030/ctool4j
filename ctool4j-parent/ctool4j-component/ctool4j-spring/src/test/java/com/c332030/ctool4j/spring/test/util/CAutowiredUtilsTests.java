package com.c332030.ctool4j.spring.test.util;

import cn.hutool.core.lang.func.LambdaUtil;
import com.c332030.ctool4j.spring.bean.CSpringConfigBeans;
import com.c332030.ctool4j.spring.util.CAutowiredUtils;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CAutowiredUtilsTests
 * </p>
 *
 * @since 2025/12/28
 */
public class CAutowiredUtilsTests {

    @Test
    public void listFieldMap() {

        val fieldMap = CAutowiredUtils.listFieldMap(CSpringConfigBeans.class);
        val fieldName = LambdaUtil.getFieldName(CSpringConfigBeans::getSpringApplicationConfig);

        Assertions.assertNotNull(fieldMap.get(fieldName));

    }

}
