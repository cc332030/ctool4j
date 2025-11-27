package com.c332030.ctool4j.mybatisplus.injector;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: MySqlMethod
 * </p>
 *
 * @author c332030
 * @since 2024/5/7
 */
@Getter
@AllArgsConstructor
public enum CSqlMethod implements ISqlMethodEnum {

    INSERT_IGNORE(
            "insertIgnore",
            "插入一条数据（如果存在则忽略）",
            "<script>\nINSERT IGNORE INTO %s %s VALUES %s\n</script>"
    ),

    ;

    private final String method;
    private final String desc;
    private final String sql;

}
