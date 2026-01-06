package com.c332030.ctool4j.mybatisplus.injector.methods;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.methods.Insert;
import com.c332030.ctool4j.mybatisplus.injector.CSqlMethod;
import com.c332030.ctool4j.mybatisplus.injector.ICMpMethod;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: MySQLInsertIgnore
 * </p>
 *
 * @author c332030
 * @since 2024/5/7
 */
@Component
public class CInsertIgnoreMethod extends Insert implements ICMpMethod {

    private static final long serialVersionUID = 1L;

    @Override
    public String getMethod(SqlMethod sqlMethod) {
        return CSqlMethod.INSERT_IGNORE.getMethod();
    }

}
