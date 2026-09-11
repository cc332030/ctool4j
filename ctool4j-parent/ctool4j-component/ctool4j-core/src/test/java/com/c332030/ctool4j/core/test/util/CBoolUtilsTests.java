package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CBoolUtils;
import com.c332030.ctool4j.definition.function.CFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CBoolUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「基础判断 / 对象函数重载」两个维度组织，每类覆盖四种判断（isTrue/isNotTrue/isFalse/isNotFalse）。</li>
 *   <li>每种判断覆盖 true/false/null 三个取值，验证 null 视为 false 的语义与取反关系（isNotTrue=!isTrue 等）。</li>
 *   <li>对象函数重载额外覆盖 null 对象入参，验证经 {@code CObjUtils.convert} 取值后的 null 语义。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 null 视为 false 的约定与取反关系。</li>
 *   <li>依据测试方法（等价类/边界值）：true/false 正例、null 边界。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：四种判断 ×（true/false/null）；对象函数重载的四种判断 ×（true/false/null 对象入参）。</li>
 *   <li>未覆盖：非布尔对象的属性取值判断的真实对象场景（以 String 布尔值经 Boolean::valueOf 模拟，行为一致）。</li>
 * </ul>
 * <h2>基础判断</h2>
 * <ul>
 *   <li>1.1 isTrue：true→true、false→false、null→false（isTrue）</li>
 *   <li>1.2 isNotTrue：true→false、false→true、null→true（isNotTrue）</li>
 *   <li>1.3 isFalse：false→true、true→false、null→false（isFalse）</li>
 *   <li>1.4 isNotFalse：false→false、true→true、null→true（isNotFalse）</li>
 * </ul>
 * <h2>对象函数重载</h2>
 * <ul>
 *   <li>2.1 isTrue(T, function)：{@code "true"}→true、{@code "false"}→false、null 对象→false（isTrueByFunction）</li>
 *   <li>2.2 isNotTrue(T, function)：{@code "true"}→false、{@code "false"}→true、null 对象→true（isNotTrueByFunction）</li>
 *   <li>2.3 isFalse(T, function)：{@code "false"}→true、{@code "true"}→false、null 对象→false（isFalseByFunction）</li>
 *   <li>2.4 isNotFalse(T, function)：{@code "false"}→false、{@code "true"}→true、null 对象→true（isNotFalseByFunction）</li>
 * </ul>
 *
 * @since 2025/12/22
 * @version 1.0
 */
public class CBoolUtilsTests {

    /**
     * 对应测试用例 1.1：true→true、false→false、null→false
     */
    @Test
    public void isTrue() {

        Assertions.assertTrue(CBoolUtils.isTrue(Boolean.TRUE));
        Assertions.assertFalse(CBoolUtils.isTrue(Boolean.FALSE));
        Assertions.assertFalse(CBoolUtils.isTrue(null));

    }

    /**
     * 对应测试用例 1.2：true→false、false→true、null→true
     */
    @Test
    public void isNotTrue() {

        Assertions.assertFalse(CBoolUtils.isNotTrue(Boolean.TRUE));
        Assertions.assertTrue(CBoolUtils.isNotTrue(Boolean.FALSE));
        Assertions.assertTrue(CBoolUtils.isNotTrue(null));

    }

    /**
     * 对应测试用例 1.3：false→true、true→false、null→false
     */
    @Test
    public void isFalse() {

        Assertions.assertTrue(CBoolUtils.isFalse(Boolean.FALSE));
        Assertions.assertFalse(CBoolUtils.isFalse(Boolean.TRUE));
        Assertions.assertFalse(CBoolUtils.isFalse(null));

    }

    /**
     * 对应测试用例 1.4：false→false、true→true、null→true
     */
    @Test
    public void isNotFalse() {

        Assertions.assertFalse(CBoolUtils.isNotFalse(Boolean.FALSE));
        Assertions.assertTrue(CBoolUtils.isNotFalse(Boolean.TRUE));
        Assertions.assertTrue(CBoolUtils.isNotFalse(null));

    }

    /**
     * 对应测试用例 2.1：isTrue(T, function)：{@code "true"}→true、{@code "false"}→false、null 对象→false
     */
    @Test
    public void isTrueByFunction() {

        Assertions.assertTrue(CBoolUtils.isTrue("true", Boolean::valueOf));
        Assertions.assertFalse(CBoolUtils.isTrue("false", Boolean::valueOf));
        Assertions.assertFalse(CBoolUtils.isTrue(null, (CFunction<String, Boolean>) Boolean::valueOf));

    }

    /**
     * 对应测试用例 2.2：isNotTrue(T, function)：{@code "true"}→false、{@code "false"}→true、null 对象→true
     */
    @Test
    public void isNotTrueByFunction() {

        Assertions.assertFalse(CBoolUtils.isNotTrue("true", Boolean::valueOf));
        Assertions.assertTrue(CBoolUtils.isNotTrue("false", Boolean::valueOf));
        Assertions.assertTrue(CBoolUtils.isNotTrue(null, (CFunction<String, Boolean>) Boolean::valueOf));

    }

    /**
     * 对应测试用例 2.3：isFalse(T, function)：{@code "false"}→true、{@code "true"}→false、null 对象→false
     */
    @Test
    public void isFalseByFunction() {

        Assertions.assertTrue(CBoolUtils.isFalse("false", Boolean::valueOf));
        Assertions.assertFalse(CBoolUtils.isFalse("true", Boolean::valueOf));
        Assertions.assertFalse(CBoolUtils.isFalse(null, (CFunction<String, Boolean>) Boolean::valueOf));

    }

    /**
     * 对应测试用例 2.4：isNotFalse(T, function)：{@code "false"}→false、{@code "true"}→true、null 对象→true
     */
    @Test
    public void isNotFalseByFunction() {

        Assertions.assertFalse(CBoolUtils.isNotFalse("false", Boolean::valueOf));
        Assertions.assertTrue(CBoolUtils.isNotFalse("true", Boolean::valueOf));
        Assertions.assertTrue(CBoolUtils.isNotFalse(null, (CFunction<String, Boolean>) Boolean::valueOf));

    }

}
