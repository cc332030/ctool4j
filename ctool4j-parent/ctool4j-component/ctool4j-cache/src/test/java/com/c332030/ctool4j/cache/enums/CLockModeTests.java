package com.c332030.ctool4j.cache.enums;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * <p>
 * Description: CLockModeTests
 * </p>
 *
 * <p>
 * 是 {@link CLockMode} 的测试用例。
 * </p>
 * <h2>设计思路</h2>
 * <ul>
 *   <li>覆盖枚举全量常量数量与枚举值；覆盖每个枚举常量的 {@code text} 描述字段取值。</li>
 *   <li>按错误推测法验证枚举常量集合的完整性（防止新增/删除常量被遗漏）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对两种锁模式（本地锁/分布式锁）及其描述字段的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：枚举数量与常量完整性、每个常量的描述字段。</li>
 *   <li>未覆盖：无（纯枚举，无状态与副作用，全部入口均已覆盖）。</li>
 * </ul>
 * <h2>枚举定义</h2>
 * <ul>
 *   <li>1.1 values：枚举数量为 2，含 LOCAL 与 DISTRIBUTED（values）</li>
 *   <li>1.2 text：LOCAL 为 {@code 本地锁}、DISTRIBUTED 为 {@code 分布式锁}（text）</li>
 * </ul>
 *
 * @since 1.0
 * @version 1.0
 */
public class CLockModeTests {

    /**
     * 对应测试用例 1.1：枚举数量为 2，含 LOCAL 与 DISTRIBUTED
     */
    @Test
    public void values() {

        CLockMode[] values = CLockMode.values();

        Assertions.assertEquals(2, values.length);
        Assertions.assertTrue(Arrays.asList(values).contains(CLockMode.LOCAL));
        Assertions.assertTrue(Arrays.asList(values).contains(CLockMode.DISTRIBUTED));

    }

    /**
     * 对应测试用例 1.2：LOCAL 为 {@code 本地锁}、DISTRIBUTED 为 {@code 分布式锁}
     */
    @Test
    public void text() {

        Assertions.assertEquals("本地锁", CLockMode.LOCAL.getText());
        Assertions.assertEquals("分布式锁", CLockMode.DISTRIBUTED.getText());

    }

}
