package com.astronomy.mall.module.admin.service;

import com.astronomy.mall.module.admin.dto.*;
import com.astronomy.mall.module.admin.vo.AdminProductVO;
import com.astronomy.mall.module.admin.vo.StockWarningVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 管理员-商品管理服务
 */
public interface AdminProductService {

    /**
     * 商品列表查询
     */
    Page<AdminProductVO> getProductList(ProductQueryDTO dto);

    /**
     * 商品详情查询
     */
    AdminProductVO getProductDetail(Long id);

    /**
     * 新增商品
     */
    void addProduct(ProductSaveDTO dto);

    /**
     * 编辑商品
     */
    void updateProduct(ProductSaveDTO dto);

    /**
     * 商品上下架
     */
    void updateStatus(ProductStatusDTO dto);

    /**
     * 库存调整
     */
    void adjustStock(Long productId, StockAdjustDTO dto);

    /**
     * 删除商品
     */
    void deleteProduct(Long id);

    /**
     * 库存预警列表
     */
    List<StockWarningVO> getStockWarning();

    /**
     * 批量导入商品
     *
     * @param file Excel文件
     * @return 导入结果统计
     */
    Map<String, Object> importProducts(MultipartFile file);

    /**
     * 批量导出商品
     *
     * @param response HTTP响应
     * @param productName 商品名称(可选)
     * @param categoryId 分类ID(可选)
     * @param status 状态(可选)
     */
    void exportProducts(HttpServletResponse response, String productName,
                        Long categoryId, Integer status) throws IOException;

    /**
     * 下载导入模板
     *
     * @param response HTTP响应
     */
    void downloadTemplate(HttpServletResponse response) throws IOException;
}