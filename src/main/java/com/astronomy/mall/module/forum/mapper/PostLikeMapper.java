package com.astronomy.mall.module.forum.mapper;

import com.astronomy.mall.module.forum.entity.PostLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子点赞 Mapper
 *
 * 继承 BaseMapper<PostLike> 获得通用 CRUD
 * 主要通过 LambdaQueryWrapper 查询，无需自定义XML
 */
@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {

}
