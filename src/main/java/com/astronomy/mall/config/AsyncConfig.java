package com.astronomy.mall.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 异步配置
 * 用于异步发送通知,不阻塞业务流程
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // 启用异步支持,@Async注解的方法将异步执行
}