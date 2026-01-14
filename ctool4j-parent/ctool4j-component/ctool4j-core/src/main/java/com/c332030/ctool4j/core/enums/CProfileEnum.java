package com.c332030.ctool4j.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: CProfileEnum
 * </p>
 *
 * @since 2026/1/14
 */
@Getter
@AllArgsConstructor
public enum CProfileEnum {

    local("本地"),

    dev("开发"),

    test("测试"),

    uat("验收"),

    prod("生产"),

    ;

    /**
     * 描述
     */
    final String text;

}
