package com.astronomy.mall.module.admin.controller;

import com.astronomy.mall.common.annotation.AdminLog;
import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.admin.dto.AnnouncementCreateDTO;
import com.astronomy.mall.module.admin.dto.AnnouncementQueryDTO;
import com.astronomy.mall.module.admin.service.AdminAnnouncementService;
import com.astronomy.mall.module.admin.vo.AnnouncementVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 系统公告管理 Controller（管理员后台）
 *
 * 📌 接口清单（4个）:
 *   POST   /api/admin/announcement          - 创建并发送公告（批量写 tb_notification）
 *   GET    /api/admin/announcement/list     - 公告列表（GROUP BY 去重展示）
 *   GET    /api/admin/announcement/{id}     - 公告详情（含已读率统计）
 *   DELETE /api/admin/announcement/{id}     - 删除公告（软删除所有对应通知）
 *
 * 📌 权限: 需要管理员权限（AdminInterceptor 拦截 /api/admin/**）
 *
 * 文件路径: com.astronomy.mall.module.admin.controller.AdminAnnouncementController
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/announcement")
@RequiredArgsConstructor
@Api(tags = "后台-系统公告管理")
public class AdminAnnouncementController {

    private final AdminAnnouncementService adminAnnouncementService;

    // ================================================================
    // POST /api/admin/announcement
    // 创建并发送公告
    // ================================================================

    /**
     * 创建并发送公告
     *
     * 📌 流程:
     * 1. 校验请求参数（标题、内容不能为空）
     * 2. 生成唯一公告分组ID（System.currentTimeMillis()）
     * 3. 查询所有活跃用户ID
     * 4. 分批写入 tb_notification（每批500条）
     * 5. 返回公告VO（含发送数量）
     *
     * 📌 前端调用示例:
     * POST /api/admin/announcement
     * Body: { "title": "系统维护通知", "content": "内容...", "priority": 1 }
     */
    @PostMapping
    @AdminLog("创建系统公告")
    @ApiOperation("创建并发送公告（批量写 tb_notification）")
    public Result<AnnouncementVO> createAnnouncement(
            @Validated @RequestBody AnnouncementCreateDTO dto,
            HttpServletRequest request
    ) {
        // 从 JwtInterceptor 存入的 request attribute 中获取管理员ID
        Long adminId = (Long) request.getAttribute("userId");
        AnnouncementVO vo = adminAnnouncementService.createAnnouncement(dto, adminId);
        return Result.success(vo);
    }

    // ================================================================
    // GET /api/admin/announcement/list
    // 公告列表（分页）
    // ================================================================

    /**
     * 查询公告列表（分页）
     *
     * 📌 返回结构（标准 MyBatis-Plus Page）:
     * {
     *   "code": 200,
     *   "data": {
     *     "records": [...],
     *     "total": 10,
     *     "size": 10,
     *     "current": 1
     *   }
     * }
     *
     * 📌 前端调用示例:
     * GET /api/admin/announcement/list?pageNum=1&pageSize=10&keyword=维护
     */
    @GetMapping("/list")
    @ApiOperation("公告列表（分页+搜索）")
    public Result<Page<AnnouncementVO>> listAnnouncements(AnnouncementQueryDTO dto) {
        Page<AnnouncementVO> page = adminAnnouncementService.listAnnouncements(dto);
        return Result.success(page);
    }

    // ================================================================
    // GET /api/admin/announcement/{id}
    // 公告详情
    // ================================================================

    /**
     * 查询公告详情
     *
     * @param id 公告ID（即 tb_notification.related_id）
     *
     * 📌 前端调用示例:
     * GET /api/admin/announcement/1742000000000
     */
    @GetMapping("/{id}")
    @ApiOperation("公告详情（含已读率统计）")
    public Result<AnnouncementVO> getAnnouncementDetail(
            @ApiParam(value = "公告ID", required = true, example = "1742000000000")
            @PathVariable Long id
    ) {
        AnnouncementVO vo = adminAnnouncementService.getAnnouncementDetail(id);
        return Result.success(vo);
    }

    // ================================================================
    // DELETE /api/admin/announcement/{id}
    // 删除公告（软删除）
    // ================================================================

    /**
     * 删除公告（软删除）
     *
     * 将该公告对应的所有 tb_notification 记录的 deleted 设为 1。
     * 用户通知列表因 deleted=1 自动不可见。
     *
     * @param id 公告ID（即 tb_notification.related_id）
     *
     * 📌 前端调用示例:
     * DELETE /api/admin/announcement/1742000000000
     */
    @DeleteMapping("/{id}")
    @AdminLog("删除系统公告")
    @ApiOperation("删除公告（软删除所有对应通知记录）")
    public Result<Void> deleteAnnouncement(
            @ApiParam(value = "公告ID", required = true, example = "1742000000000")
            @PathVariable Long id
    ) {
        adminAnnouncementService.deleteAnnouncement(id);
        return Result.success(null);
    }
}