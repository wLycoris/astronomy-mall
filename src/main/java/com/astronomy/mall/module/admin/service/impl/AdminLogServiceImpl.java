package com.astronomy.mall.module.admin.service.impl;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.ResultCode;
import com.astronomy.mall.module.admin.dto.AdminLogQueryDTO;
import com.astronomy.mall.module.admin.entity.AdminLogEntity;
import com.astronomy.mall.module.admin.mapper.AdminLogMapper;
import com.astronomy.mall.module.admin.service.AdminLogService;
import com.astronomy.mall.module.admin.vo.AdminLogVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员操作日志Service实现
 *
 * 📌 功能:
 *   - getLogPage: 多条件分页查询日志列表
 *   - getLogDetail: 查询单条日志详情（含params等完整字段）
 *   - exportLog: 导出日志为Excel（Hutool ExcelWriter）
 *
 * 📌 日志写入由 AdminLogAspect 调用 save() 完成，此处不处理写入逻辑
 */
@Slf4j
@Service
public class AdminLogServiceImpl extends ServiceImpl<AdminLogMapper, AdminLogEntity>
        implements AdminLogService {

    @Autowired
    private AdminLogMapper adminLogMapper;

    // =============================================
    // 一、分页查询
    // =============================================

    @Override
    public Map<String, Object> getLogPage(AdminLogQueryDTO query) {
        // 参数安全校验
        if (query.getPageNum() == null || query.getPageNum() < 1) {
            query.setPageNum(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(20);
        }

        // 时间格式兼容处理（前端可能只传日期不传时分秒）
        query.setStartTime(normalizeStartTime(query.getStartTime()));
        query.setEndTime(normalizeEndTime(query.getEndTime()));

        // 执行分页查询
        Page<AdminLogEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<AdminLogEntity> pageResult = (Page<AdminLogEntity>)
                adminLogMapper.selectPageByCondition(page, query);

        // 转换为 VO
        List<AdminLogVO> voList = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list",  voList);
        result.put("total", pageResult.getTotal());
        result.put("pageNum",  query.getPageNum());
        result.put("pageSize", query.getPageSize());
        return result;
    }

    // =============================================
    // 二、日志详情
    // =============================================

    @Override
    public AdminLogVO getLogDetail(Long id) {
        AdminLogEntity entity = adminLogMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToVO(entity);
    }

    // =============================================
    // 三、导出日志
    // =============================================

    @Override
    public void exportLog(AdminLogQueryDTO query, HttpServletResponse response) {
        // 时间格式兼容处理
        query.setStartTime(normalizeStartTime(query.getStartTime()));
        query.setEndTime(normalizeEndTime(query.getEndTime()));

        // 查询数据（最多10000条）
        List<AdminLogEntity> entityList = adminLogMapper.selectListForExport(query);
        log.info("操作日志导出：共 {} 条", entityList.size());

        // 构建 Excel 数据行
        List<List<Object>> rows = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (AdminLogEntity entity : entityList) {
            List<Object> row = new ArrayList<>();
            row.add(entity.getId());
            row.add(entity.getAdminName());
            row.add(entity.getAdminId());
            row.add(entity.getOperation());
            row.add(entity.getMethod());
            row.add(truncateParams(entity.getParams())); // 请求参数适当截断，避免单元格过大
            row.add(entity.getIpAddress());
            row.add(entity.getStatus() == null ? "" : (entity.getStatus() == 1 ? "成功" : "失败"));
            row.add(entity.getExecutionTime() == null ? "" : entity.getExecutionTime() + "ms");
            row.add(entity.getErrorMsg());
            row.add(entity.getCreateTime() != null ? entity.getCreateTime().format(dtf) : "");
            rows.add(row);
        }

        // 表头
        List<String> headers = Arrays.asList(
                "日志ID", "管理员", "管理员ID", "操作类型",
                "请求方法", "请求参数", "IP地址",
                "操作状态", "执行耗时", "错误信息", "操作时间"
        );

        // 写入 Excel（使用 Hutool）
        ExcelWriter writer = ExcelUtil.getWriter(true);
        try {
            writer.setOnlyAlias(true);
            writer.setColumnWidth(0, 10);   // 日志ID
            writer.setColumnWidth(1, 15);   // 管理员
            writer.setColumnWidth(2, 12);   // 管理员ID
            writer.setColumnWidth(3, 20);   // 操作类型
            writer.setColumnWidth(4, 55);   // 请求方法
            writer.setColumnWidth(5, 40);   // 请求参数
            writer.setColumnWidth(6, 20);   // IP地址
            writer.setColumnWidth(7, 10);   // 操作状态
            writer.setColumnWidth(8, 12);   // 执行耗时
            writer.setColumnWidth(9, 30);   // 错误信息
            writer.setColumnWidth(10, 20);  // 操作时间

            // 写表头（加粗处理）
            writer.writeHeadRow(headers);

            // 写数据行
            for (List<Object> row : rows) {
                writer.writeRow(row);
            }

            // 设置响应头
            String fileName = "操作日志_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20"));
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

            // 输出到响应流
            writer.flush(response.getOutputStream(), true);
        } catch (IOException e) {
            log.error("导出操作日志失败", e);
            throw new BusinessException("导出失败：" + e.getMessage());
        } finally {
            writer.close();
        }
    }

    // =============================================
    // 私有工具方法
    // =============================================

    /**
     * 实体转VO，补充状态文字
     */
    private AdminLogVO convertToVO(AdminLogEntity entity) {
        AdminLogVO vo = new AdminLogVO();
        BeanUtils.copyProperties(entity, vo);
        // 状态文字
        if (entity.getStatus() != null) {
            vo.setStatusText(entity.getStatus() == 1 ? "成功" : "失败");
        }
        return vo;
    }

    /**
     * 开始时间补全（yyyy-MM-dd → yyyy-MM-dd 00:00:00）
     */
    private String normalizeStartTime(String time) {
        if (time == null || time.isEmpty()) return null;
        // 已包含时分秒则不处理
        if (time.length() == 10) {
            return time + " 00:00:00";
        }
        return time;
    }

    /**
     * 结束时间补全（yyyy-MM-dd → yyyy-MM-dd 23:59:59）
     */
    private String normalizeEndTime(String time) {
        if (time == null || time.isEmpty()) return null;
        if (time.length() == 10) {
            return time + " 23:59:59";
        }
        return time;
    }

    /**
     * 截断过长的请求参数，避免 Excel 单元格过大
     */
    private String truncateParams(String params) {
        if (params == null) return "";
        if (params.length() > 500) {
            return params.substring(0, 500) + "... [已截断]";
        }
        return params;
    }
}