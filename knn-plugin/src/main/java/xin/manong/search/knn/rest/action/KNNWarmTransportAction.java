package xin.manong.search.knn.rest.action;

import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.DefaultShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.node.TransportBroadcastByNodeAction;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.routing.ShardsIterator;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import xin.manong.search.knn.shard.KNNIndexShard;

import java.io.IOException;
import java.util.List;

/**
 * KNN索引预热Broadcast TransportAction实现
 *
 * @author frankcl
 * @date 2023-05-22 14:26:12
 */
public class KNNWarmTransportAction extends TransportBroadcastByNodeAction<KNNWarmRequest, KNNWarmResponse,
        KNNWarmShardResponse> {

    private IndicesService indicesService;

    @Inject
    public KNNWarmTransportAction(ClusterService clusterService, TransportService transportService, IndicesService indicesService,
                                  ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver) {
        super(KNNWarmAction.NAME, clusterService, transportService, actionFilters,
                indexNameExpressionResolver, KNNWarmRequest::new, ThreadPool.Names.SEARCH);
        this.indicesService = indicesService;
    }

    @Override
    protected KNNWarmShardResponse readShardResult(StreamInput input) throws IOException {
        return new KNNWarmShardResponse(input);
    }

    @Override
    protected KNNWarmResponse newResponse(KNNWarmRequest request,
                                          int totalShards,
                                          int successShards,
                                          int failShards,
                                          List<KNNWarmShardResponse> shardResponses,
                                          List<DefaultShardOperationFailedException> failedExceptions,
                                          ClusterState clusterState) {
        Long size = 0L;
        for (KNNWarmShardResponse shardResponse : shardResponses) size += shardResponse.getSize();
        return new KNNWarmResponse(totalShards, successShards, failShards, failedExceptions, size);
    }

    @Override
    protected KNNWarmRequest readRequestFrom(StreamInput input) throws IOException {
        return new KNNWarmRequest(input);
    }

    @Override
    protected KNNWarmShardResponse shardOperation(KNNWarmRequest request, ShardRouting shardRouting) throws IOException {
        KNNIndexShard knnIndexShard = new KNNIndexShard(indicesService.indexServiceSafe(
                shardRouting.shardId().getIndex()).getShard(shardRouting.shardId().id()));
        Long size = knnIndexShard.warm();
        return new KNNWarmShardResponse(size);
    }

    @Override
    protected ShardsIterator shards(ClusterState state, KNNWarmRequest request, String[] concreteIndices) {
        return state.routingTable().allShards(concreteIndices);
    }

    @Override
    protected ClusterBlockException checkGlobalBlock(ClusterState state, KNNWarmRequest request) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA_READ);
    }

    @Override
    protected ClusterBlockException checkRequestBlock(ClusterState state, KNNWarmRequest request, String[] concreteIndices) {
        return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA_READ, concreteIndices);
    }
}
