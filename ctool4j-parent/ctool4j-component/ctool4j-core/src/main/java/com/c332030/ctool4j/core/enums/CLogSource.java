package com.c332030.ctool4j.core.enums;

import com.c332030.ctool4j.core.interfaces.ICSource;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: 日志来源枚举
 * </p>
 *
 * @author c332030
 * @see doc/design/core/CLogSource.adoc
 */
@Getter
@AllArgsConstructor
public enum CLogSource implements ICSource {

    /**
     * 服务端 MVC 接口收到的请求
     */
    MVC("mvc"),

    /**
     * feign 客户端发起的请求
     */
    FEIGN("feign"),

    ;

    /**
     * 来源标识（日志最前面 [source] 前缀）
     */
    private final String text;

}
