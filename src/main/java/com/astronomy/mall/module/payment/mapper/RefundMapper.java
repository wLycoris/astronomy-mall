package com.astronomy.mall.module.payment.mapper;

import com.astronomy.mall.module.payment.entity.Refund;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退款记录Mapper
 */
@Mapper
public interface RefundMapper extends BaseMapper<Refund> {
}