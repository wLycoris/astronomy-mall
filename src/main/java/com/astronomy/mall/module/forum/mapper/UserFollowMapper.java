package com.astronomy.mall.module.forum.mapper;

import com.astronomy.mall.module.forum.entity.UserFollow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户关注关系 Mapper
 *
 * 继承 BaseMapper<UserFollow> 获得通用 CRUD
 * 主要通过 LambdaQueryWrapper 查询，无需自定义XML
 *
 * 📌 常用查询:
 *   follower_id = ? → 查"我关注的人"
 *   followed_id = ? → 查"关注我的人"(粉丝)
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

}
