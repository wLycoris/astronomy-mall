package com.astronomy.mall.module.payment.mapper;

import com.astronomy.mall.module.payment.entity.Payment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付记录Mapper
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}