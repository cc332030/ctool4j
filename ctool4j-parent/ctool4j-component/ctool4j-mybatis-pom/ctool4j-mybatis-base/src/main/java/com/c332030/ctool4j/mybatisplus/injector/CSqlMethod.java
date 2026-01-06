package com.c332030.ctool4j.mybatisplus.injector;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
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
            "插入一条数据（如果存在则忽略）",
        SqlMethod.INSERT_ONE.getSql()
            .replaceAll("INSERT", "INSERT IGNORE")
    ),

    UPDATE_ALL_BY_ID(
        "根据ID 选择修改数据，数据为空则设置为空",
        null
    ),

    ;

    final String desc;
    final String sql;

    @Override
    public String getMethod() {
        return StrUtil.toCamelCase(name());
    }

}
