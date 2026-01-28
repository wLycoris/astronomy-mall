package com.astronomy.mall.module.user.service;

import com.astronomy.mall.module.user.dto.*;
import com.astronomy.mall.module.user.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 注册成功的用户ID
     */
    Long register(UserRegisterDTO registerDTO);

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @param ipAddress 登录IP
     * @param device 登录设备
     * @return 登录结果(包含token和用户信息)
     */
    LoginVO login(UserLoginDTO loginDTO, String ipAddress, String device);

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息VO
     */
    UserVO getUserInfo(Long userId);

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param userInfoDTO 更新的信息
     * @return true-成功, false-失败
     */
    boolean updateUserInfo(Long userId, UserInfoDTO userInfoDTO);

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return true-成功, false-失败
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    User getUserByUsername(String username);
}