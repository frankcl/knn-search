package xin.manong.search.knn.rest.action;

import org.elasticsearch.action.support.DefaultShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.BroadcastResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import xin.manong.search.knn.common.KNNConstants;

import java.io.IOException;
import java.util.List;

/**
 * KNN索引预热响应定义
 *
 * @author frankcl
 * @date 2023-05-22 14:26:12
 */
public class KNNWarmResponse extends BroadcastResponse implements ToXContentObject {

    private Long size;

    public KNNWarmResponse() {
        size = 0L;
    }

    public KNNWarmResponse(StreamInput input) throws IOException {
        super(input);
        size = input.readLong();
    }

    public KNNWarmResponse(int totalShards, int successShards, int failShards,
                           List<DefaultShardOperationFailedException> failedExceptions,
                           long size) {
        super(totalShards, successShards, failShards, failedExceptions);
        this.size = size;
    }

    @Override
    public void writeTo(StreamOutput output) throws IOException {
        super.writeTo(output);
        output.writeLong(size);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder xContentBuilder, Params params) throws IOException {
        xContentBuilder.field(KNNConstants.REST_RESPONSE_SIZE, size);
        return xContentBuilder;
    }
}
