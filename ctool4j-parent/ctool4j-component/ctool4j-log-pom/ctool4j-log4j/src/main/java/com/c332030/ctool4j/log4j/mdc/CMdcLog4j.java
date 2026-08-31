package com.c332030.ctool4j.log4j.mdc;

import com.c332030.ctool4j.log.mdc.CMdc;
import org.apache.logging.log4j.spi.ThreadContextMap;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Description: CMdcLog4j
 * </p>
 *
 * <p>
 * log4j2 后端的 MDC 适配器，实现 log4j2 {@link ThreadContextMap}；
 * 存储与基础读写逻辑见父类 {@link CMdc}，此处仅补充 log4j2 特有语义。
 * </p>
 *
 * @see "doc/design/log4j/CMdcLog4j.adoc"
 * @see "doc/design/log4j/CMdcLog4jTests.adoc"
 * @since 2026/8/31
 */
public class CMdcLog4j extends CMdc implements ThreadContextMap {

    /**
     * 获取上下文副本
     *
     * @return 上下文副本
     */
    @Override
    public Map<String, String> getCopy() {
        return new HashMap<>(getMdcMap());
    }

    /**
     * 获取不可变上下文或 null
     *
     * @return 不可变上下文，空时返回 null
     */
    @Override
    public Map<String, String> getImmutableMapOrNull() {
        if(isEmpty()) {
            return null;
        }
        return getImmutableMap();
    }

}
