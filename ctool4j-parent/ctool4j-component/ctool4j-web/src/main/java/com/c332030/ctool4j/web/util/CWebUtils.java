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
 * <h2>能力目录</h2>
 * <ul>
 *   <li>将输入流写出为响应内容（含文件名/MIME/内容长度设置）</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>文件名为空</td>
 *     <td>不设置 Content-Disposition</td>
 *   </tr>
 *   <tr>
 *     <td>MIME 未知</td>
 *     <td>记录 warn 日志，不设置 Content-Type</td>
 *   </tr>
 *   <tr>
 *     <td>contentLength 为 null</td>
 *     <td>不设置 Content-Length</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>文件下载响应（附件/内容写出）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 CRequestUtils.getResponse()（容器上下文）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>响应头</b></p>
 * <ul>
 *   <li>{@code getContentDispositionValue} 拼接 {@code attachment;filename=} 前缀。</li>
 *   <li>{@code writeResponse} 设置 Content-Disposition（有文件名时）、Content-Type（有 MIME 时，未知则告警）、</li>
 *   <li>Content-Length（指定时），并拷贝输入流到响应输出流。</li>
 * </ul>
 *
 * @since 2025/9/28
 * @version 1.0
 */
@CustomLog
@UtilityClass
public class CWebUtils {

    /**
     * 构造下载响应头值
     * <ul>
     *   <li>{@code getContentDispositionValue(filename)}：构造 Content-Disposition 响应头值</li>
     * </ul>
     *
     * @param filename 文件名
     * @return Content-Disposition 响应头值
     */
    public String getContentDispositionValue(String filename) {
        return "attachment;filename=" + filename;
    }

    /**
     * 将输入流写出为响应内容
     * <ul>
     *   <li>{@code writeResponse(inputStream, filePath)} / {@code writeResponse(inputStream, contentLength, filePath)}：</li>
     * </ul>
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
