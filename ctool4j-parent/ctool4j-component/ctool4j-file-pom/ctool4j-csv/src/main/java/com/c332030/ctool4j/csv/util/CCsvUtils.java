package com.c332030.ctool4j.csv.util;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CStrUtils;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * <p>
 * Description: CCsvUtils
 * </p>
 *
 * @see doc/design/csv/CCsvUtils.adoc
 * @see doc/design/csv/CCsvUtilsTests.adoc
 * @since 2026/1/14
 */
@UtilityClass
public class CCsvUtils {

    /**
     * 去除字符串两端空白，空字符串返回 null
     *
     * @param str 原始字符串
     * @return 去除空白后的字符串；空白字符串返回 null
     */
    public String trim(String str) {

        val strNew = CStrUtils.trim(str);
        if(StrUtil.isBlank(strNew)) {
            return null;
        }

        return strNew.replaceAll("\b", "");
    }

}
