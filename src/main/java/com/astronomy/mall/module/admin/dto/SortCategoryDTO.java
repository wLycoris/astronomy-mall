package com.astronomy.mall.module.admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 分类排序 DTO
 *
 * 📌 前端拖拽排序后，将新的排序列表提交到后端
 * 📌 批量更新每个分类的 sort 字段
 */
@Data
@ApiModel("分类排序DTO")
public class SortCategoryDTO {

    /**
     * 排序项列表
     * 每项包含分类ID和新的排序值
     */
    @NotEmpty(message = "排序列表不能为空")
    @ApiModelProperty(value = "排序项列表", required = true)
    private List<SortItem> items;

    /**
     * 排序项
     */
    @Data
    @ApiModel("排序项")
    public static class SortItem {

        /**
         * 分类ID
         */
        @ApiModelProperty(value = "分类ID", required = true, example = "1")
        private Long id;

        /**
         * 新排序值（数字越大越靠前）
         */
        @ApiModelProperty(value = "新排序值", required = true, example = "10")
        private Integer sort;
    }
}