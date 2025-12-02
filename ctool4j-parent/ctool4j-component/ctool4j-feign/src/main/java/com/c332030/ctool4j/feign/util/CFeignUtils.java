package com.c332030.ctool4j.feign.util;

import feign.Response;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * <p>
 * Description: CFeignUtils
 * </p>
 *
 * @since 2025/9/21
 */
@UtilityClass
public class CFeignUtils {

    public final ThreadLocal<StringBuilder> HTTP_LOG_THREAD_LOCAL = ThreadLocal.withInitial(StringBuilder::new);

    /**
     * 构建新的响应
     * @param response 原始响应
     * @param responseBodyBytes 响应体字节数组
     * @return 新的响应
     */
    public Response newResponse(Response response, byte[] responseBodyBytes) {

        val request = response.request();

        return Response.builder()
            .requestTemplate(request.requestTemplate())
            // TODO 低版本不支持
            // .protocolVersion(response.protocolVersion())
            .status(response.status())
            .reason(response.reason())
            .request(request)
            .headers(response.headers())
            .body(responseBodyBytes)
            .build();
    }

}
