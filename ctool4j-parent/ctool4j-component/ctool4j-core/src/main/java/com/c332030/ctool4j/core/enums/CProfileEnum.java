package com.c332030.ctool4j.core.enums;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.util.*;

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

    /**
     * 生产环境集合
     */
    public static final Set<CProfileEnum> PROD_PROFILES = Collections.unmodifiableSet(EnumSet.of(PROD));

    /**
     * 环境名到枚举的 Map（忽略大小写）
     */
    public static final Map<String, CProfileEnum> PROFILE_MAP;
    static {
        val map = new TreeMap<String, CProfileEnum>(String.CASE_INSENSITIVE_ORDER);
        for (val value : values()) {
            map.put(value.name(), value);
        }
        PROFILE_MAP = Collections.unmodifiableMap(map);
    }

    /**
     * 描述
     */
    final String text;

    /**
     * 根据环境名获取枚举
     *
     * @param name 环境名
     * @return 环境枚举
     * @throws IllegalArgumentException 环境名未知时抛出
     */
    public static CProfileEnum of(String name) {
        Assert.notNull(name, "profile name must not be null");
        return Opt.ofNullable(PROFILE_MAP.get(name))
            .orElseThrow(() -> new IllegalArgumentException("unknown profile: " + name));
    }

}
