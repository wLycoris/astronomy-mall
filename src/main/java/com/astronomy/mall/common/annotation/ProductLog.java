package com.astronomy.mall.common.annotation;

import java.lang.annotation.*;

/**
 * 商品日志注解
 * 用于标记需要记录商品调整日志的方法
 *
 * 路径: com.astronomy.mall.common.annotation.ProductLog
 *
 * 使用示例:
 * @ProductLog(operationType = "修改商品")
 * public void updateProduct(ProductSaveDTO dto) {
 *     // 业务逻辑
 * }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProductLog {

    /**
     * 操作类型
     * 可选值: 新增商品、修改商品、上架商品、下架商品、删除商品
     */
    String operationType() default "";

    /**
     * 备注
     */
    String remark() default "";
}