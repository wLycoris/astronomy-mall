package com.astronomy.mall.interceptor;

import cn.hutool.core.util.StrUtil;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.ResultCode;
import com.astronomy.mall.utils.JwtUtil;
import com.astronomy.mall.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * JWT拦截器
 * 用于验证用户登录状态
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 不需要登录就能访问的接口(白名单)
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            // 用户相关
            "/api/user/register",
            "/api/user/login",

            // 商品相关 (浏览商品不需要登录)
            "/api/category/tree",
            "/api/category/first-level",
            "/api/category/children",
            "/api/product/list",
            "/api/product/detail",
            "/api/product/recommend",
            "/api/product/hot",
            "/api/product/new",
            "/api/review/list",
            "/api/review/statistics",

            // Knife4j文档
            "/doc.html",
            "/swagger-resources",
            "/v3/api-docs",
            "/webjars"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();

        // 1. 检查是否在白名单中
        if (isWhiteList(requestURI)) {
            return true;
        }

        // 2. 获取请求头中的token
        String token = request.getHeader("Authorization");

        // 3. 如果token为空,抛出未登录异常
        if (StrUtil.isBlank(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 4. 去除 "Bearer " 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 5. 验证token
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                throw new BusinessException(ResultCode.TOKEN_INVALID);
            }

            // 检查token是否过期
            if (jwtUtil.isTokenExpired(token)) {
                throw new BusinessException(ResultCode.TOKEN_EXPIRED);
            }

            // 🔥 关键修改：同时设置到 request 和 ThreadLocal
            request.setAttribute("userId", userId);
            UserContext.setUserId(userId); // ✅ 添加这行代码

            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
    }

    /**
     * 🔥 新增方法：请求完成后清理 ThreadLocal
     * 防止内存泄漏
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear(); // ✅ 清理 ThreadLocal
    }

    /**
     * 判断请求路径是否在白名单中
     */
    private boolean isWhiteList(String requestURI) {
        for (String pattern : WHITE_LIST) {
            if (requestURI.startsWith(pattern)) {
                return true;
            }
        }
        return false;
    }
}