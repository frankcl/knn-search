package xin.manong.search.knn.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsRequest;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsResponse;
import org.elasticsearch.client.internal.Client;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.monitor.jvm.JvmInfo;
import org.elasticsearch.monitor.os.OsProbe;
import xin.manong.search.knn.cache.KNNIndexCache;
import xin.manong.search.knn.cache.KNNIndexCacheConfig;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.elasticsearch.common.settings.Setting.Property.*;
import static org.elasticsearch.common.settings.Setting.Property.IndexScope;
import static org.elasticsearch.common.unit.ByteSizeValue.parseBytesSizeValue;

/**
 * KNN索引设置
 *
 * @author frankcl
 * @date 2023-01-19 11:38:03
 */
public class KNNSettings {

    private static final Logger logger = LogManager.getLogger(KNNSettings.class);

    private static final int DEFAULT_MAX_CACHE_EXPIRED_TIME_MINUTES = 180;
    private static final int DEFAULT_MEMORY_CIRCUIT_BREAKER_UNSET_PERCENTAGE = 85;
    private static final int MAX_NMSLIB_INDEX_THREAD_QTY = 32;
    private static final int MAX_HNSW_INDEX_VECTOR_NUM = 500000;
    private static final int DEFAULT_HNSW_M = 16;
    private static final int DEFAULT_HNSW_EF_SEARCH = 512;
    private static final int DEFAULT_HNSW_EF_CONSTRUCTION = 512;
    private static final int DEFAULT_FAISS_PQ_M = 16;
    private static final int DEFAULT_FAISS_PQ_ENCODE_BITS = 8;
    private static final int DEFAULT_FAISS_PCA_DIMENSION = 0;
    private static final int MIN_FAISS_PQ_M = 2;
    private static final int MIN_FAISS_PQ_ENCODE_BITS = 8;
    private static final int MIN_FAISS_PCA_DIMENSION = 0;
    private static final String DEFAULT_MAX_MEMORY_CIRCUIT_BREAKER_LIMIT = "90%";

    public static final String KNN_GLOBAL_INDEX_LAZY_LOAD = "knn.global.index.lazy_load";
    public static final String KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_ENABLED = "knn.global.memory.circuit_breaker.enabled";
    public static final String KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_LIMIT = "knn.global.memory.circuit_breaker.limit";
    public static final String KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_TRIGGERED = "knn.global.memory.circuit_breaker.triggered";
    public static final String KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_UNSET_PERCENTAGE = "knn.global.memory.circuit_breaker.unset_percentage";
    public static final String KNN_GLOBAL_CACHE_EXPIRED_ENABLED = "knn.global.cache.expired_enabled";
    public static final String KNN_GLOBAL_CACHE_EXPIRED_TIME_MINUTES = "knn.global.cache.expired_minutes";
    public static final String KNN_GLOBAL_NMSLIB_INDEX_THREAD_QTY = "knn.global.nmslib.index_thread_qty";

    public static final String KNN_INDEX_MAX_HNSW_VECTOR_NUM = "knn.index.max_hnsw_vector_num";
    public static final String KNN_INDEX_HNSW_M = "knn.index.hnsw.m";
    public static final String KNN_INDEX_HNSW_EF_CONSTRUCTION = "knn.index.hnsw.ef_construction";
    public static final String KNN_INDEX_HNSW_EF_SEARCH = "knn.index.hnsw.ef_search";

    public static final String KNN_INDEX_FAISS_PQ_M = "knn.index.faiss.pq_m";
    public static final String KNN_INDEX_FAISS_PQ_ENCODE_BITS = "knn.index.faiss.pq_encode_bits";
    public static final String KNN_INDEX_FAISS_PCA_DIMENSION = "knn.index.faiss.pca_dimension";

    private Client client;
    private ClusterService clusterService;

    public static final Setting<Boolean> KNN_GLOBAL_INDEX_LAZY_LOAD_SETTING = Setting.boolSetting(
            KNN_GLOBAL_INDEX_LAZY_LOAD, false, NodeScope, Dynamic);
    public static final Setting<Integer> KNN_GLOBAL_NMSLIB_INDEX_THREAD_QTY_SETTING = Setting.intSetting(
            KNN_GLOBAL_NMSLIB_INDEX_THREAD_QTY, Math.min(1, Math.max(Runtime.getRuntime().availableProcessors() / 2, 1)),
            1, MAX_NMSLIB_INDEX_THREAD_QTY, NodeScope, Dynamic);
    public static final Setting<Boolean> KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_TRIGGERED_SETTING = Setting.boolSetting(
            KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_TRIGGERED, false, NodeScope, Dynamic);
    public static final Setting<Double> KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_UNSET_PERCENTAGE_SETTING =  Setting.doubleSetting(
            KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_UNSET_PERCENTAGE, DEFAULT_MEMORY_CIRCUIT_BREAKER_UNSET_PERCENTAGE,
            0, 100, NodeScope, Dynamic);

    public static final Setting<Integer> KNN_INDEX_HNSW_M_SETTING = Setting.intSetting(
            KNN_INDEX_HNSW_M, DEFAULT_HNSW_M, 2, IndexScope);
    public static final Setting<Integer> KNN_INDEX_HNSW_EF_SEARCH_SETTING = Setting.intSetting(
            KNN_INDEX_HNSW_EF_SEARCH, DEFAULT_HNSW_EF_SEARCH, 2, IndexScope);
    public static final Setting<Integer> KNN_INDEX_HNSW_EF_CONSTRUCTION_SETTING = Setting.intSetting(
            KNN_INDEX_HNSW_EF_CONSTRUCTION, DEFAULT_HNSW_EF_CONSTRUCTION, 2, IndexScope);

    public static final Setting<Integer> KNN_INDEX_FAISS_PQ_M_SETTING = Setting.intSetting(
            KNN_INDEX_FAISS_PQ_M, DEFAULT_FAISS_PQ_M, MIN_FAISS_PQ_M, IndexScope);
    public static final Setting<Integer> KNN_INDEX_FAISS_PQ_ENCODE_BITS_SETTING = Setting.intSetting(
            KNN_INDEX_FAISS_PQ_ENCODE_BITS, DEFAULT_FAISS_PQ_ENCODE_BITS, MIN_FAISS_PQ_ENCODE_BITS, IndexScope);
    public static final Setting<Integer> KNN_INDEX_FAISS_PCA_DIMENSION_SETTING = Setting.intSetting(
            KNN_INDEX_FAISS_PCA_DIMENSION, DEFAULT_FAISS_PCA_DIMENSION, MIN_FAISS_PCA_DIMENSION, IndexScope);

    private static KNNSettings instance;
    private static OsProbe osProbe = OsProbe.getInstance();
    private static Map<String, Setting<?>> dynamicCacheSettingMap = new HashMap<>() {
        {
            put(KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_ENABLED, Setting.boolSetting(
                    KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_ENABLED, true, NodeScope, Dynamic));
            put(KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_LIMIT, new Setting<>(
                    KNNSettings.KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_LIMIT,
                    DEFAULT_MAX_MEMORY_CIRCUIT_BREAKER_LIMIT,
                    s -> parseMaxUsedNativeMemory(s, KNNSettings.KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_LIMIT),
                    NodeScope,
                    Dynamic)
            );
            put(KNN_GLOBAL_CACHE_EXPIRED_ENABLED, Setting.boolSetting(
                    KNN_GLOBAL_CACHE_EXPIRED_ENABLED, false, NodeScope, Dynamic));
            put(KNN_GLOBAL_CACHE_EXPIRED_TIME_MINUTES, Setting.positiveTimeSetting(KNN_GLOBAL_CACHE_EXPIRED_TIME_MINUTES,
                    TimeValue.timeValueMinutes(DEFAULT_MAX_CACHE_EXPIRED_TIME_MINUTES), NodeScope, Dynamic)
            );
        }
    };

    private KNNSettings() {
    }

    /**
     * 初始化
     *
     * @param client
     * @param clusterService
     */
    public void initialize(Client client, ClusterService clusterService) {
        this.client = client;
        this.clusterService = clusterService;
        setDynamicCacheSettingsUpdateConsumer();
    }

    /**
     * 获取设置实例
     *
     * @return 设置实例
     */
    public static KNNSettings getInstance() {
        if (instance != null) return instance;
        synchronized (KNNSettings.class) {
            if (instance != null) return instance;
            return instance = new KNNSettings();
        }
    }

    /**
     * 获取所有配置信息
     *
     * @return 配置列表
     */
    public static List<Setting<?>> getSettings() {
        List<Setting<?>> settings =  Arrays.asList(
                KNN_GLOBAL_INDEX_LAZY_LOAD_SETTING,
                KNN_GLOBAL_NMSLIB_INDEX_THREAD_QTY_SETTING,
                KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_TRIGGERED_SETTING,
                KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_UNSET_PERCENTAGE_SETTING,
                KNN_INDEX_HNSW_M_SETTING,
                KNN_INDEX_HNSW_EF_SEARCH_SETTING,
                KNN_INDEX_HNSW_EF_CONSTRUCTION_SETTING,
                KNN_INDEX_FAISS_PQ_M_SETTING,
                KNN_INDEX_FAISS_PQ_ENCODE_BITS_SETTING,
                KNN_INDEX_FAISS_PCA_DIMENSION_SETTING);
        return Stream.concat(settings.stream(), dynamicCacheSettingMap.values().stream())
                .collect(Collectors.toList());
    }

    /**
     * 获取缓存大小熔断限制
     *
     * @return 缓存熔断限制字节数
     */
    public static ByteSizeValue getCircuitBreakerLimit() {
        return getInstance().getGlobalSettingValue(KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_LIMIT);
    }

    /**
     * 是否索引延迟加载
     *
     * @return 索引延迟加载true，否则false
     */
    public static boolean isLazyLoad() {
        return getInstance().getGlobalSettingValue(KNN_GLOBAL_INDEX_LAZY_LOAD);
    }

    /**
     * 获取最大HNSW索引向量数量
     *
     * @param index 索引名
     * @return 最大HNSW索引向量数量
     */
    public static int getMaxHNSWIndexVectorNum(String index) {
        return getInstance().clusterService.state().metadata().index(index).
                getSettings().getAsInt(KNN_INDEX_MAX_HNSW_VECTOR_NUM, MAX_HNSW_INDEX_VECTOR_NUM);
    }

    /**
     * 获取HNSW索引参数M
     *
     * @param index 索引名
     * @return 参数M
     */
    public static int getM(String index) {
        return getInstance().clusterService.state().metadata().index(index).
                getSettings().getAsInt(KNN_INDEX_HNSW_M, DEFAULT_HNSW_M);
    }

    /**
     * 获取HNSW索引参数efSearch
     *
     * @param index 索引名
     * @return 参数efSearch
     */
    public static int getEfSearch(String index) {
        return getInstance().clusterService.state().metadata().index(index).
                getSettings().getAsInt(KNN_INDEX_HNSW_EF_SEARCH, DEFAULT_HNSW_EF_SEARCH);
    }

    /**
     * 获取HNSW索引参数efConstruction
     *
     * @param index 索引名
     * @return 参数efConstruction
     */
    public static int getEfConstruction(String index) {
        return getInstance().clusterService.state().metadata().index(index).
                getSettings().getAsInt(KNN_INDEX_HNSW_EF_CONSTRUCTION, DEFAULT_HNSW_EF_CONSTRUCTION);
    }

    /**
     * 获取FAISS索引PQ参数M
     *
     * @param index 索引名
     * @return PQ参数M
     */
    public static int getProductQuantizationM(String index) {
        return getInstance().clusterService.state().metadata().index(index).
                getSettings().getAsInt(KNN_INDEX_FAISS_PQ_M, DEFAULT_FAISS_PQ_M);
    }

    /**
     * 获取FAISS索引PQ参数encodeBits
     *
     * @param index 索引名
     * @return PQ参数encodeBits
     */
    public static int getProductQuantizationEncodeBits(String index) {
        return getInstance().clusterService.state().metadata().index(index).
                getSettings().getAsInt(KNN_INDEX_FAISS_PQ_ENCODE_BITS, DEFAULT_FAISS_PQ_ENCODE_BITS);
    }

    /**
     * 获取FAISS索引PCA降维维数
     *
     * @param index 索引名
     * @return PCA降维维数
     */
    public static int getPCADimension(String index) {
        return getInstance().clusterService.state().metadata().index(index).
                getSettings().getAsInt(KNN_INDEX_FAISS_PCA_DIMENSION, DEFAULT_FAISS_PCA_DIMENSION);
    }

    /**
     * 获取全局setting值
     *
     * @param key setting key
     * @return 成功返回全局setting值，否则抛出异常
     * @param <T>
     */
    public static <T> T getGlobalSettingValue(String key) {
        return (T) getInstance().clusterService.getClusterSettings().get(getGlobalSetting(key));
    }

    /**
     * 获取全局setting
     *
     * @param key setting key
     * @return 成功返回setting，否则抛出异常
     */
    private static Setting<?> getGlobalSetting(String key) {
        if (dynamicCacheSettingMap.containsKey(key)) {
            return dynamicCacheSettingMap.get(key);
        }
        if (KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_TRIGGERED.equals(key)) {
            return KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_TRIGGERED_SETTING;
        }
        if (KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_UNSET_PERCENTAGE.equals(key)) {
            return KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_UNSET_PERCENTAGE_SETTING;
        }
        if (KNN_GLOBAL_INDEX_LAZY_LOAD.equals(key)) return KNN_GLOBAL_INDEX_LAZY_LOAD_SETTING;
        if (KNN_GLOBAL_NMSLIB_INDEX_THREAD_QTY.equals(key)) return KNN_GLOBAL_NMSLIB_INDEX_THREAD_QTY_SETTING;
        throw new IllegalArgumentException(String.format("setting is not found for key[%s]", key));
    }

    /**
     * 设置缓存动态更新配置consumer
     */
    private void setDynamicCacheSettingsUpdateConsumer() {
        clusterService.getClusterSettings().addSettingsUpdateConsumer(updateCacheSettings -> {
            Boolean cacheExpiredEnable = updateCacheSettings.hasValue(KNN_GLOBAL_CACHE_EXPIRED_ENABLED) ?
                    updateCacheSettings.getAsBoolean(KNN_GLOBAL_CACHE_EXPIRED_ENABLED, null) : null;
            Integer cacheExpiredTimeMinutes = updateCacheSettings.hasValue(KNN_GLOBAL_CACHE_EXPIRED_TIME_MINUTES) ?
                    updateCacheSettings.getAsInt(KNN_GLOBAL_CACHE_EXPIRED_TIME_MINUTES, null) : null;
            Boolean memoryCircuitBreakerEnable = updateCacheSettings.hasValue(KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_ENABLED) ?
                    updateCacheSettings.getAsBoolean(KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_TRIGGERED, null) : null;
            ByteSizeValue memoryCircuitBreakerLimit = updateCacheSettings.hasValue(KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_LIMIT) ?
                    updateCacheSettings.getAsBytesSize(KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_LIMIT, null) : null;
            if (cacheExpiredEnable == null && cacheExpiredTimeMinutes == null &&
                memoryCircuitBreakerEnable == null && memoryCircuitBreakerLimit == null) return;
            KNNIndexCacheConfig config = new KNNIndexCacheConfig();
            config.cacheExpiredEnable = cacheExpiredEnable;
            config.cacheExpiredTimeMinutes = cacheExpiredTimeMinutes;
            config.memoryCircuitBreakerEnable = memoryCircuitBreakerEnable;
            config.memoryCircuitBreakerLimit = memoryCircuitBreakerLimit == null ? null :
                    memoryCircuitBreakerLimit.getBytes();
            KNNIndexCache.getInstance().rebuild(config);
        }, new ArrayList<>(dynamicCacheSettingMap.values()));
    }

    /**
     * 解析最大可用堆外内存大小
     *
     * @param value 配置内存值：百分比或内存大小
     * @param name 最大内存配置名
     * @return 内存字节大小
     */
    private static ByteSizeValue parseMaxUsedNativeMemory(String value, String name) {
        Objects.requireNonNull(name);
        if (value != null && !value.endsWith("%")) return parseBytesSizeValue(value, name);
        try {
            final double percent = Double.parseDouble(value.substring(0, value.length() - 1));
            if (percent < 0 || percent > 100) {
                throw new ElasticsearchParseException("percentage should be in [0-100], unexpected[{}]", percent);
            }
            long physicalMemorySize = osProbe.getTotalPhysicalMemorySize();
            if (physicalMemorySize <= 0) {
                throw new IllegalStateException("can not get physical memory size");
            }
            long maxHeapMemorySize = JvmInfo.jvmInfo().getMem().getHeapMax().getBytes();
            long eligibleMemorySize = physicalMemorySize - maxHeapMemorySize;
            return new ByteSizeValue((long) ((percent / 100) * eligibleMemorySize), ByteSizeUnit.BYTES);
        } catch (NumberFormatException e) {
            throw new ElasticsearchParseException("parse memory percentage[{}] failed", e, value);
        }
    }

    /**
     * 更新熔断状态
     *
     * @param status 开启熔断true，取消熔断false
     */
    public synchronized void updateCircuitBreakerTrigger(boolean status) {
        ClusterUpdateSettingsRequest request = new ClusterUpdateSettingsRequest();
        Settings updateSettings = Settings.builder().put(
                KNNSettings.KNN_GLOBAL_MEMORY_CIRCUIT_BREAKER_TRIGGERED, status).build();
        request.persistentSettings(updateSettings);
        client.admin().cluster().updateSettings(request, new ActionListener<>() {
            @Override
            public void onResponse(ClusterUpdateSettingsResponse response) {
                logger.debug("update circuit breaker trigger settings[{}] success, ack[{}]",
                        request.persistentSettings(), response.isAcknowledged());
            }

            @Override
            public void onFailure(Exception e) {
                logger.error("update circuit breaker trigger setting[{}] failed, cause[{}]",
                        request.persistentSettings(), e.getMessage());
            }
        });
    }
}
