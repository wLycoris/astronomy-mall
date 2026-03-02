package com.astronomy.mall.module.admin.service.impl;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.admin.dto.UserQueryDTO;
import com.astronomy.mall.module.admin.dto.UserRoleDTO;
import com.astronomy.mall.module.admin.dto.UserStatusDTO;
import com.astronomy.mall.module.admin.service.AdminUserService;
import com.astronomy.mall.module.admin.vo.AdminUserDetailVO;
import com.astronomy.mall.module.order.entity.Order;
import com.astronomy.mall.module.order.mapper.OrderMapper;
import com.astronomy.mall.module.payment.entity.Refund;
import com.astronomy.mall.module.payment.mapper.RefundMapper;
import com.astronomy.mall.module.product.entity.Review;
import com.astronomy.mall.module.product.mapper.ReviewMapper;
import com.astronomy.mall.module.user.entity.LoginLog;
import com.astronomy.mall.module.user.entity.User;
import com.astronomy.mall.module.user.mapper.LoginLogMapper;
import com.astronomy.mall.module.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * 后台用户管理 ServiceImpl
 *
 * <p>⚠️ MySQL JDBC tinyint 类型映射说明：</p>
 * <p>MySQL JDBC 驱动会将所有 tinyint 字段映射为 Boolean 返回（selectMaps 时）。
 * 包括：role、status、observation_level 等。
 * 因此所有从 Map 取出的 tinyint 字段必须通过 toInt() 安全转换。</p>
 *
 * @author astronomy-mall
 * @since 2026-03-02
 */
@Slf4j
@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private LoginLogMapper loginLogMapper;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private RefundMapper refundMapper;

    @Resource
    private ReviewMapper reviewMapper;

    // ==================== 用户列表 ====================

    @Override
    public IPage<?> getUserList(UserQueryDTO dto) {
        Page<Map<String, Object>> page = new Page<>(dto.getPage(), dto.getSize());

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0);

        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.and(w -> w
                    .like("username", dto.getKeyword())
                    .or().like("nickname", dto.getKeyword())
                    .or().like("phone", dto.getKeyword())
                    .or().like("email", dto.getKeyword())
            );
        }
        if (dto.getRole() != null)             wrapper.eq("role", dto.getRole());
        if (dto.getStatus() != null)           wrapper.eq("status", dto.getStatus());
        if (dto.getObservationLevel() != null) wrapper.eq("observation_level", dto.getObservationLevel());
        if (StringUtils.hasText(dto.getStartTime())) wrapper.ge("create_time", dto.getStartTime());
        if (StringUtils.hasText(dto.getEndTime()))   wrapper.le("create_time", dto.getEndTime() + " 23:59:59");

        // 排序规则：
        //   1. 管理员（role=1）全部排在普通用户（role=0）前面  → role DESC
        //   2. 同角色内部均按创建时间升序（早的在上）           → create_time ASC
        wrapper.last("ORDER BY role DESC, create_time ASC");

        // 不返回 password 等敏感字段
        // ⚠️ tinyint 字段（role/status/observation_level）通过 selectMaps 会被 JDBC 映射为 Boolean。
        //    解决方案：用唯一别名 role_int / status_int / level_int 做 CAST，
        //    确保 MyBatis-Plus 不会因别名与原列名相同而产生 key 混乱。
        wrapper.select("id", "username", "nickname", "email", "phone", "avatar",
                "CAST(role AS UNSIGNED) AS role_int",
                "CAST(status AS UNSIGNED) AS status_int",
                "CAST(observation_level AS UNSIGNED) AS level_int",
                "city", "interest_tags",
                "create_time", "last_login_time");

        IPage<Map<String, Object>> result = userMapper.selectMapsPage(page, wrapper);

        result.getRecords().forEach(map -> {
            // 用唯一别名取值，CAST AS UNSIGNED 后 JDBC 返回 Long，toLong 安全转换
            int role             = toLong(map.get("role_int")).intValue();
            int status           = toLong(map.get("status_int")).intValue();
            int observationLevel = toLong(map.get("level_int")).intValue();

            // 将标准字段名写入 map，方便前端统一使用 row.role / row.status
            map.put("role",   role);
            map.put("status", status);
            map.put("observation_level", observationLevel);

            // 补充中文名称
            map.put("roleName",             role == 1 ? "管理员" : "普通用户");
            map.put("statusName",           status == 1 ? "启用" : "禁用");
            map.put("observationLevelName", getObservationLevelName(observationLevel));

            // 订单统计
            Long userId = toLong(map.get("id"));
            Map<String, Object> stats = getOrderStats(userId);
            map.put("orderCount",  stats.get("orderCount"));
            map.put("totalAmount", stats.get("totalAmount"));
        });

        return result;
    }

    // ==================== 用户详情 ====================

    @Override
    public Object getUserDetail(Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }

        AdminUserDetailVO vo = new AdminUserDetailVO();

        // User 实体字段类型已确定（MyBatis 通过实体映射，不走 selectMaps），直接赋值无需转换
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setRoleName(user.getRole() != null && user.getRole() == 1 ? "管理员" : "普通用户");
        vo.setStatus(user.getStatus());
        vo.setStatusName(user.getStatus() != null && user.getStatus() == 1 ? "启用" : "禁用");
        vo.setObservationLevel(user.getObservationLevel());
        vo.setObservationLevelName(getObservationLevelName(user.getObservationLevel()));
        vo.setCity(user.getCity());
        vo.setProvince(user.getProvince());
        vo.setInterestTags(user.getInterestTags());
        vo.setCreateTime(user.getCreateTime());
        vo.setLastLoginTime(user.getLastLoginTime());

        // 消费统计
        Map<String, Object> orderStats = getOrderStats(id);
        vo.setOrderCount((Long) orderStats.get("orderCount"));
        vo.setTotalAmount((BigDecimal) orderStats.get("totalAmount"));

        Long completedCount = orderMapper.selectCount(
                new QueryWrapper<Order>().eq("user_id", id).eq("status", 3).eq("deleted", 0)
        );
        vo.setCompletedOrderCount(completedCount != null ? completedCount : 0L);

        Long refundCount = refundMapper.selectCount(
                new QueryWrapper<Refund>().eq("user_id", id)
        );
        vo.setRefundCount(refundCount != null ? refundCount : 0L);

        Long reviewCount = reviewMapper.selectCount(
                new QueryWrapper<Review>().eq("user_id", id).eq("deleted", 0)
        );
        vo.setReviewCount(reviewCount != null ? reviewCount : 0L);

        // 近期订单（最近5条）
        List<Map<String, Object>> recentOrders = orderMapper.selectMaps(
                new QueryWrapper<Order>()
                        .eq("user_id", id)
                        .eq("deleted", 0)
                        .select("id", "order_no", "total_amount", "status", "create_time")
                        .orderByDesc("create_time")
                        .last("LIMIT 5")
        );
        String[] orderStatusNames = {"待支付", "待发货", "待收货", "已完成", "已取消"};
        recentOrders.forEach(o -> {
            // order.status 也是 tinyint，同样需要 toInt 转换
            int s = toInt(o.get("status"));
            o.put("status",     s);
            o.put("statusName", s >= 0 && s < orderStatusNames.length ? orderStatusNames[s] : "未知");
        });
        vo.setRecentOrders(recentOrders);

        // 近期登录日志（最近5条）
        List<Map<String, Object>> loginLogs = loginLogMapper.selectMaps(
                new QueryWrapper<LoginLog>()
                        .eq("user_id", id)
                        .select("id", "login_time", "ip_address", "device", "status")
                        .orderByDesc("login_time")
                        .last("LIMIT 5")
        );
        // login_log.status 也是 tinyint，写回整数值
        loginLogs.forEach(l -> l.put("status", toInt(l.get("status"))));
        vo.setLoginLogs(loginLogs);

        return vo;
    }

    // ==================== 修改用户状态 ====================

    @Override
    public void updateUserStatus(Long id, UserStatusDTO dto) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        User update = new User();
        update.setId(id);
        update.setStatus(dto.getStatus());
        userMapper.updateById(update);
        log.info("管理员修改用户[{}]状态为[{}]，原因: {}", id, dto.getStatus(), dto.getReason());
    }

    // ==================== 设置用户角色 ====================

    @Override
    public void updateUserRole(Long id, UserRoleDTO dto) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        User update = new User();
        update.setId(id);
        update.setRole(dto.getRole());
        userMapper.updateById(update);
        log.info("管理员设置用户[{}]角色为[{}]", id, dto.getRole());
    }

    // ==================== 私有工具方法 ====================

    /**
     * 将数据库 tinyint 字段值安全转为 int
     *
     * <p>MySQL JDBC 驱动会把所有 tinyint 字段映射为 Boolean，
     * 此方法统一处理 Boolean / Number / null 三种情况。</p>
     *
     * @param obj 原始值（Boolean / Number / null）
     * @return int 值，null 时返回 0
     */
    private int toInt(Object obj) {
        if (obj == null)            return 0;
        if (obj instanceof Boolean) return Boolean.TRUE.equals(obj) ? 1 : 0;
        if (obj instanceof Number)  return ((Number) obj).intValue();
        return 0;
    }

    /**
     * 将数据库 bigint 字段值安全转为 Long
     *
     * @param obj 原始值（Number / null）
     * @return Long 值，null 时返回 0L
     */
    private Long toLong(Object obj) {
        if (obj == null)           return 0L;
        if (obj instanceof Number) return ((Number) obj).longValue();
        return 0L;
    }

    /**
     * 观测等级数值转中文名称
     */
    private String getObservationLevelName(Integer level) {
        if (level == null) return "未知";
        switch (level) {
            case 1: return "入门";
            case 2: return "初级";
            case 3: return "中级";
            case 4: return "高级";
            case 5: return "专家";
            default: return "未知";
        }
    }

    /**
     * 获取用户订单统计（订单总数 + 消费总金额）
     *
     * <p>消费总金额只统计已完成订单（status=3）。</p>
     */
    private Map<String, Object> getOrderStats(Long userId) {
        Map<String, Object> result = new HashMap<>();

        Long count = orderMapper.selectCount(
                new QueryWrapper<Order>().eq("user_id", userId).eq("deleted", 0)
        );
        result.put("orderCount", count != null ? count : 0L);

        List<Map<String, Object>> amountResult = orderMapper.selectMaps(
                new QueryWrapper<Order>()
                        .eq("user_id", userId)
                        .eq("status", 3)
                        .eq("deleted", 0)
                        .select("IFNULL(SUM(total_amount), 0) AS totalAmount")
        );
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (!amountResult.isEmpty() && amountResult.get(0).get("totalAmount") != null) {
            Object val = amountResult.get(0).get("totalAmount");
            totalAmount = val instanceof BigDecimal
                    ? (BigDecimal) val
                    : new BigDecimal(val.toString());
        }
        result.put("totalAmount", totalAmount);

        return result;
    }
}