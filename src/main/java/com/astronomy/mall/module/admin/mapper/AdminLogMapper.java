package com.astronomy.mall.module.admin.mapper;

import com.astronomy.mall.module.admin.dto.AdminLogQueryDTO;
import com.astronomy.mall.module.admin.entity.AdminLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 管理员操作日志Mapper
 *
 * 📌 继承 BaseMapper<AdminLogEntity>，具备基本CRUD能力
 * 📌 自定义方法: selectPageByCondition（分页多条件查询）、selectListForExport（导出查询）
 */
@Mapper
public interface AdminLogMapper extends BaseMapper<AdminLogEntity> {

    /**
     * 分页查询操作日志（多条件筛选）
     *
     * @param page  分页参数（MyBatis-Plus 会自动注入 LIMIT）
     * @param query 查询条件（操作类型/管理员/时间范围/状态）
     * @return 分页结果
     */
    IPage<AdminLogEntity> selectPageByCondition(
            @Param("page") Page<AdminLogEntity> page,
            @Param("query") AdminLogQueryDTO query);

    /**
     * 查询日志列表（用于导出，不分页）
     *
     * @param query 查询条件（同列表查询，但不分页）
     * @return 日志列表（最多10000条，避免内存溢出）
     */
    List<AdminLogEntity> selectListForExport(@Param("query") AdminLogQueryDTO query);
}