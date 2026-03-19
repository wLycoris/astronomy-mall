package com.astronomy.mall.module.recognition.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.recognition.dto.SubmitRecognitionDTO;
import com.astronomy.mall.module.recognition.service.RecognitionRecommendService;
import com.astronomy.mall.module.recognition.service.RecognitionService;
import com.astronomy.mall.module.recognition.vo.RecognitionProductVO;
import com.astronomy.mall.module.recognition.vo.RecognitionStatsVO;
import com.astronomy.mall.module.recognition.vo.RecognitionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * AI星图识别 Controller
 *
 * 接口列表 (共8个):
 *   POST   /api/recognition/submit           提交识别任务（4.1）
 *   GET    /api/recognition/status/{id}      查询识别状态（4.1，等待页轮询）
 *   GET    /api/recognition/history          用户历史记录（4.2/4.5升级，支持 status 筛选）
 *   GET    /api/recognition/stats            识别统计（4.5⭐新增）
 *   GET    /api/recognition/result/{id}      完整识别结果（4.3，含中英文天体+坐标格式化）
 *   GET    /api/recognition/recommend/{id}   器材推荐（4.4）
 *   GET    /api/recognition/{id}             识别详情（4.2，基础版）
 *   DELETE /api/recognition/{id}             删除单条记录（4.5⭐新增）
 *
 * 📌 鉴权: 所有接口需要 JWT
 *    userId 从 request attribute 中取（JwtInterceptor 解析后存入）
 *
 * ⚠️ 路由注册顺序说明:
 *    /status/{id}、/result/{id}、/recommend/{id} 路径前缀不同，与 /{id} 无冲突
 *    /history、/stats 无路径参数，Spring MVC 精确路径优先，与 /{id} 无冲突
 *    保持从具体到抽象的排列顺序
 */
@Slf4j
@RestController
@RequestMapping("/api/recognition")
@RequiredArgsConstructor
@Api(tags = "AI星图识别")
public class RecognitionController {

    private final RecognitionService          recognitionService;
    private final RecognitionRecommendService recognitionRecommendService;

    // ============================================================
    // POST /api/recognition/submit  提交识别任务 (4.1)
    // ============================================================

    /**
     * 提交星图识别任务
     *
     * 前端流程:
     *   Canvas 压缩 → base64（去前缀） → POST → 获得 recognitionId → 跳转等待页
     */
    @PostMapping("/submit")
    @ApiOperation("提交星图识别任务")
    public Result<RecognitionVO> submit(
            @Validated @RequestBody SubmitRecognitionDTO dto,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        RecognitionVO vo = recognitionService.submit(dto, userId);
        return Result.success(vo);
    }

    // ============================================================
    // GET /api/recognition/status/{id}  查询识别状态 (4.1)
    // ============================================================

    /**
     * 查询识别状态
     *
     * 前端等待页每 5 秒调用一次，根据 status 决定跳转时机。
     * status=0 → 继续等；status=1 → 跳结果页；status=2 → 显示失败
     */
    @GetMapping("/status/{id}")
    @ApiOperation("查询识别状态（等待页轮询）")
    public Result<RecognitionVO> getStatus(
            @ApiParam("识别记录ID") @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        RecognitionVO vo = recognitionService.getStatus(id, userId);
        return Result.success(vo);
    }

    // ============================================================
    // GET /api/recognition/history  用户历史记录 (4.2 → 4.5升级)
    // ============================================================

    /**
     * 查询当前用户的历史识别记录（分页 + 可选状态筛选）
     *
     * 📌 v4.5 升级: 新增 status 参数（可选）
     *   status=null  → 全部
     *   status=0     → 识别中
     *   status=1     → 识别成功
     *   status=2     → 识别失败
     *
     * @param pageNum  页码（默认 1）
     * @param pageSize 每页数量（默认 10，最大 50）
     * @param status   状态筛选（可选，不传则查全部）
     * @return { list: RecognitionVO[], total: long, pageNum: int, pageSize: int }
     */
    @GetMapping("/history")
    @ApiOperation("用户历史识别记录（支持状态筛选）")
    public Result<Map<String, Object>> getHistory(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") int pageSize,
            @ApiParam("状态筛选：0-识别中 1-成功 2-失败，不传则查全部") @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = recognitionService.getHistory(userId, pageNum, pageSize, status);
        return Result.success(result);
    }

    // ============================================================
    // ⭐ GET /api/recognition/stats  识别统计 (4.5 新增)
    // ============================================================

    /**
     * 获取当前用户的识别统计
     * 📌 v4.5新增 ⭐
     *
     * 返回: RecognitionStatsVO {
     *   total,         // 总识别次数
     *   successCount,  // 识别成功次数
     *   failCount,     // 识别失败次数
     *   pendingCount,  // 识别中次数
     *   successRate    // 成功率（百分比，保留1位小数）
     * }
     *
     * ⚠️ /stats 为精确路径，Spring MVC 优先于 /{id} 匹配，无需特殊处理
     */
    @GetMapping("/stats")
    @ApiOperation("获取识别统计（总次数/成功率）")
    public Result<RecognitionStatsVO> getStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(recognitionService.getStats(userId));
    }

    // ============================================================
    // GET /api/recognition/result/{id}  完整识别结果 (4.3)
    // ============================================================

    /**
     * 获取完整识别结果
     * 📌 v4.3新增接口 ⭐
     *
     * 与 GET /{id} 的区别:
     *   - 返回 celestialObjects（中英文天体名称 + 类型）
     *   - 返回 raFormatted / decFormatted / orientationFormatted / radiusFormatted（格式化坐标）
     *   - status != 1 时仅返回基础信息（不含格式化字段）
     *
     * 前端 RecognitionResult.vue 使用此接口（升级自原来的 /{id}）
     */
    @GetMapping("/result/{id}")
    @ApiOperation("获取完整识别结果（含中英文天体名称+坐标格式化）")
    public Result<RecognitionVO> getResult(
            @ApiParam("识别记录ID") @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        RecognitionVO vo = recognitionService.getResult(id, userId);
        return Result.success(vo);
    }

    // ============================================================
    // GET /api/recognition/recommend/{id}  器材推荐 (4.4)
    // ============================================================

    /**
     * 获取识别结果关联的推荐器材
     * 📌 4.4新增接口 ⭐
     *
     * 逻辑:
     *   machine_tags → TAG_MAPPING → 匹配 tb_product.tags → 最多6个商品
     *   无匹配时兜底返回热销前6个
     *   推荐结果 ID 写回 tb_recognition.recommended_products 缓存
     *
     * 返回: List<RecognitionProductVO>（id/productName/mainImage/price/reason）
     */
    @GetMapping("/recommend/{id}")
    @ApiOperation("获取识别关联推荐器材（最多6个）")
    public Result<List<RecognitionProductVO>> getRecommend(
            @ApiParam("识别记录ID") @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.debug("[Recognition] 用户 {} 请求推荐器材, recognitionId={}", userId, id);
        List<RecognitionProductVO> products = recognitionRecommendService.getRecommend(id, userId);
        return Result.success(products);
    }

    // ============================================================
    // GET /api/recognition/{id}  识别详情基础版 (4.2)
    // ⚠️ GET 通配符路径放最后
    // ============================================================

    @GetMapping("/{id}")
    @ApiOperation("获取识别详情（基础版）")
    public Result<RecognitionVO> getDetail(
            @ApiParam("识别记录ID") @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        RecognitionVO vo = recognitionService.getDetail(id, userId);
        return Result.success(vo);
    }

    // ============================================================
    // ⭐ DELETE /api/recognition/{id}  删除单条记录 (4.5 新增)
    // ============================================================

    /**
     * 删除单条识别记录
     * 📌 v4.5新增 ⭐
     *
     * 校验: 只能删除属于当前用户的记录，否则返回 403
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除单条识别记录（只能删自己的）")
    public Result<Void> deleteRecord(
            @ApiParam("识别记录ID") @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        recognitionService.deleteRecord(id, userId);
        return Result.success(null);
    }
}