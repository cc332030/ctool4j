package com.c332030.ctool4j.core.test.interfaces;

import com.c332030.ctool4j.core.interfaces.ICopy;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: ICopyTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「拷贝到类型 / 拷贝到实例」两个入口组织。</li>
 *   <li>用 Source（实现 ICopy，name/age）与 Target（同名字段）验证拷贝正确。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对两个拷贝入口的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：copyTo(Class) 创建新实例拷贝；copyTo(instance) 拷贝到已有实例。</li>
 *   <li>未覆盖：无（覆盖了两个入口）。</li>
 * </ul>
 * <h2>拷贝到类型</h2>
 * <ul>
 *   <li>1.1 copyToClass：Source → Target 新实例，name/age 正确（copyToClass）</li>
 * </ul>
 * <h2>拷贝到实例</h2>
 * <ul>
 *   <li>2.1 copyToInstance：Source → 已有 Target 实例，name/age 正确（copyToInstance）</li>
 * </ul>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class ICopyTests {

    /**
     * 对应测试用例 1.1：Source → Target 新实例，name/age 正确
     */
    @Test
    public void copyToClass() {

        Source source = new Source("tom", 20);
        Target target = source.copyTo(Target.class);

        Assertions.assertEquals("tom", target.getName());
        Assertions.assertEquals(20, target.getAge());

    }

    /**
     * 对应测试用例 2.1：Source → 已有 Target 实例，name/age 正确
     */
    @Test
    public void copyToInstance() {

        Source source = new Source("tom", 20);
        Target target = new Target();
        source.copyTo(target);

        Assertions.assertEquals("tom", target.getName());
        Assertions.assertEquals(20, target.getAge());

    }

    /**
     * 源对象
     */
    @Getter
    @RequiredArgsConstructor
    public static class Source implements ICopy {

        private final String name;
        private final Integer age;

    }

    /**
     * 目标对象
     */
    @Data
    public static class Target {

        private String name;
        private Integer age;

    }

}
