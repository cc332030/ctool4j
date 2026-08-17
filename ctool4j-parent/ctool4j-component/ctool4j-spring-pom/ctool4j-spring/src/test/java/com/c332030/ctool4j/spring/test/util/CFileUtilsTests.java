package com.c332030.ctool4j.spring.test.util;

import com.c332030.ctool4j.spring.util.CFileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * <p>
 * Description: CFileUtilsTests
 * </p>
 *
 * @since 2026/8/14
 */
class CFileUtilsTests {

    // ---------- getFileName ----------

    @Test
    void testGetFileName_null() {
        Assertions.assertNull(CFileUtils.getFileName(null));
    }

    @Test
    void testGetFileName_empty() {
        Assertions.assertNull(CFileUtils.getFileName(""));
    }

    @Test
    void testGetFileName_blank() {
        // 易错：hutool StrUtil.isEmpty 仅把 null/"" 视为空，纯空格非空，原样返回
        Assertions.assertEquals("  ", CFileUtils.getFileName("  "));
    }

    @Test
    void testGetFileName_noSlash() {
        Assertions.assertEquals("a.txt", CFileUtils.getFileName("a.txt"));
    }

    @Test
    void testGetFileName_withSlash() {
        Assertions.assertEquals("a.txt", CFileUtils.getFileName("/dir/a.txt"));
    }

    @Test
    void testGetFileName_multiSlash() {
        Assertions.assertEquals("c.txt", CFileUtils.getFileName("/a/b/c.txt"));
    }

    @Test
    void testGetFileName_endsWithSlash() {
        Assertions.assertEquals("", CFileUtils.getFileName("/a/b/"));
    }

    @Test
    void testGetFileName_singleSlash() {
        Assertions.assertEquals("", CFileUtils.getFileName("/"));
    }

    @Test
    void testGetFileName_backslash_kept() {
        // 仅按 "/" 分割，反斜杠不作为分隔符
        Assertions.assertEquals("a\\b.txt", CFileUtils.getFileName("a\\b.txt"));
    }

    // ---------- getFileType(File) ----------

    @Test
    void testGetFileTypeFile_null() {
        Assertions.assertEquals("", CFileUtils.getFileType((File) null));
    }

    @Test
    void testGetFileTypeFile_normal() {
        File f = new File("test.txt");
        Assertions.assertEquals("txt", CFileUtils.getFileType(f));
    }

    @Test
    void testGetFileTypeFile_noExtension() {
        File f = new File("test");
        Assertions.assertEquals("", CFileUtils.getFileType(f));
    }

    // ---------- getFileType(String) ----------

    @Test
    void testGetFileTypeString_null_throwsNPE() {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> CFileUtils.getFileType((String) null));
    }

    @Test
    void testGetFileTypeString_normal() {
        Assertions.assertEquals("txt", CFileUtils.getFileType("a.txt"));
    }

    @Test
    void testGetFileTypeString_uppercase() {
        Assertions.assertEquals("jpg", CFileUtils.getFileType("a.JPG"));
    }

    @Test
    void testGetFileTypeString_noDot() {
        Assertions.assertEquals("", CFileUtils.getFileType("abc"));
    }

    @Test
    void testGetFileTypeString_multiDot() {
        // 易错：lastIndexOf 定位最后一个点，只取最后一段扩展名
        Assertions.assertEquals("gz", CFileUtils.getFileType("a.tar.gz"));
    }

    @Test
    void testGetFileTypeString_endsWithDot() {
        Assertions.assertEquals("", CFileUtils.getFileType("a."));
    }

    // ---------- getMimeType ----------

    @Test
    void testGetMimeType_jpg() {
        Assertions.assertEquals("image/jpeg", CFileUtils.getMimeType("a.jpg"));
    }

    @Test
    void testGetMimeType_webp_extraMap() {
        Assertions.assertEquals("image/webp", CFileUtils.getMimeType("a.webp"));
    }

    @Test
    void testGetMimeType_xlsx_extraMap() {
        Assertions.assertEquals(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            CFileUtils.getMimeType("a.xlsx")
        );
    }

    @Test
    void testGetMimeType_unknown_returnsDefault() {
        Assertions.assertEquals("application/octet-stream", CFileUtils.getMimeType("a.zzz", "application/octet-stream"));
    }

    @Test
    void testGetMimeType_unknown_noDefault_returnsNull() {
        Assertions.assertNull(CFileUtils.getMimeType("a.zzz"));
    }

    @Test
    void testGetMimeType_noExtension_pathAsExt() {
        // 无后缀时 extension 兜底为 path 本身，查不到则返回 default
        Assertions.assertEquals("text/plain", CFileUtils.getMimeType("txt", "text/plain"));
    }

    @Test
    void testGetMimeType_uppercasePath() {
        Assertions.assertEquals("image/png", CFileUtils.getMimeType("a.PNG"));
    }

    @Test
    void testGetMimeType_nullPath_throwsNPE() {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> CFileUtils.getMimeType(null));
    }
}
