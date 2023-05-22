package xin.manong.search.knn.rest.action;

import org.elasticsearch.action.support.nodes.BaseNodeRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

/**
 * KNN节点级别统计请求定义
 *
 * @author frankcl
 * @date 2023-05-22 14:37:21
 */
public class KNNStatsNodeRequest extends BaseNodeRequest {

    private KNNStatsRequest request;

    public KNNStatsNodeRequest() {
        super();
    }

    public KNNStatsNodeRequest(KNNStatsRequest request) {
        this.request = request;
    }

    public KNNStatsNodeRequest(StreamInput input) throws IOException {
        super(input);
        request = new KNNStatsRequest(input);
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
