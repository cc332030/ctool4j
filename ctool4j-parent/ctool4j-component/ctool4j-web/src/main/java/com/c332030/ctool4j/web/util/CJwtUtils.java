package com.c332030.ctool4j.web.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWTUtil;
import com.c332030.ctool4j.core.classes.CBeanUtils;
import com.c332030.ctool4j.core.util.CArrUtils;
import com.c332030.ctool4j.core.util.CJsonUtils;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Map;

/**
 * <p>
 * Description: CJwtUtils
 * </p>
 *
 * @since 2025/9/25
 */
@UtilityClass
public class CJwtUtils {

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
     * @param body body
     * @param secret 密钥
     * @return jwt
     */
    public String create(Map<String, Object> body, String secret) {
        return JWTUtil.createToken(
            body,
            secret.getBytes()
        );
    }

    /**
     * 验证
     * @param jwt jwt
     * @param secret 密钥
     * @return 验证结果
     */
    public boolean verify(String jwt, String secret) {
        return JWTUtil.verify(jwt, secret.getBytes());
    }

    public String[] parseJwt(String jwt) {

        if(StrUtil.isEmpty(jwt)) {
            return null;
        }

        return jwt.split("\\.");
    }

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

    public String getHeaderJson(String jwt) {
        return getHeaderJson(parseJwt(jwt));
    }

    public <T> T parseHeader(String jwt, Class<T> clazz) {

        val json = getHeaderJson(jwt);
        if(StrUtil.isEmpty(json)) {
            return null;
        }
        return CJsonUtils.fromJson(json, clazz);
    }

    public String getHeaderJson(String[] arr) {
        return getJson(arr, 0);
    }

    public String getBodyJson(String jwt) {
        return getBodyJson(parseJwt(jwt));
    }

    public String getBodyJson(String[] arr) {
        return getJson(arr, 1);
    }

    public <T> T parseBody(String jwt, Class<T> clazz) {

        val json = getBodyJson(jwt);
        if(StrUtil.isEmpty(json)) {
            return null;
        }
        return CJsonUtils.fromJson(json, clazz);
    }

}
