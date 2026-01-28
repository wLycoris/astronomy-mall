package com.astronomy.mall.module.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.astronomy.mall.module.admin.dto.ProductLogQueryDTO;
import com.astronomy.mall.module.admin.entity.ProductLog;
import com.astronomy.mall.module.admin.mapper.ProductLogMapper;
import com.astronomy.mall.module.admin.service.ProductLogService;
import com.astronomy.mall.module.admin.vo.ProductLogVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品日志服务实现
 *
 * 路径: com.astronomy.mall.module.admin.service.impl.ProductLogServiceImpl
 */
@Slf4j
@Service
public class ProductLogServiceImpl extends ServiceImpl<ProductLogMapper, ProductLog>
        implements ProductLogService {

    @Override
    public Page<ProductLogVO> getProductLogList(ProductLogQueryDTO dto) {

        log.info("========== 查询商品日志列表 ==========");
        log.info("请求参数: {}", JSON.toJSONString(dto));

        // 1. 构建分页对象
        Page<ProductLog> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<ProductLog> wrapper = new LambdaQueryWrapper<>();

        // 商品ID
        if (dto.getProductId() != null) {
            wrapper.eq(ProductLog::getProductId, dto.getProductId());
        }

        // 商品名称 (模糊查询)
        if (StrUtil.isNotBlank(dto.getProductName())) {
            wrapper.like(ProductLog::getProductName, dto.getProductName());
        }

        // 操作类型
        if (StrUtil.isNotBlank(dto.getOperationType())) {
            wrapper.eq(ProductLog::getOperationType, dto.getOperationType());
        }

        // 操作人ID
        if (dto.getOperatorId() != null) {
            wrapper.eq(ProductLog::getOperatorId, dto.getOperatorId());
        }

        // 操作人姓名 (模糊查询)
        if (StrUtil.isNotBlank(dto.getOperatorName())) {
            wrapper.like(ProductLog::getOperatorName, dto.getOperatorName());
        }

        // 时间范围
        if (dto.getStartTime() != null) {
            wrapper.ge(ProductLog::getCreateTime, dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            wrapper.le(ProductLog::getCreateTime, dto.getEndTime());
        }

        // 按创建时间倒序排列
        wrapper.orderByDesc(ProductLog::getCreateTime);

        // 3. 执行查询
        Page<ProductLog> productLogPage = this.page(page, wrapper);

        // 4. 转换为VO
        Page<ProductLogVO> voPage = new Page<>(
                productLogPage.getCurrent(),
                productLogPage.getSize(),
                productLogPage.getTotal()
        );

        List<ProductLogVO> voList = new ArrayList<>();
        for (ProductLog productLog : productLogPage.getRecords()) {
            voList.add(convertToVO(productLog));
        }
        voPage.setRecords(voList);

        log.info("查询到 {} 条商品日志, 总数: {}", voList.size(), voPage.getTotal());
        return voPage;
    }

    @Override
    public void saveProductLog(ProductLog productLog) {
        log.info("保存商品日志: productId={}, operationType={}",
                productLog.getProductId(), productLog.getOperationType());

        int result = this.baseMapper.insert(productLog);
        if (result > 0) {
            log.info("✅ 商品日志保存成功, logId: {}", productLog.getId());
        } else {
            log.error("❌ 商品日志保存失败");
        }
    }

    /**
     * Entity转VO
     */
    private ProductLogVO convertToVO(ProductLog productLog) {
        ProductLogVO vo = new ProductLogVO();
        BeanUtil.copyProperties(productLog, vo);

        // 解析 changeFields JSON 字符串
        if (StrUtil.isNotBlank(productLog.getChangeFields())) {
            try {
                List<ProductLogVO.ChangeField> changeFields =
                        JSON.parseArray(productLog.getChangeFields(), ProductLogVO.ChangeField.class);
                vo.setChangeFields(changeFields);
            } catch (Exception e) {
                log.warn("解析变更字段失败: {}", e.getMessage());
                vo.setChangeFields(new ArrayList<>());
            }
        } else {
            vo.setChangeFields(new ArrayList<>());
        }

        return vo;
    }
}