package com.astronomy.mall.module.product.mapper;

import com.astronomy.mall.module.product.entity.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 增加浏览次数
     */
    @Update("UPDATE tb_product SET view_count = view_count + 1 WHERE id = #{productId} AND deleted = 0")
    int increaseViewCount(@Param("productId") Long productId);
}