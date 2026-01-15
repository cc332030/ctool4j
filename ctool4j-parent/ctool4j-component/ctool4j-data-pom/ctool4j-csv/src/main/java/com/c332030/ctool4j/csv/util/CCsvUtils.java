package com.c332030.ctool4j.csv.util;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.util.CStrUtils;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CCsvUtils
 * </p>
 *
 * @since 2026/1/14
 */
@UtilityClass
public class CCsvUtils {

    public <T> List<T> toObjects(List<Map<String, String>> rowMaps, Class<T> tClass) {
        return rowMaps
            .stream()
            .map(rowMap -> CReflectUtils.fillValues(tClass, rowMap))
            .collect(Collectors.toList());
    }

    public String trim(String str) {

        val strNew = CStrUtils.trim(str);
        if(StrUtil.isBlank(strNew)) {
            return null;
        }

        return strNew.replaceAll("\b", "");
    }

}
