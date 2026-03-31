package com.codesonify.repository;

import com.codesonify.domain.entity.ProjectAnalysis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务（内存实现 - MVP 测试用）
 *
 * 注意：生产环境应使用 Redis 实现
 */
@Slf4j
@Service
public class CacheService {

    /**
     * 内存缓存存储
     */
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * 默认缓存时间（秒）
     */
    private static final long DEFAULT_TTL = 3600;

    /**
     * 定时清理过期缓存
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public CacheService() {
        // 每分钟清理一次过期缓存
        scheduler.scheduleAtFixedRate(this::cleanupExpired, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * 缓存分析结果
     *
     * @param analysisId 分析 ID
     * @param analysis 分析结果
     */
    public void cacheAnalysisResult(String analysisId, ProjectAnalysis analysis) {
        cacheAnalysisResult(analysisId, analysis, DEFAULT_TTL);
    }

    /**
     * 缓存分析结果（指定过期时间）
     *
     * @param analysisId 分析 ID
     * @param analysis 分析结果
     * @param ttl 过期时间（秒）
     */
    public void cacheAnalysisResult(String analysisId, ProjectAnalysis analysis, long ttl) {
        String key = generateKey(analysisId);
        long expireTime = System.currentTimeMillis() + (ttl * 1000);
        cache.put(key, new CacheEntry(analysis, expireTime));
        log.debug("分析结果已缓存：{} (过期时间：{})", key, expireTime);
    }

    /**
     * 获取缓存的分析结果
     *
     * @param analysisId 分析 ID
     * @return 分析结果，不存在则返回 null
     */
    public ProjectAnalysis getCachedAnalysis(String analysisId) {
        String key = generateKey(analysisId);
        CacheEntry entry = cache.get(key);
        if (entry != null) {
            if (System.currentTimeMillis() < entry.expireTime) {
                log.debug("缓存命中：{}", key);
                return entry.analysis;
            } else {
                log.debug("缓存已过期：{}", key);
                cache.remove(key);
            }
        }
        log.debug("缓存未命中：{}", key);
        return null;
    }

    /**
     * 删除缓存的分析结果
     *
     * @param analysisId 分析 ID
     */
    public void evictAnalysisCache(String analysisId) {
        String key = generateKey(analysisId);
        cache.remove(key);
        log.debug("缓存已删除：{}", key);
    }

    /**
     * 检查缓存是否存在
     *
     * @param analysisId 分析 ID
     * @return 是否存在
     */
    public boolean hasAnalysisCache(String analysisId) {
        String key = generateKey(analysisId);
        CacheEntry entry = cache.get(key);
        if (entry != null && System.currentTimeMillis() < entry.expireTime) {
            return true;
        }
        if (entry != null) {
            cache.remove(key);
        }
        return false;
    }

    /**
     * 生成缓存键
     *
     * @param analysisId 分析 ID
     * @return 缓存键
     */
    private String generateKey(String analysisId) {
        return "analysis:" + analysisId;
    }

    /**
     * 清理过期缓存
     */
    private void cleanupExpired() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> entry.getValue().expireTime < now);
    }

    /**
     * 缓存条目
     */
    private static class CacheEntry {
        final ProjectAnalysis analysis;
        final long expireTime;

        CacheEntry(ProjectAnalysis analysis, long expireTime) {
            this.analysis = analysis;
            this.expireTime = expireTime;
        }
    }
}
