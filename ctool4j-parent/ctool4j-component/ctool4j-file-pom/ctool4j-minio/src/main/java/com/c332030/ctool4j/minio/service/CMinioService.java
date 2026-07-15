package com.c332030.ctool4j.minio.service;

import io.minio.*;
import io.minio.errors.MinioException;
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
     *
     * @param bucket 存储桶名称
     * @param object 对象名称
     * @return 对象元数据，包含长度、ETag、修改时间等
     */
    public StatObjectResponse statObject(String bucket, String object) throws MinioException {
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
    public GetObjectResponse getObject(String bucket, String object) throws MinioException {
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
    public ObjectWriteResponse write(String bucket, String object, byte[] bytes) throws MinioException {
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
    public ObjectWriteResponse write(String bucket, String object, InputStream inputStream) throws MinioException {

        val args = PutObjectArgs.builder()
            .bucket(bucket)
            .object(object)
            .stream(inputStream, -1L, 5L * 1024 * 1024)
            .build();
        return client.putObject(args);
    }

}
