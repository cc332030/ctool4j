package com.c332030.ctool4j.core.util;

import cn.hutool.core.codec.Base62;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.DES;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Description: CDesUtils
 * </p>
 *
 * @since 2026/1/4
 */
@UtilityClass
public class CDesUtils {

    final Map<String, DES> DES_MAP = new ConcurrentHashMap<>();

    public DES getDES(String key){
        return DES_MAP.computeIfAbsent(key,
            k -> SecureUtil.des(k.getBytes()));
    }

    public byte[] encrypt(String key, String plainText) {
        if(StrUtil.isBlank(key)
            || StrUtil.isBlank(plainText)
        ){
            return null;
        }
        return getDES(key).encrypt(plainText);
    }

    public String encryptStr64(String key, String plainText) {
        return Base64.encode(encrypt(key, plainText));
    }

    public String encryptStr62(String key, String plainText) {
        return Base62.encode(encrypt(key, plainText));
    }

    public String decrypt(String key, byte[] cipherBytes) {
        if(StrUtil.isBlank(key)
            || ArrayUtil.isEmpty(cipherBytes)
        ){
            return null;
        }
        return getDES(key).decryptStr(cipherBytes);
    }

    public String decryptStr64(String key, String cipherText) {
        if(StrUtil.isBlank(key)
            || StrUtil.isBlank(cipherText)
        ){
            return null;
        }

        val bytes = Base64.decode(cipherText);
        return decrypt(key, bytes);
    }

    public String decryptStr62(String key, String cipherText) {
        if(StrUtil.isBlank(key)
            || StrUtil.isBlank(cipherText)
        ){
            return null;
        }

        val bytes = Base62.decode(cipherText);
        return decrypt(key, bytes);
    }

}
