package com.astronomy.mall.module.notification.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知类型枚举
 *
 * 📌 变更记录:
 * - v6.7  初始化，商城/论坛/课程/AI/系统模块枚举
 * - 5.1   新增课程3种: COURSE_CHAPTER_ADDED / COURSE_APOD_UPDATED / COURSE_COMPLETED
 * - 6.0   地理位置模块: 替换旧4个占位枚举 → 实际使用的2个
 *          删除: NEARBY_ACTIVITY / CHECKIN_SUCCESS / WEATHER_ALERT / LOCATION_RECOMMEND
 *          新增: LOCATION_CHECKIN_SUCCESS / LOCATION_WEATHER_SUITABLE
 */
@Getter
@AllArgsConstructor
public enum NotificationType {

    // ==================== 商城模块 (10种) ====================
    ORDER_PAID("order_paid", "订单支付成功", NotificationModule.MALL),
    ORDER_SHIPPED("order_shipped", "订单已发货", NotificationModule.MALL),
    ORDER_DELIVERING("order_delivering", "订单派送中", NotificationModule.MALL),
    ORDER_COMPLETED("order_completed", "订单已完成", NotificationModule.MALL),
    ORDER_CANCELLED("order_cancelled", "订单已取消", NotificationModule.MALL),
    REFUND_APPROVED("refund_approved", "退款审核通过", NotificationModule.MALL),
    REFUND_REJECTED("refund_rejected", "退款审核拒绝", NotificationModule.MALL),
    REFUND_COMPLETED("refund_completed", "退款已到账", NotificationModule.MALL),
    PRODUCT_ON_SALE("product_on_sale", "商品上架", NotificationModule.MALL),
    PRODUCT_PRICE_DOWN("product_price_down", "商品降价", NotificationModule.MALL),

    // ==================== 售后服务模块 (2种) ====================
    INSTALLATION_CONFIRMED("installation_confirmed", "安装预约已确认", NotificationModule.MALL),
    INSTALLATION_CANCELLED("installation_cancelled", "安装预约已取消", NotificationModule.MALL),

    // ==================== 论坛模块 (9种) ====================
    POST_LIKED("post_liked", "帖子被点赞", NotificationModule.FORUM),
    POST_COMMENTED("post_commented", "帖子被评论", NotificationModule.FORUM),
    COMMENT_REPLIED("comment_replied", "评论被回复", NotificationModule.FORUM),
    POST_COLLECTED("post_collected", "帖子被收藏", NotificationModule.FORUM),
    MENTION("mention", "被@提及", NotificationModule.FORUM),
    POST_APPROVED("post_approved", "帖子审核通过", NotificationModule.FORUM),
    POST_REJECTED("post_rejected", "帖子审核拒绝", NotificationModule.FORUM),
    FOLLOW("follow", "被关注", NotificationModule.FORUM),
    HOT_POST("hot_post", "帖子热门", NotificationModule.FORUM),

    // ==================== 课程模块 (9种) ====================
    COURSE_PURCHASED("course_purchased", "课程购买成功", NotificationModule.COURSE),
    COURSE_UPDATED("course_updated", "课程更新", NotificationModule.COURSE),
    COURSE_REMIND("course_remind", "课程提醒", NotificationModule.COURSE),
    HOMEWORK_REVIEWED("homework_reviewed", "作业批改", NotificationModule.COURSE),
    CERTIFICATE_ISSUED("certificate_issued", "证书颁发", NotificationModule.COURSE),
    COURSE_EXPIRED("course_expired", "课程即将过期", NotificationModule.COURSE),
    // 课程模块-新增 (3种) ✅ 2026-03-20
    COURSE_CHAPTER_ADDED("course_chapter_added", "课程新增章节",  NotificationModule.COURSE),
    COURSE_APOD_UPDATED("course_apod_updated",   "APOD课程更新", NotificationModule.COURSE),
    COURSE_COMPLETED("course_completed",          "课程学习完成", NotificationModule.COURSE),

    // ==================== 地理位置模块 (2种) ✅ 6.0新增 ====================
    // 📌 6.0: 替换原4个占位枚举(NEARBY_ACTIVITY/CHECKIN_SUCCESS/WEATHER_ALERT/LOCATION_RECOMMEND)
    //         改为实际对应通知模板的2个枚举，code与tb_notification_template.type字段保持一致
    LOCATION_CHECKIN_SUCCESS("checkin_success",   "观测点签到成功",     NotificationModule.LOCATION),
    LOCATION_WEATHER_SUITABLE("weather_suitable", "今晚观测条件极佳",   NotificationModule.LOCATION),

    // ==================== 推荐系统 (3种) ====================
    PRODUCT_RECOMMEND("product_recommend", "商品推荐", NotificationModule.RECOMMEND),
    COURSE_RECOMMEND("course_recommend", "课程推荐", NotificationModule.RECOMMEND),
    USER_RECOMMEND("user_recommend", "用户推荐", NotificationModule.RECOMMEND),

    // ==================== AI识别模块 (2种) ====================
    IDENTIFY_COMPLETED("identify_completed", "识别完成", NotificationModule.AI),
    IDENTIFY_FAILED("identify_failed", "识别失败", NotificationModule.AI),

    // ==================== 系统模块 (4种) ====================
    ANNOUNCEMENT("announcement", "系统公告", NotificationModule.SYSTEM),
    ACCOUNT_SECURITY("account_security", "账号安全", NotificationModule.SYSTEM),
    VERSION_UPDATE("version_update", "版本更新", NotificationModule.SYSTEM),
    PROMOTION("promotion", "活动推广", NotificationModule.SYSTEM);

    private final String code;
    private final String name;
    private final NotificationModule module;

    /**
     * 根据code获取枚举
     */
    public static NotificationType getByCode(String code) {
        for (NotificationType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据模块获取所有类型
     */
    public static NotificationType[] getByModule(NotificationModule module) {
        return java.util.Arrays.stream(values())
                .filter(type -> type.getModule() == module)
                .toArray(NotificationType[]::new);
    }
}