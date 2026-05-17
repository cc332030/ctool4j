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
     * @param res 响应
     * @return 响应信息
     */
    public String formatMessage(ICRes<?> res) {
        return formatMessage(res, null);
    }

    /**
     * 格式化响应信息
     * @param res 响应
     * @param msgExtend 扩展信息
     * @return 响应信息
     */
    public String formatMessage(ICRes<?> res, String msgExtend) {

        if(res == null) {
            return msgExtend;
        }

        if(StrUtil.isEmpty(msgExtend)){
            return res.getMsg();
        }

        return res.getMsg() + ": " + msgExtend;
    }

    /**
     * 格式化响应信息-带错误码
     * @param res 响应
     * @return 响应信息-带错误码
     */
    public String formatResMessage(ICRes<?> res) {
        return formatResMessage(res, null);
    }

    /**
     * 格式化响应信息-带错误码
     * @param res 响应
     * @param msgExtend 扩展信息
     * @return 响应信息-带错误码
     */
    public String formatResMessage(ICRes<?> res, String msgExtend) {

        if(res == null) {
            return msgExtend;
        }

        val sb = new StringBuilder();
        sb.append("[");
        sb.append(res.getCode());
        sb.append("] ");
        sb.append(res.getMsg());

        if(StrUtil.isNotEmpty(msgExtend)) {
            sb.append(": ");
            sb.append(msgExtend);
        }

        return sb.toString();
    }

}
