package com.c332030.ctool4j.core.util;

import cn.hutool.core.codec.Base62;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

/**
 * <p>
 * Description: CBase64Utils
 * </p>
 *
 * @since 2026/1/4
 */
@UtilityClass
public class CBase62Utils {

    public String encode(byte[] bytes) {

        if(ArrayUtil.isEmpty(bytes)) {
            return null;
        }
        return Base62.encode(bytes);
    }

    public byte[] decode(String value) {
        if(StrUtil.isEmpty(value)) {
            return null;
        }
        return Base62.decode(value);
    }

}
