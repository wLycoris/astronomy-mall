package com.astronomy.mall.module.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 个人中心概览 Mapper
 *
 * 📌 职责：
 *   - 聚合查询订单各状态数量
 *   - 查询进行中退款数量
 *   - 查询最近一条余额流水
 *   - 查询右上角三格统计数字（累计订单/消费/评价）
 *
 * 📌 注意：selectOrderStatusCount 使用 selectMaps 方式返回 Map<String, Object>，
 *          需要搭配 UserOverviewMapper.xml 使用（CASE WHEN COUNT 结构）
 *
 * 文件路径: com.astronomy.mall.module.user.mapper.UserOverviewMapper
 */
@Mapper
public interface UserOverviewMapper {

    /**
     * 聚合查询当前用户各状态订单数
     * 一条 SQL 返回 Map，包含以下 key：
     *   - pendingPay    (待付款, status=0)
     *   - pendingShip   (待发货, status=1)
     *   - pendingReceive(待收货, status=2)
     *   - pendingReview (待评价, status=3 且无对应评价)
     *
     * 📌 使用 CASE WHEN SUM 模式，避免多次 COUNT 查询
     */
    Map<String, Object> selectOrderStatusCount(Long userId);

    /**
     * 查询当前用户进行中的退款数量
     * 退款状态: 0-待审核, 1-审核通过(等待退款) 都算"进行中"
     *
     * @param userId 用户ID
     * @return 进行中退款数量
     */
    @Select("SELECT COUNT(*) FROM tb_refund " +
            "WHERE user_id = #{userId} AND status IN (0, 1) AND deleted = 0")
    int selectRefundingCount(Long userId);

    /**
     * 查询最近一条余额流水（最新插入的一条）
     * 返回 Map，包含: remark / amount / createTime / type
     *
     * @param userId 用户ID
     * @return 最近一条流水信息（可能为 null，无流水时）
     */
    @Select("SELECT remark, amount, create_time AS createTime, type " +
            "FROM tb_balance_log " +
            "WHERE user_id = #{userId} " +
            "ORDER BY create_time DESC LIMIT 1")
    Map<String, Object> selectLastBalanceLog(Long userId);

    /**
     * 查询累计订单总数（所有状态，包含已取消）
     *
     * @param userId 用户ID
     * @return 累计订单数
     */
    @Select("SELECT COUNT(*) FROM tb_order WHERE user_id = #{userId}")
    int selectTotalOrders(Long userId);

    /**
     * 查询累计消费金额（仅统计已完成订单 status=3 的实付金额）
     * 使用 COALESCE 避免无记录时返回 null
     *
     * @param userId 用户ID
     * @return 累计消费金额
     */
    @Select("SELECT COALESCE(SUM(payment_amount), 0) FROM tb_order " +
            "WHERE user_id = #{userId} AND status = 3")
    BigDecimal selectTotalSpent(Long userId);

    /**
     * 查询已发布评价数（deleted=0 且 status=1 正常评价）
     *
     * @param userId 用户ID
     * @return 已发布评价数
     */
    @Select("SELECT COUNT(*) FROM tb_review " +
            "WHERE user_id = #{userId} AND deleted = 0 AND status = 1")
    int selectTotalReviews(Long userId);
}