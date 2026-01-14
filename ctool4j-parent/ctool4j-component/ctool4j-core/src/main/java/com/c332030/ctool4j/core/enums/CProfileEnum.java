package com.c332030.ctool4j.core.enums;

import cn.hutool.core.lang.Opt;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

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

    DEFAULT("默认"),

    LOCAL("本地"),

    DEV("开发"),

    TEST("测试"),

    UAT("验收"),

    PROD("生产"),

    ;

    public static final Map<String, CProfileEnum> PROFILE_MAP;
    static {
        val map = new TreeMap<String, CProfileEnum>();
        for (val value : values()) {
            map.put(value.name(), value);
        }
        PROFILE_MAP = Collections.unmodifiableMap(map);
    }

    /**
     * 描述
     */
    final String text;

    public static CProfileEnum of(String name) {
        return Opt.ofNullable(PROFILE_MAP.get(name))
            .orElseThrow(() -> new IllegalArgumentException("unknown profile name: " + name));
    }

}
