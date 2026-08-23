package com.c332030.ctool4j.mybatisplus.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.c332030.ctool4j.mybatisplus.injector.methods.CInsertIgnoreMethod;
import com.c332030.ctool4j.mybatisplus.injector.methods.CUpdateAllByIdMethod;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.val;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * Description: CSqlInjector
 * </p>
 *
 * @author c332030
 * @since 2024/5/7
 * @see "doc/design/mybatisplus/CSqlInjector.adoc"
 */
@CustomLog
@Component
@AllArgsConstructor
public class CSqlInjector extends DefaultSqlInjector {

    /**
     * 获取方法列表：默认方法基础上追加 INSERT_IGNORE、UPDATE_ALL_BY_ID
     *
     * @param mapperClass Mapper 类
     * @param tableInfo   表信息
     * @return 方法列表
     */
    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
        val methods = super.getMethodList(mapperClass, tableInfo);
        try {

            methods.add(new CInsertIgnoreMethod());
            methods.add(new CUpdateAllByIdMethod());
        } catch (Throwable e) {
            log.error("注册 mybatis plus 方法失败", e);
        }

        return methods;
    }

}
