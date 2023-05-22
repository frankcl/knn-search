package xin.manong.search.knn.rest.action;

import org.elasticsearch.action.FailedNodeException;
import org.elasticsearch.action.support.nodes.BaseNodesResponse;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * KNN集群级别统计响应定义
 *
 * @author frankcl
 * @date 2023-05-22 15:20:33
 */
public class KNNStatsNodesResponse extends BaseNodesResponse<KNNStatsNodeResponse> implements ToXContentObject {

    private static final String NODES_KEY = "nodes";

    private Map<String, Object> clusterStatMap;

    protected KNNStatsNodesResponse(StreamInput input) throws IOException {
        super(new ClusterName(input), input.readList(in -> new KNNStatsNodeResponse(in)),
                input.readList(FailedNodeException::new));
        clusterStatMap = input.readMap();
    }

    public KNNStatsNodesResponse(ClusterName clusterName, List<KNNStatsNodeResponse> nodeResponses,
                                 List<FailedNodeException> exceptions, Map<String, Object> statMap) {
        super(clusterName, nodeResponses, exceptions);
        this.clusterStatMap = statMap;
    }

    @Override
    protected List<KNNStatsNodeResponse> readNodesFrom(StreamInput input) throws IOException {
        return input.readList(in -> new KNNStatsNodeResponse(in));
    }

    @Override
    protected void writeNodesTo(StreamOutput output, List<KNNStatsNodeResponse> nodeResponses)
            throws IOException {
        output.writeList(nodeResponses);
    }

    @Override
    public void writeTo(StreamOutput output) throws IOException {
        super.writeTo(output);
        output.writeMap(clusterStatMap);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder xContentBuilder, Params params) throws IOException {
        for (Map.Entry<String, Object> entry : clusterStatMap.entrySet()) {
            xContentBuilder.field(entry.getKey(), entry.getValue());
        }
        xContentBuilder.startObject(NODES_KEY);
        for (KNNStatsNodeResponse nodeResponse : getNodes()) {
            DiscoveryNode node = nodeResponse.getNode();
            String nodeId = node.getId();
            xContentBuilder.startObject(nodeId);
            nodeResponse.toXContent(xContentBuilder, params);
            xContentBuilder.endObject();
        }
        xContentBuilder.endObject();
        return xContentBuilder;
    }
}
