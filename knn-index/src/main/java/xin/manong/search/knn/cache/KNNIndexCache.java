package xin.manong.search.knn.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.watcher.FileWatcher;
import org.elasticsearch.watcher.ResourceWatcherService;
import org.elasticsearch.watcher.WatcherHandle;
import xin.manong.search.knn.common.KNNSettings;
import xin.manong.search.knn.index.KNNIndex;
import xin.manong.search.knn.index.KNNIndexMeta;
import xin.manong.search.knn.index.faiss.FAISSIndex;
import xin.manong.search.knn.index.faiss.FAISSIndexMeta;
import xin.manong.search.knn.index.hnsw.HNSWIndex;
import xin.manong.search.knn.index.hnsw.HNSWIndexMeta;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * KNN索引cache
 *
 * @author frankcl
 * @date 2023-01-18 17:39:30
 */
public class KNNIndexCache {

    private final static Logger logger = LogManager.getLogger(KNNIndexCache.class);

    private static KNNIndexCache instance;

    private AtomicBoolean cacheCapacityReached;
    private ResourceWatcherService resourceWatcherService;
    private KNNIndexCacheConfig config;
    private final KNNIndexListener listener;
    private final ReadWriteLock readWriteLock;
    private final Executor executor;
    private Cache<String, KNNIndexAllocation> cache;

    /**
     * 获取knn索引缓存实例
     *
     * @return 缓存实例
     */
    public static KNNIndexCache getInstance() {
        if (instance != null) return instance;
        synchronized (KNNIndexCache.class) {
            if (instance != null) return instance;
            instance = new KNNIndexCache();
            return instance;
        }
    }

    /**
     * 私有化构造函数
     */
    private KNNIndexCache() {
        listener = new KNNIndexListener();
        readWriteLock = new ReentrantReadWriteLock();
        executor = Executors.newSingleThreadExecutor();
        config = new KNNIndexCacheConfig();
        config.cacheExpiredEnable = KNNSettings.getSettingValue(KNNSettings.KNN_GLOBAL_CACHE_EXPIRED_ENABLED);
        config.cacheExpiredTimeMinutes = KNNSettings.getSettingValue(KNNSettings.KNN_GLOBAL_CACHE_EXPIRED_TIME_MINUTES);
        config.memoryCircuitBreakerEnable = KNNSettings.getSettingValue(KNNSettings.KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_ENABLED);
        config.memoryCircuitBreakerLimit = KNNSettings.getSettingValue(KNNSettings.KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_LIMIT);
        config.check();
        build(config);
    }

    /**
     * 构建缓存
     */
    private void build(KNNIndexCacheConfig config) {
        this.config = config;
        CacheBuilder<String, KNNIndexAllocation> builder = CacheBuilder.newBuilder()
                .recordStats()
                .concurrencyLevel(1)
                .removalListener(n -> onRemoval(n));
        if (config.memoryCircuitBreakerEnable) {
            builder.maximumWeight(config.memoryCircuitBreakerLimit).weigher(
                    (k, v) -> (int) v.knnIndex.getMemorySize());
        }
        if (config.cacheExpiredEnable) {
            builder.expireAfterAccess(config.cacheExpiredTimeMinutes, TimeUnit.MINUTES);
        }
        cacheCapacityReached = new AtomicBoolean(false);
        cache = builder.build();
    }

    /**
     * 处理索引删除通知
     *
     * @param notification 删除通知
     */
    private void onRemoval(RemovalNotification<String, KNNIndexAllocation> notification) {
        KNNIndexAllocation allocation = notification.getValue();
        if (allocation.fileWatcherHandle != null) allocation.fileWatcherHandle.stop();
        executor.execute(() -> allocation.knnIndex.close());
        if (RemovalCause.SIZE == notification.getCause()) {
//            KNNSettings.state().updateCircuitBreakerSettings(true);
            setCacheCapacityReached(true);
        }
        logger.info("knn index[{}] has been removed, cause[{}]", notification.getKey(), notification.getCause().name());
    }

    /**
     * 加载knn索引
     *
     * @param meta 索引元数据
     * @return knn索引内存分配
     */
    private KNNIndexAllocation load(KNNIndexMeta meta) throws IOException {
        Path path = Paths.get(meta.path);
        FileWatcher fileWatcher = new FileWatcher(path);
        fileWatcher.addListener(listener);
        fileWatcher.init();
        WatcherHandle<FileWatcher> watcherHandle = resourceWatcherService != null ?
                resourceWatcherService.add(fileWatcher) : null;
        KNNIndex index = meta instanceof FAISSIndexMeta ?
                new FAISSIndex((FAISSIndexMeta) meta) :
                new HNSWIndex((HNSWIndexMeta) meta);
        index.open();
        logger.info("load knn index[{}] success for path", meta.type.name(), meta.path);
        return new KNNIndexAllocation(index, watcherHandle);
    }

    /**
     * 判断缓存配置是否变化
     *
     * @param config 新缓存配置
     * @return 变化返回true，否则返回false
     */
    private boolean isCacheConfigChanged(KNNIndexCacheConfig config) {
        if (config == null) return false;
        if (config.cacheExpiredEnable == null) config.cacheExpiredEnable = this.config.cacheExpiredEnable;
        if (config.cacheExpiredTimeMinutes == null) config.cacheExpiredTimeMinutes = this.config.cacheExpiredTimeMinutes;
        if (config.memoryCircuitBreakerEnable == null) config.memoryCircuitBreakerEnable = this.config.memoryCircuitBreakerEnable;
        if (config.memoryCircuitBreakerLimit == null) config.memoryCircuitBreakerLimit = this.config.memoryCircuitBreakerLimit;
        if (config.cacheExpiredEnable.booleanValue() != this.config.cacheExpiredEnable.booleanValue()) return true;
        if (config.cacheExpiredTimeMinutes.intValue() != this.config.cacheExpiredTimeMinutes.intValue()) return true;
        if (config.memoryCircuitBreakerEnable.booleanValue() != this.config.memoryCircuitBreakerEnable.booleanValue()) return true;
        if (config.memoryCircuitBreakerLimit.longValue() != this.config.memoryCircuitBreakerLimit.longValue()) return true;
        return false;
    }

    /**
     * 重建缓存
     */
    public synchronized void rebuild(KNNIndexCacheConfig config) {
        if (!isCacheConfigChanged(config)) {
            logger.info("knn index cache config is not changed, ignore rebuilding");
            return;
        }
        executor.execute(() -> {
            logger.info("knn index cache is rebuilding ...");
            Lock writeLock = readWriteLock.writeLock();
            writeLock.lock();
            try {
                cache.invalidateAll();
                build(config);
            } finally {
                logger.info("knn index cache rebuild success");
                writeLock.unlock();
            }
        });
    }

    /**
     * 获取knn索引
     * 1. 如果缓存中存在，返回索引内存分配
     * 2. 如果缓存中不存在，创建索引内存分配，并放入缓存
     *
     * @param meta 索引元数据
     * @return knn索引
     */
    public KNNIndex get(KNNIndexMeta meta) {
        if (meta == null || !meta.check()) {
            logger.error("invalid knn index meta");
            throw new IllegalStateException("invalid knn index meta");
        }
        Lock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            KNNIndexAllocation allocation = cache.get(meta.path, () -> load(meta));
            return allocation.knnIndex;
        } catch (ExecutionException e) {
            logger.error("get knn index[{}] from cache failed", meta.path);
            throw new RuntimeException(e);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 移除knn索引
     *
     * @param path 索引路径
     */
    public void remove(String path) {
        Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            cache.invalidate(path);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 移除指定索引所有缓存
     *
     * @param index 索引名
     */
    public void removeByIndex(String index) {
        if (StringUtils.isEmpty(index)) return;
        Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            for (Map.Entry<String, KNNIndexAllocation> entry : cache.asMap().entrySet()) {
                KNNIndexAllocation allocation = entry.getValue();
                KNNIndexMeta meta = allocation.knnIndex.getMeta();
                if (meta.index == null || !meta.index.equals(index)) continue;
                cache.invalidate(entry.getKey());
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 设置缓存容量是否达到
     *
     * @param reached 达到true，未达到false
     */
    public void setCacheCapacityReached(boolean reached) {
        cacheCapacityReached.set(reached);
    }

    /**
     * 设置资源监听服务
     *
     * @param resourceWatcherService 资源监听服务
     */
    public void setResourceWatcherService(ResourceWatcherService resourceWatcherService) {
        this.resourceWatcherService = resourceWatcherService;
    }
}
