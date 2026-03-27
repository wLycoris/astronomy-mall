package com.astronomy.mall.module.forum.mapper;

import com.astronomy.mall.module.forum.entity.PostComment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子评论 Mapper
 *
 * 继承 BaseMapper<PostComment> 获得通用 CRUD
 *
 * 📌 自定义XML方法（后续7.4实现时添加）:
 *   - selectCommentsByPostId: 查帖子的两级评论（顶级+子评论）
 *
 * XML位置: resources/mapper/forum/PostCommentMapper.xml（后续需要时创建）
 */
@Mapper
public interface PostCommentMapper extends BaseMapper<PostComment> {

}
