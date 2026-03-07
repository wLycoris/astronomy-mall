package com.astronomy.mall.module.user.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.interceptor.JwtInterceptor;
import com.astronomy.mall.module.user.dto.AddressDTO;
import com.astronomy.mall.module.user.service.AddressService;
import com.astronomy.mall.module.user.vo.AddressVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 收货地址 Controller
 *
 * 📌 接口清单 (5个):
 * GET    /api/address/list          - 我的地址列表
 * POST   /api/address/add           - 新增地址
 * PUT    /api/address/update/:id    - 编辑地址
 * DELETE /api/address/delete/:id    - 删除地址
 * POST   /api/address/default/:id   - 设为默认地址
 *
 * 📌 认证说明:
 * 所有接口均需登录（JwtInterceptor 已拦截 /api/**）
 * 通过 request.getAttribute("userId") 获取当前用户ID
 */
@Api(tags = "收货地址管理")
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * 获取我的收货地址列表
     * GET /api/address/list
     * - 默认地址排在最前面
     */
    @ApiOperation("获取收货地址列表")
    @GetMapping("/list")
    public Result<List<AddressVO>> getAddressList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<AddressVO> list = addressService.getAddressList(userId);
        return Result.success(list);
    }

    /**
     * 新增收货地址
     * POST /api/address/add
     * - 每个用户最多5个
     * - isDefault=1 时自动清除旧默认
     */
    @ApiOperation("新增收货地址")
    @PostMapping("/add")
    public Result<Void> addAddress(@Validated @RequestBody AddressDTO addressDTO,
                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        addressService.addAddress(userId, addressDTO);
        return Result.success();
    }

    /**
     * 编辑收货地址
     * PUT /api/address/update/:id
     */
    @ApiOperation("编辑收货地址")
    @PutMapping("/update/{id}")
    public Result<Void> updateAddress(@PathVariable Long id,
                                      @Validated @RequestBody AddressDTO addressDTO,
                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        addressService.updateAddress(userId, id, addressDTO);
        return Result.success();
    }

    /**
     * 删除收货地址
     * DELETE /api/address/delete/:id
     * - 地址被删除不影响历史订单（订单存的是快照字段）
     * - 删除默认地址时，自动将其他地址设为新默认
     */
    @ApiOperation("删除收货地址")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id,
                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        addressService.deleteAddress(userId, id);
        return Result.success();
    }

    /**
     * 设为默认收货地址
     * POST /api/address/default/:id
     * - 事务保证：先清除所有默认，再设置目标地址
     */
    @ApiOperation("设为默认收货地址")
    @PostMapping("/default/{id}")
    public Result<Void> setDefault(@PathVariable Long id,
                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        addressService.setDefaultAddress(userId, id);
        return Result.success();
    }
}