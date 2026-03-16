package com.astronomy.mall.module.recognition.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.recognition.dto.SubmitRecognitionDTO;
import com.astronomy.mall.module.recognition.service.RecognitionService;
import com.astronomy.mall.module.recognition.vo.RecognitionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * AI星图识别 Controller
 *
 * 接口列表:
 *   POST /api/recognition/submit         提交识别任务（4.1）
 *   GET  /api/recognition/status/{id}    查询识别状态（4.1，等待页轮询）
 *   GET  /api/recognition/{id}           识别详情（4.2，结果页）
 *   GET  /api/recognition/history        用户历史记录（4.2）
 *
 * 📌 鉴权: 所有接口需要 JWT
 *    userId 从 request attribute 中取（JwtInterceptor 解析后存入）
 */
@Slf4j
@RestController
@RequestMapping("/api/recognition")
@RequiredArgsConstructor
@Api(tags = "AI星图识别")
public class RecognitionController {

    private final RecognitionService recognitionService;

    // ============================================================
    // POST /api/recognition/submit  提交识别任务
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
    // GET /api/recognition/status/{id}  查询识别状态（等待页轮询）
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
    // GET /api/recognition/{id}  识别详情（结果页）
    // ============================================================

    /**
     * 获取识别详情（结果页使用）
     *
     * 返回完整识别结果：坐标、天体列表、机器标签、标注图片 URL、推荐商品 ID 列表。
     * ⚠️ 路由顺序很重要：此接口必须在 /status/{id} 之后注册，
     *    否则 "/status/123" 会被误匹配为此接口，Spring MVC 按定义顺序匹配。
     *    （实际上 @GetMapping 路径不同，不会冲突，但这里显式注明以防迷惑）
     */
    @GetMapping("/{id}")
    @ApiOperation("获取识别详情")
    public Result<RecognitionVO> getDetail(
            @ApiParam("识别记录ID") @PathVariable Long id,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        RecognitionVO vo = recognitionService.getDetail(id, userId);
        return Result.success(vo);
    }

    // ============================================================
    // GET /api/recognition/history  用户历史记录
    // ============================================================

    /**
     * 查询当前用户的历史识别记录（分页）
     *
     * @param pageNum  页码（默认 1）
     * @param pageSize 每页数量（默认 10，最大 50）
     * @return { list: RecognitionVO[], total: int, pageNum: int, pageSize: int }
     */
    @GetMapping("/history")
    @ApiOperation("用户历史识别记录")
    public Result<Map<String, Object>> getHistory(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = recognitionService.getHistory(userId, pageNum, pageSize);
        return Result.success(result);
    }
}