package xin.manong.search.knn.rest.action;

import org.elasticsearch.action.FailedNodeException;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.nodes.TransportNodesAction;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import xin.manong.search.knn.stat.KNNStats;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *  KNN统计TransportNodesAction定义
 */
public class KNNStatsTransportAction extends TransportNodesAction<
        KNNStatsNodesRequest, KNNStatsNodesResponse,
        KNNStatsNodeRequest, KNNStatsNodeResponse> {

    private KNNStats knnStats;

    @Inject
    public KNNStatsTransportAction(
            ThreadPool threadPool,
            ClusterService clusterService,
            TransportService transportService,
            ActionFilters actionFilters,
            KNNStats knnStats
            ) {
        super(KNNStatsAction.NAME, threadPool, clusterService, transportService, actionFilters,
                KNNStatsNodesRequest::new, KNNStatsNodeRequest::new, ThreadPool.Names.MANAGEMENT,
                KNNStatsNodeResponse.class);
        this.knnStats = knnStats;
    }

    @Override
    protected KNNStatsNodesResponse newResponse(KNNStatsNodesRequest request,
                                                List<KNNStatsNodeResponse> responses,
                                                List<FailedNodeException> exceptions) {
        Map<String, Object> clusterStatMap = new HashMap<>();
        Set<String> requestStatKeys = request.getRequest().getRequestStatKeys();
        for (String statKey : knnStats.getClusterStatMap().keySet()) {
            if (requestStatKeys.contains(statKey)) {
                clusterStatMap.put(statKey, knnStats.getStat(statKey).getValue());
            }
        }
        return new KNNStatsNodesResponse(clusterService.getClusterName(),
                responses, exceptions, clusterStatMap);
    }

    @Override
    protected KNNStatsNodeRequest newNodeRequest(KNNStatsNodesRequest request) {
        return new KNNStatsNodeRequest(request.getRequest());
    }

    @Override
    protected KNNStatsNodeResponse newNodeResponse(StreamInput input) throws IOException {
        return new KNNStatsNodeResponse(input);
    }

    @Override
    protected KNNStatsNodeResponse nodeOperation(KNNStatsNodeRequest request) {
        return buildNodeResponse(request.getRequest());
    }

    /**
     * 构建结点级别统计响应
     *
     * @param request 统计请求
     * @return 结点级别统计响应
     */
    private KNNStatsNodeResponse buildNodeResponse(KNNStatsRequest request) {
        Map<String, Object> statMap = new HashMap<>();
        Set<String> requestStatKeys = request.getRequestStatKeys();
        for (String statKey : knnStats.getNodeStatMap().keySet()) {
            if (!requestStatKeys.contains(statKey)) continue;
            statMap.put(statKey, knnStats.getStat(statKey).getValue());
        }
        return new KNNStatsNodeResponse(clusterService.localNode(), statMap);
    }
}
