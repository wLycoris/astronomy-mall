package com.astronomy.mall.module.notification.helper;

import com.astronomy.mall.module.notification.dto.SendNotificationDTO;
import com.astronomy.mall.module.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 通知助手类 - 提供简化的通知发送方法
 * 各业务模块通过此类发送通知，实现解耦
 *
 * 📌 2026-03-10 新增: sendInstallationConfirmedNotification (2.5.1 安装预约确认)
 * 📌 2026-03-10 新增: sendInstallationCancelledNotification (2.5.1 安装预约取消)
 * 📌 2026-03-11 新增: sendRecyclingCompleteNotification     (2.5.3 二手回收款到账)
 * 📌 2026-03-16 新增: sendRecognitionCompletedNotification  (4.1 星图识别成功)
 * 📌 2026-03-16 新增: sendRecognitionFailedNotification     (4.1 星图识别失败)
 */
@Slf4j
@Component
public class NotificationHelper {

    @Autowired
    private NotificationService notificationService;

    // ==================== 商城模块通知 ====================

    /**
     * 发送订单支付成功通知
     */
    @Async
    public void sendOrderPaidNotification(Long userId, String orderNo, String amount, Long orderId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("orderNo", orderNo);
        variables.put("amount", amount);
        variables.put("orderId", orderId);

        SendNotificationDTO dto = SendNotificationDTO.builder()
                .userId(userId)
                .module("mall")
                .type("order_paid")
                .relatedId(orderId)
                .relatedType("order")
                .priority(1)
                .variables(variables)
                .build();

        try {
            notificationService.sendNotification(dto);
        } catch (Exception e) {
            log.error("发送订单支付成功通知失败", e);
        }
    }

    /**
     * 发送订单发货通知
     */
    @Async
    public void sendOrderShippedNotification(Long userId, String orderNo, String logisticsCompany,
                                             String trackingNumber, Long orderId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("orderNo", orderNo);
        variables.put("logisticsCompany", logisticsCompany);
        variables.put("trackingNumber", trackingNumber);
        variables.put("orderId", orderId);

        SendNotificationDTO dto = SendNotificationDTO.builder()
                .userId(userId)
                .module("mall")
                .type("order_shipped")
                .relatedId(orderId)
                .relatedType("order")
                .priority(1)
                .variables(variables)
                .build();

        try {
            notificationService.sendNotification(dto);
        } catch (Exception e) {
            log.error("发送订单发货通知失败", e);
        }
    }

    /**
     * 发送订单派送中通知
     */
    @Async
    public void sendOrderDeliveringNotification(Long userId, String trackingNumber, Long orderId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("trackingNumber", trackingNumber);
        variables.put("orderId", orderId);

        SendNotificationDTO dto = SendNotificationDTO.builder()
                .userId(userId)
                .module("mall")
                .type("order_delivering")
                .relatedId(orderId)
                .relatedType("order")
                .priority(1)
                .variables(variables)
                .build();

        try {
            notificationService.sendNotification(dto);
        } catch (Exception e) {
            log.error("发送订单派送中通知失败", e);
        }
    }

    /**
     * 发送订单完成通知
     */
    @Async
    public void sendOrderCompletedNotification(Long userId, String orderNo, Long orderId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("orderNo", orderNo);
        variables.put("orderId", orderId);

        SendNotificationDTO dto = SendNotificationDTO.builder()
                .userId(userId)
                .module("mall")
                .type("order_completed")
                .relatedId(orderId)
                .relatedType("order")
                .priority(0)
                .variables(variables)
                .build();

        try {
            notificationService.sendNotification(dto);
        } catch (Exception e) {
            log.error("发送订单完成通知失败", e);
        }
    }

    /**
     * 发送订单取消通知
     */
    @Async
    public void sendOrderCancelledNotification(Long userId, String orderNo, Long orderId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("orderNo", orderNo);
        variables.put("orderId", orderId);

        SendNotificationDTO dto = SendNotificationDTO.builder()
                .userId(userId)
                .module("mall")
                .type("order_cancelled")
                .relatedId(orderId)
                .relatedType("order")
                .priority(1)
                .variables(variables)
                .build();

        try {
            notificationService.sendNotification(dto);
        } catch (Exception e) {
            log.error("发送订单取消通知失败", e);
        }
    }

    /**
     * 发送退款审核通过通知
     */
    @Async
    public void sendRefundApprovedNotification(Long userId, String amount, Long refundId, Long orderId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("amount", amount);
        variables.put("refundId", refundId);

        SendNotificationDTO dto = SendNotificationDTO.builder()
                .userId(userId)
                .module("mall")
                .type("refund_approved")
                .relatedId(orderId)
                .relatedType("order")
                .priority(1)
                .variables(variables)
                .build();

        try {
            notificationService.sendNotification(dto);
        } catch (Exception e) {
            log.error("发送退款审核通过通知失败", e);
        }
    }

    /**
     * 发送退款审核拒绝通知
     */
    @Async
    public void sendRefundRejectedNotification(Long userId, String reason, Long refundId, Long orderId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("reason", reason);
        variables.put("refundId", refundId);

        SendNotificationDTO dto = SendNotificationDTO.builder()
                .userId(userId)
                .module("mall")
                .type("refund_rejected")
                .relatedId(orderId)
                .relatedType("order")
                .priority(1)
                .variables(variables)
                .build();

        try {
            notificationService.sendNotification(dto);
        } catch (Exception e) {
            log.error("发送退款审核拒绝通知失败", e);
        }
    }

    /**
     * 发送退款到账通知
     */
    @Async
    public void sendRefundCompletedNotification(Long userId, String amount, Long refundId, Long orderId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("amount", amount);
        variables.put("refundId", refundId);

        SendNotificationDTO dto = SendNotificationDTO.builder()
                .userId(userId)
                .module("mall")
                .type("refund_completed")
                .relatedId(orderId)
                .relatedType("order")
                .priority(1)
                .variables(variables)
                .build();

        try {
            notificationService.sendNotification(dto);
        } catch (Exception e) {
            log.error("发送退款到账通知失败", e);
        }
    }

    /**
     * 发送商品上架通知
     */
    @Async
    public void sendProductOnSaleNotification(Long userId, String productName, Long productId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("productName", productName);
        variables.put("productId", productId);

        SendNotificationDTO dto = SendNotificationDTO.builder()
                .userId(userId)
                .module("mall")
                .type("product_on_sale")
                .relatedId(productId)
                .relatedType("product")
                .priority(0)
                .variables(variables)
                .build();

        try {
            notificationService.sendNotification(dto);
        } catch (Exception e) {
            log.error("发送商品上架通知失败", e);
        }
    }

    /**
     * 发送商品降价通知
     */
    @Async
    public void sendProductPriceDownNotification(Long userId, String productName, String price, Long productId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("productName", productName);
        variables.put("price", price);
        variables.put("productId", productId);

        SendNotificationDTO dto = SendNotificationDTO.builder()
                .userId(userId)
                .module("mall")
                .type("product_price_down")
                .relatedId(productId)
                .relatedType("product")
                .priority(0)
                .variables(variables)
                .build();

        try {
            notificationService.sendNotification(dto);
        } catch (Exception e) {
            log.error("发送商品降价通知失败", e);
        }
    }

    // ==================== 售后服务模块通知 ====================

    /**
     * 发送安装预约已确认通知
     *
     * 📌 触发时机: AdminInstallationServiceImpl.confirmInstallation() 执行后
     * 📌 通知模板: MALL_INSTALLATION_CONFIRMED
     * 📌 模板变量: engineerName / confirmedTime / engineerPhone
     * 📌 跳转路径: /after-sale/installation
     *
     * @param userId         接收通知的用户ID
     * @param engineerName   工程师姓名
     * @param confirmedTime  确认上门时间（已格式化字符串，如 "03月15日 14:00"）
     * @param engineerPhone  工程师联系方式
     * @param installationId 安装预约ID（related_id）
     */
    @Async
    public void sendInstallationConfirmedNotification(Long userId, String engineerName,
                                                      String confirmedTime, String engineerPhone,
                                                      Long installationId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("engineerName", engineerName == null ? "" : engineerName);
        variables.put("confirmedTime", confirmedTime == null ? "" : confirmedTime);
        variables.put("engineerPhone", engineerPhone == null ? "" : engineerPhone);

        SendNotificationDTO dto = SendNotificationDTO.builder()
                .userId(userId)
                .module("mall")
                .type("installation_confirmed")
                .relatedId(installationId)
                .relatedType("installation")
                .priority(1)
                .variables(variables)
                .build();

        try {
            notificationService.sendNotification(dto);
            log.info("安装预约确认通知已发送: userId={}, installationId={}", userId, installationId);
        } catch (Exception e) {
            log.error("发送安装预约确认通知失败: userId={}, installationId={}", userId, installationId, e);
        }
    }

    /**
     * 发送安装预约已取消通知
     *
     * 📌 触发时机: AdminInstallationServiceImpl.cancelInstallation() 执行后
     * 📌 通知模板: MALL_INSTALLATION_CANCELLED
     * 📌 模板变量: reason
     * 📌 跳转路径: /after-sale/installation
     *
     * @param userId         接收通知的用户ID
     * @param reason         取消原因（adminRemark）
     * @param installationId 安装预约ID（related_id）
     */
    @Async
    public void sendInstallationCancelledNotification(Long userId, String reason, Long installationId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("reason", reason == null ? "无" : reason);

        SendNotificationDTO dto = SendNotificationDTO.builder()
                .userId(userId)
                .module("mall")
                .type("installation_cancelled")
                .relatedId(installationId)
                .relatedType("installation")
                .priority(1)
                .variables(variables)
                .build();

        try {
            notificationService.sendNotification(dto);
            log.info("安装预约取消通知已发送: userId={}, installationId={}", userId, installationId);
        } catch (Exception e) {
            log.error("发送安装预约取消通知失败: userId={}, installationId={}", userId, installationId, e);
        }
    }

    /**
     * 发送二手回收款已到账通知
     *
     * 📌 触发时机: AdminRecyclingServiceImpl.completeRecycling() 标记已回收后调用
     * 📌 通知模板: MALL_RECYCLING_COMPLETED (type = recycling_completed)
     * 📌 模板变量: recycleNo(回收单号), amount(到账金额)
     * 📌 跳转路径: /user/recycling (用户端我的回收列表)
     *
     * @param userId      接收通知的用户ID
     * @param recycleNo   回收单号（如 RC20260311152300123）
     * @param amount      到账金额（BigDecimal，自动格式化为两位小数）
     * @param recyclingId 回收申请ID（related_id，用于前端跳转）
     */
    @Async
    public void sendRecyclingCompleteNotification(Long userId, String recycleNo,
                                                  BigDecimal amount, Long recyclingId) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("recycleNo", recycleNo);
            // 金额格式化为两位小数，如 "88.00"
            variables.put("amount", amount.setScale(2, RoundingMode.HALF_UP).toPlainString());

            SendNotificationDTO dto = SendNotificationDTO.builder()
                    .userId(userId)
                    .module("mall")
                    .type("recycling_completed")
                    .relatedId(recyclingId)
                    .relatedType("recycling")
                    .priority(1)
                    .variables(variables)
                    .build();

            notificationService.sendNotification(dto);
            log.info("二手回收款到账通知已发送: userId={}, recycleNo={}, amount={}",
                    userId, recycleNo, amount);
        } catch (Exception e) {
            log.error("发送二手回收款到账通知失败: userId={}, recycleNo={}, error={}",
                    userId, recycleNo, e.getMessage(), e);
        }
    }

    // ==================== AI 星图识别通知 ====================

    /**
     * 发送星图识别成功通知
     *
     * 📌 触发时机: RecognitionPollScheduler.handleSuccess() 写入结果后
     * 📌 通知模板: AI_RECOGNITION_COMPLETED
     * 📌 模板变量: objectNames（天体名称，多个用顿号拼接）/ recognitionId
     * 📌 跳转路径: /recognition/result?id={recognitionId}
     *
     * @param userId        接收通知的用户ID
     * @param objectNames   识别到的天体名称（已拼接好，如 "猎户座星云、M42"）
     * @param recognitionId 识别记录ID
     */
    @Async
    public void sendRecognitionCompletedNotification(Long userId, String objectNames, Long recognitionId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("objectNames", objectNames == null || objectNames.isEmpty() ? "未知天体" : objectNames);
        variables.put("recognitionId", recognitionId);

        SendNotificationDTO dto = SendNotificationDTO.builder()
                .userId(userId)
                .module("ai")
                .type("recognition_completed")
                .relatedId(recognitionId)
                .relatedType("recognition")
                .priority(1)
                .variables(variables)
                .build();

        try {
            notificationService.sendNotification(dto);
            log.info("星图识别成功通知已发送: userId={}, recognitionId={}", userId, recognitionId);
        } catch (Exception e) {
            log.error("发送星图识别成功通知失败: userId={}, recognitionId={}", userId, recognitionId, e);
        }
    }

    /**
     * 发送星图识别失败通知
     *
     * 📌 触发时机: RecognitionPollScheduler 标记失败（job failure 或超时）后
     * 📌 通知模板: AI_RECOGNITION_FAILED
     * 📌 模板变量: failReason（失败原因）
     * 📌 跳转路径: /recognition/history（历史列表，方便用户重新提交）
     *
     * @param userId        接收通知的用户ID
     * @param failReason    失败原因
     * @param recognitionId 识别记录ID
     */
    @Async
    public void sendRecognitionFailedNotification(Long userId, String failReason, Long recognitionId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("failReason", failReason == null ? "未知原因" : failReason);

        SendNotificationDTO dto = SendNotificationDTO.builder()
                .userId(userId)
                .module("ai")
                .type("recognition_failed")
                .relatedId(recognitionId)
                .relatedType("recognition")
                .priority(0)
                .variables(variables)
                .build();

        try {
            notificationService.sendNotification(dto);
            log.info("星图识别失败通知已发送: userId={}, recognitionId={}", userId, recognitionId);
        } catch (Exception e) {
            log.error("发送星图识别失败通知失败: userId={}, recognitionId={}", userId, recognitionId, e);
        }
    }

    // ==================== 系统模块通知 ====================

    /**
     * 发送系统公告通知
     */
    @Async
    public void sendSystemAnnouncementNotification(Long userId, String title, Long noticeId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("title", title);
        variables.put("noticeId", noticeId);

        SendNotificationDTO dto = SendNotificationDTO.builder()
                .userId(userId)
                .module("system")
                .type("announcement")
                .relatedId(noticeId)
                .relatedType("notice")
                .priority(2)
                .variables(variables)
                .build();

        try {
            notificationService.sendNotification(dto);
        } catch (Exception e) {
            log.error("发送系统公告通知失败", e);
        }
    }

    /**
     * 发送账号安全通知
     */
    @Async
    public void sendAccountSecurityNotification(Long userId, String message) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("message", message);

        SendNotificationDTO dto = SendNotificationDTO.builder()
                .userId(userId)
                .module("system")
                .type("account_security")
                .priority(2)
                .variables(variables)
                .build();

        try {
            notificationService.sendNotification(dto);
        } catch (Exception e) {
            log.error("发送账号安全通知失败", e);
        }
    }
}