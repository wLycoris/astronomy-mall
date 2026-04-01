package com.astronomy.mall.module.forum.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.forum.dto.PostCommentDTO;
import com.astronomy.mall.module.forum.dto.PostPublishDTO;
import com.astronomy.mall.module.forum.service.CommentService;
import com.astronomy.mall.module.forum.service.FollowService;
import com.astronomy.mall.module.forum.service.PostService;
import com.astronomy.mall.module.forum.service.SearchService;
import com.astronomy.mall.module.forum.vo.PostVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 论坛帖子 Controller（用户端）
 *
 * 接口前缀: /api/post
 *
 * 📌 接口规划（19个，分阶段实现）:
 *
 * ✅ 7.2 帖子发布/编辑/删除 (3个):
 *   POST   /publish           - 发布帖子（auto-approve判断状态）
 *   PUT    /{id}              - 编辑帖子（仅草稿/已拒绝可编辑）
 *   DELETE /{id}              - 删除帖子（@TableLogic逻辑删除）
 *
 * ✅ 7.3 帖子列表+详情 (2个):
 *   GET    /list              - 帖子列表（tab分流+分页，可选认证）
 *   GET    /{id}              - 帖子详情（含互动状态+浏览量+1，可选认证）
 *
 * ⬜ 7.4 评论+互动 (5个):
 *   POST   /comment           - 发布评论/回复
 *   DELETE /comment/{id}      - 删除评论
 *   POST   /comment/like/{id} - 评论点赞
 *   POST   /like/{id}         - 帖子点赞/取消
 *   POST   /collect/{id}      - 帖子收藏/取消
 *
 * ⬜ 7.5 关注+主页 (6个):
 *   POST   /user/follow/{userId}   - 关注/取消关注
 *   GET    /user/follow/list       - 我关注的人
 *   GET    /user/fans/list         - 关注我的人
 *   GET    /my/list                - 我发布的帖子
 *   GET    /my/collect             - 我收藏的帖子
 *   GET    /user/profile/{userId}  - 用户主页信息
 *
 * ⬜ 7.6 搜索 (2个):
 *   GET    /search                 - 搜索帖子/用户
 *   GET    /search/hot             - 热门搜索词
 *
 * ⚠️ @提及功能（扩展，优先级低）:
 *   GET    /user/search            - 用户搜索（@提及下拉候选）
 */
@Slf4j
@RestController
@RequestMapping("/api/post")
@Api(tags = "论坛社区 - 帖子")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private FollowService followService;

    @Autowired
    private SearchService searchService;

    // ==============================
    // 7.2 帖子发布/编辑/删除
    // ==============================

    @PostMapping("/publish")
    @ApiOperation("发布帖子")
    public Result<Long> publishPost(@Validated @RequestBody PostPublishDTO dto,
                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }
        Long postId = postService.publishPost(userId, dto);
        log.info("用户 {} 发布帖子 {}", userId, postId);
        return Result.success(postId);
    }

    @PutMapping("/{id}")
    @ApiOperation("编辑帖子（仅草稿/已拒绝状态可编辑）")
    public Result<Void> updatePost(@PathVariable Long id,
                                   @Validated @RequestBody PostPublishDTO dto,
                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }
        postService.updatePost(userId, id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除帖子")
    public Result<Void> deletePost(@PathVariable Long id,
                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }
        postService.deletePost(userId, id);
        log.info("用户 {} 删除帖子 {}", userId, id);
        return Result.success();
    }

    // ==============================
    // 7.3 帖子列表+详情
    // ==============================

    @GetMapping("/list")
    @ApiOperation("帖子列表（瀑布流分页）")
    public Result<Map<String, Object>> listPosts(
            @ApiParam("标签: all/follow/hot") @RequestParam(defaultValue = "all") String tab,
            @ApiParam("分类标签筛选") @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = postService.listPosts(tab, tag, pageNum, pageSize, userId);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @ApiOperation("帖子详情")
    public Result<PostVO> getPostDetail(@PathVariable Long id,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        PostVO post = postService.getPostDetail(id, userId);
        return Result.success(post);
    }

    // ==============================
    // 7.4 评论+互动
    // ==============================

    @PostMapping("/comment")
    @ApiOperation("发布评论/回复")
    public Result<Long> addComment(@Validated @RequestBody PostCommentDTO dto,
                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }
        Long commentId = commentService.addComment(userId, dto);
        return Result.success(commentId);
    }

    @DeleteMapping("/comment/{id}")
    @ApiOperation("删除评论")
    public Result<Void> deleteComment(@PathVariable Long id,
                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }
        commentService.deleteComment(userId, id);
        return Result.success();
    }

    @PostMapping("/comment/like/{id}")
    @ApiOperation("评论点赞")
    public Result<Void> likeComment(@PathVariable Long id,
                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }
        commentService.likeComment(userId, id);
        return Result.success();
    }

    @PostMapping("/like/{id}")
    @ApiOperation("帖子点赞/取消点赞")
    public Result<Boolean> likePost(@PathVariable Long id,
                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }
        boolean liked = postService.likePost(userId, id);
        return Result.success(liked);
    }

    @PostMapping("/collect/{id}")
    @ApiOperation("帖子收藏/取消收藏")
    public Result<Boolean> collectPost(@PathVariable Long id,
                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }
        boolean collected = postService.collectPost(userId, id);
        return Result.success(collected);
    }

    // ==============================
    // 7.5 关注+主页
    // ==============================

    @PostMapping("/user/follow/{userId}")
    @ApiOperation("关注/取消关注")
    public Result<Boolean> follow(@PathVariable Long userId,
                                  HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        boolean followed = followService.follow(currentUserId, userId);
        return Result.success(followed);
    }

    @GetMapping("/user/follow/list")
    @ApiOperation("我关注的人列表")
    public Result<Map<String, Object>> getFollowList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }
        return Result.success(followService.getFollowList(userId, pageNum, pageSize));
    }

    @GetMapping("/user/fans/list")
    @ApiOperation("关注我的人（粉丝）列表")
    public Result<Map<String, Object>> getFansList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }
        return Result.success(followService.getFansList(userId, pageNum, pageSize));
    }

    @GetMapping("/my/list")
    @ApiOperation("我发布的帖子")
    public Result<Map<String, Object>> getMyPosts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }
        return Result.success(postService.getMyPosts(userId, pageNum, pageSize));
    }

    @GetMapping("/my/collect")
    @ApiOperation("我收藏的帖子")
    public Result<Map<String, Object>> getMyCollects(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }
        return Result.success(postService.getMyCollects(userId, pageNum, pageSize));
    }

    @GetMapping("/user/profile/{userId}")
    @ApiOperation("用户主页信息")
    public Result<Map<String, Object>> getUserProfile(
            @PathVariable Long userId,
            HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        return Result.success(postService.getUserProfile(userId, currentUserId));
    }

    // ==============================
    // 7.6 搜索
    // ==============================

    @GetMapping("/search")
    @ApiOperation("搜索帖子/用户")
    public Result<Map<String, Object>> search(
            @ApiParam("搜索关键词") @RequestParam String keyword,
            @ApiParam("类型: post/user") @RequestParam(defaultValue = "post") String type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(searchService.search(keyword, type, pageNum, pageSize, userId));
    }

    @GetMapping("/search/hot")
    @ApiOperation("热门搜索词Top10")
    public Result<List<String>> getHotSearch() {
        return Result.success(searchService.getHotSearch());
    }
}
