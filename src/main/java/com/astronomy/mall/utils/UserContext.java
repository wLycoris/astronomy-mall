package com.astronomy.mall.utils;

/**
 * 用户上下文工具类
 * 用于从ThreadLocal获取当前登录用户ID
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 获取当前用户ID
     * 🔥 添加非空检查，防止未登录时返回null
     */
    public static Long getUserId() {
        Long userId = USER_ID_HOLDER.get();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        return userId;
    }

    /**
     * 清除当前用户ID
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}