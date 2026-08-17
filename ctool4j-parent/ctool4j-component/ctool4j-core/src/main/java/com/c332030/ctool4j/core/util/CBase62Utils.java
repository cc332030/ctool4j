package com.c332030.ctool4j.core.util;

import cn.hutool.core.codec.Base62;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

/**
 * <p>
 * Description: CBase62Utils
 * </p>
 *
 * @since 2026/1/4
 */
@UtilityClass
public class CBase62Utils {

    /**
     * 字节数组 Base62 编码
     *
     * @param bytes 字节数组
     * @return Base62 字符串，字节数组为空时返回 null
     */
    public String encode(byte[] bytes) {

        if(ArrayUtil.isEmpty(bytes)) {
            return null;
        }
        return Base62.encode(bytes);
    }

    /**
     * Base62 字符串解码
     *
     * @param value Base62 字符串
     * @return 字节数组，字符串为空时返回 null
     */
    public byte[] decode(String value) {
        if(StrUtil.isEmpty(value)) {
            return null;
        }
        return Base62.decode(value);
    }

}
