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
 * @since 2025/12/12
 */
public class ICopyTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void copyToClass() {

        Source source = new Source("tom", 20);
        Target target = source.copyTo(Target.class);

        Assertions.assertEquals("tom", target.getName());
        Assertions.assertEquals(20, target.getAge());

    }

    /**
     * 对应测试用例 2.1
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
