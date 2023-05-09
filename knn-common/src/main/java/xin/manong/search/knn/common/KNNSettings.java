package xin.manong.search.knn.common;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * knn索引设置
 *
 * @author frankcl
 * @date 2023-01-19 11:38:03
 */
public class KNNSettings {

    public static final String KNN_MEMORY_CIRCUIT_BREAKER_ENABLED = "knn.memory.circuit_breaker.enabled";
    public static final String KNN_MEMORY_CIRCUIT_BREAKER_LIMIT = "knn.memory.circuit_breaker.limit";
    public static final String KNN_CACHE_EXPIRED_ENABLED = "knn.cache.expired.enabled";
    public static final String KNN_CACHE_EXPIRED_TIME_MINUTES = "knn.cache.expired.minutes";

    private static KNNSettings instance;
    private final Map<String, Object> latestSettings;

    private KNNSettings() {
        latestSettings = new ConcurrentHashMap<>();
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
            instance = new KNNSettings();
            return instance;
        }
    }

    public static ByteSizeValue getCircuitBreakerLimit() {
        return KNNSettings.state().getSettingValue(KNNSettings.KNN_MEMORY_CIRCUIT_BREAKER_LIMIT);
    }

    public <T> T getSettingValue(String key) {
        return (T) latestSettings.getOrDefault(key, getSetting(key).getDefault(Settings.EMPTY));
    }

    public Setting<?> getSetting(String key) {
        return null;
    }
}
