package com.astronomy.mall.module.admin.service;

import com.astronomy.mall.module.admin.dto.AdminLogQueryDTO;
import com.astronomy.mall.module.admin.entity.AdminLogEntity;
import com.astronomy.mall.module.admin.vo.AdminLogVO;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 管理员操作日志Service接口
 *
 * 📌 主要职责:
 *   1. 日志分页查询（供列表页）
 *   2. 日志详情查看
 *   3. 日志Excel导出
 *
 * 📌 日志写入由 AdminLogAspect AOP切面自动调用 save() 方法完成，
 *    无需在此暴露 save 接口（BaseMapper已提供）。
 */
public interface AdminLogService extends IService<AdminLogEntity> {

    /**
     * 分页查询操作日志
     *
     * @param query 查询条件（操作类型/管理员/时间范围/状态/分页）
     * @return 分页结果 Map，包含 list、total、page、size
     */
    Map<String, Object> getLogPage(AdminLogQueryDTO query);

    /**
     * 查询日志详情
     *
     * @param id 日志ID
     * @return AdminLogVO（含 params 等完整字段）
     */
    AdminLogVO getLogDetail(Long id);

    /**
     * 导出操作日志为Excel
     *
     * @param query    查询条件（同列表查询，不分页）
     * @param response HttpServletResponse（写入Excel文件流）
     */
    void exportLog(AdminLogQueryDTO query, HttpServletResponse response);
}