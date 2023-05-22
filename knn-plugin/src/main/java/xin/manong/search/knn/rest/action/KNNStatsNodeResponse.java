package xin.manong.search.knn.rest.action;

import org.elasticsearch.action.support.nodes.BaseNodeResponse;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Map;

/**
 * KNN结点级别统计响应定义
 *
 * @author frankcl
 * @date 2023-05-22 14:56:30
 */
public class KNNStatsNodeResponse extends BaseNodeResponse implements ToXContentFragment {

    private Map<String, Object> nodeStatMap;

    public KNNStatsNodeResponse(StreamInput input) throws IOException {
        super(input);
        this.nodeStatMap = input.readMap(StreamInput::readString, StreamInput::readGenericValue);
    }

    protected KNNStatsNodeResponse(DiscoveryNode node, Map<String, Object> statMap) {
        super(node);
        this.nodeStatMap = statMap;
    }

    @Override
    public void writeTo(StreamOutput output) throws IOException {
        super.writeTo(output);
        output.writeMap(nodeStatMap, StreamOutput::writeString, StreamOutput::writeGenericValue);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder xContentBuilder, Params params) throws IOException {
        for (String statKey : nodeStatMap.keySet()) {
            xContentBuilder.field(statKey, nodeStatMap.get(statKey));
        }
        return xContentBuilder;
    }

    /**
     * 获取结点级别统计结果
     *
     * @return 统计结果
     */
    public Map<String, Object> getNodeStatMap() {
        return nodeStatMap;
    }
}
