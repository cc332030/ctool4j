package com.c332030.ctool4j.mybatisplus.injector.methods;

import com.baomidou.mybatisplus.core.injector.methods.Insert;
import com.c332030.ctool4j.mybatisplus.injector.CSqlMethod;
import com.c332030.ctool4j.mybatisplus.injector.ICMpMethod;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

/**
 * <p>
 * Description: MySQLInsertIgnore
 * </p>
 *
 * @author c332030
 * @since 2024/5/7
 */
public class CInsertIgnoreMethod extends Insert implements ICMpMethod {

    private static final long serialVersionUID = 1L;

    public CInsertIgnoreMethod(boolean ignoreAutoIncrementColumn) {
        super(
            CSqlMethod.INSERT_IGNORE.getMethod(),
            ignoreAutoIncrementColumn
        );
    }

    @Override
    public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
        script = script.replaceAll("INSERT", "INSERT IGNORE");
        return super.createSqlSource(configuration, script, parameterType);
    }

}
