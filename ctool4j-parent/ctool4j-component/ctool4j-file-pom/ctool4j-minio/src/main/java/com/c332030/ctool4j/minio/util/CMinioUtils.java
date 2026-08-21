package com.c332030.ctool4j.minio.util;

import cn.hutool.core.lang.Opt;
import io.minio.GetObjectResponse;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;

/**
 * <p>
 * Description: CMinioUtils
 * </p>
 *
 * @see doc/design/minio/CMinioUtils.adoc
 * @since 2026/7/15
 */
@UtilityClass
public class CMinioUtils {

    /**
     * 从响应头获取对象大小
     *
     * @param response Minio 获取对象响应
     * @return 对象大小（字节）；无法获取时返回 null
     */
    public Long getSize(GetObjectResponse response) {
        return Opt.ofNullable(response)
            .map(GetObjectResponse::headers)
            .map(e -> e.get(HttpHeaders.CONTENT_LENGTH))
            .map(Long::valueOf)
            .orElse(null);
    }

}
