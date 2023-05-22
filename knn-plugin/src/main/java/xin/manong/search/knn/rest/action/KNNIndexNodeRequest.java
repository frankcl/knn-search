package xin.manong.search.knn.rest.action;

import org.elasticsearch.action.support.nodes.BaseNodeRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

/**
 * KNN结点级别索引请求
 *
 * @author frankcl
 * @date 2020-03-30 12:44:00
 */
public class KNNIndexNodeRequest extends BaseNodeRequest {

    private KNNIndexRequest request;

    public KNNIndexNodeRequest() {
        super();
    }

    public KNNIndexNodeRequest(KNNIndexRequest request) {
        this.request = request;
    }

    public KNNIndexNodeRequest(StreamInput input) throws IOException {
        super(input);
        request = new KNNIndexRequest(input);
    }

    @Override
    public void writeTo(StreamOutput output) throws IOException {
        super.writeTo(output);
        request.writeTo(output);
    }

    /**
     * 获取索引请求对象
     *
     * @return 索引请求对象
     */
    public KNNIndexRequest getRequest() {
        return request;
    }
}
