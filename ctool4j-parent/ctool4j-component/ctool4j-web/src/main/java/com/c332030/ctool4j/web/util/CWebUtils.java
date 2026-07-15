package com.c332030.ctool4j.web.util;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.spring.util.CFileUtils;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;

import java.io.InputStream;

/**
 * <p>
 * Description: CWebUtils
 * </p>
 *
 * @since 2025/9/28
 */
@CustomLog
@UtilityClass
public class CWebUtils {

    public String getContentDispositionValue(String filename) {
        return "attachment;filename=" + filename;
    }

    @SneakyThrows
    public void writeResponse(InputStream inputStream, String filePath) {

        val bytes = IoUtil.readBytes(inputStream);
        if (ArrayUtils.isEmpty(bytes)) {
            return;
        }

        val response = CRequestUtils.getResponse();

        val mineType = CFileUtils.getMimeType(filePath);
        if (StrUtil.isEmpty(mineType)) {
            log.warn("未知 MimeType，object: {}", filePath);
        } else {
            response.setContentType(mineType);
        }

        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);

    }

}
