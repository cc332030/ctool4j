package com.c332030.ctool4j.log.logback;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.slf4j.spi.MDCAdapter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>
 * Description: CMdc
 * </p>
 *
 * @since 2025/9/26
 */
public class CMdc implements MDCAdapter {

    private static final TransmittableThreadLocal<Map<String, String>> MDC_MAP_THREAD_LOCAL =
            TransmittableThreadLocal.withInitial(ConcurrentHashMap::new);

    private Map<String, String> getMdcMap() {
        return MDC_MAP_THREAD_LOCAL.get();
    }

    @Override
    public void put(String key, String val) {
        getMdcMap().put(key, val);
    }

    @Override
    public String get(String key) {
        return getMdcMap().get(key);
    }

    @Override
    public void remove(String key) {
        getMdcMap().remove(key);
    }

    @Override
    public void clear() {
        MDC_MAP_THREAD_LOCAL.remove();
    }

    @Override
    public Map<String, String> getCopyOfContextMap() {
        return new LinkedHashMap<>(getMdcMap());
    }

    @Override
    public void setContextMap(Map<String, String> contextMap) {

        if(contextMap == null) {
            clear();
            return;
        }

        if(contextMap instanceof ConcurrentMap){
            MDC_MAP_THREAD_LOCAL.set(contextMap);
            return;
        }

        MDC_MAP_THREAD_LOCAL.set(Collections.synchronizedMap(contextMap));
    }

}
