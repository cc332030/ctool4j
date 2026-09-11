package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CResUtils;
import com.c332030.ctool4j.definition.interfaces.ICRes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CResUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「formatMessage / formatResMessage」两个方法、各覆盖「正例 / res null / 扩展信息为空」三类场景。</li>
 *   <li>用匿名实现 {@code ICRes&lt;String&gt;} 模拟响应对象（getCode/getMsg），贴近真实使用。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对空值语义的约定（res null 返回扩展信息、扩展空返回 msg）。</li>
 *   <li>依据测试方法（等价类/边界值）：正例、res null、扩展信息空/空白。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：两方法的正例（含扩展信息）、res null、msgExtend 为 "" / null。</li>
 *   <li>未覆盖：无（覆盖了全部入口与空值分支）。</li>
 * </ul>
 * <h2>formatMessage</h2>
 * <ul>
 *   <li>1.1 正例：{@code res("ok")} → {@code "ok"}；带扩展 {@code "ok: extra"}（formatMessage）</li>
 *   <li>1.2 res null：返回 null / 返回 {@code "extra"}（formatMessageNullRes）</li>
 *   <li>1.3 扩展为空：{@code ""} / null 均返回 {@code "ok"}（formatMessageEmptyExtend）</li>
 * </ul>
 * <h2>formatResMessage</h2>
 * <ul>
 *   <li>2.1 正例：{@code [200] ok}；带扩展 {@code [200] ok: extra}（formatResMessage）</li>
 *   <li>2.2 res null：返回 null / 返回 {@code "extra"}（formatResMessageNullRes）</li>
 *   <li>2.3 扩展为空：{@code ""} / null 均返回 {@code [200] ok}（formatResMessageEmptyExtend）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CResUtilsTests {

    private static ICRes<String> res(String code, String msg) {
        return new ICRes<String>() {
            @Override
            public String getCode() {
                return code;
            }

            @Override
            public String getMsg() {
                return msg;
            }
        };
    }

    /**
     * 对应测试用例 1.1：正例：{@code res("ok")} → {@code "ok"}；带扩展 {@code "ok: extra"}
     */
    @Test
    public void formatMessage() {

        Assertions.assertEquals("ok", CResUtils.formatMessage(res("200", "ok")));
        Assertions.assertEquals("ok: extra", CResUtils.formatMessage(res("200", "ok"), "extra"));

    }

    /**
     * 对应测试用例 1.2：res null：返回 null / 返回 {@code "extra"}
     */
    @Test
    public void formatMessageNullRes() {

        Assertions.assertNull(CResUtils.formatMessage(null));
        Assertions.assertEquals("extra", CResUtils.formatMessage(null, "extra"));

    }

    /**
     * 对应测试用例 1.3：扩展为空：{@code ""} / null 均返回 {@code "ok"}
     */
    @Test
    public void formatMessageEmptyExtend() {

        Assertions.assertEquals("ok", CResUtils.formatMessage(res("200", "ok"), ""));
        Assertions.assertEquals("ok", CResUtils.formatMessage(res("200", "ok"), null));

    }

    /**
     * 对应测试用例 2.1：正例：{@code [200] ok}；带扩展 {@code [200] ok: extra}
     */
    @Test
    public void formatResMessage() {

        Assertions.assertEquals("[200] ok", CResUtils.formatResMessage(res("200", "ok")));
        Assertions.assertEquals("[200] ok: extra", CResUtils.formatResMessage(res("200", "ok"), "extra"));

    }

    /**
     * 对应测试用例 2.2：res null：返回 null / 返回 {@code "extra"}
     */
    @Test
    public void formatResMessageNullRes() {

        Assertions.assertNull(CResUtils.formatResMessage(null));
        Assertions.assertEquals("extra", CResUtils.formatResMessage(null, "extra"));

    }

    /**
     * 对应测试用例 2.3：扩展为空：{@code ""} / null 均返回 {@code [200] ok}
     */
    @Test
    public void formatResMessageEmptyExtend() {

        Assertions.assertEquals("[200] ok", CResUtils.formatResMessage(res("200", "ok"), ""));
        Assertions.assertEquals("[200] ok", CResUtils.formatResMessage(res("200", "ok"), null));

    }

}
