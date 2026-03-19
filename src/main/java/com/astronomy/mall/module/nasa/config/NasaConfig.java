package com.astronomy.mall.module.nasa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * NASA 模块配置类
 *
 * 📌 注册 RestTemplate Bean，供 NasaApiServiceImpl 注入使用
 * 📌 如果项目中其他模块已注册 RestTemplate Bean（如 recognition 模块），
 *    可以删除此类，直接在 NasaApiServiceImpl 中 @Autowired 已有的 Bean
 *
 * ⚠️ 注意：若项目已有 RestTemplate Bean 会导致冲突，请检查是否已存在！
 *    可搜索全局 @Bean RestTemplate 确认，若已存在请直接删除此文件
 */
@Configuration
public class NasaConfig {

    /**
     * 注册 RestTemplate
     * Spring Boot 不自动注册 RestTemplate，需要手动声明
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}