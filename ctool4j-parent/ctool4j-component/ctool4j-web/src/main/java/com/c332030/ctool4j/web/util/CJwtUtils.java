package com.c332030.ctool4j.web.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWTUtil;
import com.c332030.ctool4j.core.classes.CBeanUtils;
import com.c332030.ctool4j.core.util.CArrUtils;
import com.c332030.ctool4j.core.util.CCharsets;
import com.c332030.ctool4j.core.util.CJsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Map;

/**
 * <p>
 * Description: CJwtUtils
 * </p>
 *
 * <p>JWT 工具类，基于 hutool JWTUtil，提供 jwt 创建、验证、解析等纯编解码能力，不感知配置与业务类型。
 * 密钥由调用方显式传入；依赖配置（{@code CAuthConfig}）与业务载荷接口（{@code ICJwtInfo}）的封装见 ctool4j-auth-base 的
 * {@code CAuthUtils}。</p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CJwtUtils}（{@code @UtilityClass}）提供 jwt 的纯编解码能力，密钥由调用方显式传入：</p>
 * <ul>
 *   <li>{@code getJson(String[], int)} / {@code getHeaderJson(String)} / {@code getBodyJson(String)}：取并 base64 解码指定段</li>
 *   <li>{@code parseHeader(String, Class)} / {@code parseBody(String, Class)} / {@code parseBody(String, TypeReference)}：解析为指定类型</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>create / verify：secret 空白</td>
 *     <td>抛 IllegalArgumentException（快速失败）</td>
 *   </tr>
 *   <tr>
 *     <td>verify：jwt 为 null / 空</td>
 *     <td>返回 false</td>
 *   </tr>
 *   <tr>
 *     <td>verify：jwt 格式非法</td>
 *     <td>抛 JWTException（不吞异常，由调用方决定是否容错）</td>
 *   </tr>
 *   <tr>
 *     <td>parseJwt / getJson：输入为空或无对应段</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>getHeaderJson / getBodyJson：jwt 为空或对应段为空</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>parseHeader / parseBody：头段 / 载荷段为空</td>
 *     <td>返回 null</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要自行管理密钥的 jwt 编解码场景。</li>
 *   <li>作为上层封装（{@code CAuthUtils}）的底层实现。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要读取配置、绑定业务载荷的认证场景请用 {@code CAuthUtils}。</li>
 *   <li>本类不做签名算法选择、不校验过期时间（仅 {@code verify} 签名）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>{@code verify} 仅校验签名，不做过期校验与声明校验，需由调用方自行处理。</li>
 *   <li>jwt 格式非法时 {@code verify} 抛 {@code JWTException} 而非返回 false，属刻意取舍（异常与「签名不匹配」语义不同）。</li>
 *   <li>{@code parseJwt} 对纯空白串走 split 返回非空数组，与 null / 空串的返回 null 行为不同（已知边界）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>密钥显式传入</b></p>
 * <ul>
 *   <li>本类不读取任何配置，密钥完全由调用方传入，保证可单测、可复用（不绑定 Spring 上下文）。</li>
 * </ul>
 * <p><b>快速失败</b></p>
 * <ul>
 *   <li>{@code create} / {@code verify} 在进入底层库前先校验 secret 非空白，避免底层库对空密钥的隐式行为。</li>
 *   <li>secret 为空白（null / 空串 / 全空白）时抛 {@code IllegalArgumentException}。</li>
 * </ul>
 * <p><b>空值不依赖底层库</b></p>
 * <ul>
 *   <li>{@code verify} 对 null / 空 jwt 直接返回 false，不依赖底层库抛错行为。</li>
 *   <li>{@code parseJwt} / {@code getJson} / {@code getHeaderJson} / {@code getBodyJson} 对空输入或无对应段返回 null。</li>
 * </ul>
 *
 * @author c332030
 * @since 2025/9/25
 * @version 1.0
 */
@UtilityClass
public class CJwtUtils {

    /**
     * 创建 jwt
     * <ul>
     *   <li>{@link #create(Object, String)} / {@link #create(Map, String)}：创建 jwt（secret 空白快速失败）；</li>
     *   <li>{@code create(Object, String)} / {@code create(Map, String)}：创建 jwt</li>
     * </ul>
     *
     * @param body body
     * @param secret 密钥
     * @return jwt
     */
    public String create(Object body, String secret) {
        return create(
            CBeanUtils.toMap(body),
            secret
        );
    }

    /**
     * 创建 jwt
     *
     * @param body   body
     * @param secret 密钥，不能为空白
     * @return jwt
     * @throws IllegalArgumentException secret 为空白时抛出
     */
    public String create(Map<String, Object> body, String secret) {
        Assert.isTrue(StrUtil.isNotBlank(secret), "secret must not be blank");
        return JWTUtil.createToken(
            body,
            secret.getBytes(CCharsets.UTF_8)
        );
    }

    /**
     * 验证
     *
     * <p>格式非法（如段数不足、非 jwt 串）时不返回 false，而是由底层 hutool 抛 {@code JWTException}；
     * 调用方若需容错应自行捕获（参见 {@code CAuthUtils#getTokenByJwt}）。空 jwt 走短路返回 false。</p>
     * <ul>
     *   <li>{@link #verify(String, String)}：校验签名（jwt 为空直接返回 false）；</li>
     *   <li>{@code verify(String, String)}：校验签名</li>
     * </ul>
     *
     * @param jwt    jwt，为空时不校验签名，直接返回 false
     * @param secret 密钥，不能为空白
     * @return 验证结果；jwt 为空时返回 false
     * @throws IllegalArgumentException secret 为空白时抛出
     * @throws cn.hutool.jwt.JWTException jwt 格式非法时抛出
     */
    public boolean verify(String jwt, String secret) {
        Assert.isTrue(StrUtil.isNotBlank(secret), "secret must not be blank");
        // jwt 为空视为未认证，直接返回 false，避免依赖底层库对空 jwt 的抛错行为
        if(StrUtil.isEmpty(jwt)) {
            return false;
        }
        return JWTUtil.verify(jwt, secret.getBytes(CCharsets.UTF_8));
    }

    /**
     * 按点拆分 jwt 为头部、载荷、签名三段
     * <ul>
     *   <li>{@link #parseJwt(String)}：按 "." 拆分为三段；</li>
     *   <li>{@code parseJwt(String)}：按 "." 拆分为三段</li>
     * </ul>
     *
     * @param jwt jwt
     * @return 拆分后的三段数组；jwt 为空时返回 null
     */
    public String[] parseJwt(String jwt) {

        if(StrUtil.isEmpty(jwt)) {
            return null;
        }

        return jwt.split("\\.");
    }

    /**
     * 解码指定索引段的 Base64 内容
     *
     * @param arr   拆分后的 jwt 段数组
     * @param index 段索引
     * @return 解码后的 JSON 字符串；无对应段时返回 null
     */
    public String getJson(String[] arr, int index) {

        if(ArrayUtil.isEmpty(arr)) {
            return null;
        }

        val str = CArrUtils.get(arr, index);
        if(StrUtil.isEmpty(str)) {
            return null;
        }

        return Base64.decodeStr(str);
    }

    /**
     * 获取 jwt 头部 JSON
     *
     * @param jwt jwt
     * @return 头部 JSON 字符串
     */
    public String getHeaderJson(String jwt) {
        return getHeaderJson(parseJwt(jwt));
    }

    /**
     * 解析 jwt 头部为指定类型
     *
     * <p>注意：仅 base64 解码，不校验签名，返回内容未认证不可信；需要认证时请先调用 {@link #verify(String, String)}</p>
     *
     * @param jwt   jwt
     * @param clazz 目标类型
     * @param <T>   目标类型
     * @return 解析结果；头部为空时返回 null
     */
    public <T> T parseHeader(String jwt, Class<T> clazz) {

        val json = getHeaderJson(jwt);
        if(StrUtil.isEmpty(json)) {
            return null;
        }
        return CJsonUtils.fromJson(json, clazz);
    }

    /**
     * 获取 jwt 头部 JSON
     *
     * @param arr 拆分后的 jwt 段数组
     * @return 头部 JSON 字符串
     */
    public String getHeaderJson(String[] arr) {
        return getJson(arr, 0);
    }

    /**
     * 获取 jwt 载荷 JSON
     *
     * @param jwt jwt
     * @return 载荷 JSON 字符串
     */
    public String getBodyJson(String jwt) {
        return getBodyJson(parseJwt(jwt));
    }

    /**
     * 获取 jwt 载荷 JSON
     *
     * @param arr 拆分后的 jwt 段数组
     * @return 载荷 JSON 字符串
     */
    public String getBodyJson(String[] arr) {
        return getJson(arr, 1);
    }

    /**
     * 解析 jwt 载荷为指定类型
     *
     * <p>注意：仅 base64 解码，不校验签名，返回内容未认证不可信；需要认证时请先调用 {@link #verify(String, String)}</p>
     *
     * @param jwt   jwt
     * @param clazz 目标类型
     * @param <T>   目标类型
     * @return 解析结果；载荷为空时返回 null
     */
    public <T> T parseBody(String jwt, Class<T> clazz) {

        val json = getBodyJson(jwt);
        if(StrUtil.isEmpty(json)) {
            return null;
        }
        return CJsonUtils.fromJson(json, clazz);
    }

    /**
     * 解析 jwt 载荷为指定类型
     *
     * <p>注意：仅 base64 解码，不校验签名，返回内容未认证不可信；需要认证时请先调用 {@link #verify(String, String)}</p>
     *
     * @param jwt   jwt
     * @param typeReference 目标类型
     * @param <T>   目标类型
     * @return 解析结果；载荷为空时返回 null
     */
    public <T> T parseBody(String jwt, TypeReference<T> typeReference) {

        val json = getBodyJson(jwt);
        if(StrUtil.isEmpty(json)) {
            return null;
        }
        return CJsonUtils.fromJson(json, typeReference);
    }

}
