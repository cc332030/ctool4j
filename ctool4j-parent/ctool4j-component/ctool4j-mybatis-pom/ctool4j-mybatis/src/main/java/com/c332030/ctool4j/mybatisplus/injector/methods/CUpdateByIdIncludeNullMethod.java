package com.c332030.ctool4j.mybatisplus.injector.methods;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.extension.injector.methods.AlwaysUpdateSomeColumnById;
import com.c332030.ctool4j.mybatisplus.injector.CSqlMethod;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: CUpdateByIdIncludeNullMethod
 * </p>
 *
 * @since 2026/1/6
 */
@Component
public class CUpdateByIdIncludeNullMethod extends AlwaysUpdateSomeColumnById {

    private static final long serialVersionUID = 1L;

    public CUpdateByIdIncludeNullMethod() {
        super(
            CSqlMethod.UPDATE_BY_ID_INCLUDE_NULL.getMethod(),
            TableFieldInfo::isWithUpdateFill
        );
    }

}
