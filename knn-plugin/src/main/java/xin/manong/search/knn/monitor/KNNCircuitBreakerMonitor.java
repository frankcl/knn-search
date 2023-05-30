package xin.manong.search.knn.monitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.threadpool.ThreadPool;
import xin.manong.search.knn.cache.KNNIndexCache;
import xin.manong.search.knn.common.KNNConstants;
import xin.manong.search.knn.common.KNNSettings;
import xin.manong.search.knn.rest.action.*;
import xin.manong.search.knn.stat.KNNStatsConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
        logger.info("KNN circuit breaker monitor has been started");
    }

    @Override
    public void run() {
        if (clusterService.localNode().isDataNode() &&
                KNNIndexCache.getInstance().isCacheCapacityReached()) {
            KNNIndexCache knnIndexCache = KNNIndexCache.getInstance();
            Long currentMemorySize = knnIndexCache.getCacheMemorySizeKB();
            Long circuitBreakerMemoryLimit = KNNSettings.getCircuitBreakerLimit().getKb();
            Long circuitBreakerUnsetMemory = (long) (circuitBreakerMemoryLimit *
                    KNNSettings.getCircuitBreakerUnsetPercentage() / 100);
            if (currentMemorySize <= circuitBreakerUnsetMemory) {
                knnIndexCache.setCacheCapacityReached(false);
                logger.info("KNN index cache circuit breaker has been recovered");
            }
        }
        if (clusterService.state().nodes().isLocalNodeElectedMaster() &&
                KNNSettings.isCircuitBreakerTriggered()) {
            KNNStatsRequest knnStatsRequest = new KNNStatsRequest(KNNStatsConfig.KNN_STATS.keySet());
            knnStatsRequest.addRequestStat(KNNConstants.CACHE_CAPACITY_REACHED);
            KNNStatsNodesRequest request = new KNNStatsNodesRequest(knnStatsRequest);
            request.timeout(new TimeValue(10, TimeUnit.SECONDS));
            try {
                KNNStatsNodesResponse response = client.execute(KNNStatsAction.INSTANCE, request).get();
                List<KNNStatsNodeResponse> nodeResponses = response.getNodes();
                List<String> reachedCapacityNodes = new ArrayList<>();
                for (KNNStatsNodeResponse nodeResponse : nodeResponses) {
                    Map<String, Object> nodeStatMap = nodeResponse.getNodeStatMap();
                    if ((Boolean) nodeStatMap.get(KNNConstants.CACHE_CAPACITY_REACHED)) {
                        reachedCapacityNodes.add(nodeResponse.getNode().getId());
                    }
                }
                if (!reachedCapacityNodes.isEmpty()) {
                    logger.info("KNN memory circuit breaker is triggered on nodes[{}]",
                            String.join(",", reachedCapacityNodes));
                } else {
                    logger.info("KNN cluster memory circuit breaker is unset");
                    KNNSettings.getInstance().updateCircuitBreakerTrigger(false);
                }
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }
    }
}
