package com.c332030.ctool4j.auth.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWTUtil;
import com.c332030.ctool4j.auth.config.CAuthConfig;
import com.c332030.ctool4j.auth.interfaces.ICJwtInfo;
import com.c332030.ctool4j.core.classes.CBeanUtils;
import com.c332030.ctool4j.core.util.CArrUtils;
import com.c332030.ctool4j.core.util.CCharsets;
import com.c332030.ctool4j.core.util.CJsonUtils;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Map;

/**
 * <p>
 * Description: CJwtUtils
 * </p>
 *
 * <p>JWT 工具类，基于 hutool JWTUtil，提供 jwt 创建、验证、解析，以及为 {@link ICJwtInfo}
 * 生成并回填 jwt（{@link #setJwt}，密钥取自配置 {@link CAuthConfig#getJwtSecret()}）。</p>
 *
 * <p>注意：{@code parseHeader}/{@code parseBody} 仅 base64 解码、不校验签名，内容未认证不可信，
 * 需认证时先调用 {@link #verify(String, String)}。</p>
 *
 * @author c332030
 * @since 2025/9/25
 */
@UtilityClass
@CAutowiredScan
public class CJwtUtils {

    @Setter
    @CAutowired
    CAuthConfig authConfig;

    /**
     * 创建 jwt
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
     * 为 jwt 信息对象生成并设置 jwt（密钥取自配置 {@code CAuthConfig#jwtSecret}）
     *
     * @param jwtInfo jwt 信息对象（以其内容为载荷，生成后回填 token）
     * @param <S>     jwt 信息类型
     * @return 传入的 jwtInfo（token 已设置）
     */
    public <S extends ICJwtInfo> S setJwt(S jwtInfo) {
        jwtInfo.setToken(
            create(jwtInfo, authConfig.getJwtSecret())
        );
        return jwtInfo;
    }

    /**
     * 验证
     *
     * @param jwt    jwt，为空时不校验签名，直接返回 false
     * @param secret 密钥，不能为空白
     * @return 验证结果；jwt 为空时返回 false
     * @throws IllegalArgumentException secret 为空白时抛出
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
