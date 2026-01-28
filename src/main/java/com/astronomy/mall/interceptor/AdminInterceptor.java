package com.astronomy.mall.interceptor;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.ResultCode;
import com.astronomy.mall.module.user.entity.User;
import com.astronomy.mall.module.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 管理员权限拦截器
 *
 * 📌 功能说明:
 * 1. 拦截所有 /api/admin/* 请求
 * 2. 验证用户是否为管理员 (role=1)
 * 3. 将管理员信息存入request,供后续使用
 *
 * 📌 执行顺序:
 * JwtInterceptor (验证登录) → AdminInterceptor (验证管理员权限)
 *
 * 📌 配置说明:
 * 需在 WebMvcConfig 中注册,拦截路径: /api/admin/**
 */
@Slf4j
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        log.info("=== AdminInterceptor 执行 ===");
        log.info("请求路径: {}", request.getRequestURI());

        // 1. 从request中获取userId (JwtInterceptor已存入)
        Long userId = (Long) request.getAttribute("userId");

        if (userId == null) {
            log.error("userId为null,可能JwtInterceptor未执行或登录失效");
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        log.info("当前用户ID: {}", userId);

        // 2. 查询用户信息
        User user = userService.getById(userId);

        if (user == null) {
            log.error("用户不存在,userId: {}", userId);
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 3. 验证是否为管理员 (role=1)
        if (user.getRole() == null || user.getRole() != 1) {
            log.error("用户无管理员权限,userId: {}, role: {}", userId, user.getRole());
            throw new BusinessException(ResultCode.ADMIN_NO_PERMISSION);
        }

        log.info("管理员权限验证通过,用户名: {}", user.getUsername());

        // 4. 将管理员信息存入request,供后续使用
        request.setAttribute("adminId", userId);
        request.setAttribute("adminName", user.getUsername());

        return true;
    }
}