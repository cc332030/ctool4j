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
 * <h2>能力目录</h2>
 * <p>{@code CProfileEnum} 为环境枚举，定义 6 种环境（DEFAULT/LOCAL/DEV/TEST/UAT/PROD），每种带中文描述。</p>
 * <ul>
 *   <li>{@code getText()}：环境描述</li>
 *   <li>{@code PROD_PROFILES}：生产环境集合（仅 PROD）</li>
 *   <li>{@code PROFILE_MAP}：环境名到枚举的忽略大小写 Map</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>of(null)</td>
 *     <td>抛 IllegalArgumentException</td>
 *   </tr>
 *   <tr>
 *     <td>of(未知名)</td>
 *     <td>抛 IllegalArgumentException</td>
 *   </tr>
 *   <tr>
 *     <td>判断是否生产环境</td>
 *     <td>用 {@code PROD_PROFILES.contains}</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>Spring Profile / 部署环境统一枚举，按环境名解析、生产环境判断。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>of 依赖精确匹配（忽略大小写），未知名抛异常而非返回 null。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>未知环境名显式抛异常，保证调用方明确感知解析失败。</li>
 *   <li>PROFILE_MAP 忽略大小写，兼容 {@code DEV}/{@code dev} 等写法。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>生产环境集合</b></p>
 * <ul>
 *   <li>{@code PROD_PROFILES} 为不可变 {@code EnumSet.of(PROD)}，仅含生产环境。</li>
 * </ul>
 *
 * @since 2026/1/14
 * @version 1.0
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
     * <h2>按名获取（of）</h2>
     * <ul>
     *   <li>基于 {@code PROFILE_MAP}（{@code TreeMap(String.CASE_INSENSITIVE_ORDER)} 忽略大小写）反查。</li>
     *   <li>name 为 null 抛 IllegalArgumentException；未知名抛 {@code IllegalArgumentException("unknown profile: ...")}。</li>
     * </ul>
     * <ul>
     *   <li>{@code of(name)}：按环境名获取枚举（忽略大小写，未知名抛 IllegalArgumentException）</li>
     * </ul>
     *
     * @param name 环境名
     * @return 环境枚举
     * @throws IllegalArgumentException 环境名未知时抛出*/
    public static CProfileEnum of(String name) {
        Assert.notNull(name, "profile name must not be null");
        return Opt.ofNullable(PROFILE_MAP.get(name))
            .orElseThrow(() -> new IllegalArgumentException("unknown profile: " + name));
    }

}
