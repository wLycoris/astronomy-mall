package com.astronomy.mall.module.aftersale.mapper;

import com.astronomy.mall.module.aftersale.entity.ServiceReminder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 器材保养提醒 Mapper 接口
 *
 * 📌 继承 BaseMapper<ServiceReminder>，提供标准 CRUD 方法：
 *   - insert / deleteById / updateById / selectById / selectList
 *
 * 📌 业务查询均使用 MyBatis-Plus QueryWrapper 在 ServiceImpl 中完成，
 *    无需额外 XML 文件（查询条件简单，只按 user_id 过滤 + 排序）
 *
 * 路径: com.astronomy.mall.module.aftersale.mapper.ServiceReminderMapper
 */
@Mapper
public interface ServiceReminderMapper extends BaseMapper<ServiceReminder> {
    // 所有查询逻辑在 ServiceImpl 中使用 QueryWrapper 实现，无需自定义 SQL
}