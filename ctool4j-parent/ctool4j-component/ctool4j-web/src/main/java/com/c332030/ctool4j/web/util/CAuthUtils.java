package com.c332030.ctool4j.web.util;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * Description: CAuthUtils
 * </p>
 *
 * @since 2026/3/16
 */
@UtilityClass
public class CAuthUtils {

    public String TOKEN_PREFIX = "Bearer";

    public String getToken() {
        return getToken(TOKEN_PREFIX);
    }

    public String getToken(String prefix) {
        return getToken(CRequestUtils.getRequest(), prefix);
    }

    public String getToken(HttpServletRequest request) {
        return getToken(request, TOKEN_PREFIX);
    }

    public String getToken(HttpServletRequest request, String prefix) {

        val authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(StrUtil.isEmpty(authorization)
            || !authorization.startsWith(prefix)
        ) {
            return null;
        }

        return authorization.substring(prefix.length() + 1);

    }

}
