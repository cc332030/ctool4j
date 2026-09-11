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
 * <h2>能力目录</h2>
 * <p>{@code CInsertIgnoreMethod}：插入忽略方法。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>生成 INSERT IGNORE 注入方法（存在则忽略）</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>插入忽略 SQL</p>
 * <h2>不适用与边界场景</h2>
 * <p>注入方法</p>
 * <h2>已知限制与取舍</h2>
 * <p>注入方法</p>
 *
 * @author c332030
 * @since 2024/5/7
 * @version 1.0
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
