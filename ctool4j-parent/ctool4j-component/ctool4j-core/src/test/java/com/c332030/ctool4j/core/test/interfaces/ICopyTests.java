package com.c332030.ctool4j.core.test.interfaces;

import com.c332030.ctool4j.core.interfaces.ICopy;
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

    @Test
    public void copyToClass() {

        Source source = new Source("tom", 20);
        Target target = source.copyTo(Target.class);

        Assertions.assertEquals("tom", target.getName());
        Assertions.assertEquals(20, target.getAge());

    }

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
    public static class Source implements ICopy {

        private final String name;
        private final Integer age;

        Source(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public Integer getAge() {
            return age;
        }

    }

    /**
     * 目标对象
     */
    public static class Target {

        private String name;
        private Integer age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

    }

}
