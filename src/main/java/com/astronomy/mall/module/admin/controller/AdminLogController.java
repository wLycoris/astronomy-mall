package com.astronomy.mall.module.admin.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.admin.dto.AdminLogQueryDTO;
import com.astronomy.mall.module.admin.service.AdminLogService;
import com.astronomy.mall.module.admin.vo.AdminLogVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 管理员操作日志 Controller
 *
 * 📌 接口列表:
 *   GET /api/admin/log/list          - 日志列表（分页+多条件筛选）
 *   GET /api/admin/log/detail/:id    - 日志详情（含请求参数等完整信息）
 *   GET /api/admin/log/export        - 导出日志（Excel格式）
 *
 * 📌 注意事项:
 *   - 此模块只读，不提供删除/修改接口（保证日志完整性）
 *   - 不加 @AdminLog 注解（避免循环记录日志）
 *   - 权限：AdminInterceptor 已保证只有管理员(role=1)可访问 /api/admin/**
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/log")
@Api(tags = "后台-操作日志管理")
public class AdminLogController {

    @Autowired
    private AdminLogService adminLogService;

    // =============================================
    // 1. 日志列表（分页）
    // =============================================

    /**
     * 获取操作日志列表（分页 + 多条件筛选）
     *
     * @param adminId   管理员ID（精确，可选）
     * @param adminName 管理员姓名（模糊，可选）
     * @param operation 操作类型（模糊，可选，如"商品"、"订单"）
     * @param status    状态（0-失败 1-成功，可选）
     * @param startTime 开始时间（yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss，可选）
     * @param endTime   结束时间（yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss，可选）
     * @param page      当前页（默认1）
     * @param size      每页条数（默认20）
     * @return 分页结果 { list, total, page, size }
     */
    @GetMapping("/list")
    @ApiOperation("日志列表（分页）")
    public Result<Map<String, Object>> list(
            @ApiParam("管理员ID（精确）")
            @RequestParam(required = false) Long adminId,

            @ApiParam("管理员姓名（模糊）")
            @RequestParam(required = false) String adminName,

            @ApiParam("操作类型（模糊，如：商品上架、订单发货）")
            @RequestParam(required = false) String operation,

            @ApiParam("状态：0失败 1成功")
            @RequestParam(required = false) Integer status,

            @ApiParam("开始时间（yyyy-MM-dd）")
            @RequestParam(required = false) String startTime,

            @ApiParam("结束时间（yyyy-MM-dd）")
            @RequestParam(required = false) String endTime,

            @ApiParam("当前页（默认1）")
            @RequestParam(defaultValue = "1") Integer pageNum,

            @ApiParam("每页条数（默认20）")
            @RequestParam(defaultValue = "20") Integer pageSize) {

        log.info("查询操作日志列表 - adminId:{}, adminName:{}, operation:{}, status:{}, pageNum:{}, pageSize:{}",
                adminId, adminName, operation, status, pageNum, pageSize);

        AdminLogQueryDTO query = new AdminLogQueryDTO();
        query.setAdminId(adminId);
        query.setAdminName(adminName);
        query.setOperation(operation);
        query.setStatus(status);
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);

        Map<String, Object> result = adminLogService.getLogPage(query);
        return Result.success(result);
    }

    // =============================================
    // 2. 日志详情
    // =============================================

    /**
     * 查询操作日志详情
     *
     * 详情页额外展示: 请求参数(params)、User-Agent、执行耗时、错误信息
     *
     * @param id 日志ID
     * @return AdminLogVO（完整字段）
     */
    @GetMapping("/detail/{id}")
    @ApiOperation("日志详情")
    public Result<AdminLogVO> detail(
            @ApiParam("日志ID") @PathVariable Long id) {

        log.info("查询操作日志详情 - id:{}", id);
        AdminLogVO vo = adminLogService.getLogDetail(id);
        return Result.success(vo);
    }

    // =============================================
    // 3. 导出日志
    // =============================================

    /**
     * 导出操作日志（Excel格式）
     *
     * 📌 说明:
     *   - 导出条件与列表查询相同
     *   - 最多导出10000条，防止内存溢出
     *   - 文件名格式: 操作日志_yyyyMMddHHmmss.xlsx
     *   - 前端使用 window.open 或 a标签 href 直接下载
     *
     * @param adminId   管理员ID（可选）
     * @param adminName 管理员姓名（可选）
     * @param operation 操作类型（可选）
     * @param status    状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param response  HttpServletResponse（写入文件流）
     */
    @GetMapping("/export")
    @ApiOperation("导出操作日志（Excel）")
    public void export(
            @ApiParam("管理员ID") @RequestParam(required = false) Long adminId,
            @ApiParam("管理员姓名") @RequestParam(required = false) String adminName,
            @ApiParam("操作类型") @RequestParam(required = false) String operation,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("开始时间") @RequestParam(required = false) String startTime,
            @ApiParam("结束时间") @RequestParam(required = false) String endTime,
            HttpServletResponse response) {

        log.info("导出操作日志 - adminId:{}, operation:{}, startTime:{}, endTime:{}",
                adminId, operation, startTime, endTime);

        AdminLogQueryDTO query = new AdminLogQueryDTO();
        query.setAdminId(adminId);
        query.setAdminName(adminName);
        query.setOperation(operation);
        query.setStatus(status);
        query.setStartTime(startTime);
        query.setEndTime(endTime);

        adminLogService.exportLog(query, response);
    }
}