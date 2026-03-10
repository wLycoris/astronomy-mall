package com.astronomy.mall.module.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 管理员确认安装预约 DTO
 *
 * 📌 文件路径:
 *   module/admin/dto/InstallationConfirmDTO.java
 *
 * 确认后触发 MALL_INSTALLATION_CONFIRMED 通知
 */
@Data
public class InstallationConfirmDTO {

    /**
     * 确认上门时间（必填）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "确认上门时间不能为空")
    private LocalDateTime confirmedTime;

    /**
     * 工程师姓名（必填）
     */
    @NotBlank(message = "工程师姓名不能为空")
    private String engineerName;

    /**
     * 工程师联系方式（必填）
     */
    @NotBlank(message = "工程师联系方式不能为空")
    private String engineerPhone;
}