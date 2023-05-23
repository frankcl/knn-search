package xin.manong.search.knn.rest.action;

import org.elasticsearch.action.support.broadcast.BroadcastRequest;
import org.elasticsearch.common.io.stream.StreamInput;

import java.io.IOException;

/**
 * KNN索引预热请求定义
 *
 * @author frankcl
 * @date 2023-05-22 14:26:12
 */
public class KNNWarmRequest extends BroadcastRequest<KNNWarmRequest> {

    public KNNWarmRequest(StreamInput input) throws IOException {
        super(input);
    }

    public KNNWarmRequest(String... indices) {
        super(indices);
    }
}
