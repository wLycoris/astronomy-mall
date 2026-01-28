package com.astronomy.mall.module.admin.service.impl;

import com.astronomy.mall.module.admin.entity.AdminLogEntity;
import com.astronomy.mall.module.admin.mapper.AdminLogMapper;
import com.astronomy.mall.module.admin.service.AdminLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 管理员日志服务实现
 */
@Service
public class AdminLogServiceImpl extends ServiceImpl<AdminLogMapper, AdminLogEntity> implements AdminLogService {
}