package com.c332030.ctool4j.mybatisplus.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * Description: CSqlInjector
 * </p>
 *
 * @author c332030
 * @since 2024/5/7
 */
@Component
@AllArgsConstructor
public class CSqlInjector extends DefaultSqlInjector {

    Collection<ICMpMethod> icMpMethods;

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
        val methods = super.getMethodList(mapperClass, tableInfo);
        icMpMethods.stream()
            .map(e -> (AbstractMethod)e)
            .forEach(methods::add);
        return methods;
    }

}
