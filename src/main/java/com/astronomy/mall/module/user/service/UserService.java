package com.astronomy.mall.module.user.service;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.user.dto.*;
import com.astronomy.mall.module.user.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    Long register(UserRegisterDTO registerDTO);

    /**
     * 用户登录
     */
    LoginVO login(UserLoginDTO loginDTO, String ipAddress, String device);

    /**
     * 获取当前用户信息
     */
    UserVO getUserInfo(Long userId);

    /**
     * 更新用户信息
     */
    boolean updateUserInfo(Long userId, UserInfoDTO userInfoDTO);

    /**
     * 修改密码 (旧接口，保留兼容)
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 修改密码 (新接口 v7.7)
     * 校验: 旧密码正确 + 两次新密码一致 + 新密码不能与旧密码相同
     *
     * @param userId 当前登录用户ID
     * @param dto    ChangePasswordDTO (oldPassword/newPassword/confirmPassword)
     * @throws BusinessException 旧密码不正确 / 两次新密码不一致 / 新旧密码相同
     */
    void changePasswordSecure(Long userId, ChangePasswordDTO dto);

    /**
     * 根据用户名查询用户
     */
    User getUserByUsername(String username);
}