package com.astronomy.mall.module.notification.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知模块枚举
 */
@Getter
@AllArgsConstructor
public enum NotificationModule {

    MALL("mall", "商城模块"),
    FORUM("forum", "论坛模块"),
    COURSE("course", "课程模块"),
    LOCATION("location", "地理位置模块"),
    RECOMMEND("recommend", "推荐系统"),
    AI("ai", "AI识别模块"),
    SYSTEM("system", "系统模块");

    private final String code;
    private final String name;

    /**
     * 根据code获取枚举
     */
    public static NotificationModule getByCode(String code) {
        for (NotificationModule module : values()) {
            if (module.getCode().equals(code)) {
                return module;
            }
        }
        return null;
    }
}