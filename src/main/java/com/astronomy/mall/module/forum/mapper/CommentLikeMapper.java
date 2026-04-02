package com.astronomy.mall.module.forum.mapper;

import com.astronomy.mall.module.forum.entity.CommentLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论点赞 Mapper
 *
 * 继承 BaseMapper<CommentLike> 获得通用 CRUD
 * 通过 LambdaQueryWrapper 查询，无需自定义XML
 *
 * 📌 与 PostLikeMapper 同模式:
 *   INSERT = 点赞, DELETE = 取消点赞
 *   selectCount 判断是否已点赞
 */
@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {

}
