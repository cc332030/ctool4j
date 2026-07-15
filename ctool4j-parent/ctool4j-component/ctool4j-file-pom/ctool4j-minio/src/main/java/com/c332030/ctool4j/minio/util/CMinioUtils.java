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
 * @since 2026/7/15
 */
@UtilityClass
public class CMinioUtils {

    public Integer getContentLength(GetObjectResponse response) {
        return Opt.ofNullable(response)
            .map(GetObjectResponse::headers)
            .map(e -> e.get(HttpHeaders.CONTENT_LENGTH))
            .map(Integer::valueOf)
            .orElse(null);
    }

}
