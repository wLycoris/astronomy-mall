package com.astronomy.mall.module.recognition.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 星图识别模块配置类
 *
 * 📌 包含两个 Bean:
 *   1. recognitionExecutor - 专用异步线程池（识别任务耗时长，单独隔离）
 *   2. recognitionRestTemplate - 专用 RestTemplate（超时配置适合慢速 API）
 *
 * 📌 为什么不复用 notificationExecutor?
 *   识别任务可能运行 3 分钟以上，若占用通知线程池会导致通知延迟。
 *   单独配置线程池，两个功能互不影响。
 *
 * 📌 超时配置说明:
 *   - connectTimeout = 30s: Astrometry.net 服务器偶尔响应慢
 *   - readTimeout = 120s: 上传大图片时需要较长读取时间
 */
@Configuration
@EnableAsync  // ⚠️ 若主启动类已有 @EnableAsync 或 AsyncConfig 已配置，则无需重复，但重复声明无害
public class RecognitionConfig {

    /**
     * 星图识别专用异步线程池
     *
     * 配置说明:
     *   - 核心线程数 2: 识别任务长，不需要太多并发
     *   - 最大线程数 5: 并发峰值控制
     *   - 队列容量 20:  识别队列，超出后由调用线程执行（阻塞主线程，保证不丢任务）
     *   - 线程名前缀: "recognition-async-" 方便日志追踪
     */
    @Bean(name = "recognitionExecutor")
    public Executor recognitionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("recognition-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120); // 等待识别任务完成再关闭
        executor.initialize();
        return executor;
    }

    /**
     * 星图识别专用 RestTemplate
     *
     * ⚠️ 注意 Bean 名称为 recognitionRestTemplate，
     *    避免与项目中可能存在的其他 RestTemplate Bean 冲突。
     *    AstrometryServiceImpl 通过构造器注入 @Qualifier 不需要，
     *    因为字段名与 Bean 名匹配（按名称注入）。
     *
     * 超时配置:
     *   - 连接超时: 30 秒
     *   - 读取超时: 120 秒（上传大图 + 服务器处理时间）
     */
    @Bean(name = "recognitionRestTemplate")
    public RestTemplate recognitionRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // 连接超时：30 秒
        factory.setConnectTimeout(30 * 1000);

        // 读取超时：120 秒（图片上传可能较慢）
        factory.setReadTimeout(120 * 1000);

        return new RestTemplate(factory);
    }
}