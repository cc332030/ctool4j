package com.c332030.ctool4j.mybatisplus.injector.methods;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.extension.injector.methods.AlwaysUpdateSomeColumnById;
import com.c332030.ctool4j.mybatisplus.injector.CMpSqlMethod;
import com.c332030.ctool4j.mybatisplus.injector.ICMpMethod;
import com.c332030.ctool4j.mybatisplus.util.CMpFieldUtils;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: CUpdateAllByIdMethod
 * </p>
 *
 * @since 2026/1/6
 */
@Component
public class CUpdateAllByIdMethod extends AlwaysUpdateSomeColumnById implements ICMpMethod {

    private static final long serialVersionUID = 1L;

    public CUpdateAllByIdMethod() {
        super(CMpFieldUtils.UPDATE_NOT_NEVER);
    }

    @Override
    public String getMethod(SqlMethod sqlMethod) {
        return CMpSqlMethod.UPDATE_ALL_BY_ID.getMethod();
    }

}
