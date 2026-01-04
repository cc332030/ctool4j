package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.DES;
import lombok.experimental.UtilityClass;

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
        return CBase64Utils.encode(encrypt(key,plainText));
    }

    public String encryptStr62(String key, String plainText) {
        return CBase62Utils.encode(encrypt(key,plainText));
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
        return decrypt(key, CBase64Utils.decode(cipherText));
    }

    public String decryptStr62(String key, String cipherText) {
        return decrypt(key, CBase62Utils.decode(cipherText));
    }

}
