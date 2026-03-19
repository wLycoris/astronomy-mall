package com.astronomy.mall.module.nasa.service;

import com.astronomy.mall.module.nasa.vo.ApodVO;
import com.astronomy.mall.module.nasa.vo.MarsPhotoVO;

import java.util.List;

/**
 * NASA API 统一调用服务接口
 *
 * 📌 设计原则：
 * - 当日内存缓存（APOD），保证每天实际只调用 NASA API 一次
 * - NASA API 限额：每小时 1000 次，缓存策略保证不超限
 *
 * 📌 调用方：
 * - NasaController         → GET /api/nasa/apod（商城首页 ApodCard）
 * - APODSyncScheduler      → 课程模块 APOD 自动同步（每天凌晨1:00）
 * - MarsRoverSyncScheduler → 课程模块 火星车照片同步（每天凌晨2:30）
 */
public interface NasaApiService {

    /**
     * 获取今日 NASA 每日天文图片 (APOD)
     *
     * 📌 当日内存缓存：同一天内重复调用直接返回缓存，不再请求 NASA
     * 📌 多线程安全：方法实现加 synchronized，避免并发重复请求
     *
     * @return ApodVO（包含 date/title/explanation/url/hdurl/mediaType/copyright）
     */
    ApodVO getTodayApod();

    /**
     * 获取火星车最新照片列表（取前3张）
     *
     * 📌 Rover 优先级：先请求 Perseverance，若返回为空则降级到 Curiosity
     * 📌 无缓存（MarsRoverSyncScheduler 每天只调用一次，不需要缓存）
     *
     * @return 最多3张 MarsPhotoVO（可能为空列表，调用方需判空）
     */
    List<MarsPhotoVO> getLatestMarsPhotos();
}