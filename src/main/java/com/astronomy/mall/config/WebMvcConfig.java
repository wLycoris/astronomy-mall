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
 *
 * 📌 变更记录:
 * - 2.7.1 新增 /api/nasa/** 白名单（NASA APOD 公开接口，商城首页无需登录调用）
 * - 5.1   课程接口使用「可选认证」模式，由 JwtInterceptor.OPTIONAL_AUTH_LIST 处理
 *         ⚠️ 不在 excludePathPatterns 中配置课程路径！否则拦截器不执行，进度无法记录
 * - 6.0   新增地理位置模块4条公开接口白名单（spots/spot详情/weather/tonight）
 *         checkin 和 rating 接口需要登录，不加白名单
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
                        "/static/**", "/images/**",
                        // ✅ 2.7.1 NASA API 公开接口（商城首页 ApodCard 无需登录）
                        "/api/nasa/**",
                        // ❌ 课程接口不在此处！由 JwtInterceptor.OPTIONAL_AUTH_LIST 处理
                        // 游客放行 + 登录用户解析Token，两者兼顾

                        // ✅ 6.0 地理位置模块公开接口（无需登录即可查看观测点和天气）
                        // ⚠️ checkin(签到) 和 spot/{id}/rating(评分) 需要登录，不在此处！
                        "/api/location/spots",      // 观测点列表（含筛选）
                        "/api/location/spot/",      // 观测点详情 /spot/{id}（用前缀匹配）
                        "/api/location/weather",    // 实况天气查询
                        "/api/location/tonight"     // 今晚观测条件综合评分
                )
                .order(1); // 第一个执行

        // ============================================
        // 2. 管理员权限拦截器 (验证管理员)
        // ============================================
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/api/admin/**") // 只拦截管理员接口
                .excludePathPatterns(
                        "/api/admin/setting/maintenance",  // 🆕 维护模式公开查询
                        "/api/admin/setting/register",     // 🆕 注册开关公开查询
                        "/api/admin/setting/payment"       // 🆕 支付方式公开查询
                )
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