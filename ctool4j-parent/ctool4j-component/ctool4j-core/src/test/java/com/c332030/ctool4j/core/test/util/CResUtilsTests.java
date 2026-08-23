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
 * @since 2026/8/14
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
     * 对应测试用例 1.1
     */
    @Test
    public void formatMessage() {

        Assertions.assertEquals("ok", CResUtils.formatMessage(res("200", "ok")));
        Assertions.assertEquals("ok: extra", CResUtils.formatMessage(res("200", "ok"), "extra"));

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void formatMessageNullRes() {

        Assertions.assertNull(CResUtils.formatMessage(null));
        Assertions.assertEquals("extra", CResUtils.formatMessage(null, "extra"));

    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void formatMessageEmptyExtend() {

        Assertions.assertEquals("ok", CResUtils.formatMessage(res("200", "ok"), ""));
        Assertions.assertEquals("ok", CResUtils.formatMessage(res("200", "ok"), null));

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void formatResMessage() {

        Assertions.assertEquals("[200] ok", CResUtils.formatResMessage(res("200", "ok")));
        Assertions.assertEquals("[200] ok: extra", CResUtils.formatResMessage(res("200", "ok"), "extra"));

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void formatResMessageNullRes() {

        Assertions.assertNull(CResUtils.formatResMessage(null));
        Assertions.assertEquals("extra", CResUtils.formatResMessage(null, "extra"));

    }

    /**
     * 对应测试用例 2.3
     */
    @Test
    public void formatResMessageEmptyExtend() {

        Assertions.assertEquals("[200] ok", CResUtils.formatResMessage(res("200", "ok"), ""));
        Assertions.assertEquals("[200] ok", CResUtils.formatResMessage(res("200", "ok"), null));

    }

}
