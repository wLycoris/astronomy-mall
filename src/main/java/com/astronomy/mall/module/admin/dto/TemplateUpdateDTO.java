package com.astronomy.mall.module.admin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 编辑通知模板 DTO
 * 用于 PUT /api/admin/notification/template/{id} 接口
 *
 * 📌 注意：模板的 code/module/type 不允许修改，只能改内容和链接
 * 📌 variables 字段也不在此处修改（变量是固定的）
 */
@Data
public class TemplateUpdateDTO {

    /** 标题模板（必填，支持 {变量} 占位符） */
    @NotBlank(message = "标题模板不能为空")
    @Size(max = 100, message = "标题模板不能超过100个字符")
    private String titleTemplate;

    /** 内容模板（必填，支持 {变量} 占位符） */
    @NotBlank(message = "内容模板不能为空")
    @Size(max = 500, message = "内容模板不能超过500个字符")
    private String contentTemplate;

    /** 跳转链接模板（选填，支持 {变量} 占位符） */
    @Size(max = 200, message = "跳转链接不能超过200个字符")
    private String jumpUrlTemplate;

    /** 备注说明（选填） */
    @Size(max = 200, message = "备注不能超过200个字符")
    private String remark;
}