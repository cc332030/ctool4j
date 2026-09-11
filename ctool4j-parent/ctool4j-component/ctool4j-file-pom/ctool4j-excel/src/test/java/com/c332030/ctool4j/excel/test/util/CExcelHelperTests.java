package com.c332030.ctool4j.excel.test.util;

import com.alibaba.excel.exception.ExcelGenerateException;
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
 * <p>
 * 是 {@link CExcelHelper} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>覆盖读写往返一致：写入临时文件后再读回（含普通值、中文、超长字符串）。</li>
 *   <li>覆盖写入空值保护：null 元素过滤、空 list 不写入。</li>
 *   <li>覆盖异常路径：读取不存在文件抛异常、写入 null 输出流抛异常。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对"对象列表写入并按类读回、空 list 不写入"的约定。</li>
 *   <li>依据白盒/黑盒原则与错误推测法：读写往返、中文/超长、null/空、不存在文件、null 输出流均覆盖。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：往返一致（普通/中文/超长）、null 过滤、空 list、不存在文件、null 输出流。</li>
 *   <li>未覆盖：字节流（InputStream/OutputStream）直连读写往返、复杂 Excel 结构（多 sheet/合并单元格）。</li>
 * </ul>
 * <h2>读写往返（doWrite 后 doRead）</h2>
 * <ul>
 *   <li>1.1 往返一致（doWriteThenDoRead_roundTrip）</li>
 *   <li>1.2 中文与超长字符串往返一致（doWriteThenDoRead_chineseAndLongValue）</li>
 * </ul>
 * <h2>写入边界保护</h2>
 * <ul>
 *   <li>2.1 null 元素被过滤（doWrite_nullFiltered）</li>
 *   <li>2.2 空 list 不写入（doWrite_emptyList）</li>
 * </ul>
 * <h2>异常路径</h2>
 * <ul>
 *   <li>3.1 读取不存在的文件抛异常（doRead_nonExistentFileThrows）</li>
 *   <li>3.2 写入 null 输出流抛异常（doWrite_nullOutputStreamThrows）</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/8/14
 * @version 1.0
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
     * <p>
     * 对应测试用例 1.1：往返一致
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
     * <p>
     * 对应测试用例 1.2：中文与超长字符串往返一致
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
     * <p>
     * 对应测试用例 2.1：null 元素被过滤
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
     * <p>
     * 对应测试用例 2.2：空 list 不写入
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
     * <p>
     * 对应测试用例 3.1：读取不存在的文件抛异常
     */
    @Test
    void doRead_nonExistentFileThrows() {
        File file = new File(tempDir, "not-exist.xlsx");
        CExcelHelper helper = CExcelHelper.builder();

        Assertions.assertThrowsExactly(NoSuchFileException.class, () -> helper.doRead(file, CExcelTestBean.class));
    }

    /**
     * 异常路径：null 输出流抛出异常
     * <p>easyexcel 将写流阶段的 NPE（cause 为 null 输出流触发）包装为 ExcelGenerateException，按实际抛出类型断言</p>
     * <p>
     * 对应测试用例 3.2：写入 null 输出流抛异常
     */
    @Test
    void doWrite_nullOutputStreamThrows() {
        CExcelHelper helper = CExcelHelper.builder();

        Assertions.assertThrowsExactly(
            ExcelGenerateException.class,
            () -> helper.doWrite(Collections.singletonList(newBean(1, "tom", "d1")), (java.io.OutputStream) null)
        );
    }
}
