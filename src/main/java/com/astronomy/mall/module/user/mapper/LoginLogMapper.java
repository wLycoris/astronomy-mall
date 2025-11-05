package com.astronomy.mall.module.user.mapper;

import com.astronomy.mall.module.user.entity.LoginLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志Mapper接口
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {
}