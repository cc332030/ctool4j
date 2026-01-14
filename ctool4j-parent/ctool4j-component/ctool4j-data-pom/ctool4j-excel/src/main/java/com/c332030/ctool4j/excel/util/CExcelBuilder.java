package com.c332030.ctool4j.excel.util;

import cn.hutool.core.io.FileUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.SneakyThrows;
import lombok.val;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

/**
 * <p>
 * Description: CExcelBuilder
 * </p>
 *
 * @since 2026/1/14
 */
public class CExcelBuilder<T> {

    InputStream inputStream;

    ExcelTypeEnum excelType;

    Class<T> headClass;

    public static <T> CExcelBuilder<T> builder() {
        return new CExcelBuilder<>();
    }

    @SneakyThrows
    public CExcelBuilder<T> file(String filePath) {
        return file(new File(filePath));
    }

    @SneakyThrows
    public CExcelBuilder<T> file(File file) {

        val extName = FileUtil.extName(file);
        if(null != extName) {
            excelType(ExcelTypeEnum.valueOf(extName.toUpperCase()));
        }

        return inputStream(Files.newInputStream(file.toPath()));
    }

    public CExcelBuilder<T> inputStream(InputStream inputStream) {
        if(!(inputStream instanceof BufferedInputStream)) {
            inputStream = new BufferedInputStream(inputStream);
        }

        this.inputStream = inputStream;
        return this;
    }

    public CExcelBuilder<T> excelType(ExcelTypeEnum excelType) {
        this.excelType = excelType;
        return this;
    }

    public CExcelBuilder<T> head(Class<T> headClass) {
        this.headClass = headClass;
        return this;
    }

    public List<T> doRead() {

        val easyExcelBuilder = EasyExcel.read(inputStream);
        if(null != excelType) {
            easyExcelBuilder.excelType(excelType);
        }
        if(null != headClass) {
            easyExcelBuilder.head(headClass);
        }

        return easyExcelBuilder
            .sheet(0)
            .doReadSync();
    }

}
