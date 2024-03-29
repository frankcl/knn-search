package xin.manong.search.knn.cache;

import com.google.common.cache.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
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
    private final ExecutorService executorService;
    private Cache<String, KNNIndexAllocation> cache;

    /**
     * 获取KNN索引缓存实例
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
     * 销毁缓存
     */
    public void destroy() {
        Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            cache.invalidateAll();
            executorService.shutdown();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 私有化构造函数
     */
    private KNNIndexCache() {
        listener = new KNNIndexListener();
        readWriteLock = new ReentrantReadWriteLock();
        executorService = Executors.newSingleThreadExecutor();
        config = new KNNIndexCacheConfig();
        config.cacheExpiredEnable = KNNSettings.getGlobalSettingValue(
                KNNSettings.KNN_GLOBAL_CACHE_EXPIRED_ENABLED);
        config.cacheExpiredTimeMinutes = ((TimeValue) KNNSettings.getGlobalSettingValue(
                KNNSettings.KNN_GLOBAL_CACHE_EXPIRED_TIME_MINUTES)).getMinutes();
        config.memoryCircuitBreakerEnable = KNNSettings.getGlobalSettingValue(
                KNNSettings.KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_ENABLED);
        config.memoryCircuitBreakerLimit = ((ByteSizeValue) KNNSettings.getGlobalSettingValue(
                KNNSettings.KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_LIMIT)).getKb();
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
        executorService.execute(() -> allocation.knnIndex.close());
        if (RemovalCause.SIZE == notification.getCause()) {
            KNNSettings.getInstance().updateCircuitBreakerTrigger(true);
            setCacheCapacityReached(true);
        }
        logger.info("KNN index[{}] has been removed, cause[{}]",
                notification.getKey(), notification.getCause().name());
    }

    /**
     * 加载KNN索引
     *
     * @param meta 索引元数据
     * @return KNN索引内存分配
     */
    private KNNIndexAllocation load(KNNIndexMeta meta) throws IOException {
        KNNIndex index = meta instanceof FAISSIndexMeta ?
                new FAISSIndex((FAISSIndexMeta) meta) :
                new HNSWIndex((HNSWIndexMeta) meta);
        index.open();
        WatcherHandle<FileWatcher> watcherHandle = null;
        if (resourceWatcherService != null) {
            Path path = Paths.get(meta.path);
            FileWatcher fileWatcher = new FileWatcher(path);
            fileWatcher.addListener(listener);
            fileWatcher.init();
            watcherHandle = resourceWatcherService.add(fileWatcher);
        }
        logger.info("KNN index[{}] has been loaded", meta.path);
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
            logger.info("KNN index cache config is not changed, ignore rebuilding");
            return;
        }
        executorService.execute(() -> {
            logger.info("KNN index cache is rebuilding ...");
            Lock writeLock = readWriteLock.writeLock();
            writeLock.lock();
            try {
                cache.invalidateAll();
                build(config);
            } finally {
                logger.info("KNN index cache rebuild success");
                writeLock.unlock();
            }
        });
    }

    /**
     * 获取KNN索引
     * 1. 如果缓存中存在，返回索引内存分配
     * 2. 如果缓存中不存在，创建索引内存分配，并放入缓存
     *
     * @param meta 索引元数据
     * @return KNN索引
     */
    public KNNIndex get(KNNIndexMeta meta) {
        if (meta == null || !meta.check()) {
            logger.error("invalid KNN index meta");
            throw new IllegalStateException("invalid KNN index meta");
        }
        Lock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            KNNIndexAllocation allocation = cache.get(meta.path, () -> load(meta));
            return allocation.knnIndex;
        } catch (ExecutionException e) {
            logger.error("get KNN index[{}] failed", meta.path);
            throw new RuntimeException(e);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 移除KNN索引
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
     * @return 移除索引内存大小
     */
    public Long removeByIndex(String index) {
        if (Strings.isNullOrEmpty(index)) return 0L;
        Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            Long size = 0L;
            for (Map.Entry<String, KNNIndexAllocation> entry : cache.asMap().entrySet()) {
                KNNIndexAllocation allocation = entry.getValue();
                KNNIndexMeta meta = allocation.knnIndex.getMeta();
                if (meta.index == null || !meta.index.equals(index)) continue;
                size += allocation.knnIndex.getMemorySize();
                cache.invalidate(entry.getKey());
            }
            return size;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 获取缓存统计
     *
     * @return 缓存统计
     */
    public CacheStats getCacheStats() {
        return cache.stats();
    }

    /**
     * 计算缓存内存占用大小，单位KB
     *
     * @return 缓存内存占用大小
     */
    public Long getCacheMemorySizeKB() {
        return cache.asMap().values().stream().mapToLong(
                allocation -> allocation.knnIndex.getMemorySize()).sum();
    }

    /**
     * 获取索引缓存占用大小，单位KB
     *
     * @param index 索引名
     * @return 索引缓存占用大小
     */
    public Long getCacheMemorySizeKB(String index) {
        if (Strings.isNullOrEmpty(index)) return 0L;
        return cache.asMap().values().stream().filter(
                allocation -> {
                    String currentIndex = allocation.knnIndex.getMeta().index;
                    return currentIndex != null && currentIndex.equals(index);
                }).mapToLong(allocation -> allocation.knnIndex.getMemorySize()).sum();
    }

    /**
     * 获取缓存内存占用统计信息
     *
     * @return 缓存内存占用统计信息
     */
    public Map<String, ? extends Object> getCacheMemoryStatMap() {
        Map<String, Map<String, Long>> cacheMemoryStats = new HashMap<>();
        for (KNNIndexAllocation allocation : cache.asMap().values()) {
            KNNIndexMeta indexMeta = allocation.knnIndex.getMeta();
            if (!cacheMemoryStats.containsKey(indexMeta.index)) cacheMemoryStats.put(indexMeta.index, new HashMap<>());
            Map<String, Long> fieldMemoryMap = cacheMemoryStats.get(indexMeta.index);
            if (!fieldMemoryMap.containsKey(indexMeta.field)) fieldMemoryMap.put(indexMeta.field, 0L);
            fieldMemoryMap.put(indexMeta.field, fieldMemoryMap.get(indexMeta.field) +
                    allocation.knnIndex.getMemorySize());
        }
        return cacheMemoryStats;
    }

    /**
     * 判断缓存容量是否达到上限
     *
     * @return 达到上限返回true，否则返回false
     */
    public boolean isCacheCapacityReached() {
        return cacheCapacityReached == null ? false : cacheCapacityReached.get();
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
