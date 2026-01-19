package com.c332030.ctool4j.csv.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CBeanUtils;
import com.c332030.ctool4j.core.classes.CReflectUtils;
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

    public static CCsvHelper builder() {
        return new CCsvHelper();
    }

    public CCsvHelper recordSeparator(String recordSeparator) {
        this.recordSeparator = recordSeparator;
        return this;
    }

    public CCsvHelper delimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

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

    @SneakyThrows
    public List<Map<String, String>> doRead(InputStreamReader reader) {

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

    public List<Map<String, String>> doRead(InputStream inputStream) {
        if(!(inputStream instanceof BufferedInputStream)) {
            inputStream = new BufferedInputStream(inputStream);
        }
        return doRead(new InputStreamReader(inputStream));
    }

    @SneakyThrows
    public List<Map<String, String>> doRead(File file) {
        return doRead(Files.newInputStream(file.toPath()));
    }

    public List<Map<String, String>> doRead(String filePath) {
        return doRead(new File(filePath));
    }

    public <T> List<T> doRead(InputStreamReader reader, Class<T> tClass) {
        return CBeanUtils.copyListFromMap(doRead(reader), tClass);
    }

    public <T> List<T> doRead(InputStream inputStream, Class<T> tClass) {
        return CBeanUtils.copyListFromMap(doRead(inputStream), tClass);
    }

    public <T> List<T> doRead(File file, Class<T> tClass) {
        return CBeanUtils.copyListFromMap(doRead(file), tClass);
    }

    public <T> List<T> doRead(String filePath, Class<T> tClass) {
        return CBeanUtils.copyListFromMap(doRead(new File(filePath)), tClass);
    }

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

    public void doWrite(
        List<?> list,
        OutputStream outputStream
    ) {
        if(!(outputStream instanceof BufferedOutputStream)) {
            outputStream = new BufferedOutputStream(outputStream);
        }
        doWrite(list, new OutputStreamWriter(outputStream));
    }

    @SneakyThrows
    public void doWrite(
        List<?> list,
        File file
    ) {
        doWrite(list, Files.newOutputStream(file.toPath()));
    }

    public void doWrite(
        List<?> list,
        String filePath
    ) {
        doWrite(list, new File(filePath));
    }

}
