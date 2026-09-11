package com.c332030.ctool4j.core.test.config;

import com.c332030.ctool4j.core.config.CPageConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CPageConfigTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「默认值 / 设置取值 / toString」三个维度组织。</li>
 *   <li>默认值验证无参构造 defaultPageSize=100；设置取值验证 setter/getter；toString 验证字段输出。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对默认值与 Lombok @Data 行为的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：无参构造默认值；setDefaultPageSize/getDefaultPageSize；toString 含类名与字段。</li>
 *   <li>未覆盖：Spring 配置绑定（依赖容器，单测直接构造对象验证）。</li>
 * </ul>
 * <h2>默认值</h2>
 * <ul>
 *   <li>1.1 无参构造：defaultPageSize 默认 100（noArgsConstructor）</li>
 * </ul>
 * <h2>设置与取值</h2>
 * <ul>
 *   <li>2.1 setter/getter：设置 50 后取回 50（setterAndGetter）</li>
 * </ul>
 * <h2>toString</h2>
 * <ul>
 *   <li>3.1 toString：含类名与 {@code defaultPageSize=50}（toStringContainsFields）</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.0
 */
public class CPageConfigTests {

    /**
     * 对应测试用例 1.1：无参构造：defaultPageSize 默认 100
     */
    @Test
    public void noArgsConstructor() {

        CPageConfig config = new CPageConfig();

        Assertions.assertEquals(Integer.valueOf(100), config.getDefaultPageSize());

    }

    /**
     * 对应测试用例 2.1：setter/getter：设置 50 后取回 50
     */
    @Test
    public void setterAndGetter() {

        CPageConfig config = new CPageConfig();
        config.setDefaultPageSize(50);

        Assertions.assertEquals(Integer.valueOf(50), config.getDefaultPageSize());

    }

    /**
     * 对应测试用例 3.1：含类名与 {@code defaultPageSize=50}
     */
    @Test
    public void toStringContainsFields() {

        CPageConfig config = new CPageConfig();
        config.setDefaultPageSize(50);

        String str = config.toString();

        Assertions.assertNotNull(str);
        Assertions.assertTrue(str.contains("CPageConfig"));
        Assertions.assertTrue(str.contains("defaultPageSize=50"));

    }

}
