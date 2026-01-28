package com.astronomy.mall.module.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 库存调整日志表
 */
@Data
@TableName("tb_stock_log")
public class StockLog {

    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称(快照)
     */
    private String productName;

    /**
     * 调整类型(1-增加 2-减少)
     */
    private Integer adjustType;

    /**
     * 调整数量
     */
    private Integer quantity;

    /**
     * 调整前库存
     */
    private Integer beforeStock;

    /**
     * 调整后库存
     */
    private Integer afterStock;

    /**
     * 调整原因
     */
    private String reason;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}