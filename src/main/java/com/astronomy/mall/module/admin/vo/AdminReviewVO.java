package com.astronomy.mall.module.admin.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 后台评价列表/详情 VO
 *
 * 修复记录 2026-02-26:
 * - 加 @JsonInclude(JsonInclude.Include.ALWAYS)
 *   原因: application.yml 配置了 jackson.default-property-inclusion: non_null
 *   导致 reply、replyTime 等为 null 时被 Jackson 过滤掉，前端收不到这些字段，
 *   row.reply 变成 undefined，v-if="row.reply" 和显示逻辑全部失效
 *   加此注解后，即使字段为 null 也会正常序列化输出
 */
@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class AdminReviewVO {

    /** 评价ID */
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 商品名称 */
    private String productName;

    /** 商品主图 */
    private String productImage;

    /** 用户ID */
    private Long userId;

    /** 用户昵称 */
    private String userNickname;

    /** 用户头像 */
    private String userAvatar;

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 评分（1-5星） */
    private Integer rating;

    /** 评价内容 */
    private String content;

    /** 评价图片（JSON数组） */
    private String images;

    /** 是否匿名（0-否 1-是） */
    private Integer isAnonymous;

    /** 点赞数 */
    private Integer likeCount;

    /**
     * 评价状态
     * 0 - 已删除
     * 1 - 正常
     * 2 - 待审核
     */
    private Integer status;

    /** 商家回复（null 表示未回复，空串不会出现） */
    private String reply;

    /** 回复时间 */
    private LocalDateTime replyTime;

    /** 是否置顶（0-否 1-是） */
    private Integer isTop;

    /** 创建时间 */
    private LocalDateTime createTime;
}