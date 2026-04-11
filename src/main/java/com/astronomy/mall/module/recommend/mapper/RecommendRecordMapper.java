package com.astronomy.mall.module.recommend.mapper;

import com.astronomy.mall.module.recommend.entity.RecommendRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 推荐曝光与点击记录 Mapper
 * 统计SQL放在 XML 中（RecommendRecordMapper.xml）
 */
@Mapper
public interface RecommendRecordMapper extends BaseMapper<RecommendRecord> {
}
