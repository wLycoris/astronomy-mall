package com.astronomy.mall.module.recognition.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI星图识别记录实体类
 *
 * 对应数据库表: tb_recognition
 *
 * 状态说明:
 *   0 - 识别中 (已提交给 Astrometry.net，等待结果)
 *   1 - 识别成功
 *   2 - 识别失败
 *
 * 📌 注意:
 *   - image_data 存储前端 Canvas 压缩后的 base64（最长边 1200px，JPEG 质量 0.85）
 *   - submission_id / job_id 由 Astrometry.net 返回，异步轮询时写入
 *   - recommended_products 由推荐模块在识别成功后填充（4.3 节完成）
 */
@Data
@TableName("tb_recognition")
public class Recognition {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /**
     * 原始图片（base64 编码，前端压缩后上传）
     * ⚠️ 字段较大，列表查询时不要 SELECT *
     */
    private String imageData;

    /**
     * Astrometry.net 提交 ID
     * 异步提交成功后写入，用于后续轮询 job
     */
    private String submissionId;

    /**
     * Astrometry.net 任务 ID
     * submission 处理完成后可拿到 job_id
     */
    private String jobId;

    /**
     * 识别状态
     * 0-识别中  1-成功  2-失败
     */
    private Integer status;

    /**
     * 识别到的天体列表（JSON 数组，如 ["Orion Nebula","M42"]）
     * 由 Astrometry.net job info 的 objects_in_field 字段返回
     */
    private String objectsInField;

    /**
     * 机器标签（JSON 数组，如 ["nebula","galaxy"]）
     * 由 Astrometry.net job info 的 machine_tags 字段返回
     */
    private String machineTags;

    /** 赤经（Right Ascension，度） */
    private BigDecimal ra;

    /**
     * 赤纬（Declination，度）
     * ⚠️ dec 是 MySQL 保留关键字，必须加 @TableField 指定反引号，
     *    否则 MyBatis-Plus 自动生成的 SELECT/INSERT 语句会报 SQL 语法错误
     */
    @TableField("`dec`")
    private BigDecimal dec;

    /** 方向角（度） */
    private BigDecimal orientation;

    /** 视野半径（度） */
    private BigDecimal radius;

    /**
     * Astrometry.net 返回的标注图片 URL
     * 格式: https://nova.astrometry.net/annotated_display/{jobId}
     * ⚠️ 下载此图片时必须携带 Referer: https://nova.astrometry.net/api/login
     */
    private String resultImageUrl;

    /**
     * 推荐商品 ID 列表（JSON 数组，如 [1, 2, 3]）
     * 识别成功后由推荐模块异步填充
     */
    private String recommendedProducts;

    /**
     * 失败原因（识别失败时填写）
     */
    private String failReason;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 最后更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}