package xin.manong.search.knn.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * KNN索引缓存配置
 *
 * @author frankcl
 * @date 2023-05-11 11:43:18
 */
public class KNNIndexCacheConfig {

    private static final Logger logger = LogManager.getLogger(KNNIndexCacheConfig.class);

    public Boolean cacheExpiredEnable;
    public Boolean memoryCircuitBreakerEnable;
    public Integer cacheExpiredTimeMinutes;
    public Long memoryCircuitBreakerLimit;

    /**
     * 检测配置有效性，无效配置抛出异常
     */
    public void check() {
        if (cacheExpiredEnable == null) {
            logger.error("cacheExpiredEnable is not config");
            throw new RuntimeException("cacheExpiredEnable is not config");
        }
        if (memoryCircuitBreakerEnable == null) {
            logger.error("memoryCircuitBreakerEnable is not config");
            throw new RuntimeException("memoryCircuitBreakerEnable is not config");
        }
        if (cacheExpiredEnable && (cacheExpiredTimeMinutes == null || cacheExpiredTimeMinutes <= 0)) {
            logger.error("cacheExpiredTimeMinutes must be config");
            throw new RuntimeException("cacheExpiredTimeMinutes must be config");
        }
        if (memoryCircuitBreakerEnable && (memoryCircuitBreakerLimit == null || memoryCircuitBreakerLimit <= 0)) {
            logger.error("memoryCircuitBreakerLimit must be config");
            throw new RuntimeException("memoryCircuitBreakerLimit must be config");
        }
    }
}
