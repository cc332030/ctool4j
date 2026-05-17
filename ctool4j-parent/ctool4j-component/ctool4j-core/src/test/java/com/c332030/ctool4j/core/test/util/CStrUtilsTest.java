package com.c332030.ctool4j.core.test.util;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.core.util.CStrUtils;
import lombok.CustomLog;
import lombok.val;
import org.junit.jupiter.api.Assertions;
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

        Assertions.assertEquals("My name is ", result);

    }

    @Test
    public void incrLastNum() {

        val string = "c332030-1";
        val result = CStrUtils.incrLastNum(string);

        Assertions.assertEquals("c332030-2", result);

    }

    @Test
    public void chineseOnly() {

        Assertions.assertEquals("张三", CStrUtils.chineseOnly("c张三Zhang123"));
        Assertions.assertEquals("李四", CStrUtils.chineseOnly(".李四Li456"));
        Assertions.assertEquals("王五", CStrUtils.chineseOnly("-王五Wang_789"));
        Assertions.assertEquals("测试", CStrUtils.chineseOnly("\\测试test123@example.com"));
        Assertions.assertEquals("只有中文", CStrUtils.chineseOnly("只有中文"));
        Assertions.assertEquals("", CStrUtils.chineseOnly("123456"));
        Assertions.assertEquals("", CStrUtils.chineseOnly("ABCdef"));
        Assertions.assertEquals("混合中文", CStrUtils.chineseOnly("a混合123ABC中文test"));

    }

}
