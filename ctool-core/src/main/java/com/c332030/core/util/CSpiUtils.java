package com.c332030.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * <p>
 * Description: CSpiUtils
 * </p>
 *
 * @since 2025/9/14
 */
@UtilityClass
public class CSpiUtils {

    /**
     * 获取第一个实现
     */
    public <T> T getFirstImpl(Class<T> clazz) {

        val services = getImpls(clazz);
        if(CollUtil.isEmpty(services)) {
            throw new IllegalStateException("No impl for " + clazz);
        }

        return services.get(0);
    }

    /**
     * 获取第一个自定义实现，如果没有则返回默认实现
     */
    public <T> T getFirstCustomImplOrDefault(Class<T> clazz, Class<? extends T> defaultImpl) {

        val providers = CSpiUtils.getImpls(clazz);

        T defaultProvider = null;
        T customProvider = null;
        for (val provider : providers) {

            if(defaultImpl.isInstance(provider)) {
                defaultProvider = provider;
            } else {
                customProvider = provider;
                break;
            }
        }

        return ObjUtil.defaultIfNull(customProvider, defaultProvider);
    }

    /**
     * 获取所有实现
     */
    public <T> List<T> getImpls(Class<T> clazz) {

        val services = ServiceLoader.load(clazz);

        val list = new ArrayList<T>();
        for (val service : services) {
            list.add(service);
        }

        return list;
    }

}
