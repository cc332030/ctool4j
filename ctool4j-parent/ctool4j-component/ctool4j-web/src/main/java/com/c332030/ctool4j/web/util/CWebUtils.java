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
 * @see doc/design/web/CWebUtils.adoc
 * @see doc/design/web/CWebUtilsTests.adoc
 */
@CustomLog
@UtilityClass
public class CWebUtils {

    /**
     * 构造下载响应头值
     *
     * @param filename 文件名
     * @return Content-Disposition 响应头值
     */
    public String getContentDispositionValue(String filename) {
        return "attachment;filename=" + filename;
    }

    /**
     * 将输入流写出为响应内容
     *
     * @param inputStream 内容输入流
     * @param filePath    文件路径，用于推断文件名与 MIME 类型
     */
    @SneakyThrows
    public void writeResponse(InputStream inputStream, String filePath) {
        writeResponse(inputStream, null, filePath);
    }

    /**
     * 将输入流写出为响应内容，可指定内容长度
     *
     * @param inputStream    内容输入流
     * @param contentLength  内容长度，为空时不设置
     * @param filePath       文件路径，用于推断文件名与 MIME 类型
     */
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
