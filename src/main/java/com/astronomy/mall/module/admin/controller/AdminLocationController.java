package com.astronomy.mall.module.admin.controller;

import com.astronomy.mall.common.annotation.AdminLog;
import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.admin.service.AdminLocationService;
import com.astronomy.mall.module.location.dto.SpotCreateDTO;
import com.astronomy.mall.module.location.dto.SpotUpdateDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 管理员端 - 观测点管理 Controller
 * Base URL: /api/admin/location
 *
 * 接口清单（5个，6.5节实现）:
 *   GET    /api/admin/location/spot/list       观测点列表（分页+省市/关键词筛选）
 *   POST   /api/admin/location/spot/add        新增观测点
 *   PUT    /api/admin/location/spot/{id}       编辑观测点
 *   DELETE /api/admin/location/spot/{id}       删除观测点（逻辑删除）
 *   GET    /api/admin/location/spot/{id}/stats 签到统计
 */
@Slf4j
@Api(tags = "管理员-观测点管理")
@RestController
@RequestMapping("/api/admin/location")
public class AdminLocationController {

    @Autowired
    private AdminLocationService adminLocationService;

    /**
     * 观测点分页列表
     * GET /api/admin/location/spot/list
     * 支持按省份、城市、关键词筛选
     */
    @ApiOperation("观测点列表（分页+按省市/关键词筛选）")
    @AdminLog("查询观测点列表")
    @GetMapping("/spot/list")
    public Result<Map<String, Object>> listSpots(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("省份筛选") @RequestParam(required = false) String province,
            @ApiParam("城市筛选") @RequestParam(required = false) String city,
            @ApiParam("关键词搜索") @RequestParam(required = false) String keyword) {
        Map<String, Object> data = adminLocationService.listSpots(pageNum, pageSize, province, city, keyword);
        return Result.success(data);
    }

    /**
     * 新增观测点
     * POST /api/admin/location/spot/add
     */
    @ApiOperation("新增观测点")
    @AdminLog("新增观测点")
    @PostMapping("/spot/add")
    public Result<String> addSpot(@Valid @RequestBody SpotCreateDTO dto) {
        adminLocationService.addSpot(dto);
        return Result.success("新增成功");
    }

    /**
     * 编辑观测点
     * PUT /api/admin/location/spot/{id}
     */
    @ApiOperation("编辑观测点信息")
    @AdminLog("编辑观测点")
    @PutMapping("/spot/{id}")
    public Result<String> updateSpot(@PathVariable Long id,
                                     @Valid @RequestBody SpotUpdateDTO dto) {
        adminLocationService.updateSpot(id, dto);
        return Result.success("更新成功");
    }

    /**
     * 删除观测点（逻辑删除）
     * DELETE /api/admin/location/spot/{id}
     */
    @ApiOperation("删除观测点（逻辑删除，deleted=1）")
    @AdminLog("删除观测点")
    @DeleteMapping("/spot/{id}")
    public Result<String> deleteSpot(@PathVariable Long id) {
        adminLocationService.deleteSpot(id);
        return Result.success("删除成功");
    }

    /**
     * 签到统计
     * GET /api/admin/location/spot/{id}/stats
     * 返回: totalCount, last7Days, last30Days, topUsers(TOP5)
     */
    @ApiOperation("观测点签到统计")
    @GetMapping("/spot/{id}/stats")
    public Result<Map<String, Object>> getSpotStats(@PathVariable Long id) {
        Map<String, Object> stats = adminLocationService.getSpotStats(id);
        return Result.success(stats);
    }
}
