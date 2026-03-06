package com.astronomy.mall.module.admin.mapper;

import com.astronomy.mall.module.admin.entity.SystemSetting;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统设置 Mapper
 *
 * 📌 说明:
 *   继承 BaseMapper 获得基础 CRUD
 *   补充按分组批量查询的方法
 */
@Mapper
public interface SystemSettingMapper extends BaseMapper<SystemSetting> {

    /**
     * 按分组名称查询该组下所有配置
     *
     * @param groupName 分组名称，如 "basic"、"freight"
     * @return 该分组的配置列表
     */
    @Select("SELECT * FROM tb_system_setting WHERE group_name = #{groupName} ORDER BY id ASC")
    List<SystemSetting> selectByGroupName(@Param("groupName") String groupName);

    /**
     * 按分组名称 + 配置键查询单条配置
     *
     * @param groupName  分组名称
     * @param settingKey 配置键
     * @return 单条配置，不存在返回 null
     */
    @Select("SELECT * FROM tb_system_setting WHERE group_name = #{groupName} AND setting_key = #{settingKey} LIMIT 1")
    SystemSetting selectByGroupAndKey(@Param("groupName") String groupName,
                                      @Param("settingKey") String settingKey);
}