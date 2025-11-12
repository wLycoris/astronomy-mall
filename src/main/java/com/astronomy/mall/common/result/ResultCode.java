package com.astronomy.mall.common.result;

import lombok.Getter;

/**
 * 返回码枚举
 * 规则:
 * 200-599    → 通用HTTP状态码
 * 1001-1099  → 用户相关
 * 2001-2099  → 商品相关
 * 2101-2199  → 分类相关
 * 2201-2299  → 购物车相关
 * 2301-2399  → 评价相关
 * 3001-3099  → 订单相关
 * 3101-3199  → 售后服务相关
 * 4001-4099  → 课程相关
 * 4101-4199  → NASA APOD相关
 * 5001-5099  → AI识别相关
 * 6001-6099  → 地理位置相关
 * 7001-7099  → 论坛相关
 * 8001-8099  → 后台管理相关
 * 9001-9099  → 推荐系统相关
 * 9101-9199  → 文件上传相关
 * 9201-9299  → 支付相关
 * 9301-9399  → 第三方API相关
 */
@Getter
public enum ResultCode {

    // =============================================
    // 通用返回码 (200-599)
    // =============================================
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或token已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    REQUEST_TIMEOUT(408, "请求超时"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),

    // =============================================
    // 用户相关错误码 (1001-1099)
    // =============================================
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_ALREADY_EXIST(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    USER_DISABLED(1004, "用户已被禁用"),
    USERNAME_EXISTS(1005, "用户名已被使用"),
    EMAIL_EXISTS(1006, "邮箱已被注册"),
    PHONE_EXISTS(1007, "手机号已被注册"),
    TOKEN_INVALID(1008, "Token无效或已过期"),
    TOKEN_EXPIRED(1009, "Token已过期"),
    OLD_PASSWORD_ERROR(1010, "原密码错误"),
    NEW_PASSWORD_SAME(1011, "新密码不能与原密码相同"),
    USER_INFO_INCOMPLETE(1012, "用户信息不完整"),
    VERIFICATION_CODE_ERROR(1013, "验证码错误"),
    VERIFICATION_CODE_EXPIRED(1014, "验证码已过期"),
    LOGIN_FAILED_TOO_MANY(1015, "登录失败次数过多,请稍后再试"),
    NICKNAME_TOO_LONG(1016, "昵称长度不能超过20个字符"),

    // =============================================
    // 商品相关错误码 (2001-2099)
    // =============================================
    PRODUCT_NOT_FOUND(2001, "商品不存在或已下架"),
    PRODUCT_STOCK_NOT_ENOUGH(2002, "商品库存不足"),
    PRODUCT_ALREADY_EXISTS(2003, "商品已存在"),
    PRODUCT_OFFLINE(2004, "商品已下架"),
    PRODUCT_NAME_EMPTY(2005, "商品名称不能为空"),
    PRODUCT_PRICE_INVALID(2006, "商品价格无效"),
    PRODUCT_STOCK_INVALID(2007, "商品库存无效"),
    PRODUCT_IMAGE_REQUIRED(2008, "商品主图不能为空"),

    // =============================================
    // 分类相关错误码 (2101-2199)
    // =============================================
    CATEGORY_NOT_FOUND(2101, "分类不存在"),
    CATEGORY_HAS_PRODUCTS(2102, "该分类下存在商品,无法删除"),
    CATEGORY_HAS_CHILDREN(2103, "该分类下存在子分类,无法删除"),
    CATEGORY_NAME_EXISTS(2104, "分类名称已存在"),
    CATEGORY_LEVEL_ERROR(2105, "分类层级错误"),
    PARENT_CATEGORY_NOT_FOUND(2106, "父分类不存在"),

    // =============================================
    // 购物车相关错误码 (2201-2299)
    // =============================================
    CART_ITEM_NOT_FOUND(2201, "购物车商品不存在"),
    CART_ITEM_ALREADY_EXISTS(2202, "该商品已在购物车中"),
    CART_QUANTITY_INVALID(2203, "商品数量无效"),
    CART_EXCEED_STOCK(2204, "购物车商品数量超过库存"),
    CART_IS_EMPTY(2205, "购物车为空"),
    CART_PRODUCT_OFFLINE(2206, "购物车中有商品已下架"),
    CART_EXCEED_LIMIT(2207, "购物车商品数量超过限制"),

    // =============================================
    // 评价相关错误码 (2301-2399)
    // =============================================
    REVIEW_NOT_FOUND(2301, "评价不存在"),
    REVIEW_ALREADY_EXISTS(2302, "该订单已评价"),
    REVIEW_NOT_ALLOWED(2303, "无权评价该订单"),
    REVIEW_CONTENT_TOO_LONG(2304, "评价内容过长"),
    REVIEW_RATING_INVALID(2305, "评分必须在1-5星之间"),
    REVIEW_ORDER_NOT_FINISHED(2306, "订单未完成,无法评价"),
    REVIEW_IMAGES_EXCEED_LIMIT(2307, "评价图片数量超过限制"),

    // =============================================
    // 订单相关错误码 (3001-3099)
    // =============================================
    ORDER_NOT_FOUND(3001, "订单不存在"),
    ORDER_STATUS_ERROR(3002, "订单状态错误"),
    ORDER_CANNOT_CANCEL(3003, "订单无法取消"),
    ORDER_CANNOT_DELETE(3004, "订单无法删除"),
    ORDER_AMOUNT_ERROR(3005, "订单金额错误"),
    ORDER_NO_GENERATE_FAILED(3006, "订单号生成失败"),
    ORDER_PRODUCT_EMPTY(3007, "订单商品为空"),
    ORDER_ADDRESS_REQUIRED(3008, "收货地址不能为空"),
    ORDER_ALREADY_PAID(3009, "订单已支付"),
    ORDER_PAYMENT_TIMEOUT(3010, "订单支付超时"),
    ORDER_ALREADY_CANCELLED(3011, "订单已取消"),
    ORDER_ALREADY_FINISHED(3012, "订单已完成"),
    ORDER_NOT_PAID(3013, "订单未支付"),
    ORDER_NOT_SHIPPED(3014, "订单未发货"),
    ORDER_RECEIVER_INFO_INCOMPLETE(3015, "收货人信息不完整"),

    // =============================================
    // 售后相关错误码 (3101-3199)
    // =============================================
    INSTALLATION_NOT_FOUND(3101, "安装预约不存在"),
    INSTALLATION_TIME_INVALID(3102, "预约时间无效"),
    INSTALLATION_ALREADY_EXISTS(3103, "该订单已预约安装"),
    MAINTENANCE_NOT_FOUND(3104, "维护提醒不存在"),
    MAINTENANCE_ALREADY_FINISHED(3105, "维护已完成"),
    TRADE_IN_NOT_FOUND(3106, "以旧换新记录不存在"),
    TRADE_IN_ALREADY_EXISTS(3107, "该订单已申请以旧换新"),
    TRADE_IN_ESTIMATE_ERROR(3108, "估价失败"),

    // =============================================
    // 课程相关错误码 (4001-4099)
    // =============================================
    COURSE_NOT_FOUND(4001, "课程不存在"),
    COURSE_ALREADY_EXISTS(4002, "课程已存在"),
    COURSE_CHAPTER_NOT_FOUND(4003, "课程章节不存在"),
    COURSE_NOT_PUBLISHED(4004, "课程未发布"),
    COURSE_ALREADY_ENROLLED(4005, "已经报名该课程"),
    COURSE_CHAPTER_ORDER_ERROR(4006, "章节顺序错误"),
    STUDY_RECORD_NOT_FOUND(4007, "学习记录不存在"),
    STUDY_PROGRESS_INVALID(4008, "学习进度无效"),

    // =============================================
    // NASA APOD相关错误码 (4101-4199)
    // =============================================
    APOD_NOT_FOUND(4101, "NASA每日图片不存在"),
    APOD_FETCH_FAILED(4102, "获取NASA图片失败"),
    APOD_ALREADY_EXISTS(4103, "该日期的图片已存在"),
    APOD_API_KEY_INVALID(4104, "NASA API Key无效"),

    // =============================================
    // AI识别相关错误码 (5001-5099)
    // =============================================
    IDENTIFICATION_NOT_FOUND(5001, "识别记录不存在"),
    IDENTIFICATION_IMAGE_REQUIRED(5002, "识别图片不能为空"),
    IDENTIFICATION_IMAGE_SIZE_EXCEED(5003, "图片大小超过限制"),
    IDENTIFICATION_IMAGE_FORMAT_ERROR(5004, "图片格式不支持"),
    IDENTIFICATION_IN_PROGRESS(5005, "识别正在进行中"),
    IDENTIFICATION_FAILED(5006, "识别失败"),
    ASTROMETRY_API_ERROR(5007, "Astrometry API调用失败"),
    ASTROMETRY_API_TIMEOUT(5008, "Astrometry API超时"),
    ASTROMETRY_API_KEY_INVALID(5009, "Astrometry API Key无效"),

    // =============================================
    // 地理位置相关错误码 (6001-6099)
    // =============================================
    OBSERVATION_SITE_NOT_FOUND(6001, "观测点不存在"),
    LOCATION_GET_FAILED(6002, "获取位置信息失败"),
    COORDINATES_INVALID(6003, "坐标无效"),
    AMAP_API_ERROR(6004, "高德地图API调用失败"),
    AMAP_API_KEY_INVALID(6005, "高德地图API Key无效"),
    CELESTIAL_OBJECT_NOT_FOUND(6006, "天体信息不存在"),
    LIGHT_POLLUTION_LEVEL_INVALID(6007, "光污染等级无效"),

    // =============================================
    // 论坛相关错误码 (7001-7099)
    // =============================================
    POST_NOT_FOUND(7001, "帖子不存在"),
    POST_TITLE_EMPTY(7002, "帖子标题不能为空"),
    POST_CONTENT_EMPTY(7003, "帖子内容不能为空"),
    POST_ALREADY_DELETED(7004, "帖子已被删除"),
    POST_IMAGES_EXCEED_LIMIT(7005, "帖子图片数量超过限制"),
    COMMENT_NOT_FOUND(7006, "评论不存在"),
    COMMENT_CONTENT_EMPTY(7007, "评论内容不能为空"),
    COMMENT_ALREADY_DELETED(7008, "评论已被删除"),
    LIKE_ALREADY_EXISTS(7009, "已经点赞"),
    LIKE_NOT_FOUND(7010, "点赞记录不存在"),
    FAVORITE_ALREADY_EXISTS(7011, "已经收藏"),
    FAVORITE_NOT_FOUND(7012, "收藏记录不存在"),
    TAG_NOT_FOUND(7013, "标签不存在"),
    TAG_NAME_EXISTS(7014, "标签名称已存在"),

    // =============================================
    // 后台管理相关错误码 (8001-8099)
    // =============================================
    ADMIN_NOT_FOUND(8001, "管理员不存在"),
    ADMIN_NO_PERMISSION(8002, "无管理员权限"),
    ADMIN_CANNOT_DELETE_SELF(8003, "不能删除自己的账号"),
    ADMIN_OPERATION_FAILED(8004, "管理员操作失败"),
    STATISTICS_QUERY_FAILED(8005, "统计数据查询失败"),
    EXPORT_DATA_FAILED(8006, "数据导出失败"),
    IMPORT_DATA_FAILED(8007, "数据导入失败"),
    LOG_QUERY_FAILED(8008, "日志查询失败"),

    // =============================================
    // 推荐系统相关错误码 (9001-9099)
    // =============================================
    RECOMMEND_SERVICE_ERROR(9001, "推荐服务异常"),
    RECOMMEND_SERVICE_TIMEOUT(9002, "推荐服务超时"),
    RECOMMEND_MODEL_NOT_READY(9003, "推荐模型未就绪"),
    RECOMMEND_DATA_INSUFFICIENT(9004, "推荐数据不足"),
    BROWSE_LOG_SAVE_FAILED(9005, "浏览记录保存失败"),

    // =============================================
    // 文件上传相关错误码 (9101-9199)
    // =============================================
    FILE_UPLOAD_FAILED(9101, "文件上传失败"),
    FILE_SIZE_EXCEED(9102, "文件大小超过限制"),
    FILE_TYPE_NOT_ALLOWED(9103, "文件类型不支持"),
    FILE_NOT_FOUND(9104, "文件不存在"),
    FILE_DELETE_FAILED(9105, "文件删除失败"),
    OSS_SERVICE_ERROR(9106, "OSS服务异常"),

    // =============================================
    // 支付相关错误码 (9201-9299)
    // =============================================
    PAYMENT_FAILED(9201, "支付失败"),
    PAYMENT_AMOUNT_ERROR(9202, "支付金额错误"),
    PAYMENT_TYPE_NOT_SUPPORT(9203, "不支持的支付方式"),
    PAYMENT_CALLBACK_FAILED(9204, "支付回调失败"),
    REFUND_FAILED(9205, "退款失败"),
    REFUND_AMOUNT_EXCEED(9206, "退款金额超过订单金额"),

    // =============================================
    // 第三方API相关错误码 (9301-9399)
    // =============================================
    THIRD_PARTY_API_ERROR(9301, "第三方API调用失败"),
    THIRD_PARTY_API_TIMEOUT(9302, "第三方API超时"),
    THIRD_PARTY_API_KEY_INVALID(9303, "第三方API Key无效"),
    THIRD_PARTY_API_RATE_LIMIT(9304, "第三方API调用频率超限");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}