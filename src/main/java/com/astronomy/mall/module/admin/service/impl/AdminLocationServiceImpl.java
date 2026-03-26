package com.astronomy.mall.module.admin.service.impl;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.ResultCode;
import com.astronomy.mall.module.admin.service.AdminLocationService;
import com.astronomy.mall.module.location.dto.SpotCreateDTO;
import com.astronomy.mall.module.location.dto.SpotUpdateDTO;
import com.astronomy.mall.module.location.entity.ObservationSpot;
import com.astronomy.mall.module.location.mapper.ObservationSpotMapper;
import com.astronomy.mall.module.location.mapper.UserCheckinMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * 管理员端 - 观测点管理服务实现
 * 6.5 后台观测点管理
 *
 * 实现5个方法:
 *   listSpots    MyBatis-Plus分页 + LambdaQueryWrapper多条件筛选
 *   addSpot      BeanUtils拷贝 → insert
 *   updateSpot   selectById → 非null字段覆盖 → updateById
 *   deleteSpot   MyBatis-Plus @TableLogic 自动逻辑删除
 *   getSpotStats 3条COUNT查询 + TOP5用户聚合查询
 */
@Slf4j
@Service
public class AdminLocationServiceImpl implements AdminLocationService {

    @Autowired
    private ObservationSpotMapper observationSpotMapper;

    @Autowired
    private UserCheckinMapper userCheckinMapper;

    /**
     * 观测点分页列表
     * 支持省份、城市、名称关键词三重筛选
     */
    @Override
    public Map<String, Object> listSpots(Integer pageNum, Integer pageSize,
                                         String province, String city, String keyword) {
        Page<ObservationSpot> page = new Page<>(pageNum, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<ObservationSpot> wrapper = new LambdaQueryWrapper<>();
        // 省份筛选（模糊匹配，兼容"福建"和"福建省"两种写法）
        if (province != null && !province.isEmpty()) {
            wrapper.like(ObservationSpot::getProvince, province);
        }
        // 城市筛选（模糊匹配，兼容"厦门"和"厦门市"两种写法）
        if (city != null && !city.isEmpty()) {
            wrapper.like(ObservationSpot::getCity, city);
        }
        // 关键词模糊搜索观测点名称
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(ObservationSpot::getSpotName, keyword);
        }
        // 按创建时间倒序
        wrapper.orderByDesc(ObservationSpot::getCreateTime);

        Page<ObservationSpot> result = observationSpotMapper.selectPage(page, wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        return data;
    }

    /**
     * 新增观测点
     * DTO字段拷贝到实体，设置默认值后insert
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSpot(SpotCreateDTO dto) {
        ObservationSpot spot = new ObservationSpot();
        BeanUtils.copyProperties(dto, spot);

        // 设置默认值
        if (spot.getAltitude() == null) {
            spot.setAltitude(0);
        }
        if (spot.getLightPollutionLevel() == null) {
            spot.setLightPollutionLevel(5);
        }
        spot.setRating(new java.math.BigDecimal("0.00"));
        spot.setRatingCount(0);
        spot.setCheckinCount(0);

        observationSpotMapper.insert(spot);
        log.info("管理员新增观测点: id={}, name={}", spot.getId(), spot.getSpotName());
    }

    /**
     * 编辑观测点
     * 先查询原记录，非null字段覆盖后updateById
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpot(Long id, SpotUpdateDTO dto) {
        ObservationSpot spot = observationSpotMapper.selectById(id);
        if (spot == null) {
            throw new BusinessException("观测点不存在");
        }

        // 仅更新非null字段
        if (dto.getSpotName() != null) spot.setSpotName(dto.getSpotName());
        if (dto.getLongitude() != null) spot.setLongitude(dto.getLongitude());
        if (dto.getLatitude() != null) spot.setLatitude(dto.getLatitude());
        if (dto.getProvince() != null) spot.setProvince(dto.getProvince());
        if (dto.getCity() != null) spot.setCity(dto.getCity());
        if (dto.getAddress() != null) spot.setAddress(dto.getAddress());
        if (dto.getAltitude() != null) spot.setAltitude(dto.getAltitude());
        if (dto.getLightPollutionLevel() != null) spot.setLightPollutionLevel(dto.getLightPollutionLevel());
        if (dto.getDescription() != null) spot.setDescription(dto.getDescription());
        if (dto.getImages() != null) spot.setImages(dto.getImages());

        observationSpotMapper.updateById(spot);
        log.info("管理员编辑观测点: id={}, name={}", id, spot.getSpotName());
    }

    /**
     * 删除观测点（逻辑删除）
     * MyBatis-Plus @TableLogic 自动将 DELETE 转为 UPDATE deleted=1
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSpot(Long id) {
        ObservationSpot spot = observationSpotMapper.selectById(id);
        if (spot == null) {
            throw new BusinessException("观测点不存在");
        }
        observationSpotMapper.deleteById(id);
        log.info("管理员删除观测点: id={}, name={}", id, spot.getSpotName());
    }

    /**
     * 签到统计
     * 返回: totalCount(总签到次数), last7Days(近7日), last30Days(近30日), topUsers(TOP5用户)
     *
     * topUsers 格式: List<Map>，每项含 userId, checkinCount
     */
    @Override
    public Map<String, Object> getSpotStats(Long spotId) {
        ObservationSpot spot = observationSpotMapper.selectById(spotId);
        if (spot == null) {
            throw new BusinessException("观测点不存在");
        }

        Map<String, Object> stats = new HashMap<>();

        // 总签到次数（直接读冗余字段）
        stats.put("totalCount", spot.getCheckinCount() != null ? spot.getCheckinCount() : 0);

        // 近7日签到次数
        LocalDate date7 = LocalDate.now().minusDays(7);
        int last7Days = userCheckinMapper.countBySpotIdAfterDate(spotId, date7);
        stats.put("last7Days", last7Days);

        // 近30日签到次数
        LocalDate date30 = LocalDate.now().minusDays(30);
        int last30Days = userCheckinMapper.countBySpotIdAfterDate(spotId, date30);
        stats.put("last30Days", last30Days);

        // TOP5签到用户
        List<Map<String, Object>> topUsers = userCheckinMapper.selectTopUsersBySpotId(spotId, 5);
        stats.put("topUsers", topUsers);

        return stats;
    }
}
