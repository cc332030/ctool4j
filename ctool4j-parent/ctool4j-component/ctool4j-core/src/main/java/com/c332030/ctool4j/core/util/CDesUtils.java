package com.c332030.ctool4j.core.util;

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

    public String encrypt(String key, String plainText) {
        if(StrUtil.isBlank(key)
            || StrUtil.isBlank(plainText)
        ){
            return null;
        }
        return getDES(key).encryptBase64(plainText);
    }

    public String decrypt(String key, String cipherText) {
        if(StrUtil.isBlank(key)
            || StrUtil.isBlank(cipherText)
        ){
            return null;
        }
        return getDES(key).decryptStr(cipherText);
    }

}
