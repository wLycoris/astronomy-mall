package com.astronomy.mall.module.user.mapper;

import com.astronomy.mall.module.user.entity.BalanceLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 余额流水 Mapper
 *
 * 文件路径: com.astronomy.mall.module.user.mapper.BalanceLogMapper
 */
@Mapper
public interface BalanceLogMapper extends BaseMapper<BalanceLog> {

    /**
     * 查询用户最近 N 条流水（用于钱包首页展示）
     *
     * @param userId 用户ID
     * @param limit  条数
     * @return 流水列表，按创建时间倒序
     */
    List<BalanceLog> selectRecentByUserId(@Param("userId") Long userId,
                                          @Param("limit") int limit);

    /**
     * 分页查询用户完整流水（用于流水列表页）
     *
     * @param page   分页参数
     * @param userId 用户ID
     * @return 分页流水
     */
    IPage<BalanceLog> selectPageByUserId(Page<BalanceLog> page,
                                         @Param("userId") Long userId);
}