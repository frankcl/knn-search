package xin.manong.search.knn.stat.supplier;

import com.google.common.cache.CacheStats;
import xin.manong.search.knn.cache.KNNIndexCache;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * KNN索引缓存统计项获取supplier
 *
 * @author frankcl
 * @date 2020-07-01 19:38:11
 */
public class KNNIndexCacheStatsSupplier implements Supplier<Long> {

    private Function<CacheStats, Long> getter;

    public KNNIndexCacheStatsSupplier(Function<CacheStats, Long> getter) {
        this.getter = getter;
    }

    @Override
    public Long get() {
        return getter.apply(KNNIndexCache.getInstance().getCacheStats());
    }
}