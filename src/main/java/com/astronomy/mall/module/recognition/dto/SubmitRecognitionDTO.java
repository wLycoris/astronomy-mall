package com.astronomy.mall.module.recognition.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 提交星图识别请求 DTO
 *
 * 前端使用 Canvas 将图片压缩后转为 base64 字符串传入。
 *
 * 📌 前端压缩规格（StarRecognition.vue）:
 *   - 最长边缩放至 1200px（等比）
 *   - JPEG 质量: 0.85
 *   - 编码格式: base64（不含 data:image/jpeg;base64, 前缀，前端已去除）
 *
 * 📌 支持格式: jpg / png / fits（fits 直接上传原始 base64）
 */
@Data
public class SubmitRecognitionDTO {

    /**
     * 图片 base64 编码字符串
     *
     * ⚠️ 不含 data:... 前缀，纯 base64 内容
     * 示例: /9j/4AAQSkZJRgABAQAAAQABAAD...
     */
    @NotBlank(message = "图片数据不能为空")
    private String imageData;
}