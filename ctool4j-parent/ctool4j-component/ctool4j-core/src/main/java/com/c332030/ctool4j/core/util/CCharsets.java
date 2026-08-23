package com.c332030.ctool4j.core.util;

import lombok.experimental.UtilityClass;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 * Description: CCharsets
 * </p>
 *
 * <p>
 * 项目全局统一字符集常量，各模块共用，避免每处重复指定字符集或依赖平台默认字符集导致跨环境不一致
 * </p>
 *
 * @since 2026/8/15
 * @see "doc/design/core/CCharsets.adoc"
 */
@UtilityClass
public class CCharsets {

    /**
     * 全局统一编码：UTF-8
     */
    public static final Charset UTF_8 = StandardCharsets.UTF_8;

}
