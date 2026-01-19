package com.c332030.ctool4j.excel.util;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.c332030.ctool4j.core.util.CCollUtils;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.val;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;

/**
 * <p>
 * Description: CExcelHelper
 * </p>
 *
 * @since 2026/1/14
 */
@CustomLog
public class CExcelHelper {

    public static CExcelHelper builder() {
        return new CExcelHelper();
    }

    public <T> List<T> doRead(InputStream inputStream, Class<T> tClass) {
        return EasyExcel.read(inputStream)
            .head(tClass)
            .sheet(0)
            .doReadSync();
    }

    @SneakyThrows
    public <T> List<T> doRead(File file, Class<T> tClass) {
        return doRead(Files.newInputStream(file.toPath()), tClass);
    }

    public <T> List<T> doRead(String filePath, Class<T> tClass) {
        return doRead(new File(filePath), tClass);
    }

    public void doWrite(
        List<?> list,
        OutputStream outputStream
    ) {

        list = CCollUtils.filterNull(list);
        if(CollUtil.isEmpty(list)) {
            return;
        }

        val tClass = list.get(0).getClass();
        EasyExcel.write(outputStream)
            .head(tClass)
            .sheet(0)
            .doWrite(list);
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
