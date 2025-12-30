package com.c332030.ctool4j.definition.enums;

import com.c332030.ctool4j.definition.interfaces.ICOperate;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: CDbOperateEnum
 * </p>
 *
 * @since 2025/11/10
 */
@Getter
@AllArgsConstructor
public enum CDbOperateEnum implements ICOperate {

    INSERT("插入"),

    SELECT("查询"),

    UPDATE("更新"),

    DELETE("删除"),

    ;

    /**
     * 描述
     */
    final String text;

}
