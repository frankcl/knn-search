package xin.manong.search.knn.breaker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.threadpool.ThreadPool;
import xin.manong.search.knn.cache.KNNIndexCache;
import xin.manong.search.knn.common.KNNSettings;

/**
 * KNN熔断监控
 *
 * @author frankcl
 * @date 2023-05-19 16:48:02
 */
public class KNNCircuitBreakerMonitor implements Runnable {

    private static final Logger logger = LogManager.getLogger(KNNCircuitBreakerMonitor.class);

    private static final Integer CHECK_TIME_INTERVAL_SECONDS = 120;

    private ThreadPool threadPool;
    private ClusterService clusterService;
    private Client client;

    public KNNCircuitBreakerMonitor(ThreadPool threadPool,
                                     ClusterService clusterService,
                                     Client client) {
        this.threadPool = threadPool;
        this.clusterService = clusterService;
        this.client = client;
    }

    /**
     * 启动熔断监控
     */
    public void start() {
        threadPool.scheduleWithFixedDelay(this,
                TimeValue.timeValueSeconds(CHECK_TIME_INTERVAL_SECONDS),
                ThreadPool.Names.GENERIC);
    }

    @Override
    public void run() {
        if (clusterService.localNode().isDataNode() && KNNIndexCache.getInstance().isCacheCapacityReached()) {
            KNNIndexCache knnIndexCache = KNNIndexCache.getInstance();
            Long currentMemorySize =  knnIndexCache.getCacheMemorySizeKB();
            Long circuitBreakerMemoryLimit = KNNSettings.getCircuitBreakerLimit().getKb();
            Long circuitBreakerUnsetMemory = (long) ((KNNSettings.getCircuitBreakerUnsetPercentage() / 100) *
                    circuitBreakerMemoryLimit);
            if (currentMemorySize <= circuitBreakerUnsetMemory) knnIndexCache.setCacheCapacityReached(false);
        }
        if (clusterService.state().nodes().isLocalNodeElectedMaster() && KNNSettings.isCircuitBreakerTriggered()) {
        }
    }
}
