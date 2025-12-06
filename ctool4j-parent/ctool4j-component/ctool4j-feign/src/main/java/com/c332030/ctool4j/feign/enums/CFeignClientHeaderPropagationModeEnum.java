package com.c332030.ctool4j.feign.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: CFeignClientHeaderPropagationModeEnum 客户端请求头传播模式
 * </p>
 *
 * @since 2025/12/6
 */
@Getter
@AllArgsConstructor
public enum CFeignClientHeaderPropagationModeEnum {

    ALL("全部"),

    CUSTOM("自定义"),

    NONE("无"),

    ;

    /**
     * 描述
     */
    final String text;

}
