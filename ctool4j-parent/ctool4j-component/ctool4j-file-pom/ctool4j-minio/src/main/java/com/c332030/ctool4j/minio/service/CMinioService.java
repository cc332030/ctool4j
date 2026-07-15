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
 * MinIO 对象存储服务，封装 MinIO 客户端常用操作。
 * </p>
 *
 * @since 2026/7/15
 */
@Service
@AllArgsConstructor
public class CMinioService {

    MinioClient client;

    /**
     * 获取对象元数据（HEAD 请求，不下载文件内容）。
     * 可用于获取文件大小（{@link ObjectStat#length()}）、ETag 等信息。
     *
     * @param bucket 存储桶名称
     * @param object 对象名称
     * @return 对象元数据，包含长度、ETag、修改时间等
     */
    @SneakyThrows
    public ObjectStat statObject(String bucket, String object) {
        val args = StatObjectArgs.builder()
            .bucket(bucket)
            .object(object)
            .build();
        return client.statObject(args);
    }

    /**
     * 下载对象，返回输入流。
     * 调用方使用完毕后需自行关闭流。获取文件大小请使用 {@link #statObject(String, String)}。
     *
     * @param bucket 存储桶名称
     * @param object 对象名称
     * @return 文件内容的输入流
     */
    @SneakyThrows
    public InputStream getObject(String bucket, String object) {
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
     * @param outputStream 目标输出流
     * @return 写入的字节数
     */
    @SneakyThrows
    public int getObjectThenWrite(String bucket, String object, OutputStream outputStream) {
        val inputStream = getObject(bucket, object);
        return IOUtils.copy(inputStream, outputStream);
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
            .stream(inputStream, -1, 5 * 1024 * 1024)
            .build();
        return client.putObject(args);
    }

}
