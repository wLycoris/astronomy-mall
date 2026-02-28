package com.astronomy.mall.module.admin.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 商家回复评价 DTO
 * 接口: POST /api/admin/review/reply/:id
 */
@Data
public class ReviewReplyDTO {

    /**
     * 回复内容（必填，最多500字）
     */
    @NotBlank(message = "回复内容不能为空")
    @Size(max = 500, message = "回复内容不能超过500字")
    private String reply;
}