package com.c332030.ctool4j.web.util;

import com.c332030.ctool4j.core.util.CJsonUtils;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 * Description: CWebUtils
 * </p>
 *
 * @since 2025/9/28
 */
@CustomLog
@UtilityClass
public class CWebUtils {

    public String getContentDispositionValue(String filename) {
        return "attachment;filename=" + filename;
    }

    @SneakyThrows
    public void writeJson(
        HttpServletResponse response,
        HttpStatus httpStatus,
        Object body
    ) {
        writeJson(response, httpStatus, CJsonUtils.toJson(body));
    }

    @SneakyThrows
    public void writeJson(
        HttpServletResponse response,
        HttpStatus httpStatus,
        String jsonBody
    ) {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(httpStatus.value());

        val writer = response.getWriter();
        writer.write(jsonBody);
        writer.flush();
        writer.close();

    }

}
