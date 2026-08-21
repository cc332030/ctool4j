package com.c332030.ctool4j.mybatisplus.injector.methods;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.extension.injector.methods.AlwaysUpdateSomeColumnById;
import com.c332030.ctool4j.mybatisplus.injector.CMpSqlMethod;
import com.c332030.ctool4j.mybatisplus.injector.ICMpMethod;
import com.c332030.ctool4j.mybatisplus.util.CMpFieldUtils;

/**
 * <p>
 * Description: CUpdateAllByIdMethod
 * </p>
 *
 * @since 2026/1/6
 * @see "doc/design/mybatisplus/CUpdateAllByIdMethod.adoc"
 */
public class CUpdateAllByIdMethod extends AlwaysUpdateSomeColumnById implements ICMpMethod {

    /**
     * 构造方法，指定无需更新的字段
     */
    public CUpdateAllByIdMethod() {
        super(CMpFieldUtils.UPDATE_NOT_NEVER);
    }

    /**
     * 返回 UPDATE_ALL_BY_ID 方法名
     *
     * @param sqlMethod 默认 SQL 方法
     * @return 方法名
     */
    @Override
    public String getMethod(SqlMethod sqlMethod) {
        return CMpSqlMethod.UPDATE_ALL_BY_ID.getMethod();
    }

}
