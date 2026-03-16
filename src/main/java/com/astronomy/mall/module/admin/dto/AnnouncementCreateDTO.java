package com.astronomy.mall.module.admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 系统公告创建 DTO
 *
 * 📌 设计说明:
 * 不新建 tb_announcement 表，直接复用 tb_notification 表。
 * 公告本质是"批量发给所有用户的系统通知"：
 *   - module = "system"
 *   - type   = "announcement"
 *   - related_type = "announcement"
 *   - related_id = 公告分组ID（System.currentTimeMillis() 生成，唯一标识一次公告）
 *
 * 文件路径: com.astronomy.mall.module.admin.dto.AnnouncementCreateDTO
 */
@Data
@ApiModel(description = "创建系统公告请求")
public class AnnouncementCreateDTO {

    /**
     * 公告标题（必填，最多100字）
     */
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 100, message = "公告标题不能超过100个字符")
    @ApiModelProperty(value = "公告标题", required = true, example = "系统维护通知")
    private String title;

    /**
     * 公告内容（必填，最多5000字）
     */
    @NotBlank(message = "公告内容不能为空")
    @Size(max = 5000, message = "公告内容不能超过5000个字符")
    @ApiModelProperty(value = "公告内容", required = true, example = "本系统将于2026-03-20凌晨2点进行例行维护...")
    private String content;

    /**
     * 优先级（0-普通 1-重要 2-紧急）
     * 默认 0-普通
     */
    @ApiModelProperty(value = "优先级(0-普通 1-重要 2-紧急)", example = "0")
    private Integer priority = 0;
}