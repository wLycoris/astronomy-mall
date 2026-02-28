package com.astronomy.mall.module.admin.mapper;

import com.astronomy.mall.module.admin.dto.ReviewQueryDTO;
import com.astronomy.mall.module.admin.entity.ReviewEntity;
import com.astronomy.mall.module.admin.vo.AdminReviewVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 后台评价管理 Mapper
 * 对应XML: AdminReviewMapper.xml
 */
@Mapper
public interface AdminReviewMapper extends BaseMapper<ReviewEntity> {

    /**
     * 分页查询评价列表（关联商品/用户/订单）
     *
     * @param page 分页对象（MyBatis-Plus自动处理分页）
     * @param dto  查询条件
     * @return 评价VO列表
     */
    List<AdminReviewVO> selectReviewPage(
            @Param("page") Page<AdminReviewVO> page,
            @Param("dto") ReviewQueryDTO dto
    );

    /**
     * 查询评价详情（关联商品/用户/订单）
     *
     * @param id 评价ID
     * @return 评价详情VO
     */
    AdminReviewVO selectReviewDetail(@Param("id") Long id);
}