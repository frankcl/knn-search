package xin.manong.search.knn.rest.action;

import org.elasticsearch.action.support.nodes.BaseNodesRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

/**
 * KNN集群级别统计请求定义
 *
 * @author frankcl
 * @date 2023-05-22 14:45:53
 */
public class KNNStatsNodesRequest extends BaseNodesRequest<KNNStatsNodesRequest> {

    private KNNStatsRequest request;

    public KNNStatsNodesRequest(StreamInput input) throws IOException {
        super(input);
        request = new KNNStatsRequest(input);
    }

    public KNNStatsNodesRequest(KNNStatsRequest request, String... nodesIds) {
        super(nodesIds);
        this.request = request;
    }

    @Override
    public void writeTo(StreamOutput output) throws IOException {
        super.writeTo(output);
        request.writeTo(output);
    }

    /**
     * 获取统计请求对象
     *
     * @return 统计请求对象
     */
    public KNNStatsRequest getRequest() {
        return request;
    }
}
