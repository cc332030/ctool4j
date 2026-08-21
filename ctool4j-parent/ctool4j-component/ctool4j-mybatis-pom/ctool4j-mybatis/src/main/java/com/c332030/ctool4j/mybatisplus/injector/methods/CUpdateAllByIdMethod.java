package com.c332030.ctool4j.mybatisplus.injector.methods;

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
 * @see doc/design/mybatisplus/CUpdateAllByIdMethod.adoc
 */
public class CUpdateAllByIdMethod extends AlwaysUpdateSomeColumnById implements ICMpMethod {

    private static final long serialVersionUID = 1L;

    /**
     * 构造方法，指定使用 UPDATE_ALL_BY_ID 方法
     */
    public CUpdateAllByIdMethod() {
        super(
            CMpSqlMethod.UPDATE_ALL_BY_ID.getMethod(),
            CMpFieldUtils.UPDATE_NOT_NEVER
        );
    }

}
