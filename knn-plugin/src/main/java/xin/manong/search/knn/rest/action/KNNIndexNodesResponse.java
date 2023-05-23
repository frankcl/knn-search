package xin.manong.search.knn.rest.action;

import org.elasticsearch.action.FailedNodeException;
import org.elasticsearch.action.support.nodes.BaseNodesResponse;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import xin.manong.search.knn.common.KNNConstants;

import java.io.IOException;
import java.util.List;

/**
 * KNN集群级别索引响应定义
 *
 * @author frankcl
 * @date 2020-03-30 11:54:20
 */
public class KNNIndexNodesResponse extends BaseNodesResponse<KNNIndexNodeResponse> implements ToXContentObject {

    private static final String NODES_KEY = "nodes";

    private Long size;

    public KNNIndexNodesResponse(StreamInput input) throws IOException {
        super(new ClusterName(input), input.readList(in -> new KNNIndexNodeResponse(in)),
                input.readList(FailedNodeException::new));
        size = input.readLong();
    }

    public KNNIndexNodesResponse(ClusterName clusterName,
                                 List<KNNIndexNodeResponse> nodeResponses,
                                 List<FailedNodeException> exceptions,
                                 Long size) {
        super(clusterName, nodeResponses, exceptions);
        this.size = size == null ? 0L : size;
    }

    @Override
    public void writeTo(StreamOutput output) throws IOException {
        super.writeTo(output);
        output.writeLong(size);
    }

    @Override
    public void writeNodesTo(StreamOutput output, List<KNNIndexNodeResponse> nodeResponses) throws IOException {
        output.writeList(nodeResponses);
    }

    @Override
    public List<KNNIndexNodeResponse> readNodesFrom(StreamInput input) throws IOException {
        return input.readList(in -> new KNNIndexNodeResponse(in));
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder xContentBuilder, Params params) throws IOException {
        xContentBuilder.field(KNNConstants.REST_RESPONSE_SIZE, size);
        xContentBuilder.startObject(NODES_KEY);
        for (KNNIndexNodeResponse nodeResponse : getNodes()) {
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
