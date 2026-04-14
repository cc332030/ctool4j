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

    /**
     * 格式化响应信息
     * @param response 响应
     * @return 响应信息
     */
    public String formatMessage(ICRes<?> response) {
        return formatMessage(response, null);
    }

    /**
     * 格式化响应信息
     * @param response 响应
     * @param msgExtend 扩展信息
     * @return 响应信息
     */
    public String formatMessage(ICRes<?> response, String msgExtend) {

        if(response == null) {
            return msgExtend;
        }

        if(StrUtil.isEmpty(msgExtend)){
            return response.getResMsg();
        }

        return response.getResMsg() + ": " + msgExtend;
    }

    /**
     * 格式化响应信息-带错误码
     * @param response 响应
     * @return 响应信息-带错误码
     */
    public String formatResMessage(ICRes<?> response) {
        return formatResMessage(response, null);
    }

    /**
     * 格式化响应信息-带错误码
     * @param response 响应
     * @param msgExtend 扩展信息
     * @return 响应信息-带错误码
     */
    public String formatResMessage(ICRes<?> response, String msgExtend) {

        if(response == null) {
            return msgExtend;
        }

        val sb = new StringBuilder();
        sb.append("[");
        sb.append(response.getResCode());
        sb.append("] ");
        sb.append(response.getResMsg());

        if(StrUtil.isNotEmpty(msgExtend)) {
            sb.append(": ");
            sb.append(msgExtend);
        }

        return sb.toString();
    }

}
