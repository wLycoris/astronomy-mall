package com.astronomy.mall.module.aftersale.mapper;

import com.astronomy.mall.module.admin.vo.AdminInstallationVO;
import com.astronomy.mall.module.aftersale.entity.Installation;
import com.astronomy.mall.module.aftersale.vo.InstallationVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 安装预约 Mapper 接口
 *
 * 📌 用户端查询: selectUserList  (关联 tb_order 获取 order_no)
 * 📌 管理端查询: selectAdminList (关联 tb_order + tb_user 获取完整信息)
 */
@Mapper
public interface InstallationMapper extends BaseMapper<Installation> {

    /**
     * 查询用户的预约列表（关联订单获取订单号）
     *
     * @param page   分页参数
     * @param userId 用户ID
     * @return 分页预约VO列表
     */
    IPage<InstallationVO> selectUserList(IPage<InstallationVO> page,
                                         @Param("userId") Long userId);

    /**
     * 管理端查询预约列表（关联订单和用户）
     *
     * @param page      分页参数
     * @param status    状态筛选，null 表示不过滤
     * @param startTime 开始时间，格式 yyyy-MM-dd HH:mm:ss
     * @param endTime   结束时间，格式 yyyy-MM-dd HH:mm:ss
     * @return 分页管理员预约VO列表
     */
    IPage<AdminInstallationVO> selectAdminList(IPage<AdminInstallationVO> page,
                                               @Param("status") Integer status,
                                               @Param("startTime") String startTime,
                                               @Param("endTime") String endTime);
}