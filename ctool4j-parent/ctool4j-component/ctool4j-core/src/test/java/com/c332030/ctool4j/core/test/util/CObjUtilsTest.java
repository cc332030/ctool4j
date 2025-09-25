package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CObjUtils;
import lombok.CustomLog;
import lombok.val;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CObjUtilsTest
 * </p>
 *
 * @since 2025/9/25
 */
@CustomLog
public class CObjUtilsTest {

    @Test
    public void convert() {

        val value = "999";
        val result = CObjUtils.convert(value, String.class);

        log.info("value hashCode: {}, result hashCode: {}", value.hashCode(), result.hashCode());

    }

}
