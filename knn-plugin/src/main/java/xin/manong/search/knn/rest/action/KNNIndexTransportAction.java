package xin.manong.search.knn.rest.action;

import org.elasticsearch.action.FailedNodeException;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.nodes.TransportNodesAction;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import xin.manong.search.knn.cache.KNNIndexCache;
import xin.manong.search.knn.common.KNNConstants;

import java.io.IOException;
import java.util.List;

/**
 * KNN索引TransportNodesAction实现
 *
 * @author frankcl
 * @date 2020-03-31 14:06:11
 */
public class KNNIndexTransportAction extends
        TransportNodesAction<KNNIndexNodesRequest, KNNIndexNodesResponse,
        KNNIndexNodeRequest, KNNIndexNodeResponse> {

    @Inject
    public KNNIndexTransportAction(ThreadPool threadPool,
                                   ClusterService clusterService,
                                   TransportService transportService,
                                   ActionFilters actionFilters) {
        super(KNNIndexAction.NAME, threadPool, clusterService, transportService, actionFilters,
                KNNIndexNodesRequest::new, KNNIndexNodeRequest::new, ThreadPool.Names.MANAGEMENT,
                KNNIndexNodeResponse.class);
    }

    @Override
    protected KNNIndexNodesResponse newResponse(KNNIndexNodesRequest request,
                                                List<KNNIndexNodeResponse> responses,
                                                List<FailedNodeException> exceptions) {
        Long size = 0L;
        for (KNNIndexNodeResponse nodeResponse : responses) size += nodeResponse.getSize();
        return new KNNIndexNodesResponse(clusterService.getClusterName(), responses, exceptions, size);
    }

    @Override
    protected KNNIndexNodeRequest newNodeRequest(KNNIndexNodesRequest request) {
        return new KNNIndexNodeRequest(request.getRequest());
    }

    @Override
    protected KNNIndexNodeResponse newNodeResponse(StreamInput input) throws IOException {
        return new KNNIndexNodeResponse(input);
    }

    @Override
    protected KNNIndexNodeResponse nodeOperation(KNNIndexNodeRequest request) {
        return buildNodeResponse(request.getRequest());
    }

    /**
     * 构建结点级别索引响应
     *
     * @param request 索引请求
     * @return 结点级别索引响应
     */
    private KNNIndexNodeResponse buildNodeResponse(KNNIndexRequest request) {
        Long size = 0L;
        String operation = request.getOperation();
        if (operation.equals(KNNConstants.OPERATION_VIEW)) {
            size = KNNIndexCache.getInstance().getCacheMemorySizeKB(request.getIndex());
        } else if (operation.equals(KNNConstants.OPERATION_EVICT)) {
            size = KNNIndexCache.getInstance().removeByIndex(request.getIndex());
        } else {
            logger.warn("unsupported operation[{}]", operation);
        }
        return new KNNIndexNodeResponse(clusterService.localNode(), size);
    }
}
