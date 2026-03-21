package com.astronomy.mall.module.nasa.service;

import com.astronomy.mall.module.nasa.vo.ApodVO;
import com.astronomy.mall.module.nasa.vo.MarsPhotoVO;

import java.time.LocalDate;
import java.util.List;

/**
 * NASA API 统一调用服务接口
 *
 * 📌 设计原则：
 * - 当日内存缓存（APOD），保证每天实际只调用 NASA API 一次
 * - NASA API 限额：每小时 1000 次，缓存策略保证不超限
 *
 * 📌 调用方：
 * - NasaController             → GET /api/nasa/apod（商城首页 ApodCard）
 * - APODSyncScheduler          → 课程模块 APOD 自动同步（每天凌晨 2:00）✅
 * - MarsRoverSyncScheduler     → 课程模块 火星车照片同步（每天凌晨 2:30）✅
 *
 * 📌 版本变更说明 (5.2):
 * - 新增 getApodByDate(date)        管理员批量补录历史 APOD，不走缓存
 * - 新增 getAllLatestMarsPhotos()    MarsRoverSyncScheduler 专用，返回全量照片（不限3张）
 */
public interface NasaApiService {

    /**
     * 获取今日 NASA 每日天文图片 (APOD)
     *
     * 📌 当日内存缓存：同一天内重复调用直接返回缓存，不再请求 NASA
     * 📌 多线程安全：方法实现加 synchronized
     *
     * @return ApodVO（date/title/explanation/url/hdurl/mediaType/copyright），失败返回 null
     */
    ApodVO getTodayApod();

    /**
     * 获取指定日期的 NASA APOD 数据（管理员批量补录历史数据专用）
     *
     * ⚠️ 与 getTodayApod() 的区别：
     * - getTodayApod()    带当日内存缓存，保证全天只请求一次（首页 + 定时任务共用）
     * - getApodByDate()   每次都直接请求 NASA API，不走缓存（历史补录场景）
     *
     * NASA APOD 历史最早日期：1995-06-16
     * NASA API：GET https://api.nasa.gov/planetary/apod?api_key={key}&date={yyyy-MM-dd}
     *
     * @param date 指定日期（不能晚于今天）
     * @return ApodVO，失败返回 null（调用方判空后跳过即可）
     */
    ApodVO getApodByDate(LocalDate date);

    /**
     * 获取火星车最新照片（取前3张，用于 NasaController 接口展示）
     *
     * 📌 降级策略：Perseverance → Curiosity
     * 📌 返回数量上限：3张（前端展示用，减少传输量）
     *
     * @return 最多3张 MarsPhotoVO，可能为空列表
     */
    List<MarsPhotoVO> getLatestMarsPhotos();

    /**
     * 获取火星车最新照片（全量，MarsRoverSyncScheduler 专用）
     *
     * ⚠️ 与 getLatestMarsPhotos() 的区别：
     * - getLatestMarsPhotos()     限制返回3张，供前端展示
     * - getAllLatestMarsPhotos()  返回全量照片（最多200张），供定时任务按 earthDate 分组建章节
     *
     * 📌 降级策略：Perseverance → Curiosity（与 getLatestMarsPhotos 相同）
     * 📌 NASA latest_photos 接口返回最近一个 sol 的全部照片
     *
     * @return 全量照片列表（最多200张），可能为空列表
     */
    List<MarsPhotoVO> getAllLatestMarsPhotos();
}