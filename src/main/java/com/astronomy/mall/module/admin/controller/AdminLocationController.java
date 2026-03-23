package com.astronomy.mall.module.admin.controller;

import com.astronomy.mall.common.annotation.AdminLog;
import com.astronomy.mall.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端 - 观测点管理 Controller
 * Base URL: /api/admin/location
 *
 * 接口清单（5个，6.5节实现）:
 *   GET    /api/admin/location/spots         观测点列表（分页+筛选）
 *   POST   /api/admin/location/spot          新增观测点
 *   PUT    /api/admin/location/spot/{id}     编辑观测点
 *   DELETE /api/admin/location/spot/{id}     删除观测点（逻辑删除）
 *   GET    /api/admin/location/checkins      签到记录查询（分页）
 *
 * 📌 6.0 骨架类，所有方法在 6.5 节填充实现
 * 📌 需注入 AdminLocationService（6.5节一同创建）
 */
@Slf4j
@Api(tags = "管理员-观测点管理")
@RestController
@RequestMapping("/admin/location")
public class AdminLocationController {

    // TODO 6.5: @Autowired AdminLocationService adminLocationService;

    /**
     * 观测点列表（分页+筛选）
     * GET /api/admin/location/spots
     *
     * TODO 6.5: 填充实现
     */
    @ApiOperation("观测点列表（分页+按省市筛选）")
    @AdminLog("查询观测点列表")
    @GetMapping("/spots")
    public Result<?> listSpots() {
        // TODO 6.5: adminLocationService.listSpots(queryDTO)
        return Result.success("待6.5节实现");
    }

    /**
     * 新增观测点
     * POST /api/admin/location/spot
     *
     * TODO 6.5: 填充实现
     */
    @ApiOperation("新增观测点")
    @AdminLog("新增观测点")
    @PostMapping("/spot")
    public Result<?> addSpot(@RequestBody Object dto) {
        // TODO 6.5: adminLocationService.addSpot(dto)
        return Result.success("待6.5节实现");
    }

    /**
     * 编辑观测点
     * PUT /api/admin/location/spot/{id}
     *
     * TODO 6.5: 填充实现
     */
    @ApiOperation("编辑观测点信息")
    @AdminLog("编辑观测点")
    @PutMapping("/spot/{id}")
    public Result<?> updateSpot(@PathVariable Long id, @RequestBody Object dto) {
        // TODO 6.5: adminLocationService.updateSpot(id, dto)
        return Result.success("待6.5节实现");
    }

    /**
     * 删除观测点（逻辑删除）
     * DELETE /api/admin/location/spot/{id}
     *
     * TODO 6.5: 填充实现
     */
    @ApiOperation("删除观测点（逻辑删除，deleted=1）")
    @AdminLog("删除观测点")
    @DeleteMapping("/spot/{id}")
    public Result<?> deleteSpot(@PathVariable Long id) {
        // TODO 6.5: adminLocationService.deleteSpot(id)
        return Result.success("待6.5节实现");
    }

    /**
     * 签到记录查询（分页）
     * GET /api/admin/location/checkins
     *
     * TODO 6.5: 填充实现
     */
    @ApiOperation("签到记录查询（分页）")
    @AdminLog("查询签到记录")
    @GetMapping("/checkins")
    public Result<?> listCheckins() {
        // TODO 6.5: adminLocationService.listCheckins(queryDTO)
        return Result.success("待6.5节实现");
    }
}