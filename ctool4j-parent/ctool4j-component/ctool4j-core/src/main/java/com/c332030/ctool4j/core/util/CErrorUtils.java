package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.interfaces.ICError;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * <p>
 * Description: CErrorUtils
 * </p>
 *
 * @since 2025/10/24
 */
@UtilityClass
public class CErrorUtils {

    public String formatMessage(ICError<?> error) {
        return formatMessage(error, null);
    }

    public String formatMessage(ICError<?> error, String errorExtend) {

        if(error == null) {
            return errorExtend;
        }

        val sb = new StringBuilder();
        sb.append(error.getErrorMsg());
        sb.append("[");
        sb.append(error.getErrorCode());
        sb.append("]");

        if(StrUtil.isNotBlank(errorExtend)) {
            sb.append(": ");
            sb.append(errorExtend);
        }

        return sb.toString();
    }

}
