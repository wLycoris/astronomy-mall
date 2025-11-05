package com.astronomy.mall.module.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.ResultCode;
import com.astronomy.mall.module.user.dto.*;
import com.astronomy.mall.module.user.entity.LoginLog;
import com.astronomy.mall.module.user.entity.User;
import com.astronomy.mall.module.user.mapper.LoginLogMapper;
import com.astronomy.mall.module.user.mapper.UserMapper;
import com.astronomy.mall.module.user.service.UserService;
import com.astronomy.mall.utils.JwtUtil;
import com.astronomy.mall.utils.MD5Util;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginLogMapper loginLogMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(UserRegisterDTO registerDTO) {
        // 1. 检查用户名是否存在
        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(User::getUsername, registerDTO.getUsername());
        if (userMapper.selectCount(usernameWrapper) > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 2. 检查邮箱是否存在
        if (registerDTO.getEmail() != null && !registerDTO.getEmail().isEmpty()) {
            LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(User::getEmail, registerDTO.getEmail());
            if (userMapper.selectCount(emailWrapper) > 0) {
                throw new BusinessException(ResultCode.EMAIL_EXISTS);
            }
        }

        // 3. 检查手机号是否存在
        if (registerDTO.getPhone() != null && !registerDTO.getPhone().isEmpty()) {
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, registerDTO.getPhone());
            if (userMapper.selectCount(phoneWrapper) > 0) {
                throw new BusinessException(ResultCode.PHONE_EXISTS);
            }
        }

        // 4. 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(MD5Util.encrypt(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname());
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setRole(0); // 默认普通用户
        user.setStatus(1); // 默认启用
        user.setObservationLevel(1); // 默认入门级别

        userMapper.insert(user);

        log.info("用户注册成功: {}", user.getUsername());
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO login(UserLoginDTO loginDTO, String ipAddress, String device) {
        // 1. 查询用户
        User user = getUserByUsername(loginDTO.getUsername());
        if (user == null) {
            // 记录登录失败日志
            saveLoginLog(null, loginDTO.getUsername(), ipAddress, device, 0, "用户不存在");
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 2. 检查用户状态
        if (user.getStatus() == 0) {
            saveLoginLog(user.getId(), user.getUsername(), ipAddress, device, 0, "用户已被禁用");
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 3. 验证密码
        if (!MD5Util.verify(loginDTO.getPassword(), user.getPassword())) {
            saveLoginLog(user.getId(), user.getUsername(), ipAddress, device, 0, "密码错误");
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 4. 生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 5. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 6. 记录登录成功日志
        saveLoginLog(user.getId(), user.getUsername(), ipAddress, device, 1, "登录成功");

        // 7. 组装返回数据
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(convertToVO(user));

        log.info("用户登录成功: {}", user.getUsername());
        return loginVO;
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        return convertToVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserInfo(Long userId, UserInfoDTO userInfoDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 检查邮箱是否被其他用户使用
        if (userInfoDTO.getEmail() != null && !userInfoDTO.getEmail().isEmpty()) {
            LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(User::getEmail, userInfoDTO.getEmail())
                    .ne(User::getId, userId);
            if (userMapper.selectCount(emailWrapper) > 0) {
                throw new BusinessException(ResultCode.EMAIL_EXISTS);
            }
        }

        // 检查手机号是否被其他用户使用
        if (userInfoDTO.getPhone() != null && !userInfoDTO.getPhone().isEmpty()) {
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, userInfoDTO.getPhone())
                    .ne(User::getId, userId);
            if (userMapper.selectCount(phoneWrapper) > 0) {
                throw new BusinessException(ResultCode.PHONE_EXISTS);
            }
        }

        // 更新用户信息
        BeanUtil.copyProperties(userInfoDTO, user, "id", "username", "password", "role", "status");
        userMapper.updateById(user);

        log.info("用户信息更新成功: {}", user.getUsername());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 验证旧密码
        if (!MD5Util.verify(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 更新密码
        user.setPassword(MD5Util.encrypt(newPassword));
        userMapper.updateById(user);

        log.info("用户密码修改成功: {}", user.getUsername());
        return true;
    }

    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    /**
     * 保存登录日志
     */
    private void saveLoginLog(Long userId, String username, String ipAddress,
                              String device, Integer status, String message) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(userId);
        loginLog.setUsername(username);
        loginLog.setIpAddress(ipAddress);
        loginLog.setDevice(device);
        loginLog.setStatus(status);
        loginLog.setMessage(message);
        loginLogMapper.insert(loginLog);
    }

    /**
     * 转换为VO
     */
    private UserVO convertToVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO, "password", "deleted");
        return userVO;
    }
}