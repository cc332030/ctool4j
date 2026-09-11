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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证 getContentDispositionValue 的拼接行为，覆盖正常/空/null/中文文件名。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 {@code attachment;filename=} 拼接的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：正常/空/null/中文文件名。</li>
 *   <li>未覆盖：writeResponse（依赖容器上下文，未在单测覆盖）。</li>
 * </ul>
 * <h2>Content-Disposition 值</h2>
 * <ul>
 *   <li>1.1 正常：{@code attachment;filename=report.xlsx}（getContentDispositionValue）</li>
 *   <li>1.2 空文件名：{@code attachment;filename=}（getContentDispositionValue_emptyFilename）</li>
 *   <li>1.3 null 文件名：{@code attachment;filename=null}（getContentDispositionValue_nullFilename）</li>
 *   <li>1.4 中文文件名：{@code attachment;filename=报表.xlsx}（getContentDispositionValue_withChinese）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
@CustomLog
public class CWebUtilsTests {

    // ---------- getContentDispositionValue ----------

    /**
     * 对应测试用例 1.1：正常：{@code attachment;filename=report.xlsx}
     */
    @Test
    public void getContentDispositionValue() {
        // 正例：拼接 attachment;filename= 前缀
        val value = CWebUtils.getContentDispositionValue("report.xlsx");
        Assertions.assertEquals("attachment;filename=report.xlsx", value);
    }

    /**
     * 对应测试用例 1.2：空文件名：{@code attachment;filename=}
     */
    @Test
    public void getContentDispositionValue_emptyFilename() {
        // 边界：空文件名
        Assertions.assertEquals("attachment;filename=", CWebUtils.getContentDispositionValue(""));
    }

    /**
     * 对应测试用例 1.3：null 文件名：{@code attachment;filename=null}
     */
    @Test
    public void getContentDispositionValue_nullFilename() {
        // 边界：null 文件名（拼接 "null"）
        Assertions.assertEquals("attachment;filename=null", CWebUtils.getContentDispositionValue(null));
    }

    /**
     * 对应测试用例 1.4：中文文件名：{@code attachment;filename=报表.xlsx}
     */
    @Test
    public void getContentDispositionValue_withChinese() {
        // 边界：中文文件名
        val value = CWebUtils.getContentDispositionValue("报表.xlsx");
        Assertions.assertEquals("attachment;filename=报表.xlsx", value);
    }

}
