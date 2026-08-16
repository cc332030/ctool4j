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
 * @since 2026/1/14
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
     * @param reader 字符流
     * @return 表头与值的映射列表
     */
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
     * @param headers 表头列表
     * @param rows    行数据列表
     * @param writer  字符流
     */
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
     * @param list         对象列表
     * @param outputStream 字节流
     */
    public void doWrite(
        List<?> list,
        OutputStream outputStream
    ) {
        if(!(outputStream instanceof BufferedOutputStream)) {
            outputStream = new BufferedOutputStream(outputStream);
        }
        doWrite(list, new OutputStreamWriter(outputStream, CCharsets.UTF_8));
    }

    /**
     * 将对象列表写入文件
     *
     * @param list 对象列表
     * @param file 目标文件
     */
    @SneakyThrows
    public void doWrite(
        List<?> list,
        File file
    ) {
        doWrite(list, Files.newOutputStream(file.toPath()));
    }

    /**
     * 将对象列表写入文件路径
     *
     * @param list     对象列表
     * @param filePath 目标文件路径
     */
    public void doWrite(
        List<?> list,
        String filePath
    ) {
        doWrite(list, new File(filePath));
    }

}
