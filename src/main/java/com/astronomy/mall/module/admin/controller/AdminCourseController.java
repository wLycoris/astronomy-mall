package com.astronomy.mall.module.admin.controller;

import com.astronomy.mall.common.annotation.AdminLog;
import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.admin.dto.ApodSyncDTO;
import com.astronomy.mall.module.admin.dto.ChapterCreateDTO;
import com.astronomy.mall.module.admin.dto.CourseCreateDTO;
import com.astronomy.mall.module.admin.service.AdminCourseService;
import com.astronomy.mall.module.admin.vo.AdminCourseVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台课程管理 Controller（5.5 完整版，替换 5.2 的单接口版本）
 *
 * 📌 与 5.2 旧版的关键区别：
 *   - 不再直接注入 APODSyncScheduler
 *   - 注入 AdminCourseService，所有逻辑走 Service 层
 *   - 11个完整接口
 *
 * 共 11 个接口（全部需要管理员权限，由 AdminInterceptor 统一拦截）：
 *
 *   GET    /api/admin/course/list           - 课程列表（分页+搜索+type+status）
 *   POST   /api/admin/course/add           - 新增课程
 *   PUT    /api/admin/course/update/{id}   - 编辑课程
 *   DELETE /api/admin/course/delete/{id}   - 删除课程（deleted=1）
 *   POST   /api/admin/course/status/{id}   - 发布/下架
 *   GET    /api/admin/course/{id}/chapters - 章节列表
 *   POST   /api/admin/course/chapter/add   - 新增章节
 *   PUT    /api/admin/course/chapter/{id}  - 编辑章节
 *   DELETE /api/admin/course/chapter/{id}  - 删除章节
 *   POST   /api/admin/course/chapter/sort  - 章节排序（批量更新 sort）
 *   POST   /api/admin/course/apod/sync     - 手动触发 APOD 历史数据批量同步
 */
@Slf4j
@Api(tags = "管理员 - 课程管理")
@RestController
@RequestMapping("/api/admin/course")
@RequiredArgsConstructor
public class AdminCourseController {

    private final AdminCourseService adminCourseService;

    // ============================================================
    // =================== 课程管理接口（5个）=====================
    // ============================================================

    /**
     * 1. 课程列表（分页 + 关键词 + type + status 筛选）
     */
    @ApiOperation("课程列表")
    @GetMapping("/list")
    public Result<Page<AdminCourseVO>> getCourseList(
            @ApiParam("页码，默认1")        @RequestParam(defaultValue = "1")  Integer pageNum,
            @ApiParam("每页条数，默认10")   @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("标题关键词（可选）") @RequestParam(required = false)     String keyword,
            @ApiParam("课程类型：0-视频课 1-书本课（不传=全部）") @RequestParam(required = false) Integer type,
            @ApiParam("状态：0-草稿 1-已发布（不传=全部）")      @RequestParam(required = false) Integer status) {

        return Result.success(adminCourseService.getCourseList(pageNum, pageSize, keyword, type, status));
    }

    /**
     * 2. 新增课程（默认草稿状态）
     */
    @ApiOperation("新增课程")
    @PostMapping("/add")
    @AdminLog("新增课程")
    public Result<Map<String, Long>> addCourse(@Validated @RequestBody CourseCreateDTO dto) {
        Long id = adminCourseService.addCourse(dto);
        Map<String, Long> data = new HashMap<>();
        data.put("id", id);
        return Result.success(data);
    }

    /**
     * 3. 编辑课程基本信息
     */
    @ApiOperation("编辑课程")
    @PutMapping("/update/{id}")
    @AdminLog("编辑课程")
    public Result<Void> updateCourse(
            @ApiParam("课程ID") @PathVariable Long id,
            @Validated @RequestBody CourseCreateDTO dto) {
        adminCourseService.updateCourse(id, dto);
        return Result.success();
    }

    /**
     * 4. 删除课程（逻辑删除，deleted=1）
     */
    @ApiOperation("删除课程")
    @DeleteMapping("/delete/{id}")
    @AdminLog("删除课程")
    public Result<Void> deleteCourse(@ApiParam("课程ID") @PathVariable Long id) {
        adminCourseService.deleteCourse(id);
        return Result.success();
    }

    /**
     * 5. 发布 / 下架课程（status 0↔1）
     */
    @ApiOperation("发布/下架课程")
    @PostMapping("/status/{id}")
    @AdminLog("课程状态变更")
    public Result<Void> updateCourseStatus(
            @ApiParam("课程ID") @PathVariable Long id,
            @ApiParam("状态：0-下架 1-发布") @RequestParam Integer status) {
        adminCourseService.updateCourseStatus(id, status);
        return Result.success();
    }

    // ============================================================
    // =================== 章节管理接口（5个）=====================
    // ============================================================

    /**
     * 6. 获取课程章节列表（按 sort 升序）
     */
    @ApiOperation("获取课程章节列表")
    @GetMapping("/{id}/chapters")
    public Result<List<AdminCourseVO.ChapterVO>> getChapterList(
            @ApiParam("课程ID") @PathVariable Long id) {
        return Result.success(adminCourseService.getChapterList(id));
    }

    /**
     * 7. 新增章节
     * 📌 成功后异步通知收藏了该课程的所有用户
     */
    @ApiOperation("新增章节")
    @PostMapping("/chapter/add")
    @AdminLog("新增课程章节")
    public Result<Map<String, Long>> addChapter(@Validated @RequestBody ChapterCreateDTO dto) {
        Long id = adminCourseService.addChapter(dto);
        Map<String, Long> data = new HashMap<>();
        data.put("id", id);
        return Result.success(data);
    }

    /**
     * 8. 编辑章节
     */
    @ApiOperation("编辑章节")
    @PutMapping("/chapter/{id}")
    @AdminLog("编辑课程章节")
    public Result<Void> updateChapter(
            @ApiParam("章节ID") @PathVariable Long id,
            @RequestBody ChapterCreateDTO dto) {
        adminCourseService.updateChapter(id, dto);
        return Result.success();
    }

    /**
     * 9. 删除章节（物理删除，同步更新 chapter_count）
     */
    @ApiOperation("删除章节")
    @DeleteMapping("/chapter/{id}")
    @AdminLog("删除课程章节")
    public Result<Void> deleteChapter(@ApiParam("章节ID") @PathVariable Long id) {
        adminCourseService.deleteChapter(id);
        return Result.success();
    }

    /**
     * 10. 批量更新章节排序（拖拽排序）
     * 请求体格式: [{"id":1,"sort":0},{"id":2,"sort":1},...]
     */
    @ApiOperation("批量更新章节排序")
    @PostMapping("/chapter/sort")
    @AdminLog("更新章节排序")
    public Result<Void> sortChapters(@RequestBody List<Map<String, Object>> sortList) {
        adminCourseService.sortChapters(sortList);
        return Result.success();
    }

    // ============================================================
    // =================== APOD 批量同步（1个）====================
    // ============================================================

    /**
     * 11. 手动触发 APOD 历史数据批量同步
     *
     * 📌 与 5.2 版本的区别：
     *   - 5.2 直接注入 APODSyncScheduler 调用
     *   - 5.5 通过 AdminCourseService 调用，NasaApiService 在 ServiceImpl 中直接调用
     *
     * 📌 幂等：已存在日期自动跳过，可重复执行
     * 📌 单次范围 ≤ 60 天（Service 层校验），超过请分批
     *
     * 请求体示例:
     * { "startDate": "2024-01-01", "endDate": "2024-01-31" }
     *
     * 返回示例:
     * { "code": 200, "data": { "newCount": 28 }, "message": "操作成功" }
     */
    @ApiOperation("手动触发 APOD 历史数据批量同步")
    @PostMapping("/apod/sync")
    @AdminLog("手动同步 APOD 历史数据")
    public Result<Map<String, Integer>> syncApod(@Validated @RequestBody ApodSyncDTO dto) {
        int newCount = adminCourseService.syncApodRange(dto);
        Map<String, Integer> data = new HashMap<>();
        data.put("newCount", newCount);
        return Result.success(data);
    }
}