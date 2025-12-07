package com.c332030.ctool4j.definition.interfaces;

/**
 * <p>
 * Description: ICError
 * </p>
 *
 * @since 2025/10/24
 */
public interface ICError<T> {

    /**
     * 错误码
     * @return 错误码
     */
    T getErrorCode();

    /**
     * 错误信息
     * @return 错误信息
     */
    String getErrorMsg();

    /**
     * 是否打印日志
     * @return 是否打印日志
     */
    default Boolean getPrintLog() {
        return true;
    }

}
