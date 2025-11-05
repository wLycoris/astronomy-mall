package com.astronomy.mall.module.user.mapper;

import com.astronomy.mall.module.user.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * MyBatis-Plus已提供以下方法:
     * - insert(User)
     * - deleteById(Serializable)
     * - updateById(User)
     * - selectById(Serializable)
     * - selectOne(Wrapper)
     * - selectList(Wrapper)
     * - selectPage(Page, Wrapper)
     *
     * 如需自定义SQL,在UserMapper.xml中编写
     */
}