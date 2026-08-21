package com.c332030.ctool4j.core.test.interfaces;

import com.c332030.ctool4j.core.enums.CDataTypeEnum;
import com.c332030.ctool4j.core.interfaces.ICRequestHeader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: ICRequestHeaderTests
 * </p>
 *
 * @since 2025/12/12
 */
public class ICRequestHeaderTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void defaults() {

        ICRequestHeader header = new ICRequestHeader() {
            @Override
            public String name() {
                return "TRACE_ID";
            }

            @Override
            public String getText() {
                return "链路追踪 ID";
            }
        };

        Assertions.assertEquals(CDataTypeEnum.STRING, header.getDataType());
        Assertions.assertFalse(header.isRequired());
        Assertions.assertEquals("Trace-Id", header.getHeaderName());
        Assertions.assertEquals("链路追踪 ID", header.getText());

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void override() {

        ICRequestHeader header = new ICRequestHeader() {
            @Override
            public String name() {
                return "X_TOKEN";
            }

            @Override
            public String getText() {
                return "token";
            }

            @Override
            public CDataTypeEnum getDataType() {
                return CDataTypeEnum.LONG;
            }

            @Override
            public boolean isRequired() {
                return true;
            }
        };

        Assertions.assertEquals(CDataTypeEnum.LONG, header.getDataType());
        Assertions.assertTrue(header.isRequired());
        Assertions.assertEquals("X-Token", header.getHeaderName());

    }

}
