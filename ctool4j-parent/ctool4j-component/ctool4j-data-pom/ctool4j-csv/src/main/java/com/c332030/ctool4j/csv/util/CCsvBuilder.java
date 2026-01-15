package com.c332030.ctool4j.csv.util;

import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.util.CMapUtils;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * <p>
 * Description: CCsvBuilder
 * </p>
 *
 * @since 2026/1/14
 */
public class CCsvBuilder<T> {

    final Class<T> headClass;

    InputStream inputStream;

    String recordSeparator = "\n";

    String delimiter = ",";

    boolean skipHeaderRecord = false;

    public CCsvBuilder(Class<T> headClass) {
        this.headClass = headClass;
    }

    @SneakyThrows
    public CCsvBuilder<T> file(String filePath) {
        return file(new File(filePath));
    }

    @SneakyThrows
    public CCsvBuilder<T> file(File file) {
        return inputStream(Files.newInputStream(file.toPath()));
    }

    public CCsvBuilder<T> recordSeparator(String recordSeparator) {
        this.recordSeparator = recordSeparator;
        return this;
    }

    public CCsvBuilder<T> delimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public CCsvBuilder<T> skipHeaderRecord(boolean skipHeaderRecord) {
        this.skipHeaderRecord = skipHeaderRecord;
        return this;
    }

    public CCsvBuilder<T> inputStream(InputStream inputStream) {
        if(!(inputStream instanceof BufferedInputStream)) {
            inputStream = new BufferedInputStream(inputStream);
        }

        this.inputStream = inputStream;
        return this;
    }

    @SneakyThrows
    public List<T> doRead() {

        val csvFormat = CSVFormat.DEFAULT.builder()
            .setRecordSeparator(recordSeparator)
            .setDelimiter(delimiter)
            .setHeader()
            .setSkipHeaderRecord(skipHeaderRecord)
            .get();

        List<Map<String, String>> rowMapList;
        try(val csvParser = csvFormat.parse(new InputStreamReader(inputStream))) {

            rowMapList = StreamSupport.stream(csvParser.spliterator(), false)
                .map(CSVRecord::toMap)
                .collect(Collectors.toList());
        }

        val rows = new ArrayList<T>();
        rowMapList.forEach(rowMap -> {

            val map = CMapUtils.map(
                rowMap,
                CCsvUtils::trim,
                CCsvUtils::trim
            );

            val row = CReflectUtils.fillValues(headClass, map);
            rows.add(row);

        });

        return rows;
    }

}
