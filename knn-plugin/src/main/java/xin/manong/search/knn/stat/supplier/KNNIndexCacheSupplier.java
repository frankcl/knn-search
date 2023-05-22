package xin.manong.search.knn.stat.supplier;

import xin.manong.search.knn.cache.KNNIndexCache;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * KNN索引缓存数据获取supplier
 *
 * @author frankcl
 * @date 2020-07-01 19:38:11
 */
public class KNNIndexCacheSupplier<T> implements Supplier<T> {

    private Function<KNNIndexCache, T> getter;

    public KNNIndexCacheSupplier(Function<KNNIndexCache, T> getter) {
        this.getter = getter;
    }

    @Override
    public T get() {
        return getter.apply(KNNIndexCache.getInstance());
    }
}