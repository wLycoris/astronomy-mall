package com.astronomy.mall.module.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.astronomy.mall.common.annotation.ProductLog;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.ResultCode;
import com.astronomy.mall.module.admin.dto.*;
import com.astronomy.mall.module.admin.entity.StockLog;
import com.astronomy.mall.module.admin.mapper.StockLogMapper;
import com.astronomy.mall.module.admin.service.AdminProductService;
import com.astronomy.mall.module.admin.vo.AdminProductVO;
import com.astronomy.mall.module.admin.vo.ProductExportVO;
import com.astronomy.mall.module.admin.vo.StockWarningVO;
import com.astronomy.mall.module.cart.entity.Cart;
import com.astronomy.mall.module.cart.mapper.CartMapper;
import com.astronomy.mall.module.favorite.mapper.ProductFavoriteMapper;  // 🆕
import com.astronomy.mall.module.notification.helper.NotificationHelper;  // 🆕
import com.astronomy.mall.module.order.entity.OrderItem;
import com.astronomy.mall.module.order.mapper.OrderItemMapper;
import com.astronomy.mall.module.product.entity.Category;
import com.astronomy.mall.module.product.entity.Product;
import com.astronomy.mall.module.product.mapper.ProductMapper;
import com.astronomy.mall.module.product.service.CategoryService;
import com.astronomy.mall.module.user.entity.User;
import com.astronomy.mall.module.user.mapper.UserMapper;
import com.astronomy.mall.utils.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 管理员-商品管理服务实现
 */
@Slf4j
@Service
public class AdminProductServiceImpl implements AdminProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private StockLogMapper stockLogMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired  // 🆕
    private ProductFavoriteMapper productFavoriteMapper;

    @Autowired  // 🆕
    private NotificationHelper notificationHelper;

    /**
     * 1. 商品列表查询 (分页/搜索/筛选)
     */
    @Override
    public Page<AdminProductVO> getProductList(ProductQueryDTO dto) {

        log.info("========== 查询商品列表 ==========");
        log.info("请求参数: pageNum={}, pageSize={}, productName={}, status={}, brand={}",
                dto.getPageNum(), dto.getPageSize(), dto.getProductName(), dto.getStatus(), dto.getBrand());

        // 1. 构建分页对象
        Page<Product> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        log.info("分页对象: current={}, size={}", page.getCurrent(), page.getSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 商品名称模糊查询
        if (StrUtil.isNotBlank(dto.getProductName())) {
            wrapper.like(Product::getProductName, dto.getProductName());
        }

        // 分类筛选
        if (dto.getCategoryId() != null) {
            wrapper.eq(Product::getCategoryId, dto.getCategoryId());
        }

        // 品牌筛选
        if (StrUtil.isNotBlank(dto.getBrand())) {
            wrapper.eq(Product::getBrand, dto.getBrand());
        }

        // 状态筛选
        if (dto.getStatus() != null) {
            wrapper.eq(Product::getStatus, dto.getStatus());
        }

        // 价格区间筛选
        if (dto.getMinPrice() != null) {
            wrapper.ge(Product::getPrice, dto.getMinPrice());
        }
        if (dto.getMaxPrice() != null) {
            wrapper.le(Product::getPrice, dto.getMaxPrice());
        }

        // 库存不足筛选 (库存<10)
        if (dto.getLowStock() != null && dto.getLowStock()) {
            wrapper.lt(Product::getStock, 10);
        }

        // 排除逻辑删除
        wrapper.eq(Product::getDeleted, 0);

        // 🔧 按ID正序排列（ID小的在前，先创建的在前）
        wrapper.orderByAsc(Product::getId);

        // 3. 执行查询
        Page<Product> productPage = productMapper.selectPage(page, wrapper);

        log.info("查询到 {} 条商品, 总数: {}", productPage.getRecords().size(), productPage.getTotal());

        // 4. 转换为VO
        Page<AdminProductVO> voPage = new Page<>(
                productPage.getCurrent(),
                productPage.getSize(),
                productPage.getTotal()
        );

        List<AdminProductVO> voList = new ArrayList<>();
        for (Product product : productPage.getRecords()) {
            voList.add(convertToVO(product));
        }

        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 2. 商品详情查询
     */
    @Override
    public AdminProductVO getProductDetail(Long id) {
        log.info("查询商品详情, productId: {}", id);

        Product product = productMapper.selectById(id);
        if (product == null || product.getDeleted() == 1) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        return convertToVO(product);
    }

    /**
     * 3. 新增商品
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @ProductLog(operationType = "新增商品")
    public void addProduct(ProductSaveDTO dto) {

        log.info("新增商品, 商品名: {}", dto.getProductName());

        // 1. 校验分类是否存在
        Category category = categoryService.getById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }

        // 2. 转换为实体
        Product product = new Product();
        BeanUtil.copyProperties(dto, product);

        // 设置初始值
        product.setSales(0);
        product.setViewCount(0);
        product.setStatus(1); // 默认上架
        product.setDeleted(0);

        // 3. 保存
        int result = productMapper.insert(product);
        if (result <= 0) {
            throw new BusinessException("新增商品失败");
        }

        log.info("新增商品成功, productId: {}", product.getId());
    }

    /**
     * 4. 编辑商品
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @ProductLog(operationType = "修改商品")
    public void updateProduct(ProductSaveDTO dto) {

        log.info("编辑商品, productId: {}", dto.getId());

        // 1. 检查商品是否存在
        Product product = productMapper.selectById(dto.getId());
        if (product == null || product.getDeleted() == 1) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 2. 校验分类是否存在
        if (!dto.getCategoryId().equals(product.getCategoryId())) {
            Category category = categoryService.getById(dto.getCategoryId());
            if (category == null) {
                throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
            }
        }

        // 3. 更新
        BeanUtil.copyProperties(dto, product, "id", "sales", "viewCount", "createTime", "deleted");

        int result = productMapper.updateById(product);
        if (result <= 0) {
            throw new BusinessException("编辑商品失败");
        }

        log.info("编辑商品成功, productId: {}", product.getId());
    }

    /**
     * 5. 商品上下架 (批量)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @ProductLog(operationType = "修改商品状态")
    public void updateStatus(ProductStatusDTO dto) {

        log.info("批量修改商品状态, productIds: {}, status: {}", dto.getProductIds(), dto.getStatus());

        List<Long> productIds = dto.getProductIds();
        Integer status = dto.getStatus();

        // 1. 检查商品是否存在
        List<Product> products = productMapper.selectBatchIds(productIds);
        if (products.size() != productIds.size()) {
            throw new BusinessException("部分商品不存在或已删除");
        }

        // 2. 批量更新状态
        for (Product product : products) {
            product.setStatus(status);
            productMapper.updateById(product);
        }

        log.info("批量修改商品状态成功, 共 {} 件商品", products.size());

        // 🆕 3. 上架时，通知收藏该商品的用户
        if (status == 1) {
            for (Product product : products) {
                try {
                    List<Long> userIds = productFavoriteMapper.selectFavoriteUserIds(product.getId());
                    for (Long userId : userIds) {
                        notificationHelper.sendProductOnSaleNotification(userId, product.getProductName(), product.getId());
                    }
                    if (!userIds.isEmpty()) {
                        log.info("商品上架通知已发送, productId={}, 通知人数={}", product.getId(), userIds.size());
                    }
                } catch (Exception e) {
                    // 通知失败不影响主流程
                    log.error("发送商品上架通知失败, productId={}", product.getId(), e);
                }
            }
        }
    }

    /**
     * 6. 库存调整 (自动记录日志)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustStock(Long productId, StockAdjustDTO dto) {

        log.info("========== 调整库存 ==========");
        log.info("调整商品ID: {}, 类型: {}, 数量: {}", productId, dto.getAdjustType(), dto.getQuantity());

        // 1. 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted() == 1) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 2. 记录调整前库存
        Integer beforeStock = product.getStock();
        Integer newStock;

        // 3. 计算调整后的库存
        if (dto.getAdjustType() == 1) {
            // 增加库存
            newStock = beforeStock + dto.getQuantity();
        } else {
            // 减少库存
            newStock = beforeStock - dto.getQuantity();
            if (newStock < 0) {
                throw new BusinessException("库存不足,无法减少");
            }
        }

        // 4. 更新库存
        product.setStock(newStock);
        int result = productMapper.updateById(product);
        if (result <= 0) {
            throw new BusinessException("库存调整失败");
        }

        // 5. 🔥 记录库存日志
        saveStockLog(product, dto, beforeStock, newStock);

        log.info("✅ 库存调整成功, 原库存: {}, 新库存: {}", beforeStock, newStock);
    }

    /**
     * 🔥 保存库存调整日志
     */
    private void saveStockLog(Product product, StockAdjustDTO dto, Integer beforeStock, Integer afterStock) {

        // 1. 获取当前登录的管理员信息
        Long operatorId = UserContext.getUserId();
        if (operatorId == null) {
            log.warn("⚠️ 无法获取操作人ID,跳过日志记录");
            return;
        }

        User operator = userMapper.selectById(operatorId);
        String operatorName = operator != null ? operator.getUsername() : "未知";

        // 2. 创建日志记录
        StockLog stockLog = new StockLog();
        stockLog.setProductId(product.getId());
        stockLog.setProductName(product.getProductName());
        stockLog.setAdjustType(dto.getAdjustType());
        stockLog.setQuantity(dto.getQuantity());
        stockLog.setBeforeStock(beforeStock);
        stockLog.setAfterStock(afterStock);
        stockLog.setReason(dto.getReason());
        stockLog.setOperatorId(operatorId);
        stockLog.setOperatorName(operatorName);

        // 3. 保存日志
        int result = stockLogMapper.insert(stockLog);
        if (result > 0) {
            log.info("✅ 库存日志记录成功, logId: {}", stockLog.getId());
        } else {
            log.error("❌ 库存日志记录失败");
        }
    }

    /**
     * 7. 删除商品 (物理删除 - 直接从数据库删除)
     *
     * ⚠️ 注意:
     * - 物理删除会永久删除数据,无法恢复
     * - 删除前需要检查是否有关联数据(订单、购物车等)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @ProductLog(operationType = "删除商品")
    public void deleteProduct(Long id) {

        log.info("========== 物理删除商品 ==========");
        log.info("删除商品, productId: {}", id);

        // 1. 检查商品是否存在
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 2. 检查商品是否在购物车中被使用
        LambdaQueryWrapper<Cart> cartWrapper = new LambdaQueryWrapper<>();
        cartWrapper.eq(Cart::getProductId, id);
        Long cartCount = cartMapper.selectCount(cartWrapper);

        if (cartCount > 0) {
            throw new BusinessException("该商品在购物车中被使用,无法删除");
        }

        // 3. 检查商品是否在订单中被使用
        LambdaQueryWrapper<OrderItem> orderItemWrapper = new LambdaQueryWrapper<>();
        orderItemWrapper.eq(OrderItem::getProductId, id);
        Long orderItemCount = orderItemMapper.selectCount(orderItemWrapper);

        if (orderItemCount > 0) {
            throw new BusinessException("该商品在订单中被使用,无法删除");
        }

        // 4. 物理删除商品 (直接从数据库删除)
        int result = productMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException("删除商品失败");
        }

        log.info("✅ 物理删除商品成功, productId: {}, productName: {}", id, product.getProductName());
    }

    /**
     * 8. 库存预警列表
     */
    @Override
    public List<StockWarningVO> getStockWarning() {

        log.info("查询库存预警列表");

        // 1. 查询库存不足的商品 (库存<10或=0)
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(Product::getStock, 10) // 库存<10
                .eq(Product::getDeleted, 0) // 未删除
                .eq(Product::getStatus, 1) // 已上架
                .orderByAsc(Product::getStock) // 按库存升序
                .orderByDesc(Product::getSales); // 再按销量降序

        List<Product> products = productMapper.selectList(wrapper);

        // 2. 转换为VO
        List<StockWarningVO> voList = new ArrayList<>();

        for (Product product : products) {
            StockWarningVO vo = new StockWarningVO();
            vo.setProductId(product.getId());
            vo.setProductName(product.getProductName());
            vo.setMainImage(product.getMainImage());
            vo.setStock(product.getStock());
            vo.setSales(product.getSales());

            // 预警等级
            if (product.getStock() == 0) {
                vo.setWarningLevel(2);
                vo.setWarningDesc("缺货");
            } else {
                vo.setWarningLevel(1);
                vo.setWarningDesc("低库存");
            }

            // 分类名称
            Category category = categoryService.getById(product.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }

            voList.add(vo);
        }

        log.info("库存预警列表查询成功, 共 {} 件商品", voList.size());
        return voList;
    }

    /**
     * Entity转VO (私有方法)
     */
    private AdminProductVO convertToVO(Product product) {
        AdminProductVO vo = new AdminProductVO();

        // ✅ Hutool 的 copyProperties 会自动复制所有同名字段
        // 包括: images, detail, specifications, tags, keywords
        BeanUtil.copyProperties(product, vo);

        // 状态描述
        vo.setStatusDesc(product.getStatus() == 1 ? "已上架" : "已下架");

        // 分类名称
        try {
            Category category = categoryService.getById(product.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
        } catch (Exception e) {
            log.warn("获取分类名称失败, categoryId: {}", product.getCategoryId());
            vo.setCategoryName("未知分类");
        }

        return vo;
    }

    /**
     * 批量导入商品
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importProducts(MultipartFile file) {

        log.info("========== 开始批量导入商品 ==========");

        Map<String, Object> result = new HashMap<>();
        int successCount = 0;  // 成功数量
        int failCount = 0;     // 失败数量
        List<String> errorList = new ArrayList<>();  // 错误信息

        try {
            // 1. 校验文件
            if (file == null || file.isEmpty()) {
                throw new BusinessException("请选择要导入的文件");
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
                throw new BusinessException("只支持Excel文件(.xlsx或.xls)");
            }

            // 文件大小限制 10MB
            if (file.getSize() > 10 * 1024 * 1024) {
                throw new BusinessException("文件大小不能超过10MB");
            }

            // 2. 读取Excel
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());

            // 设置表头别名(Excel列名 → DTO字段名)
            reader.addHeaderAlias("商品名称", "productName");
            reader.addHeaderAlias("分类ID", "categoryId");
            reader.addHeaderAlias("副标题", "subTitle");
            reader.addHeaderAlias("品牌", "brand");
            reader.addHeaderAlias("价格", "price");
            reader.addHeaderAlias("原价", "originalPrice");
            reader.addHeaderAlias("库存", "stock");
            reader.addHeaderAlias("主图URL", "mainImage");
            reader.addHeaderAlias("商品图片", "images");
            reader.addHeaderAlias("商品详情", "detail");
            reader.addHeaderAlias("规格参数", "specifications");
            reader.addHeaderAlias("搜索关键词", "keywords");
            reader.addHeaderAlias("商品标签", "tags");
            reader.addHeaderAlias("是否推荐", "isRecommend");
            reader.addHeaderAlias("是否热卖", "isHot");
            reader.addHeaderAlias("是否新品", "isNew");

            // 3. 读取数据
            List<ProductImportDTO> importList = reader.readAll(ProductImportDTO.class);

            log.info("读取到 {} 条商品数据", importList.size());

            if (importList.isEmpty()) {
                throw new BusinessException("Excel文件中没有数据");
            }

            if (importList.size() > 1000) {
                throw new BusinessException("单次导入不能超过1000条数据");
            }

            // 4. 逐行导入
            for (int i = 0; i < importList.size(); i++) {
                ProductImportDTO importDto = importList.get(i);
                int rowNum = i + 2;  // Excel行号(第1行是表头,从第2行开始)

                try {
                    // 4.1 数据校验
                    validateImportProduct(importDto, rowNum);

                    // 4.2 转换为实体
                    Product product = convertToProduct(importDto);

                    // 4.3 插入数据库
                    int insertResult = productMapper.insert(product);
                    if (insertResult > 0) {
                        successCount++;
                        log.info("第{}行导入成功: {}", rowNum, product.getProductName());
                    } else {
                        failCount++;
                        errorList.add("第" + rowNum + "行: 插入数据库失败");
                    }

                } catch (Exception e) {
                    failCount++;
                    String errorMsg = e.getMessage();
                    if (errorMsg.length() > 100) {
                        errorMsg = errorMsg.substring(0, 100) + "...";
                    }
                    errorList.add("第" + rowNum + "行: " + errorMsg);
                    log.error("第{}行导入失败: {}", rowNum, e.getMessage());
                }
            }

            // 5. 返回结果
            result.put("total", importList.size());
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("errorList", errorList);

            log.info("========== 导入完成 ==========");
            log.info("总数: {}, 成功: {}, 失败: {}", importList.size(), successCount, failCount);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量导入异常: ", e);
            throw new BusinessException("批量导入失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 校验导入的商品数据
     */
    private void validateImportProduct(ProductImportDTO dto, int rowNum) {

        // 1. 商品名称
        if (StrUtil.isBlank(dto.getProductName())) {
            throw new BusinessException("商品名称不能为空");
        }
        if (dto.getProductName().length() > 200) {
            throw new BusinessException("商品名称长度不能超过200字符");
        }

        // 2. 分类ID
        if (dto.getCategoryId() == null) {
            throw new BusinessException("分类ID不能为空");
        }
        Category category = categoryService.getById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException("分类ID不存在: " + dto.getCategoryId());
        }

        // 3. 价格
        if (dto.getPrice() == null) {
            throw new BusinessException("价格不能为空");
        }
        if (dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("价格必须大于0");
        }
        if (dto.getPrice().compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new BusinessException("价格不能超过99999999.99");
        }

        // 4. 原价校验
        if (dto.getOriginalPrice() != null) {
            if (dto.getOriginalPrice().compareTo(dto.getPrice()) < 0) {
                throw new BusinessException("原价不能低于现价");
            }
        }

        // 5. 库存
        if (dto.getStock() == null) {
            throw new BusinessException("库存不能为空");
        }
        if (dto.getStock() < 0) {
            throw new BusinessException("库存不能为负数");
        }

        // 6. 主图
        if (StrUtil.isBlank(dto.getMainImage())) {
            throw new BusinessException("主图URL不能为空");
        }

        // 7. 品牌长度
        if (dto.getBrand() != null && dto.getBrand().length() > 100) {
            throw new BusinessException("品牌名称长度不能超过100字符");
        }
    }

    /**
     * 将导入DTO转换为Product实体
     */
    private Product convertToProduct(ProductImportDTO dto) {
        Product product = new Product();

        product.setProductName(dto.getProductName());
        product.setCategoryId(dto.getCategoryId());
        product.setSubTitle(dto.getSubTitle());
        product.setBrand(dto.getBrand());
        product.setPrice(dto.getPrice());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setStock(dto.getStock());
        product.setMainImage(dto.getMainImage());
        product.setImages(dto.getImages());
        product.setDetail(dto.getDetail());
        product.setSpecifications(dto.getSpecifications());
        product.setKeywords(dto.getKeywords());
        product.setTags(dto.getTags());

        // 设置默认值
        product.setSales(0);
        product.setViewCount(0);
        product.setStatus(1);  // 默认上架
        product.setDeleted(0);

        // 是否推荐/热卖/新品
        product.setIsRecommend(dto.getIsRecommend() != null ? dto.getIsRecommend() : 0);
        product.setIsHot(dto.getIsHot() != null ? dto.getIsHot() : 0);
        product.setIsNew(dto.getIsNew() != null ? dto.getIsNew() : 0);

        return product;
    }

    /**
     * 批量导出商品
     */
    @Override
    public void exportProducts(HttpServletResponse response, String productName,
                               Long categoryId, Integer status) throws IOException {

        log.info("========== 开始批量导出商品 ==========");

        try {
            // 1. 查询商品数据
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

            if (StrUtil.isNotBlank(productName)) {
                wrapper.like(Product::getProductName, productName);
            }
            if (categoryId != null) {
                wrapper.eq(Product::getCategoryId, categoryId);
            }
            if (status != null) {
                wrapper.eq(Product::getStatus, status);
            }
            wrapper.eq(Product::getDeleted, 0);
            wrapper.orderByDesc(Product::getCreateTime);

            List<Product> productList = productMapper.selectList(wrapper);

            log.info("查询到 {} 条商品数据", productList.size());

            // 2. 转换为导出VO
            List<ProductExportVO> exportList = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (Product product : productList) {
                ProductExportVO vo = new ProductExportVO();

                // 复制基本属性
                vo.setId(product.getId());
                vo.setProductName(product.getProductName());
                vo.setCategoryId(product.getCategoryId());
                vo.setSubTitle(product.getSubTitle());
                vo.setBrand(product.getBrand());
                vo.setPrice(product.getPrice());
                vo.setOriginalPrice(product.getOriginalPrice());
                vo.setStock(product.getStock());
                vo.setSales(product.getSales());
                vo.setMainImage(product.getMainImage());
                vo.setImages(product.getImages());
                vo.setDetail(product.getDetail());
                vo.setSpecifications(product.getSpecifications());
                vo.setKeywords(product.getKeywords());
                vo.setTags(product.getTags());
                vo.setStatus(product.getStatus());
                vo.setStatusDesc(product.getStatus() == 1 ? "已上架" : "已下架");
                vo.setIsRecommend(product.getIsRecommend());
                vo.setIsHot(product.getIsHot());
                vo.setIsNew(product.getIsNew());
                vo.setViewCount(product.getViewCount());
                vo.setCreateTime(product.getCreateTime());
                vo.setUpdateTime(product.getUpdateTime());

                // 查询分类名称
                Category category = categoryService.getById(product.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getCategoryName());
                }

                exportList.add(vo);
            }

            // 3. 创建Excel写入器
            ExcelWriter writer = ExcelUtil.getWriter();

            // 4. 设置表头别名(实体类字段名 → Excel列名)
            writer.addHeaderAlias("id", "商品ID");
            writer.addHeaderAlias("productName", "商品名称");
            writer.addHeaderAlias("categoryId", "分类ID");
            writer.addHeaderAlias("categoryName", "分类名称");
            writer.addHeaderAlias("subTitle", "副标题");
            writer.addHeaderAlias("brand", "品牌");
            writer.addHeaderAlias("price", "价格");
            writer.addHeaderAlias("originalPrice", "原价");
            writer.addHeaderAlias("stock", "库存");
            writer.addHeaderAlias("sales", "销量");
            writer.addHeaderAlias("mainImage", "主图URL");
            writer.addHeaderAlias("images", "商品图片");
            writer.addHeaderAlias("detail", "商品详情");
            writer.addHeaderAlias("specifications", "规格参数");
            writer.addHeaderAlias("keywords", "搜索关键词");
            writer.addHeaderAlias("tags", "商品标签");
            writer.addHeaderAlias("statusDesc", "状态");
            writer.addHeaderAlias("isRecommend", "是否推荐");
            writer.addHeaderAlias("isHot", "是否热卖");
            writer.addHeaderAlias("isNew", "是否新品");
            writer.addHeaderAlias("viewCount", "浏览次数");
            writer.addHeaderAlias("createTime", "创建时间");
            writer.addHeaderAlias("updateTime", "更新时间");

            // 5. 写入数据
            writer.write(exportList, true);

            // 6. 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("商品列表_" + System.currentTimeMillis(), "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

            // 7. 输出到浏览器
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out, true);
            writer.close();
            IoUtil.close(out);

            log.info("========== 导出完成,共导出 {} 条数据 ==========", exportList.size());

        } catch (Exception e) {
            log.error("批量导出异常: ", e);
            throw new BusinessException("批量导出失败: " + e.getMessage());
        }
    }

    /**
     * 下载导入模板
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) throws IOException {

        log.info("========== 下载商品导入模板 ==========");

        try {
            // 1. 创建Excel写入器
            ExcelWriter writer = ExcelUtil.getWriter();

            // 2. 设置表头
            writer.addHeaderAlias("productName", "商品名称");
            writer.addHeaderAlias("categoryId", "分类ID");
            writer.addHeaderAlias("subTitle", "副标题");
            writer.addHeaderAlias("brand", "品牌");
            writer.addHeaderAlias("price", "价格");
            writer.addHeaderAlias("originalPrice", "原价");
            writer.addHeaderAlias("stock", "库存");
            writer.addHeaderAlias("mainImage", "主图URL");
            writer.addHeaderAlias("images", "商品图片");
            writer.addHeaderAlias("detail", "商品详情");
            writer.addHeaderAlias("specifications", "规格参数");
            writer.addHeaderAlias("keywords", "搜索关键词");
            writer.addHeaderAlias("tags", "商品标签");
            writer.addHeaderAlias("isRecommend", "是否推荐");
            writer.addHeaderAlias("isHot", "是否热卖");
            writer.addHeaderAlias("isNew", "是否新品");

            // 3. 写入示例数据
            List<Map<String, Object>> rows = new ArrayList<>();

            Map<String, Object> row1 = new LinkedHashMap<>();
            row1.put("productName", "天文望远镜");
            row1.put("categoryId", 1);
            row1.put("subTitle", "入门级高清观星望远镜");
            row1.put("brand", "星特朗");
            row1.put("price", 1999.00);
            row1.put("originalPrice", 2999.00);
            row1.put("stock", 100);
            row1.put("mainImage", "http://example.com/telescope.jpg");
            row1.put("images", "http://example.com/1.jpg,http://example.com/2.jpg");
            row1.put("detail", "<p>商品详情HTML内容</p>");
            row1.put("specifications", "{\"口径\":\"80mm\",\"焦距\":\"900mm\"}");
            row1.put("keywords", "望远镜,观星,入门");
            row1.put("tags", "[\"天文望远镜\",\"入门级\",\"便携式\"]");
            row1.put("isRecommend", 1);
            row1.put("isHot", 0);
            row1.put("isNew", 1);
            rows.add(row1);

            Map<String, Object> row2 = new LinkedHashMap<>();
            row2.put("productName", "赤道仪");
            row2.put("categoryId", 2);
            row2.put("subTitle", "精准追踪赤道仪");
            row2.put("brand", "信达");
            row2.put("price", 2999.00);
            row2.put("originalPrice", 3999.00);
            row2.put("stock", 50);
            row2.put("mainImage", "http://example.com/mount.jpg");
            row2.put("images", "http://example.com/3.jpg");
            row2.put("detail", "<p>赤道仪详情</p>");
            row2.put("specifications", "{\"承重\":\"10kg\",\"精度\":\"±10\\\"\"}");
            row2.put("keywords", "赤道仪,追星,摄影");
            row2.put("tags", "[\"赤道仪\",\"专业级\"]");
            row2.put("isRecommend", 0);
            row2.put("isHot", 1);
            row2.put("isNew", 0);
            rows.add(row2);

            writer.write(rows, true);

            // 4. 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("商品导入模板", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

            // 5. 输出到浏览器
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out, true);
            writer.close();
            IoUtil.close(out);

            log.info("========== 模板下载完成 ==========");

        } catch (Exception e) {
            log.error("下载模板异常: ", e);
            throw new BusinessException("下载模板失败: " + e.getMessage());
        }
    }
}