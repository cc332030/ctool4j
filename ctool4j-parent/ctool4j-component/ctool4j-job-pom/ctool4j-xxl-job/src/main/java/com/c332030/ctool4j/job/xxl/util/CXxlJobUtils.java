package com.c332030.ctool4j.job.xxl.util;

import cn.hutool.core.lang.Opt;
import com.c332030.ctool4j.core.util.CJsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.experimental.UtilityClass;

/**
 * <p>
 * Description: CXxlJobUtils
 * </p>
 *
 * @since 2025/11/27
 */
@UtilityClass
public class CXxlJobUtils {

    public String getJobParam() {
        return XxlJobHelper.getJobParam();
    }

    public <T> T parseJobParam(Class<T> clazz) {
        return Opt.ofBlankAble(getJobParam())
                .map(e -> CJsonUtils.fromJson(e, clazz))
                .orElse(null);
    }

    public <T> T parseJobParam(TypeReference<T> typeReference) {
        return Opt.ofBlankAble(getJobParam())
                .map(e -> CJsonUtils.fromJson(e, typeReference))
                .orElse(null);
    }

}
