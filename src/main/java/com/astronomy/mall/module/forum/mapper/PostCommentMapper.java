package com.astronomy.mall.module.forum.mapper;

import com.astronomy.mall.module.forum.entity.PostComment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 帖子评论 Mapper
 *
 * 继承 BaseMapper<PostComment> 获得通用 CRUD
 *
 * 📌 自定义XML方法（7.4）:
 *   - selectTopComments:   查帖子的顶级评论（分页，JOIN tb_user）
 *   - countTopComments:    顶级评论总数
 *   - selectChildComments: 查顶级评论下的子回复（JOIN tb_user）
 *
 * XML位置: resources/mapper/PostCommentMapper.xml
 */
@Mapper
public interface PostCommentMapper extends BaseMapper<PostComment> {

    /**
     * 查询帖子的顶级评论（parent_id=0，分页）
     * JOIN tb_user 获取用户昵称和头像
     */
    List<Map<String, Object>> selectTopComments(@Param("postId") Long postId,
                                                @Param("offset") int offset,
                                                @Param("pageSize") int pageSize);

    /**
     * 顶级评论总数
     */
    long countTopComments(@Param("postId") Long postId);

    /**
     * 查询某顶级评论下的所有子回复
     * JOIN tb_user 获取回复者昵称和头像
     */
    List<Map<String, Object>> selectChildComments(@Param("parentId") Long parentId);

    /**
     * 查询用户昵称（用于评论回复时冗余存储被回复人昵称）
     */
    @Select("SELECT nickname FROM tb_user WHERE id = #{userId}")
    String selectNicknameByUserId(@Param("userId") Long userId);
}
