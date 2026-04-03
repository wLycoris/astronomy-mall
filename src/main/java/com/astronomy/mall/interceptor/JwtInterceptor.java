package com.astronomy.mall.interceptor;

import cn.hutool.core.util.StrUtil;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.ResultCode;
import com.astronomy.mall.utils.JwtUtil;
import com.astronomy.mall.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * JWT拦截器
 * 用于验证用户登录状态
 *
 * 📌 三种路径处理模式:
 * 1. WHITE_LIST         - 完全公开，不需要登录
 * 2. OPTIONAL_AUTH_LIST - 可选认证：有Token就解析userId，没Token就放行（userId=null）
 *                         用于"游客可访问，登录后有个性化内容"的接口
 *                         ✅ 5.1 课程接口使用此模式，保证登录用户能记录学习进度
 * 3. 其他路径           - 必须登录，Token无效直接401
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 不需要登录就能访问的接口(白名单)
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            // 用户相关
            "/api/user/register",
            "/api/user/login",
            // 🆕 系统设置公开查询（前台页面需要，无需登录）
            "/api/admin/setting/maintenance",   // 维护模式（路由守卫用）
            "/api/admin/setting/register",      // 注册开关（Register.vue用）
            "/api/admin/setting/payment",       // 支付方式（PaymentPage.vue用）
            // 商品相关 (浏览商品不需要登录)
            "/api/category/tree",
            "/api/category/first-level",
            "/api/category/children",
            "/api/product/list",
            "/api/product/detail",
            "/api/product/recommend",
            "/api/product/hot",
            "/api/product/new",
            "/api/review/list",
            "/api/review/statistics",
            // NASA API 公开接口
            "/api/nasa/",
            // Knife4j文档
            "/doc.html",
            "/swagger-resources",
            "/v3/api-docs",
            "/webjars"
    );

    /**
     * 可选认证接口列表（有Token就解析设置userId，没Token就放行userId=null）
     *
     * 📌 5.1 课程模块：游客可看内容，登录后自动记录学习进度
     * ⚠️ 必须从 WebMvcConfig.excludePathPatterns 中移除这些路径，
     *    否则拦截器根本不执行，Token永远解析不到，进度永远记不上
     */
    private static final List<String> OPTIONAL_AUTH_LIST = Arrays.asList(
            "/api/course/list",
            "/api/course/chapter/",
            "/api/course/",          // 匹配 /api/course/{id} 详情
            "/api/location/spot/",   // 6.3: 观测点详情可选认证，登录后可获取myScore和签到状态
            "/api/post/list",        // 7.3: 帖子列表可选认证，游客可浏览，登录后follow模式生效
            "/api/post/comment/list", // 7.4: 评论列表公开，游客可查看评论
            "/api/post/user/profile/", // 7.5: 用户主页可选认证，游客可查看，登录后显示isFollowed
            "/api/post/search",      // 7.6: 帖子搜索可选认证
            "/api/post/search/hot"   // 7.6: 热搜词公开
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();

        // 1. 完全公开白名单：直接放行，不解析Token
        if (isWhiteList(requestURI)) {
            return true;
        }

        // 2. 获取Token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 3. 可选认证路径：有Token就解析，没Token就放行（userId保持null）
        if (isOptionalAuth(requestURI)) {
            if (StrUtil.isNotBlank(token)) {
                try {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    if (userId != null && !jwtUtil.isTokenExpired(token)) {
                        request.setAttribute("userId", userId);
                        UserContext.setUserId(userId);
                    }
                    // Token无效也不报错，当作未登录处理
                } catch (Exception e) {
                    // Token解析失败当作未登录，静默处理
                }
            }
            return true;
        }

        // 4. 普通受保护接口：必须有有效Token
        if (StrUtil.isBlank(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                throw new BusinessException(ResultCode.TOKEN_INVALID);
            }

            // 检查token是否过期
            if (jwtUtil.isTokenExpired(token)) {
                throw new BusinessException(ResultCode.TOKEN_EXPIRED);
            }

            // 🔥 关键修改：同时设置到 request 和 ThreadLocal
            request.setAttribute("userId", userId);
            UserContext.setUserId(userId); // ✅ 添加这行代码

            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
    }

    /**
     * 🔥 新增方法：请求完成后清理 ThreadLocal
     * 防止内存泄漏
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear(); // ✅ 清理 ThreadLocal
    }

    /**
     * 判断请求路径是否在完全公开白名单中
     */
    private boolean isWhiteList(String requestURI) {
        for (String pattern : WHITE_LIST) {
            if (requestURI.startsWith(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断请求路径是否在可选认证列表中
     *
     * 📌 7.3: /api/post/{id} 详情接口也需要可选认证（游客可看，登录后有互动状态）
     *   但不能用 startsWith("/api/post/") 否则会把 /api/post/publish 等也变成可选
     *   所以单独判断: /api/post/ 后面是纯数字的路径视为帖子详情
     */
    private boolean isOptionalAuth(String requestURI) {
        for (String pattern : OPTIONAL_AUTH_LIST) {
            if (requestURI.startsWith(pattern)) {
                return true;
            }
        }
        // 7.3: /api/post/{id} 帖子详情 — 仅匹配纯数字ID路径
        if (requestURI.startsWith("/api/post/")) {
            String suffix = requestURI.substring("/api/post/".length());
            if (suffix.matches("\\d+")) {
                return true;
            }
        }
        return false;
    }
}