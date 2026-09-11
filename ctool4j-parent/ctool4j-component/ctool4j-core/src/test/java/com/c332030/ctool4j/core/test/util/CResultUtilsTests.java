package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.exception.CBusinessException;
import com.c332030.ctool4j.core.util.CResultUtils;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * <p>
 * Description: CResultUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「成功码集合 / 成功判断 / 断言 / 数据获取」多个维度组织。</li>
 *   <li>成功判断用 {@code CStrResult.success("data")} / {@code CStrResult.error("500","error")} / null 三样本覆盖正反与 null。</li>
 *   <li>断言用 {@code assertThrowsExactly(CBusinessException.class, ...)} 精确匹配异常类型，覆盖成功/失败/null 三分支。</li>
 *   <li>数据获取覆盖成功取数、失败抛异常、null 抛异常、带默认值、空列表默认值。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对成功码、null 语义、断言抛异常、默认值兜底的约定。</li>
 *   <li>依据测试方法（等价类/边界值/异常路径）：成功/失败/null、数据缺失默认值。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：SUCCESS_CODES 内容；isSuccess/isNotSuccess 正反 null；assertSuccess 成功/失败/null；getData</li>
 *   <li>成功/默认值/失败抛异常/null 抛异常；getDataDefaultEmptyList 正常与 null 数据返回空列表。</li>
 *   <li>未覆盖：非固定成功码（如业务自定义码）判断（当前成功码集合固定，未构造自定义码样本）。</li>
 * </ul>
 * <h2>成功码集合</h2>
 * <ul>
 *   <li>1.1 SUCCESS_CODES 含 {@code 0}/{@code 200}/{@code 000000}（successCodes）</li>
 * </ul>
 * <h2>成功判断</h2>
 * <ul>
 *   <li>2.1 isSuccess：success→true、error→false、null→false（isSuccess）</li>
 *   <li>2.2 isNotSuccess：success→false、error→true、null→true（isNotSuccess）</li>
 * </ul>
 * <h2>断言（assertSuccess）</h2>
 * <ul>
 *   <li>3.1 成功不抛异常（assertSuccessOk）</li>
 *   <li>3.2 失败抛 CBusinessException（assertSuccessErrorThrows）</li>
 *   <li>3.3 null 抛 CBusinessException（assertSuccessNullThrows）</li>
 * </ul>
 * <h2>数据获取（getData）</h2>
 * <ul>
 *   <li>4.1 成功取数：{@code success("data")} → {@code "data"}（getData）</li>
 *   <li>4.2 带默认值：数据非空用数据、数据 null 用默认值（getDataWithDefault）</li>
 *   <li>4.3 失败抛异常：error 结果抛 CBusinessException（getDataErrorThrows）</li>
 *   <li>4.4 null 抛异常：null 结果抛 CBusinessException（getDataNullThrows）</li>
 *   <li>4.5 空列表默认值：数据为 null 返回空列表（getDataDefaultEmptyList）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CResultUtilsTests {

    /**
     * 对应测试用例 1.1：SUCCESS_CODES 含 {@code 0}/{@code 200}/{@code 000000}
     */
    @Test
    public void successCodes() {

        Assertions.assertTrue(CResultUtils.SUCCESS_CODES.contains("0"));
        Assertions.assertTrue(CResultUtils.SUCCESS_CODES.contains("200"));
        Assertions.assertTrue(CResultUtils.SUCCESS_CODES.contains("000000"));

    }

    /**
     * 对应测试用例 2.1：success→true、error→false、null→false
     */
    @Test
    public void isSuccess() {

        Assertions.assertTrue(CResultUtils.isSuccess(CStrResult.success("data")));
        Assertions.assertFalse(CResultUtils.isSuccess(CStrResult.error("500", "error")));
        Assertions.assertFalse(CResultUtils.isSuccess(null));

    }

    /**
     * 对应测试用例 2.2：success→false、error→true、null→true
     */
    @Test
    public void isNotSuccess() {

        Assertions.assertFalse(CResultUtils.isNotSuccess(CStrResult.success("data")));
        Assertions.assertTrue(CResultUtils.isNotSuccess(CStrResult.error("500", "error")));
        Assertions.assertTrue(CResultUtils.isNotSuccess(null));

    }

    /**
     * 对应测试用例 3.1：成功不抛异常
     */
    @Test
    public void assertSuccessOk() {

        Assertions.assertDoesNotThrow(() -> CResultUtils.assertSuccess(CStrResult.success("data")));

    }

    /**
     * 对应测试用例 3.2：失败抛 CBusinessException
     */
    @Test
    public void assertSuccessErrorThrows() {

        Assertions.assertThrowsExactly(CBusinessException.class,
                () -> CResultUtils.assertSuccess(CStrResult.error("500", "boom")));

    }

    /**
     * 对应测试用例 3.3：null 抛 CBusinessException
     */
    @Test
    public void assertSuccessNullThrows() {

        Assertions.assertThrowsExactly(CBusinessException.class,
                () -> CResultUtils.assertSuccess(null));

    }

    /**
     * 对应测试用例 4.1：成功取数：{@code success("data")} → {@code "data"}
     */
    @Test
    public void getData() {

        Assertions.assertEquals("data", CResultUtils.getData(CStrResult.success("data")));

    }

    /**
     * 对应测试用例 4.2：带默认值：数据非空用数据、数据 null 用默认值
     */
    @Test
    public void getDataWithDefault() {

        Assertions.assertEquals("data", CResultUtils.getData(CStrResult.success("data"), "default"));
        Assertions.assertEquals("default", CResultUtils.getData(CStrResult.success(null), "default"));

    }

    /**
     * 对应测试用例 4.3：失败抛异常：error 结果抛 CBusinessException
     */
    @Test
    public void getDataErrorThrows() {

        Assertions.assertThrowsExactly(CBusinessException.class,
                () -> CResultUtils.getData(CStrResult.error("500", "boom")));

    }

    /**
     * 对应测试用例 4.4：null 抛异常：null 结果抛 CBusinessException
     */
    @Test
    public void getDataNullThrows() {

        Assertions.assertThrowsExactly(CBusinessException.class,
                () -> CResultUtils.getData(null));

    }

    /**
     * 对应测试用例 4.5：空列表默认值：数据为 null 返回空列表
     */
    @Test
    public void getDataDefaultEmptyList() {

        Assertions.assertEquals(Arrays.asList("a", "b"),
                CResultUtils.getDataDefaultEmptyList(CStrResult.success(Arrays.asList("a", "b"))));
        Assertions.assertEquals(Collections.emptyList(),
                CResultUtils.getDataDefaultEmptyList(CStrResult.success(null)));

    }

}
