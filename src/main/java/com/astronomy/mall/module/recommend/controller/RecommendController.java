package com.astronomy.mall.module.recommend.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.recommend.dto.BrowseLogDTO;
import com.astronomy.mall.module.recommend.dto.PostBrowseLogDTO;
import com.astronomy.mall.module.recommend.dto.RecommendClickDTO;
import com.astronomy.mall.module.recommend.service.RecommendService;
import com.astronomy.mall.module.recommend.vo.RecommendPostVO;
import com.astronomy.mall.module.recommend.vo.RecommendProductVO;
import com.astronomy.mall.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 推荐系统接口
 *
 * 📌 共 10 个端点:
 * - 埋点(2): POST /browse, POST /post/browse
 * - 商品推荐(3): GET /home, GET /similar/{productId}, GET /cart
 * - 跨模块(4): GET /recognition/{id}/courses, GET /course/{id}/next,
 *               GET /spot/{id}/equipment, GET /post/recommend
 * - 效果回写(1): POST /click
 *
 * 📌 认证模式:
 * - /home, /similar: 可选认证（未登录返回热门/通用推荐）
 * - 其余: 需要登录
 */
@Slf4j
@RestController
@RequestMapping("/api/recommend")
@Api(tags = "推荐系统接口")
public class RecommendController {

    @Resource
    private RecommendService recommendService;

    // ======================== 埋点 ========================

    @PostMapping("/browse")
    @ApiOperation("商品浏览埋点")
    public Result<Void> logProductBrowse(@Valid @RequestBody BrowseLogDTO dto) {
        Long userId = UserContext.getUserId();
        recommendService.logProductBrowse(userId, dto);
        return Result.success(null);
    }

    @PostMapping("/post/browse")
    @ApiOperation("帖子浏览埋点")
    public Result<Void> logPostBrowse(@Valid @RequestBody PostBrowseLogDTO dto) {
        Long userId = UserContext.getUserId();
        recommendService.logPostBrowse(userId, dto);
        return Result.success(null);
    }

    // ======================== 商品推荐 ========================

    @GetMapping("/home")
    @ApiOperation("首页个性化推荐")
    public Result<List<RecommendProductVO>> getHomeRecommend(
            HttpServletRequest request,
            @ApiParam("数量(默认10)") @RequestParam(defaultValue = "10") int limit) {
        // 可选认证: 未登录 userId 为 null
        Long userId = (Long) request.getAttribute("userId");
        List<RecommendProductVO> list = recommendService.getHomeRecommend(userId, limit);
        return Result.success(list);
    }

    @GetMapping("/similar/{productId}")
    @ApiOperation("相似商品推荐")
    public Result<List<RecommendProductVO>> getSimilarProducts(
            @PathVariable Long productId,
            @ApiParam("数量(默认6)") @RequestParam(defaultValue = "6") int limit) {
        List<RecommendProductVO> list = recommendService.getSimilarProducts(productId, limit);
        return Result.success(list);
    }

    @GetMapping("/cart")
    @ApiOperation("购物车推荐")
    public Result<List<RecommendProductVO>> getCartRecommend(
            @ApiParam("数量(默认6)") @RequestParam(defaultValue = "6") int limit) {
        Long userId = UserContext.getUserId();
        List<RecommendProductVO> list = recommendService.getCartRecommend(userId, limit);
        return Result.success(list);
    }

    // ======================== 跨模块联动 ========================

    @GetMapping("/recognition/{recognitionId}/courses")
    @ApiOperation("AI识别→推荐相关课程")
    public Result<List<Object>> getRecognitionCourseRecommend(
            @PathVariable Long recognitionId,
            @ApiParam("数量(默认6)") @RequestParam(defaultValue = "6") int limit) {
        List<Object> list = recommendService.getRecognitionCourseRecommend(recognitionId, limit);
        return Result.success(list);
    }

    @GetMapping("/course/{courseId}/next")
    @ApiOperation("完课→推荐下一门课程")
    public Result<List<Object>> getNextCourseRecommend(
            @PathVariable Long courseId,
            @ApiParam("数量(默认6)") @RequestParam(defaultValue = "6") int limit) {
        Long userId = UserContext.getUserId();
        List<Object> list = recommendService.getNextCourseRecommend(userId, courseId, limit);
        return Result.success(list);
    }

    @GetMapping("/spot/{spotId}/equipment")
    @ApiOperation("签到观测点→推荐适合器材")
    public Result<List<RecommendProductVO>> getSpotEquipmentRecommend(
            @PathVariable Long spotId,
            @ApiParam("数量(默认6)") @RequestParam(defaultValue = "6") int limit) {
        List<RecommendProductVO> list = recommendService.getSpotEquipmentRecommend(spotId, limit);
        return Result.success(list);
    }

    @GetMapping("/post/recommend")
    @ApiOperation("帖子个性化推荐(论坛'为你推荐'Tab)")
    public Result<List<RecommendPostVO>> getPostRecommend(
            HttpServletRequest request,
            @ApiParam("数量(默认10)") @RequestParam(defaultValue = "10") int limit) {
        Long userId = (Long) request.getAttribute("userId");
        List<RecommendPostVO> list = recommendService.getPostRecommend(userId, limit);
        return Result.success(list);
    }

    // ======================== 效果回写 ========================

    @PostMapping("/click")
    @ApiOperation("推荐点击回写(前端点击推荐卡片时调用)")
    public Result<Void> recordClick(@Valid @RequestBody RecommendClickDTO dto) {
        Long userId = UserContext.getUserId();
        recommendService.recordClick(userId, dto);
        return Result.success(null);
    }
}
