package com.astronomy.mall.module.user.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.user.dto.*;
import com.astronomy.mall.module.user.service.UserService;
import com.astronomy.mall.utils.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@Api(tags = "用户管理")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result<Long> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        Long userId = userService.register(registerDTO);
        return Result.success("注册成功", userId);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO loginDTO,
                                 HttpServletRequest request) {
        String ipAddress = getIpAddress(request);
        String device = request.getHeader("User-Agent");

        LoginVO loginVO = userService.login(loginDTO, ipAddress, device);
        return Result.success("登录成功", loginVO);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    @ApiOperation("获取当前用户信息")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    @ApiOperation("更新用户信息")
    public Result<String> updateUserInfo(@Valid @RequestBody UserInfoDTO userInfoDTO,
                                         HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        userService.updateUserInfo(userId, userInfoDTO);
        return Result.success("更新成功");
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    @ApiOperation("修改密码")
    public Result<String> changePassword(@RequestParam String oldPassword,
                                         @RequestParam String newPassword,
                                         HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        userService.changePassword(userId, oldPassword, newPassword);
        return Result.success("密码修改成功");
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @ApiOperation("用户登出")
    public Result<String> logout() {
        // JWT是无状态的,前端删除token即可
        // 这里可以记录登出日志
        return Result.success("登出成功");
    }

    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    /**
     * 获取IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}