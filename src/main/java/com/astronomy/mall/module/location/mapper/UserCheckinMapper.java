package com.astronomy.mall.module.location.mapper;

import com.astronomy.mall.module.location.entity.UserCheckin;
import com.astronomy.mall.module.location.vo.CheckinVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 用户签到记录Mapper
 * 对应表: tb_user_checkin
 *
 * 📌 6.0 骨架类，业务SQL在 6.3 节填充
 */
public interface UserCheckinMapper extends BaseMapper<UserCheckin> {

    /**
     * 查询某观测点今日签到人数
     * 用于签到成功通知中的 todayCount 变量
     *
     * 📌 TODO 6.3: 由 LocationServiceImpl.checkin() 调用
     *
     * @param spotId      观测点ID
     * @param checkinDate 今日日期
     * @return 今日签到人数
     */
    @Select("SELECT COUNT(*) FROM tb_user_checkin WHERE spot_id=#{spotId} AND checkin_date=#{checkinDate}")
    int countTodayCheckin(@Param("spotId") Long spotId, @Param("checkinDate") LocalDate checkinDate);

    /**
     * 查询用户签到历史（我的足迹）
     * SQL 在 UserCheckinMapper.xml 中实现（需JOIN观测点表获取观测点名称）
     *
     * 📌 TODO 6.3: 填充XML中的SQL
     *
     * @param userId   用户ID
     * @param pageOffset 分页偏移
     * @param pageSize   每页数量
     * @return 签到历史VO列表
     */
    List<CheckinVO> listMyCheckins(@Param("userId") Long userId,
                                   @Param("pageOffset") int pageOffset,
                                   @Param("pageSize") int pageSize);

    /**
     * 查询用户历史签到总次数
     *
     * @param userId 用户ID
     * @return 签到总次数
     */
    @Select("SELECT COUNT(*) FROM tb_user_checkin WHERE user_id=#{userId}")
    int countByUserId(@Param("userId") Long userId);

    /**
     * 6.5: 查询某观测点在指定日期之后的签到次数
     * 用于管理员端签到统计（近7日/近30日）
     *
     * @param spotId    观测点ID
     * @param afterDate 起始日期（不含）
     * @return 签到次数
     */
    @Select("SELECT COUNT(*) FROM tb_user_checkin WHERE spot_id=#{spotId} AND checkin_date >= #{afterDate}")
    int countBySpotIdAfterDate(@Param("spotId") Long spotId, @Param("afterDate") LocalDate afterDate);

    /**
     * 6.5: 查询某观测点签到次数TOP N的用户
     * 用于管理员端签到统计弹窗
     *
     * @param spotId 观测点ID
     * @param limit  返回条数
     * @return 用户ID + 签到次数列表
     */
    @Select("SELECT user_id AS userId, COUNT(*) AS checkinCount " +
            "FROM tb_user_checkin WHERE spot_id=#{spotId} " +
            "GROUP BY user_id ORDER BY checkinCount DESC LIMIT #{limit}")
    List<Map<String, Object>> selectTopUsersBySpotId(@Param("spotId") Long spotId, @Param("limit") int limit);
}