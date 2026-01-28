package com.astronomy.mall.module.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商品调整日志表
 *
 * 路径: com.astronomy.mall.module.admin.entity.ProductLog
 */
@Data
@TableName("tb_product_log")
public class ProductLog {

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
     * 操作类型(新增/修改/上架/下架/删除)
     */
    private String operationType;

    /**
     * 变更字段(JSON格式)
     * 格式: [{"field":"price","fieldName":"价格","oldValue":"1999","newValue":"2999"}]
     */
    private String changeFields;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}