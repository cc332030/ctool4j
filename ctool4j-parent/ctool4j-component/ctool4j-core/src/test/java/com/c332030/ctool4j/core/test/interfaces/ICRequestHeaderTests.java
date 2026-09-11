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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「默认行为 / 覆盖行为」两个维度组织。</li>
 *   <li>默认：匿名实现仅提供 name/text，验证 dataType 默认 STRING、required 默认 false、Header 名推导。</li>
 *   <li>覆盖：覆盖 dataType/required，验证自定义值与 Header 名推导。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对默认值（STRING/false）与 Header 名推导的约定。</li>
 *   <li>依据测试方法（等价类/分支覆盖）：默认分支、覆盖分支、Header 名。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：默认 dataType/required/Header 名（TRACE_ID→Trace-Id）；覆盖 dataType/required 及 Header 名</li>
 *   <li>（X_TOKEN→X-Token）。</li>
 *   <li>未覆盖：无（覆盖了默认与覆盖两分支）。</li>
 * </ul>
 * <h2>默认行为</h2>
 * <ul>
 *   <li>1.1 defaults：dataType=STRING、required=false、Header 名 {@code Trace-Id}、text（defaults）</li>
 * </ul>
 * <h2>覆盖行为</h2>
 * <ul>
 *   <li>2.1 override：dataType=LONG、required=true、Header 名 {@code X-Token}（override）</li>
 * </ul>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class ICRequestHeaderTests {

    /**
     * 对应测试用例 1.1：dataType=STRING、required=false、Header 名 {@code Trace-Id}、text
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
     * 对应测试用例 2.1：dataType=LONG、required=true、Header 名 {@code X-Token}
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
