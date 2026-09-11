package com.c332030.ctool4j.mybatisplus.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.c332030.ctool4j.mybatisplus.injector.methods.CInsertIgnoreMethod;
import com.c332030.ctool4j.mybatisplus.injector.methods.CUpdateAllByIdMethod;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.val;
import org.apache.ibatis.session.Configuration;

import java.util.List;

/**
 * <p>
 * Description: CSqlInjector
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CSqlInjector}：SQL 注入器。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>继承 DefaultSqlInjector，注入自定义方法列表</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>默认方法 + 自定义</p>
 * <h2>适用范围</h2>
 * <p>SQL 注入器扩展</p>
 * <h2>不适用与边界场景</h2>
 * <p>继承 DefaultSqlInjector</p>
 * <h2>已知限制与取舍</h2>
 * <p>继承 DefaultSqlInjector</p>
 *
 * @author c332030
 * @since 2024/5/7
 */
@CustomLog
//@Component
@AllArgsConstructor
public class CSqlInjector extends DefaultSqlInjector {

    /**
     * 获取方法列表：默认方法基础上追加 INSERT_IGNORE、UPDATE_ALL_BY_ID
     *
     * @param configuration MyBatis 配置
     * @param mapperClass   Mapper 类
     * @param tableInfo     表信息
     * @return 方法列表
     */
    @Override
    public List<AbstractMethod> getMethodList(Configuration configuration, Class<?> mapperClass, TableInfo tableInfo) {

        val methods = super.getMethodList(configuration, mapperClass, tableInfo);
        try {

            val dbConfig = GlobalConfigUtils.getDbConfig(configuration);
            val insertIgnoreMethod = new CInsertIgnoreMethod(dbConfig.isInsertIgnoreAutoIncrementColumn());
            methods.add(insertIgnoreMethod);

            methods.add(new CUpdateAllByIdMethod());

        } catch (Throwable e) {
            log.error("注册 mybatis plus 方法失败", e);
        }

        return methods;
    }

}
