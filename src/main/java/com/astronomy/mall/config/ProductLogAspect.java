package com.astronomy.mall.config;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.astronomy.mall.common.annotation.ProductLog;
import com.astronomy.mall.module.admin.dto.ProductSaveDTO;
import com.astronomy.mall.module.admin.dto.ProductStatusDTO;
import com.astronomy.mall.module.admin.service.ProductLogService;
import com.astronomy.mall.module.product.entity.Product;
import com.astronomy.mall.module.product.mapper.ProductMapper;
import com.astronomy.mall.module.user.entity.User;
import com.astronomy.mall.module.user.mapper.UserMapper;
import com.astronomy.mall.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品日志AOP切面
 * 自动记录商品的增删改操作
 *
 * 路径: com.astronomy.mall.config.ProductLogAspect
 */
@Slf4j
@Aspect
@Component
public class ProductLogAspect {

    @Autowired
    private ProductLogService productLogService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 环绕通知: 拦截带有 @ProductLog 注解的方法
     */
    @Around("@annotation(com.astronomy.mall.common.annotation.ProductLog)")
    public Object around(ProceedingJoinPoint point) throws Throwable {

        log.info("========== 商品日志AOP拦截 ==========");

        // 1. 获取注解信息
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        ProductLog productLog = method.getAnnotation(ProductLog.class);
        String operationType = productLog.operationType();
        String remark = productLog.remark();

        log.info("操作类型: {}", operationType);

        // 2. 获取方法参数
        Object[] args = point.getArgs();

        // 3. 获取操作前的商品信息
        Product beforeProduct = null;
        Long productId = null;

        // 根据操作类型获取商品ID
        if (operationType.contains("新增")) {
            // 新增操作,没有beforeProduct
            beforeProduct = null;
        } else if (operationType.contains("修改") || operationType.contains("上架")
                || operationType.contains("下架")) {
            // 修改/上下架操作,从参数中获取商品ID
            productId = extractProductId(args);
            if (productId != null) {
                beforeProduct = productMapper.selectById(productId);
            }
        } else if (operationType.contains("删除")) {
            // 删除操作,从参数中获取商品ID
            productId = (Long) args[0];
            beforeProduct = productMapper.selectById(productId);
        }

        // 4. 执行目标方法
        Object result = point.proceed();

        // 5. 获取操作后的商品信息
        Product afterProduct = null;

        if (operationType.contains("新增")) {
            // 新增操作,从参数中提取商品ID
            if (args[0] instanceof ProductSaveDTO) {
                ProductSaveDTO dto = (ProductSaveDTO) args[0];
                // 新增后需要查询数据库获取生成的ID
                afterProduct = productMapper.selectById(dto.getId());
                productId = dto.getId();
            }
        } else if (operationType.contains("删除")) {
            // 删除操作,afterProduct为null
            afterProduct = null;
        } else {
            // 其他操作,查询最新的商品信息
            if (productId != null) {
                afterProduct = productMapper.selectById(productId);
            }
        }

        // 6. 记录日志
        try {
            saveProductLog(operationType, beforeProduct, afterProduct, productId, remark);
        } catch (Exception e) {
            log.error("❌ 保存商品日志失败: {}", e.getMessage(), e);
        }

        log.info("========== 商品日志AOP处理完成 ==========");
        return result;
    }

    /**
     * 保存商品日志
     */
    private void saveProductLog(String operationType, Product beforeProduct,
                                Product afterProduct, Long productId, String remark) {

        // 1. 获取当前登录用户
        Long operatorId = UserContext.getUserId();
        if (operatorId == null) {
            log.warn("⚠️ 无法获取操作人ID,跳过日志记录");
            return;
        }

        User operator = userMapper.selectById(operatorId);
        String operatorName = operator != null ? operator.getUsername() : "未知";

        // 2. 获取IP地址
        String ipAddress = getIpAddress();

        // 3. 对比变更字段
        List<ChangeField> changeFields = compareProducts(beforeProduct, afterProduct, operationType);

        // 4. 构建日志对象
        com.astronomy.mall.module.admin.entity.ProductLog logEntity =
                new com.astronomy.mall.module.admin.entity.ProductLog();

        logEntity.setProductId(productId);
        logEntity.setProductName(afterProduct != null ? afterProduct.getProductName() :
                (beforeProduct != null ? beforeProduct.getProductName() : "未知"));
        logEntity.setOperationType(operationType);
        logEntity.setChangeFields(JSON.toJSONString(changeFields));
        logEntity.setOperatorId(operatorId);
        logEntity.setOperatorName(operatorName);
        logEntity.setIpAddress(ipAddress);
        logEntity.setRemark(remark);

        // 5. 保存日志
        productLogService.saveProductLog(logEntity);
    }

    /**
     * 对比两个商品对象,找出变更的字段
     */
    private List<ChangeField> compareProducts(Product before, Product after, String operationType) {

        List<ChangeField> changes = new ArrayList<>();

        // 新增操作
        if (operationType.contains("新增")) {
            if (after != null) {
                changes.add(new ChangeField("productName", "商品名称", "", after.getProductName()));
                changes.add(new ChangeField("price", "价格", "", after.getPrice().toString()));
                changes.add(new ChangeField("stock", "库存", "", after.getStock().toString()));
            }
            return changes;
        }

        // 删除操作
        if (operationType.contains("删除")) {
            if (before != null) {
                changes.add(new ChangeField("productName", "商品名称", before.getProductName(), ""));
                changes.add(new ChangeField("status", "状态", "已上架", "已删除"));
            }
            return changes;
        }

        // 修改/上下架操作
        if (before == null || after == null) {
            return changes;
        }

        // 对比各个字段
        compareField(changes, "productName", "商品名称", before.getProductName(), after.getProductName());
        compareField(changes, "categoryId", "分类ID", before.getCategoryId(), after.getCategoryId());
        compareField(changes, "brand", "品牌", before.getBrand(), after.getBrand());
        compareField(changes, "price", "价格", before.getPrice(), after.getPrice());
        compareField(changes, "stock", "库存", before.getStock(), after.getStock());
        compareField(changes, "status", "状态",
                before.getStatus() == 1 ? "已上架" : "已下架",
                after.getStatus() == 1 ? "已上架" : "已下架");
        compareField(changes, "mainImage", "主图", before.getMainImage(), after.getMainImage());
        compareField(changes, "detail", "商品详情", before.getDetail(), after.getDetail());

        return changes;
    }

    /**
     * 对比单个字段
     */
    private void compareField(List<ChangeField> changes, String field, String fieldName,
                              Object oldValue, Object newValue) {

        String oldStr = oldValue != null ? oldValue.toString() : "";
        String newStr = newValue != null ? newValue.toString() : "";

        if (!oldStr.equals(newStr)) {
            changes.add(new ChangeField(field, fieldName, oldStr, newStr));
        }
    }

    /**
     * 从方法参数中提取商品ID
     */
    private Long extractProductId(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof ProductSaveDTO) {
                return ((ProductSaveDTO) arg).getId();
            } else if (arg instanceof ProductStatusDTO) {
                List<Long> ids = ((ProductStatusDTO) arg).getProductIds();
                return ids != null && !ids.isEmpty() ? ids.get(0) : null;
            } else if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        return null;
    }

    /**
     * 获取客户端IP地址
     */
    private String getIpAddress() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("Proxy-Client-IP");
                }
                if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("WL-Proxy-Client-IP");
                }
                if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            log.error("获取IP地址失败: {}", e.getMessage());
        }
        return "未知";
    }

    /**
     * 变更字段内部类
     */
    private static class ChangeField {
        private String field;
        private String fieldName;
        private String oldValue;
        private String newValue;

        public ChangeField(String field, String fieldName, String oldValue, String newValue) {
            this.field = field;
            this.fieldName = fieldName;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public String getField() {
            return field;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getOldValue() {
            return oldValue;
        }

        public String getNewValue() {
            return newValue;
        }
    }
}