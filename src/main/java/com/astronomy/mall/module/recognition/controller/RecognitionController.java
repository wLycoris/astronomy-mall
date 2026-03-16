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
 * 接口列表 (共5个):
 *   POST /api/recognition/submit           提交识别任务（4.1）
 *   GET  /api/recognition/status/{id}      查询识别状态（4.1，等待页轮询）
 *   GET  /api/recognition/{id}             识别详情（4.2，结果页基础版）
 *   GET  /api/recognition/history          用户历史记录（4.2）
 *   GET  /api/recognition/result/{id}      完整识别结果（4.3⭐新增，含中英文天体+坐标格式化）
 *
 * 📌 鉴权: 所有接口需要 JWT
 *    userId 从 request attribute 中取（JwtInterceptor 解析后存入）
 *
 * ⚠️ 路由注册顺序说明:
 *    /status/{id}  和  /result/{id}  路径前缀不同，与 /{id} 无冲突
 *    /history      无路径参数，与 /{id} 无冲突
 *    Spring MVC 路径精确度优先，不受顺序影响，但保持从具体到抽象的排列顺序
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
    // GET /api/recognition/history  用户历史记录
    // ============================================================

    /**
     * 查询当前用户的历史识别记录（分页）
     *
     * ⚠️ 必须放在 /{id} 之前注册（虽然 Spring MVC 能区分，但显式保持顺序更清晰）
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

    // ============================================================
    // ⭐ v4.3 新增: GET /api/recognition/result/{id}  完整识别结果
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
        log.debug("[Recognition] 用户 {} 请求完整结果, id={}", userId, id);
        RecognitionVO vo = recognitionService.getResult(id, userId);
        return Result.success(vo);
    }

    // ============================================================
    // GET /api/recognition/{id}  识别详情（结果页基础版）
    // ============================================================

    /**
     * 获取识别详情（基础版，不含格式化字段）
     *
     * ⚠️ 通配符路径 /{id} 必须放在所有具体路径（/status/{id}, /result/{id}, /history）之后
     *    Spring MVC 对于同级 GET 映射，具体路径优先于通配符，顺序本身不影响匹配，
     *    但在此保持最后注册以增强可读性
     */
    @GetMapping("/{id}")
    @ApiOperation("获取识别详情（基础版）")
    public Result<RecognitionVO> getDetail(
            @ApiParam("识别记录ID") @PathVariable Long id,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        RecognitionVO vo = recognitionService.getDetail(id, userId);
        return Result.success(vo);
    }
}