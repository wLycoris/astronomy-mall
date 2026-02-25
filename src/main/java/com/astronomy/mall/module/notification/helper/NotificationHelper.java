package com.astronomy.mall.module.notification.helper;

import com.astronomy.mall.module.notification.dto.SendNotificationDTO;
import com.astronomy.mall.module.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知助手类 - 提供简化的通知发送方法
 * 各业务模块通过此类发送通知,实现解耦
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
                .priority(1)  // 重要
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
                .priority(1)  // 重要
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
                .priority(1)  // 重要
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
                .priority(0)  // 普通
                .variables(variables)
                .build();

        try {
            notificationService.sendNotification(dto);
        } catch (Exception e) {
            log.error("发送订单完成通知失败", e);
        }
    }

    /**
     *
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
                .priority(1)  // 重要
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
    public void sendRefundApprovedNotification(Long userId, String amount, Long refundId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("amount", amount);
        variables.put("refundId", refundId);

        SendNotificationDTO dto = SendNotificationDTO.builder()
                .userId(userId)
                .module("mall")
                .type("refund_approved")
                .relatedId(refundId)
                .relatedType("refund")
                .priority(1)  // 重要
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
    public void sendRefundRejectedNotification(Long userId, String reason, Long refundId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("reason", reason);
        variables.put("refundId", refundId);

        SendNotificationDTO dto = SendNotificationDTO.builder()
                .userId(userId)
                .module("mall")
                .type("refund_rejected")
                .relatedId(refundId)
                .relatedType("refund")
                .priority(1)  // 重要
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
    public void sendRefundCompletedNotification(Long userId, String amount, Long refundId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("amount", amount);
        variables.put("refundId", refundId);

        SendNotificationDTO dto = SendNotificationDTO.builder()
                .userId(userId)
                .module("mall")
                .type("refund_completed")
                .relatedId(refundId)
                .relatedType("refund")
                .priority(1)  // 重要
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
                .priority(0)  // 普通
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
                .priority(0)  // 普通
                .variables(variables)
                .build();

        try {
            notificationService.sendNotification(dto);
        } catch (Exception e) {
            log.error("发送商品降价通知失败", e);
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
                .priority(2)  // 紧急
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
                .priority(2)  // 紧急
                .variables(variables)
                .build();

        try {
            notificationService.sendNotification(dto);
        } catch (Exception e) {
            log.error("发送账号安全通知失败", e);
        }
    }
}