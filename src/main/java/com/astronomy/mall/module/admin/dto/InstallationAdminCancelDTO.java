package com.astronomy.mall.module.admin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 管理员取消安装预约 DTO
 *
 * 📌 文件路径:
 *   module/admin/dto/InstallationAdminCancelDTO.java
 */
@Data
public class InstallationAdminCancelDTO {

    /**
     * 取消原因（必填）
     * 将存入 tb_installation.admin_remark 字段
     */
    @NotBlank(message = "取消原因不能为空")
    private String adminRemark;
}