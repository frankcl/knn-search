package xin.manong.search.knn.common;
import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.client.internal.Client;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.monitor.jvm.JvmInfo;
import org.elasticsearch.monitor.os.OsProbe;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.elasticsearch.common.settings.Setting.Property.Dynamic;
import static org.elasticsearch.common.settings.Setting.Property.NodeScope;
import static org.elasticsearch.common.unit.ByteSizeValue.parseBytesSizeValue;

/**
 * knn索引设置
 *
 * @author frankcl
 * @date 2023-01-19 11:38:03
 */
public class KNNSettings {

    private static final int DEFAULT_MAX_CACHE_EXPIRED_TIME_MINUTES = 180;
    private static final int DEFAULT_MEMORY_CIRCUIT_BREAKER_UNSET_PERCENTAGE = 85;
    private static final int MAX_NMSLIB_THREAD_QTY = 32;
    private static final int MAX_HNSW_INDEX_VECTOR_NUM = 500000;
    private static final String DEFAULT_MAX_MEMORY_CIRCUIT_BREAKER_LIMIT = "90%";

    public static final String KNN_MEMORY_CIRCUIT_BREAKER_ENABLED = "knn.memory.circuit_breaker.enabled";
    public static final String KNN_MEMORY_CIRCUIT_BREAKER_LIMIT = "knn.memory.circuit_breaker.limit";
    public static final String KNN_MEMORY_CIRCUIT_BREAKER_TRIGGERED = "knn.memory.circuit_breaker.triggered";
    public static final String KNN_MEMORY_CIRCUIT_BREAKER_UNSET_PERCENTAGE = "knn.memory.circuit_breaker.unset_percentage";
    public static final String KNN_CACHE_EXPIRED_ENABLED = "knn.cache.expired_enabled";
    public static final String KNN_CACHE_EXPIRED_TIME_MINUTES = "knn.cache.expired_minutes";
    public static final String KNN_NMSLIB_PARAM_INDEX_THREAD_QTY = "knn.nmslib.param.index_thread_qty";

    public static final String INDEX_MAX_HNSW_VECTOR_NUM = "index.max_hnsw_vector_num";

    private Client client;
    private ClusterService clusterService;

    public static final Setting<Boolean> KNN_MEMORY_CIRCUIT_BREAKER_TRIGGERED_SETTING = Setting.boolSetting(
            KNN_MEMORY_CIRCUIT_BREAKER_TRIGGERED, false, NodeScope, Dynamic);
    public static final Setting<Double> KNN_MEMORY_CIRCUIT_BREAKER_UNSET_PERCENTAGE_SETTING =  Setting.doubleSetting(
            KNN_MEMORY_CIRCUIT_BREAKER_UNSET_PERCENTAGE, DEFAULT_MEMORY_CIRCUIT_BREAKER_UNSET_PERCENTAGE,
            0, 100, NodeScope, Dynamic);
    public static final Setting<Integer> KNN_NMSLIB_PARAM_INDEX_THREAD_QTY_SETTING = Setting.intSetting(
            KNN_NMSLIB_PARAM_INDEX_THREAD_QTY, Math.min(1, Math.max(Runtime.getRuntime().availableProcessors() / 2, 1)),
            1, MAX_NMSLIB_THREAD_QTY, NodeScope, Dynamic);

    private static KNNSettings instance;
    private static OsProbe osProbe = OsProbe.getInstance();
    private static Map<String, Setting> dynamicCacheSettingMap = new HashMap<>() {
        {
            put(KNN_MEMORY_CIRCUIT_BREAKER_ENABLED, Setting.boolSetting(
                    KNN_MEMORY_CIRCUIT_BREAKER_ENABLED, true, NodeScope, Dynamic));
            put(KNN_MEMORY_CIRCUIT_BREAKER_LIMIT, new Setting<>(
                    KNNSettings.KNN_MEMORY_CIRCUIT_BREAKER_LIMIT,
                    DEFAULT_MAX_MEMORY_CIRCUIT_BREAKER_LIMIT,
                    s -> parseMaxUsedNativeMemory(s, KNNSettings.KNN_MEMORY_CIRCUIT_BREAKER_LIMIT),
                    NodeScope,
                    Dynamic)
            );
            put(KNN_CACHE_EXPIRED_ENABLED, Setting.boolSetting(
                    KNN_CACHE_EXPIRED_ENABLED, false, NodeScope, Dynamic));
            put(KNN_CACHE_EXPIRED_TIME_MINUTES, Setting.positiveTimeSetting(KNN_CACHE_EXPIRED_TIME_MINUTES,
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
    }

    /**
     * 获取设置实例
     *
     * @return 设置实例
     */
    public static KNNSettings state() {
        if (instance != null) return instance;
        synchronized (KNNSettings.class) {
            if (instance != null) return instance;
            return instance = new KNNSettings();
        }
    }

    public static ByteSizeValue getCircuitBreakerLimit() {
        return KNNSettings.state().getSettingValue(KNNSettings.KNN_MEMORY_CIRCUIT_BREAKER_LIMIT);
    }

    /**
     * 获取最大HNSW索引向量数量
     *
     * @param index 索引名
     * @return 最大HNSW索引向量数量
     */
    public static int getMaxHNSWIndexVectorNum(String index) {
        return KNNSettings.state().clusterService.state().metadata().index(index).
                getSettings().getAsInt(INDEX_MAX_HNSW_VECTOR_NUM, MAX_HNSW_INDEX_VECTOR_NUM);
    }

    /**
     * 获取可读setting值
     *
     * @param key setting key
     * @return 成功返回可读setting值，否则抛出异常
     * @param <T>
     */
    public <T> T getSettingValue(String key) {
        return (T) clusterService.getClusterSettings().get(getSetting(key));
    }

    /**
     * 可读setting获取
     *
     * @param key setting key
     * @return 成功返回setting，否则抛出异常
     */
    private Setting<?> getSetting(String key) {
        if (dynamicCacheSettingMap.containsKey(key)) {
            return dynamicCacheSettingMap.get(key);
        }
        if (KNN_MEMORY_CIRCUIT_BREAKER_TRIGGERED.equals(key)) {
            return KNN_MEMORY_CIRCUIT_BREAKER_TRIGGERED_SETTING;
        }
        if (KNN_MEMORY_CIRCUIT_BREAKER_UNSET_PERCENTAGE.equals(key)) {
            return KNN_MEMORY_CIRCUIT_BREAKER_UNSET_PERCENTAGE_SETTING;
        }
        if (KNN_NMSLIB_PARAM_INDEX_THREAD_QTY.equals(key)) {
            return KNN_NMSLIB_PARAM_INDEX_THREAD_QTY_SETTING;
        }
        throw new IllegalArgumentException(String.format("setting is not found for key[%s]", key));
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
}
