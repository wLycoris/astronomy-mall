package com.astronomy.mall.module.aftersale.service;

import com.astronomy.mall.module.aftersale.dto.RecyclingApplyDTO;
import com.astronomy.mall.module.aftersale.vo.RecyclingVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 二手回收服务接口（用户端）
 *
 * 📌 提供用户侧 6 个核心操作:
 *   1. 提交回收申请
 *   2. 查看我的申请列表
 *   3. 查看申请详情
 *   4. 确认报价
 *   5. 拒绝报价
 *   6. 取消申请
 */
public interface RecyclingService {

    /**
     * 提交回收申请
     *
     * @param userId 当前用户ID
     * @param dto    申请信息
     * @return 申请详情 VO
     */
    RecyclingVO submitApply(Long userId, RecyclingApplyDTO dto);

    /**
     * 查询我的申请列表（分页）
     *
     * @param userId   当前用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    Page<RecyclingVO> getMyList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 查询申请详情
     *
     * @param userId 当前用户ID（校验归属）
     * @param id     申请ID
     * @return 申请详情 VO
     */
    RecyclingVO getDetail(Long userId, Long id);

    /**
     * 确认管理员报价
     *
     * @param userId 当前用户ID
     * @param id     申请ID
     */
    void confirmQuote(Long userId, Long id);

    /**
     * 拒绝管理员报价
     *
     * @param userId 当前用户ID
     * @param id     申请ID
     */
    void rejectQuote(Long userId, Long id);

    /**
     * 取消申请（仅待审核状态可取消）
     *
     * @param userId 当前用户ID
     * @param id     申请ID
     */
    void cancelApply(Long userId, Long id);
}