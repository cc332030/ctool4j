package com.c332030.ctool4j.csv.test.util;

import com.c332030.ctool4j.csv.util.CCsvHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Description: CCsvHelperTests
 * </p>
 *
 * @author c332030
 * @since 2026/8/14
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
     */
    @Test
    void doRead_emptyLineIgnored() {
        CCsvHelper helper = CCsvHelper.builder().skipHeaderRecord(true);
        List<Map<String, String>> result = helper.doRead(toReader("a,b\n1,2\n\n3,4"));

        Assertions.assertEquals(2, result.size());
    }

    /**
     * 边界：单元格值前后空格被 trim
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
     * 异常路径：null 输入流抛出异常
     */
    @Test
    void doRead_nullInputStreamThrows() {
        CCsvHelper helper = CCsvHelper.builder();
        Assertions.assertThrowsExactly(java.io.IOException.class, () -> helper.doRead((InputStreamReader) null));
    }

    // ==================== doWrite ====================

    /**
     * 正常路径：指定表头与数据行，输出含表头和数据的 CSV
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
     */
    @Test
    void doWrite_emptyListNoOutput() {
        CCsvHelper helper = CCsvHelper.builder();
        StringWriter writer = new StringWriter();

        helper.doWrite(Collections.emptyList(), writer);

        Assertions.assertEquals("", writer.toString());
    }
}
