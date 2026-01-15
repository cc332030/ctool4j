package com.c332030.ctool4j.csv.util;

import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.util.CMapUtils;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.csv.CSVFormat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * <p>
 * Description: CCsvReadBuilder
 * </p>
 *
 * @since 2026/1/14
 */
public class CCsvReadBuilder {

    InputStream inputStream;

    String recordSeparator = "\n";

    String delimiter = ",";

    boolean skipHeaderRecord = false;

    public CCsvReadBuilder file(String filePath) {
        return file(new File(filePath));
    }

    @SneakyThrows
    public CCsvReadBuilder file(File file) {
        return inputStream(Files.newInputStream(file.toPath()));
    }

    public CCsvReadBuilder recordSeparator(String recordSeparator) {
        this.recordSeparator = recordSeparator;
        return this;
    }

    public CCsvReadBuilder delimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public CCsvReadBuilder skipHeaderRecord(boolean skipHeaderRecord) {
        this.skipHeaderRecord = skipHeaderRecord;
        return this;
    }

    public CCsvReadBuilder inputStream(InputStream inputStream) {
        if(!(inputStream instanceof BufferedInputStream)) {
            inputStream = new BufferedInputStream(inputStream);
        }

        this.inputStream = inputStream;
        return this;
    }

    @SneakyThrows
    public List<Map<String, String>> doRead() {

        val csvFormat = CSVFormat.DEFAULT.builder()
            .setRecordSeparator(recordSeparator)
            .setDelimiter(delimiter)
            .setHeader()
            .setSkipHeaderRecord(skipHeaderRecord)
            .get();

        List<Map<String, String>> rowMapList;
        try(val csvParser = csvFormat.parse(new InputStreamReader(inputStream))) {

            val headerIndexeMap = CMapUtils.mapKey(csvParser.getHeaderMap(), CCsvUtils::trim);
            rowMapList = StreamSupport.stream(csvParser.spliterator(), false)
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

        return rowMapList;
    }

    @SneakyThrows
    public <T> List<T> doRead(Class<T> tClass) {
        return doRead().stream()
            .map(rowMap -> CReflectUtils.fillValues(tClass, rowMap))
            .collect(Collectors.toList());
    }

}
