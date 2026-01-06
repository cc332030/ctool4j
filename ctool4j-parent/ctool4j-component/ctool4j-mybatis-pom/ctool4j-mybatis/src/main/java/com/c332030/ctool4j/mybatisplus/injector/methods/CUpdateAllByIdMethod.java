package com.c332030.ctool4j.mybatisplus.injector.methods;

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
        super(
            CMpSqlMethod.UPDATE_ALL_BY_ID.getMethod(),
            CMpFieldUtils.UPDATE_NOT_NEVER
        );
    }

}
