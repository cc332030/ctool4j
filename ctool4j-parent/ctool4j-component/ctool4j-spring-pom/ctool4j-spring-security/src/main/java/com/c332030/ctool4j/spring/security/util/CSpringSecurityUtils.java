package com.c332030.ctool4j.spring.security.util;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.definition.model.result.impl.CStrResult;
import com.c332030.ctool4j.web.util.CServletUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: CSpringSecurityUtils
 * </p>
 *
 * @since 2026/1/23
 */
@UtilityClass
public class CSpringSecurityUtils {

    /**
     * 获取安全上下文
     *
     * @return 安全上下文
     */
    public SecurityContext getContext() {
        return SecurityContextHolder.getContext();
    }

    /**
     * 获取当前认证信息
     *
     * @return 当前认证信息
     */
    public Authentication getAuthentication() {
        return getContext().getAuthentication();
    }

    /**
     * 设置当前认证信息
     *
     * @param authentication 认证信息
     */
    public void setAuthentication(Authentication authentication) {
        getContext().setAuthentication(authentication);
    }

    /**
     * 获取当前主体
     *
     * @param <T> 主体类型
     * @return 当前主体；未认证时返回 null
     */
    public <T> T getPrincipal() {

        val authentication = getAuthentication();
        if(authentication == null) {
            return null;
        }

        return CObjUtils.anyType(authentication.getPrincipal());
    }

    /**
     * 获取当前用户详情
     *
     * @param <T> 用户详情类型
     * @return 当前用户详情
     */
    public <T extends UserDetails> T getUserDetails() {
        // 显式 (Object) 强转，避免泛型 T（擦除为 Object）匹配到 anyType(CSupplier) 重载
        return CObjUtils.anyType((Object) getPrincipal());
    }

    /**
     * 以 JSON 形式输出认证错误
     *
     * @param httpStatus HTTP 状态码
     * @param request    请求
     * @param response   响应
     */
    public void writeJsonError(
        HttpStatus httpStatus,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        writeJsonError(httpStatus, null, request, response);
    }

    /**
     * 以 JSON 形式输出认证错误，可指定错误信息
     *
     * @param httpStatus HTTP 状态码
     * @param message    错误信息，为空时取状态码默认文案
     * @param request    请求
     * @param response   响应
     */
    public void writeJsonError(
        HttpStatus httpStatus,
        String message,
        HttpServletRequest request,
        HttpServletResponse response
    ) {

        val requestUrl = request.getRequestURI();

        message = StrUtil.blankToDefault(message, httpStatus.getReasonPhrase());
        val forbiddenResult = CStrResult.error(
            String.valueOf(httpStatus.value()),
            message + "：" + requestUrl
        );

        CServletUtils.writeJson(response, httpStatus, forbiddenResult);
    }

}
