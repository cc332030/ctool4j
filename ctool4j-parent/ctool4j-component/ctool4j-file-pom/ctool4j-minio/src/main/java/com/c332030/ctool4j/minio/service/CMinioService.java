package com.c332030.ctool4j.minio.service;

import io.minio.*;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>
 * Description: CMinioService
 * </p>
 *
 * <p>MinIO 对象存储服务，封装 MinIO 客户端常用操作。</p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CMinioService}（{@code @Service} + {@code @AllArgsConstructor}）封装 MinIO 客户端常用操作：</p>
 * <ul>
 *   <li>{@code statObject}：获取对象元数据（HEAD，不下载内容）。</li>
 *   <li>{@code getObject}：下载对象返回输入流。</li>
 *   <li>{@code getObjectThenWrite}：下载对象并写入输出流，返回字节数。</li>
 *   <li>{@code write}：上传字节数组或输入流到 MinIO。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>流大小未知</td>
 *     <td>分片大小设为 5MB</td>
 *   </tr>
 *   <tr>
 *     <td>底层异常</td>
 *     <td>由 @SneakyThrows 抛出</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>基于 MinIO 的对象存取（stat/get/put）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>未做 bucket/object 存在性校验，异常直接抛出。</li>
 *   <li>调用方负责关闭传入的输出流。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>下载</b></p>
 * <ul>
 *   <li>{@code getObjectThenWrite} 用 try-with-resources 关闭输入流，{@code IOUtils.copy} 复制到输出流。</li>
 * </ul>
 * <p><b>上传</b></p>
 * <ul>
 *   <li>字节数组经 {@code ByteArrayInputStream} 转流。</li>
 *   <li>输入流大小未知时分片大小设为 5MB。</li>
 * </ul>
 * <p><b>异常</b></p>
 * <ul>
 *   <li>所有方法标注 {@code @SneakyThrows}，底层异常直接抛出。</li>
 * </ul>
 *
 * @since 2026/7/15
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class CMinioService {

    MinioClient client;

    /**
     * 获取对象元数据（HEAD 请求，不下载文件内容）。
     *
     * @param bucket 存储桶名称
     * @param object 对象名称
     * @return 对象元数据，包含长度、ETag、修改时间等
     */
    @SneakyThrows
    public StatObjectResponse statObject(String bucket, String object) {
        val args = StatObjectArgs.builder()
            .bucket(bucket)
            .object(object)
            .build();
        return client.statObject(args);
    }

    /**
     * 下载对象，返回输入流。
     *
     * @param bucket 存储桶名称
     * @param object 对象名称
     * @return 文件内容的输入流
     */
    @SneakyThrows
    public GetObjectResponse getObject(String bucket, String object) {
        val args = GetObjectArgs.builder()
            .bucket(bucket)
            .object(object)
            .build();
        return client.getObject(args);
    }

    /**
     * 下载对象并写入到输出流，返回写入的字节数。
     *
     * @param bucket       存储桶名称
     * @param object       对象名称
     * @param outputStream 目标输出流（调用方负责关闭）
     * @return 写入的字节数
     */
    @SneakyThrows
    public int getObjectThenWrite(String bucket, String object, OutputStream outputStream) {
        try (val inputStream = getObject(bucket, object)) {
            return IOUtils.copy(inputStream, outputStream);
        }
    }

    /**
     * 上传字节数组到 MinIO。
     *
     * @param bucket 存储桶名称
     * @param object 对象名称
     * @param bytes  文件字节数组
     * @return 上传结果响应
     */
    @SneakyThrows
    public ObjectWriteResponse write(String bucket, String object, byte[] bytes) {
        return write(bucket, object, new ByteArrayInputStream(bytes));
    }

    /**
     * 上传输入流到 MinIO。
     * 流大小未知时将分片大小设为 5MB。
     *
     * @param bucket       存储桶名称
     * @param object       对象名称
     * @param inputStream  文件输入流
     * @return 上传结果响应
     */
    @SneakyThrows
    public ObjectWriteResponse write(String bucket, String object, InputStream inputStream) {

        val args = PutObjectArgs.builder()
            .bucket(bucket)
            .object(object)
            .stream(inputStream, -1L, 5L * 1024 * 1024)
            .build();
        return client.putObject(args);
    }

}
