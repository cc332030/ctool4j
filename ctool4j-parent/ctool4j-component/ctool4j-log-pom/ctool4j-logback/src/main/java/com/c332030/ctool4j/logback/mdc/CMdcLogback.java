package com.c332030.ctool4j.logback.mdc;

import com.c332030.ctool4j.log.mdc.CMdc;
import lombok.val;
import org.slf4j.spi.MDCAdapter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * Description: CMdcLogback
 * </p>
 *
 * <p>
 * logback 后端的 MDC 适配器，实现 slf4j {@link MDCAdapter}；
 * 存储与基础读写逻辑见父类 {@link CMdc}，此处仅补充 slf4j 特有语义。
 * </p>
 *
 * @see "doc/design/log/CMdcLogback.adoc"
 * @see "doc/design/log/CMdcLogbackTests.adoc"
 * @since 2025/9/26
 */
public class CMdcLogback extends CMdc implements MDCAdapter {

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

        val map = getMdcMap();
        map.clear();

        if(contextMap != null) {
            map.putAll(contextMap);
        }
    }

}
