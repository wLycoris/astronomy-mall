package com.astronomy.mall.module.order.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 创建订单请求 DTO
 *
 * 📌 v7.6 改造说明 (2.4.2 收货地址管理):
 *   - 删除: receiverName / receiverPhone / receiverProvince /
 *           receiverCity / receiverDistrict / receiverAddress
 *   - 新增: addressId (收货地址ID)
 *   - 后端 OrderServiceImpl.createOrder() 根据 addressId 查询 tb_address
 *     并将地址信息快照到 tb_order 的 receiver_* 字段
 *   - 地址被删除后，历史订单的收货信息不受影响（快照字段）
 */
@Data
public class CreateOrderDTO {

    /** 购物车ID列表（从购物车发起下单时传入） */
    @NotEmpty(message = "购物车ID列表不能为空")
    private List<Long> cartIds;

    /**
     * 收货地址ID
     * 📌 替换原来的6个地址字段，由后端查询 tb_address 并快照到订单
     * 下单前必须先在"收货地址"页面添加地址
     */
    @NotNull(message = "请选择收货地址")
    private Long addressId;

    /** 订单备注（选填） */
    @Size(max = 200, message = "订单备注不能超过200个字符")
    private String remark;
}