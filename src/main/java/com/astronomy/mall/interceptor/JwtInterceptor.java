package com.astronomy.mall.interceptor;

import com.alibaba.fastjson.JSON;
import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.common.result.ResultCode;
import com.astronomy.mall.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT拦截器
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // OPTIONS请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证token
        if (token == null || token.isEmpty()) {
            returnUnauthorized(response, "请先登录");
            return false;
        }

        if (jwtUtil.isTokenExpired(token)) {
            returnUnauthorized(response, "登录已过期,请重新登录");
            return false;
        }

        // token有效,放行
        return true;
    }

    /**
     * 返回未认证响应
     */
    private void returnUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");

        Result<Void> result = Result.error(ResultCode.UNAUTHORIZED.getCode(), message);
        response.getWriter().write(JSON.toJSONString(result));
    }
}