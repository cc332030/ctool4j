package com.c332030.ctool4j.csv.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CBeanUtils;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.util.CCharsets;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CMapUtils;
import lombok.CustomLog;
import lombok.Lombok;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * <p>
 * Description: CCsvHelper
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCsvHelper} 提供 CSV 的读写能力，采用 builder 模式配置：</p>
 * <ul>
 *   <li>读取（doRead）：将 CSV 从字符流/字节流/文件/路径读为 {@code List&lt;Map&lt;String,String&gt;&gt;}，或按类转换为对象列表。</li>
 *   <li>写入（doWrite）：将表头+行数据或对象列表写入字符流/字节流/文件/路径。</li>
 *   <li>配置项：记录分隔符（recordSeparator）、字段分隔符（delimiter）、是否跳过表头（skipHeaderRecord）。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>doRead 入参为 null（reader/inputStream）</td>
 *     <td>返回空集合</td>
 *   </tr>
 *   <tr>
 *     <td>doRead(File/String)</td>
 *     <td>经 {@code Files.newInputStream}/{@code new File} 转字节流处理</td>
 *   </tr>
 *   <tr>
 *     <td>doWrite 表头或行数据为空</td>
 *     <td>不输出，记录日志</td>
 *   </tr>
 *   <tr>
 *     <td>doWrite 对象列表为空/null</td>
 *     <td>不输出</td>
 *   </tr>
 *   <tr>
 *     <td>doWrite 写入行异常</td>
 *     <td>通过 {@code Lombok.sneakyThrow} 抛出</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>简单 CSV 的读取与导出，字段名与表头一一对应时的对象转换。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>复杂 CSV（嵌套引号、跨行字段、空表头）依赖底层库行为。</li>
 *   <li>非法行（列数少于表头）在按表头索引取值时会抛越界异常（当前实现行为）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>对象写入的表头列顺序由反射返回的字段 Map 决定，不保证与声明顺序一致。</li>
 *   <li>字节流写入统一使用 UTF-8 编码，不依赖平台默认字符集。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>空值处理</b></p>
 * <ul>
 *   <li>null 输入（reader/inputStream）视为空数据返回空集合，不依赖底层库对 null 的异常行为。</li>
 *   <li>表头/行数据中的 null 元素过滤后判断空。</li>
 * </ul>
 *
 * @since 2026/1/14
 * @version 1.0
 */
@CustomLog
public class CCsvHelper {

    String recordSeparator = "\n";

    String delimiter = ",";

    boolean skipHeaderRecord = false;

    /**
     * 创建 CCsvHelper 实例
     *
     * @return CCsvHelper 实例
     */
    public static CCsvHelper builder() {
        return new CCsvHelper();
    }

    /**
     * 设置记录分隔符
     *
     * @param recordSeparator 记录分隔符
     * @return 当前 CCsvHelper 实例
     */
    public CCsvHelper recordSeparator(String recordSeparator) {
        this.recordSeparator = recordSeparator;
        return this;
    }

    /**
     * 设置字段分隔符
     *
     * @param delimiter 字段分隔符
     * @return 当前 CCsvHelper 实例
     */
    public CCsvHelper delimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    /**
     * 设置是否跳过表头记录
     *
     * @param skipHeaderRecord 是否跳过表头记录
     * @return 当前 CCsvHelper 实例
     */
    public CCsvHelper skipHeaderRecord(boolean skipHeaderRecord) {
        this.skipHeaderRecord = skipHeaderRecord;
        return this;
    }

    private CSVFormat.Builder getCsvFormatBuilder() {
        return CSVFormat.DEFAULT.builder()
            .setRecordSeparator(recordSeparator)
            .setDelimiter(delimiter)
            .setHeader()
            .setSkipHeaderRecord(skipHeaderRecord)
            ;
    }

    /**
     * 从字符流读取 CSV 数据为 Map 列表
     *
     * <h2>读取（doRead）</h2>
     * <ul>
     *   <li>底层使用 Apache Commons CSV（{@code CSVFormat}/{@code CSVParser}）。</li>
     *   <li>默认 {@code setHeader()} 自动识别表头行，表头行不进入结果数据。</li>
     *   <li>表头与单元格值均经 {@code CCsvUtils.trim} 处理（去空白、去退格）。</li>
     *   <li>以表头为 key、单元格值为 value 组装 {@code LinkedHashMap}。</li>
     *   <li>{@code skipHeaderRecord(true)} 时跳过表头记录；默认 {@code setHeader()} 也已自动跳过表头。</li>
     * </ul>
     *
     * @param reader 字符流
     * @return 表头与值的映射列表*/
    @SneakyThrows
    public List<Map<String, String>> doRead(InputStreamReader reader) {

        // null 输入视为空数据，返回空集合，避免依赖底层库对 null 的异常行为（不同版本抛错类型不一致）
        if(reader == null) {
            return Collections.emptyList();
        }

        val csvFormat = getCsvFormatBuilder()
            .get();
        try(val csvParser = csvFormat.parse(reader)) {

            val headerIndexeMap = CMapUtils.mapKey(csvParser.getHeaderMap(), CCsvUtils::trim);
            return StreamSupport.stream(csvParser.spliterator(), false)
                .map(record -> {

                    val map = new LinkedHashMap<String, String>();
                    headerIndexeMap.forEach((key, keyIndex) -> {
                        val value = record.get(keyIndex);
                        map.put(key, CCsvUtils.trim(value));
                    });
                    return map;
                })
                .collect(Collectors.toList());
        }

    }

    /**
     * 从字节流读取 CSV 数据为 Map 列表
     *
     * @param inputStream 字节流
     * @return 表头与值的映射列表
     */
    public List<Map<String, String>> doRead(InputStream inputStream) {
        // null 输入视为空数据，返回空集合，与 doRead(InputStreamReader) 行为保持一致
        if(inputStream == null) {
            return Collections.emptyList();
        }
        if(!(inputStream instanceof BufferedInputStream)) {
            inputStream = new BufferedInputStream(inputStream);
        }
        return doRead(new InputStreamReader(inputStream, CCharsets.UTF_8));
    }

    /**
     * 从文件读取 CSV 数据为 Map 列表
     *
     * @param file CSV 文件
     * @return 表头与值的映射列表
     */
    @SneakyThrows
    public List<Map<String, String>> doRead(File file) {
        return doRead(Files.newInputStream(file.toPath()));
    }

    /**
     * 从文件路径读取 CSV 数据为 Map 列表
     *
     * @param filePath CSV 文件路径
     * @return 表头与值的映射列表
     */
    public List<Map<String, String>> doRead(String filePath) {
        return doRead(new File(filePath));
    }

    /**
     * 从字符流读取 CSV 数据并转换为指定类型列表
     *
     * @param reader  字符流
     * @param tClass  目标类型
     * @param <T>     目标类型
     * @return 目标类型列表
     */
    public <T> List<T> doRead(InputStreamReader reader, Class<T> tClass) {
        return CBeanUtils.copyListFromMap(doRead(reader), tClass);
    }

    /**
     * 从字节流读取 CSV 数据并转换为指定类型列表
     *
     * @param inputStream 字节流
     * @param tClass      目标类型
     * @param <T>         目标类型
     * @return 目标类型列表
     */
    public <T> List<T> doRead(InputStream inputStream, Class<T> tClass) {
        return CBeanUtils.copyListFromMap(doRead(inputStream), tClass);
    }

    /**
     * 从文件读取 CSV 数据并转换为指定类型列表
     *
     * @param file   CSV 文件
     * @param tClass 目标类型
     * @param <T>    目标类型
     * @return 目标类型列表
     */
    public <T> List<T> doRead(File file, Class<T> tClass) {
        return CBeanUtils.copyListFromMap(doRead(file), tClass);
    }

    /**
     * 从文件路径读取 CSV 数据并转换为指定类型列表
     *
     * @param filePath CSV 文件路径
     * @param tClass   目标类型
     * @param <T>      目标类型
     * @return 目标类型列表
     */
    public <T> List<T> doRead(String filePath, Class<T> tClass) {
        return CBeanUtils.copyListFromMap(doRead(new File(filePath)), tClass);
    }

    /**
     * 将表头与行数据写入字符流
     *
     * <h2>写入（doWrite）</h2>
     * <ul>
     *   <li>底层使用 {@code CSVPrinter}。</li>
     *   <li>{@code doWrite(Collection&lt;String&gt;, List&lt;List&lt;String&gt;&gt;, Writer)}：显式指定表头与行数据。</li>
     *   <li>{@code doWrite(List&lt;?&gt;, Writer)}：依据首元素类型的字段反射生成表头，逐行写入各字段值。</li>
     *   <li>表头或行数据为空（含过滤后为空）时不输出，仅记录日志。</li>
     * </ul>
     * <ul>
     *   <li>{@code doWrite(List&lt;?&gt;, ...)} 取首元素类型推断字段，list 元素类型不一致时字段集以首元素为准。</li>
     * </ul>
     *
     * @param headers 表头列表
     * @param rows    行数据列表
     * @param writer  字符流*/
    @SneakyThrows
    public void doWrite(
        Collection<String> headers,
        List<List<String>> rows,
        Writer writer
    ) {

        headers = CCollUtils.filterString(headers);
        rows = CCollUtils.filterNull(rows);
        if(CollUtil.isEmpty(headers)
            || CollUtil.isEmpty(rows)
        ) {
            log.info("headers or rows is empty");
            return;
        }

        val csvFormat = getCsvFormatBuilder()
            .setHeader(headers.toArray(new String[0]))
            .get();
        try (val csvPrinter = new CSVPrinter(writer, csvFormat)) {
            rows.forEach(record -> {
                try {
                    csvPrinter.printRecord(record);
                } catch (Exception ex) {
                    throw Lombok.sneakyThrow(ex);
                }
            });
            csvPrinter.flush();
        }

    }

    /**
     * 将对象列表按字段写入字符流
     *
     * @param list   对象列表
     * @param writer 字符流
     */
    public void doWrite(
        List<?> list,
        Writer writer
    ) {

        list = CCollUtils.filterNull(list);
        if(CollUtil.isEmpty(list)) {
            log.info("no data to write");
            return;
        }

        val type = list.get(0).getClass();
        val fieldMap = CReflectUtils.getInstanceFieldMap(type);

        val rows = new ArrayList<List<String>>(fieldMap.size());
        list.forEach(item -> {

            val row = fieldMap.values().stream()
                .map(field -> CReflectUtils.getValue(item, field))
                .map(StrUtil::toStringOrNull)
                .collect(Collectors.toList());
            rows.add(row);
        });

        doWrite(fieldMap.keySet(), rows, writer);

    }

    /**
     * 将对象列表写入字节流
     *
     * @param list         对象列表，为 null 或空（含过滤后为空）时不写入
     * @param outputStream 字节流
     */
    public void doWrite(
        List<?> list,
        OutputStream outputStream
    ) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        if(!(outputStream instanceof BufferedOutputStream)) {
            outputStream = new BufferedOutputStream(outputStream);
        }
        doWrite(list, new OutputStreamWriter(outputStream, CCharsets.UTF_8));
    }

    /**
     * 将对象列表写入文件
     *
     * @param list 对象列表，为 null 或空（含过滤后为空）时不写入
     * @param file 目标文件
     */
    @SneakyThrows
    public void doWrite(
        List<?> list,
        File file
    ) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        doWrite(list, Files.newOutputStream(file.toPath()));
    }

    /**
     * 将对象列表写入文件路径
     *
     * @param list     对象列表，为 null 或空（含过滤后为空）时不写入
     * @param filePath 目标文件路径
     */
    public void doWrite(
        List<?> list,
        String filePath
    ) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        doWrite(list, new File(filePath));
    }

}
