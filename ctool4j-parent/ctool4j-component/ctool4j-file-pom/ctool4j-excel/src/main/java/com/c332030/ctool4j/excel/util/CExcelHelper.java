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
 * @see doc/design/excel/CExcelHelper.adoc
 * @see doc/design/excel/CExcelHelperTests.adoc
 * @since 2026/1/14
 */
@CustomLog
public class CExcelHelper {

    /**
     * 创建 CExcelHelper 实例
     *
     * @return CExcelHelper 实例
     */
    public static CExcelHelper builder() {
        return new CExcelHelper();
    }

    /**
     * 从字节流读取 Excel 数据为指定类型列表
     *
     * @param inputStream 字节流
     * @param tClass      目标类型
     * @param <T>         目标类型
     * @return 目标类型列表
     */
    public <T> List<T> doRead(InputStream inputStream, Class<T> tClass) {
        return EasyExcel.read(inputStream)
            .head(tClass)
            .sheet(0)
            .doReadSync();
    }

    /**
     * 从文件读取 Excel 数据为指定类型列表
     *
     * @param file   Excel 文件
     * @param tClass 目标类型
     * @param <T>    目标类型
     * @return 目标类型列表
     */
    @SneakyThrows
    public <T> List<T> doRead(File file, Class<T> tClass) {
        return doRead(Files.newInputStream(file.toPath()), tClass);
    }

    /**
     * 从文件路径读取 Excel 数据为指定类型列表
     *
     * @param filePath Excel 文件路径
     * @param tClass   目标类型
     * @param <T>      目标类型
     * @return 目标类型列表
     */
    public <T> List<T> doRead(String filePath, Class<T> tClass) {
        return doRead(new File(filePath), tClass);
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
