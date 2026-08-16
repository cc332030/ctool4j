package com.c332030.ctool4j.definition.interfaces;

/**
 * <p>
 * Description: ICOperate
 * </p>
 *
 * @since 2025/11/10
 */
public interface ICOperate extends ICEnumName {

    /**
     * 获取操作名称（默认返回枚举名称）
     * @return 操作名称
     */
    default String getName() {
        return name();
    }

}
