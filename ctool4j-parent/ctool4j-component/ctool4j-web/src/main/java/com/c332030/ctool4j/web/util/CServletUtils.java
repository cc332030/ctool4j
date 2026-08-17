package com.c332030.ctool4j.web.util;

import com.c332030.ctool4j.core.util.CCharsets;
import com.c332030.ctool4j.core.util.CJsonUtils;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: CServletUtils
 * </p>
 *
 * @since 2025/9/25
 */
@UtilityClass
public class CServletUtils {

    /**
     * 以 JSON 形式写出响应，对象序列化为 JSON
     *
     * @param response   响应
     * @param httpStatus HTTP 状态码
     * @param body       响应体对象
     */
    @SneakyThrows
    public void writeJson(
        HttpServletResponse response,
        HttpStatus httpStatus,
        Object body
    ) {
        writeJson(response, httpStatus, CJsonUtils.toJson(body));
    }

    /**
     * 以 JSON 形式写出响应
     *
     * @param response   响应
     * @param httpStatus HTTP 状态码
     * @param jsonBody   JSON 字符串响应体
     */
    @SneakyThrows
    public void writeJson(
        HttpServletResponse response,
        HttpStatus httpStatus,
        String jsonBody
    ) {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(CCharsets.UTF_8.name());
        response.setStatus(httpStatus.value());

        val writer = response.getWriter();
        writer.write(jsonBody);
        writer.flush();
        writer.close();

    }

}
