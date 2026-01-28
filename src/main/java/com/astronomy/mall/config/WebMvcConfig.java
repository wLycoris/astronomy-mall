package com.astronomy.mall.config;

import com.astronomy.mall.interceptor.AdminInterceptor;
import com.astronomy.mall.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc配置
 *
 * 📌 拦截器执行顺序:
 * 1. JwtInterceptor: 验证登录 (拦截所有/api/**)
 * 2. AdminInterceptor: 验证管理员权限 (拦截所有/api/admin/**)
 *
 * 📌 重要说明:
 * - AdminInterceptor 必须在 JwtInterceptor 之后执行
 * - 因为需要从 request 中获取 JwtInterceptor 存入的 userId
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // ============================================
        // 1. JWT拦截器 (验证登录)
        // ============================================
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        // Knife4j文档
                        "/doc.html", "/swagger-resources/**",
                        "/v3/api-docs/**", "/webjars/**",
                        // 静态资源
                        "/static/**", "/images/**"
                )
                .order(1); // 第一个执行

        // ============================================
        // 2. 管理员权限拦截器 (验证管理员)
        // ============================================
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/api/admin/**") // 只拦截管理员接口
                .order(2); // 第二个执行
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}