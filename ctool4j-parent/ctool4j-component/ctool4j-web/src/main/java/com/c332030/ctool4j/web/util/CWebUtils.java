package com.c332030.ctool4j.web.util;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.spring.util.CFileUtils;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpHeaders;

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
        writeResponse(inputStream, null, filePath);
    }

    @SneakyThrows
    public void writeResponse(InputStream inputStream, Number contentLength, String filePath) {

        val response = CRequestUtils.getResponse();

        val fileName = CFileUtils.getFileName(filePath);
        if(StrUtil.isNotEmpty(fileName)){
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, getContentDispositionValue(fileName));
        }

        val mineType = CFileUtils.getMimeType(filePath);
        if (StrUtil.isEmpty(mineType)) {
            log.warn("未知 MimeType，object: {}", filePath);
        } else {
            response.setContentType(mineType);
        }

        if(null != contentLength) {
            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength));
        }
        IoUtil.copy(inputStream, response.getOutputStream());

    }

}
