package com.astronomy.mall.module.recognition.mapper;

import com.astronomy.mall.module.recognition.entity.Recognition;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI星图识别 Mapper 接口
 *
 * 📌 v4.1 新增: updateSubmissionId, updateFailed
 * 📌 v4.2 新增: updateJobId, updateResult, selectPendingList,
 *               selectHistoryByUserId
 */
@Mapper
public interface RecognitionMapper extends BaseMapper<Recognition> {

    // ============================================================
    // v4.1 方法
    // ============================================================

    /**
     * 更新 submission_id（异步提交成功后调用）
     *
     * @param id           识别记录 ID
     * @param submissionId Astrometry submission_id
     */
    void updateSubmissionId(@Param("id") Long id,
                            @Param("submissionId") String submissionId);

    /**
     * 将识别记录标记为失败（status=2）
     *
     * @param id         识别记录 ID
     * @param failReason 失败原因
     */
    void updateFailed(@Param("id") Long id,
                      @Param("failReason") String failReason);

    // ============================================================
    // v4.2 方法
    // ============================================================

    /**
     * 更新 job_id（submission 处理完成、拿到 job_id 时调用）
     *
     * @param id    识别记录 ID
     * @param jobId Astrometry job_id
     */
    void updateJobId(@Param("id") Long id,
                     @Param("jobId") String jobId);

    /**
     * 更新识别成功结果（由 RecognitionPollScheduler 调用）
     *
     * 更新字段: status=1, job_id, ra, dec, orientation, radius,
     *           objects_in_field, machine_tags, result_image_url
     *
     * @param recognition 包含 id 和所有结果字段的实体（null 字段不更新）
     */
    void updateResult(Recognition recognition);

    /**
     * 查询所有待轮询的识别记录
     *
     * 条件: status=0 AND submission_id IS NOT NULL
     * 不查 image_data 大字段（用 RecognitionSimpleMap）
     *
     * @return 待轮询记录列表
     */
    List<Recognition> selectPendingList();

    /**
     * 查询用户历史识别记录（分页，按时间倒序）
     *
     * 不查 image_data 大字段
     *
     * @param userId 用户 ID
     * @param offset 偏移量（(pageNum-1)*pageSize）
     * @param limit  每页数量
     * @return 识别记录列表（不含 imageData）
     */
    List<Recognition> selectHistoryByUserId(@Param("userId") Long userId,
                                            @Param("offset") int offset,
                                            @Param("limit") int limit);

    /**
     * 统计用户历史识别记录总数
     *
     * @param userId 用户 ID
     * @return 总记录数
     */
    int countHistoryByUserId(@Param("userId") Long userId);
}