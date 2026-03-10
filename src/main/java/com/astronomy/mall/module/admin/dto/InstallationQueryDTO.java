package com.astronomy.mall.module.admin.dto;

import lombok.Data;

/**
 * 管理员查询安装预约列表 DTO
 *
 * 📌 文件路径:
 *   module/admin/dto/InstallationQueryDTO.java
 */
@Data
public class InstallationQueryDTO {

    /** 页码，默认1 */
    private Integer pageNum = 1;

    /** 每页数量，默认10 */
    private Integer pageSize = 10;

    /**
     * 状态筛选
     *   0 - 待确认
     *   1 - 已确认
     *   2 - 已取消
     *   null - 不过滤
     */
    private Integer status;

    /**
     * 开始时间（预约提交时间）
     * 格式: yyyy-MM-dd HH:mm:ss
     */
    private String startTime;

    /**
     * 结束时间（预约提交时间）
     * 格式: yyyy-MM-dd HH:mm:ss
     */
    private String endTime;
}