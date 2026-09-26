package com.c332030.ctool4j.web.exception.handler;

import com.c332030.ctool4j.spring.test.annotation.CTool4jSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * <p>
 * Description: CFileUploadExceptionHandlerTests
 * </p>
 *
 * <h2>测试用例目录</h2>
 * <ul>
 *   <li>1.1 文件超限：由本处理器命中，返回 200 + 业务码 413 + 含上限的提示（uploadSizeExceeded_handledByThisHandler）</li>
 *   <li>1.2 其他 multipart 解析失败：由本处理器命中，返回 200 + 业务码 400 + 解析失败提示（multipartError_handledByThisHandler）</li>
 *   <li>1.3 裸抛的容器私有超限类型（简单类名 FileSizeLimitExceededException）：由兜底处理器按类名识别并委托本处理器，返回 200 + 业务码 413 + 泛化提示（uploadSizeExceededByName_handledByFallback）</li>
 * </ul>
 *
 * <h2>测试设计</h2>
 * <ul>
 *   <li>依据测试方法（正例/分支覆盖）：超限分支与其他解析失败分支；断言业务码 + 提示文案，验证不泄露原始异常文本。</li>
 *   <li>覆盖场景：见上方编号索引（含裸抛容器私有类型由兜底按类名识别并委托的场景）。</li>
 *   <li>未覆盖：其他容器使用不同简单类名的超限类型——按"简单类名清单可扩展"处理（清单见 CFileUploadExceptionHandler）。</li>
 *   <li>依据：只声明 Spring 公共类型（MultipartException 族）、容器私有类型按简单类名识别，二者都不解析容器私有类，
 *   保证在 Jetty/Undertow 等容器下不出现类加载失败。</li>
 * </ul>
 *
 * @since 2026/9/18
 * @version 1.1
 * @see CFileUploadExceptionHandler
 */
@AutoConfigureMockMvc
@CTool4jSpringBootTest
public class CFileUploadExceptionHandlerTests {

    /**
     * MockMvc
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * 对应测试用例 1.1：文件超限：返回 200 + 业务码 413 + 含上限的提示（1MB）
     */
    @Test
    public void uploadSizeExceeded_handledByThisHandler() throws Exception {

        mockMvc.perform(get("/c-exception-handler/upload-size-exceeded"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("413"))
            .andExpect(jsonPath("$.message").value("文件大小超出限制（单个文件最大 1MB），请压缩后重试"));
    }

    /**
     * 对应测试用例 1.2：其他 multipart 解析失败：返回 200 + 业务码 400 + 解析失败提示
     */
    @Test
    public void multipartError_handledByThisHandler() throws Exception {

        mockMvc.perform(get("/c-exception-handler/multipart-error"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("400"))
            .andExpect(jsonPath("$.message").value("文件上传解析失败，请重试"));
    }

    /**
     * 对应测试用例 1.3：裸抛的容器私有超限类型（简单类名 FileSizeLimitExceededException，未经 Spring 包装）：
     * 由兜底处理器按类名识别并委托本处理器，返回 200 + 业务码 413 + 泛化提示（上限不可得）
     */
    @Test
    public void uploadSizeExceededByName_handledByFallback() throws Exception {

        mockMvc.perform(get("/c-exception-handler/upload-size-exceeded-by-name"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("413"))
            .andExpect(jsonPath("$.message").value("文件大小超出限制，请压缩后重试"));
    }

}
