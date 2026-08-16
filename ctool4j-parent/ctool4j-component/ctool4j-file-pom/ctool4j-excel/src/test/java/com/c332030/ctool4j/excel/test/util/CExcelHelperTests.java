package com.c332030.ctool4j.excel.test.util;

import com.c332030.ctool4j.excel.util.CExcelHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Description: CExcelHelperTests
 * </p>
 *
 * @author c332030
 * @since 2026/8/14
 */
class CExcelHelperTests {

    @TempDir
    File tempDir;

    private CExcelTestBean newBean(Integer id, String name, String desc) {
        CExcelTestBean bean = new CExcelTestBean();
        bean.setId(id);
        bean.setName(name);
        bean.setDesc(desc);
        return bean;
    }

    /**
     * 正常路径：doWrite 写入临时文件后 doRead 可读回数据（往返一致）
     */
    @Test
    void doWriteThenDoRead_roundTrip() throws Exception {
        List<CExcelTestBean> beans = Arrays.asList(
            newBean(1, "tom", "d1"),
            newBean(2, "jerry", "d2")
        );

        File file = new File(tempDir, "roundtrip.xlsx");
        CExcelHelper helper = CExcelHelper.builder();
        helper.doWrite(beans, file);

        List<CExcelTestBean> result = helper.doRead(file, CExcelTestBean.class);

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(Integer.valueOf(1), result.get(0).getId());
        Assertions.assertEquals("tom", result.get(0).getName());
        Assertions.assertEquals("d1", result.get(0).getDesc());
        Assertions.assertEquals(Integer.valueOf(2), result.get(1).getId());
        Assertions.assertEquals("jerry", result.get(1).getName());
    }

    private String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder(count * s.length());
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * 正常路径：中文与超长字符串往返一致
     */
    @Test
    void doWriteThenDoRead_chineseAndLongValue() throws Exception {
        String longValue = repeat("x", 30000);
        CExcelTestBean bean = newBean(1, "张三", longValue);

        File file = new File(tempDir, "cn.xlsx");
        CExcelHelper helper = CExcelHelper.builder();
        helper.doWrite(Collections.singletonList(bean), file);

        List<CExcelTestBean> result = helper.doRead(file, CExcelTestBean.class);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("张三", result.get(0).getName());
        Assertions.assertEquals(longValue, result.get(0).getDesc());
    }

    /**
     * 边界：null 元素被过滤
     */
    @Test
    void doWrite_nullFiltered() throws Exception {
        File file = new File(tempDir, "null.xlsx");
        CExcelHelper helper = CExcelHelper.builder();
        helper.doWrite(Arrays.asList(newBean(1, "tom", "d1"), null), file);

        List<CExcelTestBean> result = helper.doRead(file, CExcelTestBean.class);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("tom", result.get(0).getName());
    }

    /**
     * 边界：空 list 不写入任何内容（文件存在但无数据）
     */
    @Test
    void doWrite_emptyList() throws Exception {
        File file = new File(tempDir, "empty.xlsx");
        CExcelHelper helper = CExcelHelper.builder();
        helper.doWrite(Collections.emptyList(), file);

        Assertions.assertTrue(file.exists());
        Assertions.assertEquals(0, Files.size(file.toPath()));
    }

    /**
     * 异常路径：读取不存在的文件抛出异常
     */
    @Test
    void doRead_nonExistentFileThrows() {
        File file = new File(tempDir, "not-exist.xlsx");
        CExcelHelper helper = CExcelHelper.builder();

        Assertions.assertThrowsExactly(NoSuchFileException.class, () -> helper.doRead(file, CExcelTestBean.class));
    }

    /**
     * 异常路径：null 输出流抛出异常
     */
    @Test
    void doWrite_nullOutputStreamThrows() {
        CExcelHelper helper = CExcelHelper.builder();

        Assertions.assertThrowsExactly(
            NullPointerException.class,
            () -> helper.doWrite(Collections.singletonList(newBean(1, "tom", "d1")), (java.io.OutputStream) null)
        );
    }
}
