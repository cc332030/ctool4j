package com.c332030.ctool4j.mybatisplus;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
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
 */
@Component
public class CSqlInjector extends DefaultSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        val methods = super.getMethodList(mapperClass);
        methods.add(new InsertIgnore());
        return methods;
    }

}
