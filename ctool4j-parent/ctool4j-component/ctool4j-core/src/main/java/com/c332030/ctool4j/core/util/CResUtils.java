package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.definition.interfaces.ICRes;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * <p>
 * Description: CResUtils
 * </p>
 *
 * @since 2025/10/24
 */
@UtilityClass
public class CResUtils {

    public String formatMessage(ICRes<?> response) {
        return formatMessage(response, null);
    }

    public String formatMessage(ICRes<?> response, String msgExtend) {

        if(response == null) {
            return msgExtend;
        }

        val sb = new StringBuilder();
        sb.append(response.getResCode());
        sb.append("[");
        sb.append(response.getResMsg());
        sb.append("]");

        if(StrUtil.isNotBlank(msgExtend)) {
            sb.append(": ");
            sb.append(msgExtend);
        }

        return sb.toString();
    }

}
