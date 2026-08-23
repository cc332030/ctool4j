package com.c332030.ctool4j.mybatisplus.injector.methods;

import com.baomidou.mybatisplus.core.injector.methods.Insert;
import com.c332030.ctool4j.mybatisplus.injector.CMpSqlMethod;
import com.c332030.ctool4j.mybatisplus.injector.ICMpMethod;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

/**
 * <p>
 * Description: CInsertIgnoreMethod
 * </p>
 *
 * @author c332030
 * @since 2024/5/7
 * @see "doc/design/mybatisplus/CInsertIgnoreMethod.adoc"
 */
public class CInsertIgnoreMethod extends Insert implements ICMpMethod {

    private static final long serialVersionUID = 1L;

    /**
     * 构造方法，指定使用 INSERT_IGNORE 方法
     *
     * @param ignoreAutoIncrementColumn 是否忽略自增主键列
     */
    public CInsertIgnoreMethod(boolean ignoreAutoIncrementColumn) {
        super(
            CMpSqlMethod.INSERT_IGNORE.getMethod(),
            ignoreAutoIncrementColumn
        );
    }

    /**
     * 创建 SQL 源：将 INSERT 替换为 INSERT IGNORE
     *
     * @param configuration MyBatis 配置
     * @param script        原始 SQL 脚本
     * @param parameterType 参数类型
     * @return SQL 源
     */
    @Override
    public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
        script = script.replaceAll("INSERT", "INSERT IGNORE");
        return super.createSqlSource(configuration, script, parameterType);
    }

}
