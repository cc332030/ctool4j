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

    /**
     * 获取指定 key 的 DES 实例（带缓存）
     *
     * @param key DES key
     * @return DES 实例
     */
    public DES getDES(String key){
        return DES_MAP.computeIfAbsent(key,
            k -> SecureUtil.des(k.getBytes()));
    }

    /**
     * DES 加密
     *
     * @param key       DES key
     * @param plainText 明文
     * @return 密文字节数组，key 或明文为空时返回 null
     */
    public byte[] encrypt(String key, String plainText) {
        if(StrUtil.isBlank(key)
            || StrUtil.isBlank(plainText)
        ){
            return null;
        }
        return getDES(key).encrypt(plainText);
    }

    /**
     * DES 加密，结果为 Base64 字符串
     *
     * @param key       DES key
     * @param plainText 明文
     * @return Base64 密文，key 或明文为空时返回 null
     */
    public String encryptStr64(String key, String plainText) {
        return CBase64Utils.encode(encrypt(key,plainText));
    }

    /**
     * DES 加密，结果为 Base62 字符串
     *
     * @param key       DES key
     * @param plainText 明文
     * @return Base62 密文，key 或明文为空时返回 null
     */
    public String encryptStr62(String key, String plainText) {
        return CBase62Utils.encode(encrypt(key,plainText));
    }

    /**
     * DES 解密
     *
     * @param key         DES key
     * @param cipherBytes 密文字节数组
     * @return 明文，key 或密文为空时返回 null
     */
    public String decrypt(String key, byte[] cipherBytes) {
        if(StrUtil.isBlank(key)
            || ArrayUtil.isEmpty(cipherBytes)
        ){
            return null;
        }
        return getDES(key).decryptStr(cipherBytes);
    }

    /**
     * DES 解密 Base64 密文
     *
     * @param key        DES key
     * @param cipherText Base64 密文
     * @return 明文，key 或密文为空时返回 null
     */
    public String decryptStr64(String key, String cipherText) {
        return decrypt(key, CBase64Utils.decode(cipherText));
    }

    /**
     * DES 解密 Base62 密文
     *
     * @param key        DES key
     * @param cipherText Base62 密文
     * @return 明文，key 或密文为空时返回 null
     */
    public String decryptStr62(String key, String cipherText) {
        return decrypt(key, CBase62Utils.decode(cipherText));
    }

}
