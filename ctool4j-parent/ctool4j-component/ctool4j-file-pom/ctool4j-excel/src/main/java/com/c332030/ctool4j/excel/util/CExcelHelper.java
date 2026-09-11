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
 * <h2>能力目录</h2>
 * <p>{@code CExcelHelper} 提供基于 EasyExcel 的 Excel 读写能力，采用 builder 模式：</p>
 * <ul>
 *   <li>读取（doRead）：从字节流/文件/路径将 Excel 读为指定类型列表。</li>
 *   <li>写入（doWrite）：将对象列表写入字节流/文件/路径。</li>
 *   <li>统一以 {@code tClass}（首元素类型）作为表头与转换模型。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>doWrite 对象列表为空/null</td>
 *     <td>不写入，直接返回</td>
 *   </tr>
 *   <tr>
 *     <td>读取不存在的文件</td>
 *     <td>抛 {@code NoSuchFileException}（依赖 {@code Files.newInputStream}）</td>
 *   </tr>
 *   <tr>
 *     <td>写入 null 输出流</td>
 *     <td>抛 {@code ExcelGenerateException}（easyexcel 包装 NPE）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>基于 EasyExcel 的简单 Excel 导入导出，对象字段与表头对应。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>复杂 Excel（合并单元格、样式、多 sheet 自定义）需直接使用 EasyExcel API。</li>
 *   <li>依赖 EasyExcel 底层库行为。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>写入以首元素类型推断 {@code tClass}，list 元素类型不一致时以首元素为准。</li>
 *   <li>null 输出流写入会抛出 easyexcel 包装的异常，属底层行为，未额外校验。</li>
 * </ul>
 *
 * @since 2026/1/14
 * @version 1.0
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
     * <h2>读取（doRead）</h2>
     * <ul>
     *   <li>底层使用 EasyExcel {@code read(inputStream).head(tClass).sheet(0).doReadSync()}。</li>
     *   <li>{@code doRead(File)} 经 {@code Files.newInputStream} 转字节流；{@code doRead(String)} 转 {@code File}。</li>
     * </ul>
     *
     * @param inputStream 字节流
     * @param tClass      目标类型
     * @param <T>         目标类型
     * @return 目标类型列表*/
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
     * <h2>写入（doWrite）</h2>
     * <ul>
     *   <li>底层使用 EasyExcel {@code write(outputStream).head(tClass).sheet(0).doWrite(list)}。</li>
     *   <li>过滤 null 元素；list 为空（含过滤后为空）时不写入，直接返回。</li>
     * </ul>
     *
     * @param list         对象列表
     * @param outputStream 字节流*/
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
