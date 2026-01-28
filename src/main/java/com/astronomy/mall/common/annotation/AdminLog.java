package com.astronomy.mall.common.annotation;

import java.lang.annotation.*;

/**
 * 管理员操作日志注解
 *
 * 📌 使用说明:
 * 在所有需要记录日志的管理员接口方法上添加此注解
 *
 * 📌 使用示例:
 * @AdminLog("商品上架")
 * public Result<Void> updateStatus(@RequestBody UpdateStatusDTO dto) {
 *     // 业务逻辑
 * }
 *
 * 📌 后续模块使用:
 * - AdminOrderController: @AdminLog("订单发货")
 * - AdminRefundController: @AdminLog("退款审核通过")
 * - AdminReviewController: @AdminLog("删除评价")
 * - AdminUserController: @AdminLog("禁用用户")
 * - AdminCategoryController: @AdminLog("新增分类")
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AdminLog {

    /**
     * 操作描述
     * 例如: "商品上架", "订单发货", "退款审核"
     */
    String value() default "";

    /**
     * 是否记录请求参数 (默认记录)
     */
    boolean recordParams() default true;

    /**
     * 是否记录返回结果 (默认不记录,避免日志过大)
     */
    boolean recordResult() default false;
}