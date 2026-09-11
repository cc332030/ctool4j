package com.c332030.ctool4j.csv.test.util;

import com.c332030.ctool4j.csv.util.CCsvHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * <p>
 * Description: CCsvHelperTests
 * </p>
 *
 * <p>
 * 是 {@link CCsvHelper} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>doRead：覆盖默认跳过表头、显式 skipHeaderRecord、自定义分隔符、引号包裹/转义、空单元格转 null、空行忽略、单元格 trim、非法行、超长单元格、UTF-8 中文、null 输入。</li>
 *   <li>doWrite：覆盖显式表头+行、对象列表（字段反射）、字节流 UTF-8 中文、null 行过滤、空表头/空行/空列表/null 列表不输出。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对"表头自动识别、null 返回空集合、空数据不输出、UTF-8 编码"的约定。</li>
 *   <li>依据白盒/黑盒原则：读写的各输入形态（流/字节流/文件）、分隔符、引号、空值、超长输入均覆盖。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：读取的各类单元格形态（引号、分隔符、trim、空、超长、中文）、null 输入；写入的显式/对象/流/文件、空数据与 null 保护。</li>
 *   <li>未覆盖：真实文件读写路径（File/String 重载）未直接断言文件内容；依赖底层库的极端 CSV 语法（跨行字段等）。</li>
 * </ul>
 * <h2>doRead 读取</h2>
 * <ul>
 *   <li>1.1 skipHeaderRecord(true) 跳过表头仅返回数据行（doRead_skipHeaderDataRows）</li>
 *   <li>1.2 默认配置自动识别表头并跳过（doRead_defaultSkipsHeader）</li>
 *   <li>1.3 字段含逗号用引号包裹解析保留（doRead_quotedValueWithComma）</li>
 *   <li>1.4 字段含双引号转义解析（doRead_quotedValueWithQuote）</li>
 *   <li>1.5 自定义分隔符（分号）（doRead_customDelimiter）</li>
 *   <li>1.6 空单元格 trim 后返回 null（doRead_emptyCellBecomesNull）</li>
 *   <li>1.7 空行被忽略（doRead_emptyLineIgnored）</li>
 *   <li>1.8 单元格前后空格被 trim（doRead_cellTrimmed）</li>
 *   <li>1.9 非法行（列数少于表头）抛越界异常（doRead_missingColumnThrows）</li>
 *   <li>1.10 超长单元格完整保留（doRead_superLongCell）</li>
 *   <li>1.11 UTF-8 中文内容（doRead_utf8Chinese）</li>
 *   <li>1.12 字节流按 UTF-8 解码中文（doRead_inputStreamUtf8Chinese）</li>
 *   <li>1.13 null 输入返回空集合（doRead_nullInputReturnsEmpty）</li>
 * </ul>
 * <h2>doWrite 写入</h2>
 * <ul>
 *   <li>2.1 显式表头与行数据输出（doWrite_headersAndRows）</li>
 *   <li>2.2 对象列表按字段反射生成表头并写入（doWrite_listBean）</li>
 *   <li>2.3 字节流按 UTF-8 编码中文（doWrite_outputStreamUtf8Chinese）</li>
 *   <li>2.4 对象列表 null 元素被过滤（doWrite_listNullFiltered）</li>
 *   <li>2.5 表头为空不输出（doWrite_emptyHeadersNoOutput）</li>
 *   <li>2.6 数据行为空不输出（doWrite_emptyRowsNoOutput）</li>
 *   <li>2.7 对象列表为空不输出（doWrite_emptyListNoOutput）</li>
 *   <li>2.8 对象列表 null 不输出（doWrite_nullListNoOutput）</li>
 *   <li>2.9 对象列表 null 不写字节流（doWrite_nullListOutputStreamNoWrite）</li>
 *   <li>2.10 对象列表 null 不创建文件（doWrite_nullListFileNotCreated）</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/8/14
 * @version 1.0
 */
class CCsvHelperTests {

    /**
     * 将 CSV 字符串转为 InputStreamReader，供 doRead(InputStreamReader) 使用
     */
    private InputStreamReader toReader(String csv) {
        return new InputStreamReader(
            new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)),
            StandardCharsets.UTF_8
        );
    }

    private String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder(count * s.length());
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    // ==================== doRead ====================

    /**
     * 正常路径：skipHeaderRecord(true) 时表头被跳过，仅返回数据行
     * <p>
     * 对应测试用例 1.1：skipHeaderRecord(true) 跳过表头仅返回数据行
     */
    @Test
    void doRead_skipHeaderDataRows() {
        CCsvHelper helper = CCsvHelper.builder().skipHeaderRecord(true);
        List<Map<String, String>> result = helper.doRead(toReader("a,b\n1,2\n3,4"));

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("1", result.get(0).get("a"));
        Assertions.assertEquals("2", result.get(0).get("b"));
        Assertions.assertEquals("3", result.get(1).get("a"));
        Assertions.assertEquals("4", result.get(1).get("b"));
    }

    /**
     * 正常路径：默认配置下（setHeader() 自动识别表头）表头行被跳过，仅返回数据行
     * <p>
     * 对应测试用例 1.2：默认配置自动识别表头并跳过
     */
    @Test
    void doRead_defaultSkipsHeader() {
        CCsvHelper helper = CCsvHelper.builder();
        List<Map<String, String>> result = helper.doRead(toReader("a,b\n1,2"));

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("1", result.get(0).get("a"));
        Assertions.assertEquals("2", result.get(0).get("b"));
    }

    /**
     * 正常路径：字段值含逗号时使用引号包裹，解析后保留逗号
     * <p>
     * 对应测试用例 1.3：字段含逗号用引号包裹解析保留
     */
    @Test
    void doRead_quotedValueWithComma() {
        CCsvHelper helper = CCsvHelper.builder().skipHeaderRecord(true);
        List<Map<String, String>> result = helper.doRead(toReader("name,desc\n1,\"a,b\""));

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("a,b", result.get(0).get("desc"));
    }

    /**
     * 正常路径：字段值含双引号时使用双引号转义
     * <p>
     * 对应测试用例 1.4：字段含双引号转义解析
     */
    @Test
    void doRead_quotedValueWithQuote() {
        CCsvHelper helper = CCsvHelper.builder().skipHeaderRecord(true);
        List<Map<String, String>> result = helper.doRead(toReader("name,desc\n1,\"say \"\"hi\"\"\""));

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("say \"hi\"", result.get(0).get("desc"));
    }

    /**
     * 正常路径：自定义分隔符（分号）
     * <p>
     * 对应测试用例 1.5：自定义分隔符（分号）
     */
    @Test
    void doRead_customDelimiter() {
        CCsvHelper helper = CCsvHelper.builder().skipHeaderRecord(true).delimiter(";");
        List<Map<String, String>> result = helper.doRead(toReader("a;b\n1;2"));

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("1", result.get(0).get("a"));
        Assertions.assertEquals("2", result.get(0).get("b"));
    }

    /**
     * 边界：单元格值为空白时 trim 后返回 null
     * <p>
     * 对应测试用例 1.6：空单元格 trim 后返回 null
     */
    @Test
    void doRead_emptyCellBecomesNull() {
        CCsvHelper helper = CCsvHelper.builder().skipHeaderRecord(true);
        List<Map<String, String>> result = helper.doRead(toReader("a,b\n1,"));

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("1", result.get(0).get("a"));
        Assertions.assertNull(result.get(0).get("b"));
    }

    /**
     * 边界：空行被忽略
     * <p>
     * 对应测试用例 1.7：空行被忽略
     */
    @Test
    void doRead_emptyLineIgnored() {
        CCsvHelper helper = CCsvHelper.builder().skipHeaderRecord(true);
        List<Map<String, String>> result = helper.doRead(toReader("a,b\n1,2\n\n3,4"));

        Assertions.assertEquals(2, result.size());
    }

    /**
     * 边界：单元格值前后空格被 trim
     * <p>
     * 对应测试用例 1.8：单元格前后空格被 trim
     */
    @Test
    void doRead_cellTrimmed() {
        CCsvHelper helper = CCsvHelper.builder().skipHeaderRecord(true);
        List<Map<String, String>> result = helper.doRead(toReader("a,b\n 1 , 2 "));

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("1", result.get(0).get("a"));
        Assertions.assertEquals("2", result.get(0).get("b"));
    }

    /**
     * 异常路径：非法行（列数少于表头）在按表头索引取值时抛出越界异常（当前实现行为）
     * <p>
     * 对应测试用例 1.9：非法行（列数少于表头）抛越界异常
     */
    @Test
    void doRead_missingColumnThrows() {
        CCsvHelper helper = CCsvHelper.builder().skipHeaderRecord(true);
        Assertions.assertThrowsExactly(
            ArrayIndexOutOfBoundsException.class,
            () -> helper.doRead(toReader("a,b\n1"))
        );
    }

    /**
     * 边界：超长单元格内容完整保留
     * <p>
     * 对应测试用例 1.10：超长单元格完整保留
     */
    @Test
    void doRead_superLongCell() {
        String longValue = repeat("x", 50000);
        CCsvHelper helper = CCsvHelper.builder().skipHeaderRecord(true);
        List<Map<String, String>> result = helper.doRead(toReader("a,b\n" + longValue + ",2"));

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(longValue, result.get(0).get("a"));
    }

    /**
     * 边界：UTF-8 中文内容
     * <p>
     * 对应测试用例 1.11：UTF-8 中文内容
     */
    @Test
    void doRead_utf8Chinese() {
        CCsvHelper helper = CCsvHelper.builder().skipHeaderRecord(true);
        String csv = "姓名,年龄\n张三,18\n李四,20";
        List<Map<String, String>> result = helper.doRead(toReader(csv));

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("张三", result.get(0).get("姓名"));
        Assertions.assertEquals("18", result.get(0).get("年龄"));
    }

    /**
     * 正常路径：doRead(InputStream) 按 UTF-8 解码中文内容（不依赖平台默认字符集）
     * <p>
     * 对应测试用例 1.12：字节流按 UTF-8 解码中文
     */
    @Test
    void doRead_inputStreamUtf8Chinese() {
        CCsvHelper helper = CCsvHelper.builder().skipHeaderRecord(true);
        String csv = "姓名,年龄\n张三,18";
        List<Map<String, String>> result = helper.doRead(
            new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))
        );

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("张三", result.get(0).get("姓名"));
        Assertions.assertEquals("18", result.get(0).get("年龄"));
    }

    /**
     * 边界：null 输入返回空集合，而非抛出异常（不依赖底层库对 null 的处理行为）
     * <p>
     * 对应测试用例 1.13：null 输入返回空集合
     */
    @Test
    void doRead_nullInputReturnsEmpty() {
        CCsvHelper helper = CCsvHelper.builder();
        Assertions.assertTrue(helper.doRead((InputStreamReader) null).isEmpty());
        Assertions.assertTrue(helper.doRead((InputStream) null).isEmpty());
    }

    // ==================== doWrite ====================

    /**
     * 正常路径：指定表头与数据行，输出含表头和数据的 CSV
     * <p>
     * 对应测试用例 2.1：显式表头与行数据输出
     */
    @Test
    void doWrite_headersAndRows() {
        CCsvHelper helper = CCsvHelper.builder();
        StringWriter writer = new StringWriter();

        helper.doWrite(
            Arrays.asList("a", "b"),
            Arrays.asList(
                Arrays.asList("1", "2"),
                Arrays.asList("3", "4")
            ),
            writer
        );

        Assertions.assertEquals("a,b\n1,2\n3,4\n", writer.toString());
    }

    /**
     * 正常路径：doWrite(List) 依据 bean 字段名生成表头并写入每行值（字段列顺序由反射决定，故按行内容断言）
     * <p>
     * 对应测试用例 2.2：对象列表按字段反射生成表头并写入
     */
    @Test
    void doWrite_listBean() {
        CCsvHelper helper = CCsvHelper.builder();

        CCsvTestBean bean1 = new CCsvTestBean();
        bean1.setId(1L);
        bean1.setName("tom");
        bean1.setDesc("d1");

        CCsvTestBean bean2 = new CCsvTestBean();
        bean2.setId(2L);
        bean2.setName("jerry");
        bean2.setDesc("d2");

        StringWriter writer = new StringWriter();
        helper.doWrite(Arrays.asList(bean1, bean2), writer);

        String[] lines = writer.toString().split("\n");
        Assertions.assertEquals(3, lines.length);
        // 表头包含三个字段名
        Assertions.assertTrue(lines[0].contains("id"));
        Assertions.assertTrue(lines[0].contains("name"));
        Assertions.assertTrue(lines[0].contains("desc"));
        // 每行包含对应字段值
        Assertions.assertTrue(lines[1].contains("1"));
        Assertions.assertTrue(lines[1].contains("tom"));
        Assertions.assertTrue(lines[1].contains("d1"));
        Assertions.assertTrue(lines[2].contains("2"));
        Assertions.assertTrue(lines[2].contains("jerry"));
        Assertions.assertTrue(lines[2].contains("d2"));
    }

    /**
     * 正常路径：doWrite(List, OutputStream) 按 UTF-8 编码中文内容（不依赖平台默认字符集）
     * <p>
     * 对应测试用例 2.3：字节流按 UTF-8 编码中文
     */
    @Test
    void doWrite_outputStreamUtf8Chinese() {
        CCsvHelper helper = CCsvHelper.builder();

        CCsvTestBean bean = new CCsvTestBean();
        bean.setId(1L);
        bean.setName("张三");
        bean.setDesc("测试");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        helper.doWrite(Collections.singletonList(bean), baos);

        String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        Assertions.assertTrue(content.contains("张三"));
        Assertions.assertTrue(content.contains("测试"));
    }

    /**
     * 正常路径：list 中的 null 元素被过滤，仅写入非空行
     * <p>
     * 对应测试用例 2.4：对象列表 null 元素被过滤
     */
    @Test
    void doWrite_listNullFiltered() {
        CCsvHelper helper = CCsvHelper.builder();

        CCsvTestBean bean1 = new CCsvTestBean();
        bean1.setId(1L);
        bean1.setName("tom");
        bean1.setDesc("d1");

        StringWriter writer = new StringWriter();
        helper.doWrite(Arrays.asList(bean1, null), writer);

        String[] lines = writer.toString().split("\n");
        Assertions.assertEquals(2, lines.length); // 表头 + 1 行数据
        Assertions.assertTrue(lines[0].contains("id"));
        Assertions.assertTrue(lines[0].contains("name"));
        Assertions.assertTrue(lines[0].contains("desc"));
        Assertions.assertTrue(lines[1].contains("tom"));
    }

    /**
     * 边界：表头为空时不输出任何内容
     * <p>
     * 对应测试用例 2.5：表头为空不输出
     */
    @Test
    void doWrite_emptyHeadersNoOutput() {
        CCsvHelper helper = CCsvHelper.builder();
        StringWriter writer = new StringWriter();

        helper.doWrite(
            Collections.emptyList(),
            Collections.singletonList(Arrays.asList("1", "2")),
            writer
        );

        Assertions.assertEquals("", writer.toString());
    }

    /**
     * 边界：数据行为空时不输出任何内容
     * <p>
     * 对应测试用例 2.6：数据行为空不输出
     */
    @Test
    void doWrite_emptyRowsNoOutput() {
        CCsvHelper helper = CCsvHelper.builder();
        StringWriter writer = new StringWriter();

        helper.doWrite(
            Arrays.asList("a", "b"),
            Collections.emptyList(),
            writer
        );

        Assertions.assertEquals("", writer.toString());
    }

    /**
     * 边界：list 为空时不输出任何内容
     * <p>
     * 对应测试用例 2.7：对象列表为空不输出
     */
    @Test
    void doWrite_emptyListNoOutput() {
        CCsvHelper helper = CCsvHelper.builder();
        StringWriter writer = new StringWriter();

        helper.doWrite(Collections.emptyList(), writer);

        Assertions.assertEquals("", writer.toString());
    }

    /**
     * 边界：list 为 null 时直接返回，不输出任何内容
     * <p>
     * 对应测试用例 2.8：对象列表 null 不输出
     */
    @Test
    void doWrite_nullListNoOutput() {
        CCsvHelper helper = CCsvHelper.builder();
        StringWriter writer = new StringWriter();

        helper.doWrite(null, writer);

        Assertions.assertEquals("", writer.toString());
    }

    /**
     * 边界：list 为 null 时直接返回，不向字节流写入任何内容
     * <p>
     * 对应测试用例 2.9：对象列表 null 不写字节流
     */
    @Test
    void doWrite_nullListOutputStreamNoWrite() {
        CCsvHelper helper = CCsvHelper.builder();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        helper.doWrite(null, outputStream);

        Assertions.assertEquals(0, outputStream.size());
    }

    /**
     * 边界：list 为 null 时直接返回，不创建文件
     * <p>
     * 对应测试用例 2.10：对象列表 null 不创建文件
     */
    @Test
    void doWrite_nullListFileNotCreated() {
        CCsvHelper helper = CCsvHelper.builder();
        File file = new File(
            System.getProperty("java.io.tmpdir"),
            "ctool4j-csv-null-" + UUID.randomUUID() + ".csv"
        );

        helper.doWrite(null, file);

        Assertions.assertFalse(file.exists());
    }
}
