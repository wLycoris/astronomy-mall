package com.astronomy.mall.module.forum.mapper;

import com.astronomy.mall.module.forum.entity.Post;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 帖子 Mapper
 *
 * 继承 BaseMapper<Post> 获得通用 CRUD
 *
 * 📌 自定义XML方法（7.3实现）:
 *   - selectPostList:  帖子分页列表（JOIN tb_user获取作者信息，tab分流排序）
 *   - countPostList:   帖子列表总数（配合分页）
 *   - selectPostDetail: 帖子详情（JOIN tb_user获取作者信息）
 *
 * XML位置: resources/mapper/PostMapper.xml
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 帖子分页列表（含作者信息）
     *
     * @param tab           标签: all/follow/hot
     * @param tag           分类标签筛选（可为null）
     * @param currentUserId 当前登录用户ID（follow模式需要）
     * @param offset        分页偏移量
     * @param pageSize      每页条数
     * @return 帖子列表（Map形式，Service层转PostVO）
     */
    List<Map<String, Object>> selectPostList(@Param("tab") String tab,
                                              @Param("tag") String tag,
                                              @Param("currentUserId") Long currentUserId,
                                              @Param("offset") int offset,
                                              @Param("pageSize") int pageSize);

    /**
     * 帖子列表总数
     */
    long countPostList(@Param("tab") String tab,
                       @Param("tag") String tag,
                       @Param("currentUserId") Long currentUserId);

    /**
     * 帖子详情（含作者信息）
     *
     * @param postId 帖子ID
     * @return 帖子详情Map（Service层转PostVO并补充互动状态）
     */
    Map<String, Object> selectPostDetail(@Param("postId") Long postId);

    /**
     * 7.5: 用户收藏的帖子列表（JOIN tb_post_collect）
     *
     * @param userId   用户ID
     * @param offset   分页偏移量
     * @param pageSize 每页条数
     * @return 帖子列表
     */
    List<Map<String, Object>> selectCollectedPosts(@Param("userId") Long userId,
                                                    @Param("offset") int offset,
                                                    @Param("pageSize") int pageSize);

    /**
     * 7.5: 用户收藏的帖子总数
     */
    long countCollectedPosts(@Param("userId") Long userId);

    /**
     * 7.5: 用户点赞的帖子列表（JOIN tb_post_like）
     */
    List<Map<String, Object>> selectLikedPosts(@Param("userId") Long userId,
                                                @Param("offset") int offset,
                                                @Param("pageSize") int pageSize);

    /**
     * 7.5: 用户点赞的帖子总数
     */
    long countLikedPosts(@Param("userId") Long userId);
}
