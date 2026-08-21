package com.c332030.ctool4j.spring.util;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CMap;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.server.MimeMappings;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

/**
 * <p>
 * Description: CFileUtils
 * </p>
 *
 * @since 2026/7/15
 * @see doc/design/spring/CFileUtils.adoc
 * @see doc/design/spring/CFileUtilsTests.adoc
 */
@UtilityClass
public class CFileUtils {

    /**
     * 从路径中提取文件名
     *
     * @param path 路径
     * @return 文件名；路径为空时返回 null
     */
    public String getFileName(String path) {

        if(StrUtil.isEmpty(path)){
            return null;
        }

        val index = path.lastIndexOf("/");
        if(index < 0) {
            return path;
        }

        return path.substring(index + 1);
    }

    /**
     * 获取文件类型
     * <p>
     *
     * @param file 文件名
     * @return 后缀（不含".")
     */
    public String getFileType(File file) {
        if (null == file) {
            return StringUtils.EMPTY;
        }
        return getFileType(file.getName());
    }

    /**
     * 获取文件类型
     * <p>
     *
     * @param fileName 文件名
     * @return 后缀（不含".")
     */
    public String getFileType(String fileName) {
        int separatorIndex = fileName.lastIndexOf(".");
        if (separatorIndex < 0) {
            return "";
        }
        return fileName.substring(separatorIndex + 1).toLowerCase();
    }

    /**
     * 按内容获取文件类型
     *
     * @param inputStream 输入流
     * @return 文件类型
     */
    public String getFileType(InputStream inputStream) {
        return FileTypeUtil.getType(inputStream);
    }

    /**
     * 获取路径对应文件的 MIME 类型
     *
     * @param path 路径
     * @return MIME 类型；无法识别时返回 null
     */
    public String getMimeType(String path) {
        return getMimeType(path, null);
    }

    /**
     * 常见但默认映射缺失的 MIME 类型补充映射
     */
    public final Map<String, String> EXTRA_MIME_TYPE_MAP = CMap.of(
        "webp", "image/webp",
        "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    /**
     * 获取路径对应文件的 MIME 类型，无法识别时返回默认类型
     *
     * @param path        路径
     * @param defaultType 默认 MIME 类型
     * @return MIME 类型
     */
    public String getMimeType(String path, String defaultType) {

        var extension = Opt.ofBlankAble(getFileType(path))
            .orElse(path);

        var mimeType = MimeMappings.DEFAULT.get(extension);
        if(StringUtils.isEmpty(mimeType)) {
            mimeType = EXTRA_MIME_TYPE_MAP.get(extension);
        }

        return ObjectUtils.defaultIfNull(mimeType, defaultType);
    }

}
