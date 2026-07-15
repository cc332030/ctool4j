package com.c332030.ctool4j.minio.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
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
 * @since 2026/7/15
 */
@Service
@AllArgsConstructor
public class CMinioService {

    MinioClient client;

    @SneakyThrows
    public InputStream getObject(String bucket, String object) {
        val args = GetObjectArgs.builder()
            .bucket(bucket)
            .object(object)
            .build();
        return client.getObject(args);
    }

    @SneakyThrows
    public int getObjectThenWrite(String bucket, String object, OutputStream outputStream) {
        val inputStream = getObject(bucket, object);
        return IOUtils.copy(inputStream, outputStream);
    }

    @SneakyThrows
    public ObjectWriteResponse write(String bucket, String object, byte[] bytes) {
        return write(bucket, object, new ByteArrayInputStream(bytes));
    }

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
