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

        /**
     * 对应测试用例 1.1
     */
    @Test
    void testGetFileName_null() {
        Assertions.assertNull(CFileUtils.getFileName(null));
    }

        /**
     * 对应测试用例 1.2
     */
    @Test
    void testGetFileName_empty() {
        Assertions.assertNull(CFileUtils.getFileName(""));
    }

        /**
     * 对应测试用例 1.3
     */
    @Test
    void testGetFileName_blank() {
        // 易错：hutool StrUtil.isEmpty 仅把 null/"" 视为空，纯空格非空，原样返回
        Assertions.assertEquals("  ", CFileUtils.getFileName("  "));
    }

        /**
     * 对应测试用例 1.4
     */
    @Test
    void testGetFileName_noSlash() {
        Assertions.assertEquals("a.txt", CFileUtils.getFileName("a.txt"));
    }

        /**
     * 对应测试用例 1.5
     */
    @Test
    void testGetFileName_withSlash() {
        Assertions.assertEquals("a.txt", CFileUtils.getFileName("/dir/a.txt"));
    }

        /**
     * 对应测试用例 1.6
     */
    @Test
    void testGetFileName_multiSlash() {
        Assertions.assertEquals("c.txt", CFileUtils.getFileName("/a/b/c.txt"));
    }

        /**
     * 对应测试用例 1.7
     */
    @Test
    void testGetFileName_endsWithSlash() {
        Assertions.assertEquals("", CFileUtils.getFileName("/a/b/"));
    }

        /**
     * 对应测试用例 1.8
     */
    @Test
    void testGetFileName_singleSlash() {
        Assertions.assertEquals("", CFileUtils.getFileName("/"));
    }

        /**
     * 对应测试用例 1.9
     */
    @Test
    void testGetFileName_backslash_kept() {
        // 仅按 "/" 分割，反斜杠不作为分隔符
        Assertions.assertEquals("a\\b.txt", CFileUtils.getFileName("a\\b.txt"));
    }

    // ---------- getFileType(File) ----------

        /**
     * 对应测试用例 1.10
     */
    @Test
    void testGetFileTypeFile_null() {
        Assertions.assertEquals("", CFileUtils.getFileType((File) null));
    }

        /**
     * 对应测试用例 1.11
     */
    @Test
    void testGetFileTypeFile_normal() {
        File f = new File("test.txt");
        Assertions.assertEquals("txt", CFileUtils.getFileType(f));
    }

        /**
     * 对应测试用例 1.12
     */
    @Test
    void testGetFileTypeFile_noExtension() {
        File f = new File("test");
        Assertions.assertEquals("", CFileUtils.getFileType(f));
    }

    // ---------- getFileType(String) ----------

        /**
     * 对应测试用例 1.13
     */
    @Test
    void testGetFileTypeString_null_throwsNPE() {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> CFileUtils.getFileType((String) null));
    }

        /**
     * 对应测试用例 1.14
     */
    @Test
    void testGetFileTypeString_normal() {
        Assertions.assertEquals("txt", CFileUtils.getFileType("a.txt"));
    }

        /**
     * 对应测试用例 1.15
     */
    @Test
    void testGetFileTypeString_uppercase() {
        Assertions.assertEquals("jpg", CFileUtils.getFileType("a.JPG"));
    }

        /**
     * 对应测试用例 1.16
     */
    @Test
    void testGetFileTypeString_noDot() {
        Assertions.assertEquals("", CFileUtils.getFileType("abc"));
    }

        /**
     * 对应测试用例 1.17
     */
    @Test
    void testGetFileTypeString_multiDot() {
        // 易错：lastIndexOf 定位最后一个点，只取最后一段扩展名
        Assertions.assertEquals("gz", CFileUtils.getFileType("a.tar.gz"));
    }

        /**
     * 对应测试用例 1.18
     */
    @Test
    void testGetFileTypeString_endsWithDot() {
        Assertions.assertEquals("", CFileUtils.getFileType("a."));
    }

    // ---------- getMimeType ----------

        /**
     * 对应测试用例 1.19
     */
    @Test
    void testGetMimeType_jpg() {
        Assertions.assertEquals("image/jpeg", CFileUtils.getMimeType("a.jpg"));
    }

        /**
     * 对应测试用例 1.20
     */
    @Test
    void testGetMimeType_webp_extraMap() {
        Assertions.assertEquals("image/webp", CFileUtils.getMimeType("a.webp"));
    }

        /**
     * 对应测试用例 1.21
     */
    @Test
    void testGetMimeType_xlsx_extraMap() {
        Assertions.assertEquals(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            CFileUtils.getMimeType("a.xlsx")
        );
    }

        /**
     * 对应测试用例 1.22
     */
    @Test
    void testGetMimeType_unknown_returnsDefault() {
        Assertions.assertEquals("application/octet-stream", CFileUtils.getMimeType("a.zzz", "application/octet-stream"));
    }

        /**
     * 对应测试用例 1.23
     */
    @Test
    void testGetMimeType_unknown_noDefault_returnsNull() {
        Assertions.assertNull(CFileUtils.getMimeType("a.zzz"));
    }

        /**
     * 对应测试用例 1.24
     */
    @Test
    void testGetMimeType_noExtension_pathAsExt() {
        // 无后缀时 extension 兜底为 path 本身，查不到则返回 default
        Assertions.assertEquals("text/plain", CFileUtils.getMimeType("txt", "text/plain"));
    }

        /**
     * 对应测试用例 1.25
     */
    @Test
    void testGetMimeType_uppercasePath() {
        Assertions.assertEquals("image/png", CFileUtils.getMimeType("a.PNG"));
    }

        /**
     * 对应测试用例 1.26
     */
    @Test
    void testGetMimeType_nullPath_throwsNPE() {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> CFileUtils.getMimeType(null));
    }
}
