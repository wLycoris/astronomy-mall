package com.astronomy.mall.module.user.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.user.service.UserOverviewService;
import com.astronomy.mall.module.user.vo.UserOverviewVO;
import com.astronomy.mall.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人中心概览 Controller
 *
 * 📌 接口路径: GET /api/user/overview
 * 📌 需要登录: 是（JwtInterceptor 拦截）
 * 📌 说明: 聚合查询，一个接口返回概览页所有数据
 *
 * 文件路径: com.astronomy.mall.module.user.controller.UserOverviewController
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Api(tags = "个人中心-概览")
public class UserOverviewController {

    private final UserOverviewService userOverviewService;

    /**
     * 获取个人中心概览数据
     * 返回: 用户信息 + 各状态订单数 + 余额 + 最近一笔流水
     *
     * @return UserOverviewVO 聚合概览数据
     */
    @GetMapping("/overview")
    @ApiOperation("获取个人中心概览数据")
    public Result<UserOverviewVO> getOverview() {
        // 从 ThreadLocal 取当前登录用户ID（JwtInterceptor 已存入）
        Long userId = UserContext.getUserId();
        log.info("[个人中心] 获取概览数据, userId={}", userId);

        UserOverviewVO vo = userOverviewService.getOverview(userId);
        return Result.success(vo);
    }
}