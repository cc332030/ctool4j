package com.c332030.ctool4j.mybatisplus.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.c332030.ctool4j.mybatisplus.injector.methods.CInsertIgnoreMethod;
import com.c332030.ctool4j.mybatisplus.injector.methods.CUpdateAllByIdMethod;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.val;

import java.util.List;

/**
 * <p>
 * Description: CSqlInjector
 * </p>
 *
 * @author c332030
 * @since 2024/5/7
 */
@CustomLog
//@Component
@AllArgsConstructor
public class CSqlInjector extends DefaultSqlInjector {

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
