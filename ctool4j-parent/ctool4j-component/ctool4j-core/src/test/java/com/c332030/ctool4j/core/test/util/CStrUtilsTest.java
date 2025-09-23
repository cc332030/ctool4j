package com.c332030.ctool4j.core.test.util;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.core.util.CStrUtils;
import lombok.CustomLog;
import lombok.val;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CStrUtilsTest
 * </p>
 *
 * @since 2025/9/16
 */
@CustomLog
public class CStrUtilsTest {

    @Test
    public void formatNullToEmpty() {

        val template = "My name is ${name}";

        val paramMap = CMap.of("name2", "c332030");

        val result = CStrUtils.format(template, paramMap::get, StrUtil.EMPTY);
        log.info("result: {}", result);

    }

}
