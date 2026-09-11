package com.c332030.ctool4j.mybatisplus.injector;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: CMpSqlMethod
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CMpSqlMethod}：SQL 方法枚举。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>实现 ICMpSqlMethod，定义方法名/描述/SQL</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>枚举名转驼峰</p>
 * <h2>适用范围</h2>
 * <p>SQL 方法定义</p>
 * <h2>不适用与边界场景</h2>
 * <p>枚举</p>
 * <h2>已知限制与取舍</h2>
 * <p>枚举</p>
 *
 * @author c332030
 * @since 2024/5/7
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum CMpSqlMethod implements ICMpSqlMethod {

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

    /**
     * 获取方法名（枚举名转驼峰）
     *
     * @return 方法名
     */
    @Override
    public String getMethod() {
        return StrUtil.toCamelCase(name());
    }

}
