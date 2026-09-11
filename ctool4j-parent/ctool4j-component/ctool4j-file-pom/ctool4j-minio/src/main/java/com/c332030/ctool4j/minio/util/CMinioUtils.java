package com.c332030.ctool4j.minio.util;

import cn.hutool.core.lang.Opt;
import io.minio.GetObjectResponse;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;

/**
 * <p>
 * Description: CMinioUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CMinioUtils}（{@code @UtilityClass}）提供 MinIO 相关工具：</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>response 为 null</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>Content-Length 头缺失</td>
 *     <td>返回 null</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>获取对象大小。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖响应头的 Content-Length，非所有响应都携带。</li>
 * </ul>
 *
 * @since 2026/7/15
 * @version 1.0
 */
@UtilityClass
public class CMinioUtils {

    /**
     * 从响应头获取对象大小
     *
     * <h2>getSize</h2>
     * <ul>
     *   <li>对 response 空安全处理，取 {@code headers().get(CONTENT_LENGTH)} 转 Long；任何环节缺失返回 null。</li>
     * </ul>
     * <ul>
     *   <li>{@code getSize(GetObjectResponse)}：从响应头 {@code Content-Length} 获取对象大小，无法获取时返回 null。</li>
     * </ul>
     *
     * @param response Minio 获取对象响应
     * @return 对象大小（字节）；无法获取时返回 null*/
    public Long getSize(GetObjectResponse response) {
        return Opt.ofNullable(response)
            .map(GetObjectResponse::headers)
            .map(e -> e.get(HttpHeaders.CONTENT_LENGTH))
            .map(Long::valueOf)
            .orElse(null);
    }

}
