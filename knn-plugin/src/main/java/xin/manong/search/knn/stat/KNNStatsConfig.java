package xin.manong.search.knn.stat;

import com.google.common.cache.CacheStats;
import com.google.common.collect.ImmutableMap;
import xin.manong.search.knn.cache.KNNIndexCache;
import xin.manong.search.knn.common.KNNConstants;
import xin.manong.search.knn.stat.supplier.KNNCircuitBreakerSupplier;
import xin.manong.search.knn.stat.supplier.KNNIndexCacheStatsSupplier;
import xin.manong.search.knn.stat.supplier.KNNIndexCacheSupplier;

import java.util.Map;

/**
 * KNN统计项配置
 *
 * @author frankcl
 * @date 2020-07-01 19:38:11
 */
public class KNNStatsConfig {

    public static Map<String, KNNStat<?>> KNN_STATS = ImmutableMap.<String, KNNStat<?>>builder()
            .put(KNNConstants.HIT_COUNT, new KNNStat<>(false,
                    new KNNIndexCacheStatsSupplier(CacheStats::hitCount)))
            .put(KNNConstants.MISS_COUNT, new KNNStat<>(false,
                    new KNNIndexCacheStatsSupplier(CacheStats::missCount)))
            .put(KNNConstants.EVICT_COUNT, new KNNStat<>(false,
                    new KNNIndexCacheStatsSupplier(CacheStats::evictionCount)))
            .put(KNNConstants.LOAD_SUCCESS_COUNT, new KNNStat<>(false,
                    new KNNIndexCacheStatsSupplier(CacheStats::loadSuccessCount)))
            .put(KNNConstants.LOAD_FAIL_COUNT, new KNNStat<>(false,
                    new KNNIndexCacheStatsSupplier(CacheStats::loadExceptionCount)))
            .put(KNNConstants.TOTAL_LOAD_TIME, new KNNStat<>(false,
                    new KNNIndexCacheStatsSupplier(CacheStats::totalLoadTime)))
            .put(KNNConstants.MEMORY_SIZE, new KNNStat<>(false,
                    new KNNIndexCacheSupplier<>(KNNIndexCache::getCacheMemorySizeKB)))
            .put(KNNConstants.MEMORY_STATS, new KNNStat<>(false,
                    new KNNIndexCacheSupplier<>(KNNIndexCache::getCacheMemoryStatMap)))
            .put(KNNConstants.CACHE_CAPACITY_REACHED, new KNNStat<>(false,
                    new KNNIndexCacheSupplier<>(KNNIndexCache::isCacheCapacityReached)))
            .put(KNNConstants.CIRCUIT_BREAKER_TRIGGERED, new KNNStat<>(true,
                    new KNNCircuitBreakerSupplier())).build();
}