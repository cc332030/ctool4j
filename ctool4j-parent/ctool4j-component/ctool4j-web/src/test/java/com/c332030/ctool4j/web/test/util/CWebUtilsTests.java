package com.c332030.ctool4j.web.test.util;

import com.c332030.ctool4j.web.util.CWebUtils;
import lombok.CustomLog;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CWebUtilsTests
 * </p>
 * <p>`com.c332030.ctool4j.web.util.CWebUtils`（CWebUtils）的测试用例</p>
 *
 * <p>覆盖不依赖容器的纯逻辑方法 getContentDispositionValue；
 * writeResponse 依赖 CRequestUtils.getResponse()（容器），不在本测试覆盖范围</p>
 *
 * @since 2026/8/14
 */
@CustomLog
public class CWebUtilsTests {

    // ---------- getContentDispositionValue ----------

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void getContentDispositionValue() {
        // 正例：拼接 attachment;filename= 前缀
        val value = CWebUtils.getContentDispositionValue("report.xlsx");
        Assertions.assertEquals("attachment;filename=report.xlsx", value);
    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void getContentDispositionValue_emptyFilename() {
        // 边界：空文件名
        Assertions.assertEquals("attachment;filename=", CWebUtils.getContentDispositionValue(""));
    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void getContentDispositionValue_nullFilename() {
        // 边界：null 文件名（拼接 "null"）
        Assertions.assertEquals("attachment;filename=null", CWebUtils.getContentDispositionValue(null));
    }

    /**
     * 对应测试用例 1.4
     */
    @Test
    public void getContentDispositionValue_withChinese() {
        // 边界：中文文件名
        val value = CWebUtils.getContentDispositionValue("报表.xlsx");
        Assertions.assertEquals("attachment;filename=报表.xlsx", value);
    }

}
