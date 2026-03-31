package com.codesonify.repository;

import com.codesonify.domain.entity.ProjectAnalysis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存服务
 *
 * 缓存分析结果，提高访问性能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 默认缓存时间（秒）
     */
    private static final long DEFAULT_TTL = 3600;

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
        redisTemplate.opsForValue().set(key, analysis, ttl, TimeUnit.SECONDS);
        log.debug("分析结果已缓存：{}", key);
    }

    /**
     * 获取缓存的分析结果
     *
     * @param analysisId 分析 ID
     * @return 分析结果，不存在则返回 null
     */
    public ProjectAnalysis getCachedAnalysis(String analysisId) {
        String key = generateKey(analysisId);
        Object result = redisTemplate.opsForValue().get(key);
        if (result instanceof ProjectAnalysis) {
            log.debug("命中缓存：{}", key);
            return (ProjectAnalysis) result;
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
        redisTemplate.delete(key);
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
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
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
}
