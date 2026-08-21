package com.c332030.ctool4j.core.test.classes;

import com.c332030.ctool4j.core.classes.CObjUtils;
import lombok.CustomLog;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CObjUtilsTest
 * </p>
 * <p>`com.c332030.ctool4j.core.classes.CObjUtils`（CObjUtils）的测试用例</p>
 *
 * @since 2025/9/25
 */
@CustomLog
public class CObjUtilsTest {

    /**
     * 测试对象类型转换
     * 对应测试用例 2.1
     */
    @Test
    public void convert() {

        val value = "999";
        val result = CObjUtils.convert(value, String.class);

        Assertions.assertEquals(value.hashCode(), result.hashCode());

    }

}
