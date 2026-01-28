package com.astronomy.mall.module.admin.service;

import com.astronomy.mall.module.admin.dto.ProductLogQueryDTO;
import com.astronomy.mall.module.admin.entity.ProductLog;
import com.astronomy.mall.module.admin.vo.ProductLogVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 商品日志服务接口
 *
 * 路径: com.astronomy.mall.module.admin.service.ProductLogService
 */
public interface ProductLogService extends IService<ProductLog> {

    /**
     * 分页查询商品日志
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    Page<ProductLogVO> getProductLogList(ProductLogQueryDTO dto);

    /**
     * 保存商品日志
     *
     * @param productLog 日志实体
     */
    void saveProductLog(ProductLog productLog);
}