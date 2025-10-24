package com.c332030.ctool4j.core.interfaces;

import cn.hutool.core.util.StrUtil;
import lombok.val;

/**
 * <p>
 * Description: ICError
 * </p>
 *
 * @since 2025/10/24
 */
public interface ICError<T> {

    T getErrorCode();

    String getErrorMsg();

    default String formatMessage() {
        return formatMessage(null);
    }

    default String formatMessage(String message) {

        val sb = new StringBuilder();
        sb.append(getErrorMsg());
        sb.append("[");
        sb.append(getErrorCode());
        sb.append("]");

        if(StrUtil.isNotBlank(message)) {
            sb.append(": ");
            sb.append(message);
        }

        return sb.toString();
    }

}
