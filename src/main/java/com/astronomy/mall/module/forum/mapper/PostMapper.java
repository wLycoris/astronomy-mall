package com.astronomy.mall.module.forum.mapper;

import com.astronomy.mall.module.forum.entity.Post;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子 Mapper
 *
 * 继承 BaseMapper<Post> 获得通用 CRUD
 *
 * 📌 自定义XML方法（后续7.3/7.6实现时添加）:
 *   - selectPostPage: 帖子分页列表（含作者信息/isLiked/isCollected）
 *   - selectPostDetail: 帖子详情（含作者信息/互动状态）
 *
 * XML位置: resources/mapper/forum/PostMapper.xml（后续需要时创建）
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {

}
