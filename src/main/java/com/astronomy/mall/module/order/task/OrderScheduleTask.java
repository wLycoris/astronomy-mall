package com.astronomy.mall.module.order.task;

import com.astronomy.mall.module.admin.entity.SystemSetting;
import com.astronomy.mall.module.admin.mapper.SystemSettingMapper;
import com.astronomy.mall.module.order.entity.Order;
import com.astronomy.mall.module.order.entity.OrderItem;
import com.astronomy.mall.module.order.mapper.OrderItemMapper;
import com.astronomy.mall.module.order.mapper.OrderMapper;
import com.astronomy.mall.module.product.entity.Product;
import com.astronomy.mall.module.product.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单定时任务
 *
 * 功能：自动关闭超时未支付订单，并释放库存
 * 执行时间：每天凌晨 2 点
 * 超时天数：从系统设置 payment.auto_close_days 读取，默认 3 天
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduleTask {

    private final OrderMapper          orderMapper;
    private final OrderItemMapper      orderItemMapper;
    private final ProductMapper        productMapper;
    private final SystemSettingMapper  systemSettingMapper;

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void autoCloseTimeoutOrders() {
        log.info("[定时任务] 开始执行超时订单自动关闭...");

        // 1. 从系统设置读取自动关闭天数
        int autoCloseDays = 3; // 默认值
        try {
            List<SystemSetting> settings = systemSettingMapper.selectByGroupName("payment");
            Map<String, String> map = settings.stream()
                    .collect(Collectors.toMap(SystemSetting::getSettingKey, SystemSetting::getSettingValue));
            String val = map.get("auto_close_days");
            if (val != null && !val.isEmpty()) {
                autoCloseDays = Integer.parseInt(val.trim());
            }
        } catch (Exception e) {
            log.warn("[定时任务] 读取 auto_close_days 失败，使用默认值 {} 天", autoCloseDays);
        }

        // 2. 查询所有待支付且已超时的订单（status=0，createTime < 当前时间 - autoCloseDays 天）
        LocalDateTime deadline = LocalDateTime.now().minusDays(autoCloseDays);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, 0)
                .lt(Order::getCreateTime, deadline);
        List<Order> timeoutOrders = orderMapper.selectList(wrapper);

        if (timeoutOrders.isEmpty()) {
            log.info("[定时任务] 无超时订单，任务结束");
            return;
        }

        log.info("[定时任务] 发现 {} 笔超时订单，开始处理...", timeoutOrders.size());

        int successCount = 0;
        for (Order order : timeoutOrders) {
            try {
                // 3. 恢复库存
                LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
                itemWrapper.eq(OrderItem::getOrderId, order.getId());
                List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

                for (OrderItem item : items) {
                    Product product = productMapper.selectById(item.getProductId());
                    if (product != null) {
                        product.setStock(product.getStock() + item.getQuantity());
                        productMapper.updateById(product);
                    }
                }

                // 4. 关闭订单
                order.setStatus(4); // 已取消
                order.setCancelTime(LocalDateTime.now());
                order.setCancelReason("超时未支付，系统自动关闭");
                orderMapper.updateById(order);

                successCount++;
                log.info("[定时任务] 订单 {} 已自动关闭", order.getOrderNo());

            } catch (Exception e) {
                log.error("[定时任务] 处理订单 {} 失败: {}", order.getOrderNo(), e.getMessage());
            }
        }

        log.info("[定时任务] 超时订单处理完成，成功关闭 {}/{} 笔", successCount, timeoutOrders.size());
    }
}