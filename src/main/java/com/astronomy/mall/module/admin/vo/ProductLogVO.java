package com.astronomy.mall.module.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品日志VO
 *
 * 路径: com.astronomy.mall.module.admin.vo.ProductLogVO
 */
@Data
public class ProductLogVO {

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 变更字段列表
     */
    private List<ChangeField> changeFields;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 变更字段内部类
     */
    @Data
    public static class ChangeField {
        /**
         * 字段名(英文)
         */
        private String field;

        /**
         * 字段名(中文)
         */
        private String fieldName;

        /**
         * 旧值
         */
        private String oldValue;

        /**
         * 新值
         */
        private String newValue;
    }
}