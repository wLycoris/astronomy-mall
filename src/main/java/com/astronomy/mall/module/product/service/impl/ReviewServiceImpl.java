package com.astronomy.mall.module.product.service.impl;

import com.astronomy.mall.module.product.dto.ReviewStatisticsVO;
import com.astronomy.mall.module.product.dto.ReviewVO;
import com.astronomy.mall.module.product.entity.Review;
import com.astronomy.mall.module.product.mapper.ReviewMapper;
import com.astronomy.mall.module.product.service.ReviewService;
import com.astronomy.mall.module.user.entity.User;
import com.astronomy.mall.module.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评价Service实现类
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    @Resource
    private ReviewMapper reviewMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public Page<ReviewVO> getProductReviews(Long productId, Integer pageNum, Integer pageSize) {
        // 1. 分页查询评价
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getProductId, productId)
                .orderByDesc(Review::getCreateTime);

        Page<Review> page = new Page<>(pageNum, pageSize);
        Page<Review> reviewPage = reviewMapper.selectPage(page, wrapper);

        // 2. 转换为VO
        Page<ReviewVO> result = new Page<>(reviewPage.getCurrent(), reviewPage.getSize(), reviewPage.getTotal());
        List<ReviewVO> voList = reviewPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        result.setRecords(voList);

        return result;
    }

    @Override
    public ReviewStatisticsVO getReviewStatistics(Long productId) {
        // 1. 获取基础统计
        Map<String, Object> stats = reviewMapper.getReviewStatistics(productId);

        // 2. 获取各星级评价数 (注意: selectCount返回Long类型,需要转换为int)
        // 方式1: 使用clone()复用wrapper
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getProductId, productId);

        Long fiveStarCount = reviewMapper.selectCount(wrapper.clone().eq(Review::getRating, 5));
        Long fourStarCount = reviewMapper.selectCount(wrapper.clone().eq(Review::getRating, 4));
        Long threeStarCount = reviewMapper.selectCount(wrapper.clone().eq(Review::getRating, 3));
        Long twoStarCount = reviewMapper.selectCount(wrapper.clone().eq(Review::getRating, 2));
        Long oneStarCount = reviewMapper.selectCount(wrapper.clone().eq(Review::getRating, 1));

        // 3. 组装VO（安全类型转换）
        ReviewStatisticsVO vo = new ReviewStatisticsVO();
        if (stats != null) {
            Object reviewCountObj = stats.get("reviewCount");
            vo.setReviewCount(reviewCountObj == null ? 0 : ((Number) reviewCountObj).intValue());

            Object avgObj = stats.get("avgRating");
            vo.setAvgRating(avgObj == null ? 0.0 : ((BigDecimal) avgObj).doubleValue());

            Object goodRateObj = stats.get("goodRate");
            vo.setGoodRate(goodRateObj == null ? 0.0 : ((BigDecimal) goodRateObj).doubleValue());
        }

        vo.setFiveStar(fiveStarCount.intValue());
        vo.setFourStar(fourStarCount.intValue());
        vo.setThreeStar(threeStarCount.intValue());
        vo.setTwoStar(twoStarCount.intValue());
        vo.setOneStar(oneStarCount.intValue());

        return vo;
    }

    /**
     * Entity转VO
     */
    private ReviewVO convertToVO(Review review) {
        ReviewVO vo = new ReviewVO();
        BeanUtils.copyProperties(review, vo);

        // 如果不是匿名,查询用户信息
        if (review.getIsAnonymous() == 0) {
            User user = userMapper.selectById(review.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            } else {
                // 添加这个else分支,防止用户不存在时空指针
                vo.setNickname("未知用户");
                vo.setAvatar("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
            }
        } else {
            vo.setNickname("匿名用户");
            // 修改这里的默认头像路径
            vo.setAvatar("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        }

        // 处理图片列表
        if (review.getImages() != null && !review.getImages().isEmpty()) {
            vo.setImageList(Arrays.asList(review.getImages().split(",")));
        }

        return vo;
    }
}