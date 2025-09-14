package com.c332030.core.util;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Iterator;
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

    public <T> T getFirstImpl(Class<T> clazz) {

        val serviceIterator = getImpls(clazz);
        if(!serviceIterator.hasNext()) {
            throw new IllegalStateException("No impl for " + clazz);
        }

        return serviceIterator.next();
    }

    public <T> Iterator<T> getImpls(Class<T> clazz) {

        val services = ServiceLoader.load(clazz);
        return services.iterator();
    }

}
