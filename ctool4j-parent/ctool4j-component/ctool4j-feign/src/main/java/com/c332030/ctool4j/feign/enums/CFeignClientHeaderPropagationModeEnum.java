package com.c332030.ctool4j.feign.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: CFeignClientHeaderPropagationModeEnum 客户端请求头传播模式
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code ALL}（"全部"）、{@code CUSTOM}（"自定义"）、{@code NONE}（"无"）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>三个模式枚举，{@code text} 为中文描述；lombok {@code @Getter}/{@code @AllArgsConstructor} 生成。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>无。</li>
 * </ul>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>配置 {@code CFeignClientHeaderConfig.propagationMode} 决定请求头传播策略。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>模式固定三种，扩展需修改枚举。</li>
 * </ul>
 *
 * @since 2025/12/6
 * @version 1.0
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
