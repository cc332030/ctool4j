package com.c332030.ctool4j.logback.mdc;

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

    /**
     * 放入键值
     *
     * @param key 键
     * @param val 值
     */
    @Override
    public void put(String key, String val) {
        getMdcMap().put(key, val);
    }

    /**
     * 获取键对应值
     *
     * @param key 键
     * @return 值
     */
    @Override
    public String get(String key) {
        return getMdcMap().get(key);
    }

    /**
     * 移除键
     *
     * @param key 键
     */
    @Override
    public void remove(String key) {
        getMdcMap().remove(key);
    }

    /**
     * 清空当前线程上下文
     */
    @Override
    public void clear() {
        MDC_MAP_THREAD_LOCAL.remove();
    }

    /**
     * 获取上下文副本
     *
     * @return 上下文副本
     */
    @Override
    public Map<String, String> getCopyOfContextMap() {
        return new LinkedHashMap<>(getMdcMap());
    }

    /**
     * 设置上下文，null 时清空
     *
     * @param contextMap 上下文
     */
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
