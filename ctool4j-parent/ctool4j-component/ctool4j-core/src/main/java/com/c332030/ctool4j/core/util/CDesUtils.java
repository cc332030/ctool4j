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
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code encryptStr64} / {@code encryptStr62}：加密后分别编码为 Base64 / Base62 字符串</li>
 *   <li>{@code decryptStr64} / {@code decryptStr62}：分别解密 Base64 / Base62 密文</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>encrypt：key 或明文为空白</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>decrypt：key 或密文为空</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>加密字符串入口：key/明文空白</td>
 *     <td>返回 null（经 encrypt 前置判断）</td>
 *   </tr>
 *   <tr>
 *     <td>解密字符串入口：key/密文空白</td>
 *     <td>返回 null（经 decrypt 前置判断）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要对短文本做对称加解密（如 ID、敏感字段的传输加密）。</li>
 *   <li>需要 Base64 / Base62 文本形态的密文（便于存储/传输）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>DES 密钥较短（56 位有效密钥），安全性弱于 AES；不适合高强度敏感数据加密。</li>
 *   <li>采用 ECB/默认模式，未显式指定 IV/填充；跨系统互操作时需对齐 hutool 默认行为。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>DES 实例按 key 缓存，key 理论上静态不可变，风险低。</li>
 *   <li>空入参返回 null，与"加密为空"语义一致；调用方需注意对 null 结果的判空。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>DES 实例缓存（DES_MAP）</b></p>
 * <ul>
 *   <li>按 key 用 {@code ConcurrentHashMap.computeIfAbsent} 缓存 DES 实例，避免重复创建。</li>
 *   <li>实例构建：{@code SecureUtil.des(key.getBytes(CCharsets.UTF_8))}，key 使用全局统一 UTF-8 编码。</li>
 * </ul>
 * <p><b>空入参约定</b></p>
 * <ul>
 *   <li>key 或明文/密文为空时统一返回 null，而非抛异常：</li>
 *   <li>{@code encrypt}：key 或 plainText 空白 → null</li>
 *   <li>{@code decrypt}：key 或 cipherBytes 空 → null</li>
 *   <li>字符串加解密通过 {@code CBase64Utils}/{@code CBase62Utils} 编解码，空入参语义由底层工具类保持一致（返回 null）。</li>
 * </ul>
 * <p><b>加解密语义</b></p>
 * <ul>
 *   <li>{@code encrypt} 返回 hutool {@code DES.encrypt(plainText)} 的密文字节数组。</li>
 *   <li>{@code decrypt} 委托 {@code DES.decryptStr(cipherBytes)} 还原明文。</li>
 * </ul>
 *
 * @since 2026/1/4
 * @version 1.0
 */
@UtilityClass
public class CDesUtils {

    final Map<String, DES> DES_MAP = new ConcurrentHashMap<>();

    /**
     * 获取指定 key 的 DES 实例（带缓存）
     * <ul>
     *   <li>{@code getDES(key)}：获取指定 key 的 DES 实例（带缓存）</li>
     * </ul>
     *
     * @param key DES key
     * @return DES 实例
     */
    public DES getDES(String key){
        return DES_MAP.computeIfAbsent(key,
            k -> SecureUtil.des(k.getBytes(CCharsets.UTF_8)));
    }

    /**
     * DES 加密
     * <ul>
     *   <li>{@code encrypt(key, plainText)}：加密为密文字节数组</li>
     *   <li>{@code encryptStr64} = {@code CBase64Utils.encode(encrypt(key, plainText))}，{@code decryptStr64} 为其逆操作，其余同理。</li>
     * </ul>
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
     * <ul>
     *   <li>{@code decrypt(key, cipherBytes)}：解密密文字节数组为明文</li>
     * </ul>
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
