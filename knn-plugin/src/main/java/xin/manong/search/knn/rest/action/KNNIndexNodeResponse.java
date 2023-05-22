package xin.manong.search.knn.rest.action;

import org.elasticsearch.action.support.nodes.BaseNodeResponse;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;
import xin.manong.search.knn.common.KNNConstants;

import java.io.IOException;

/**
 * KNN结点级别索引响应定义
 *
 * @author frankcl
 * @date 2020-03-30 12:44:12
 */
public class KNNIndexNodeResponse extends BaseNodeResponse implements ToXContentFragment {

    private Long size;
    private String operation;

    public KNNIndexNodeResponse(DiscoveryNode node, Long size, String operation) {
        super(node);
        this.size = size;
        this.operation = operation;
    }

    public KNNIndexNodeResponse(StreamInput input) throws IOException {
        super(input);
        this.operation = input.readString();
        this.size = input.readLong();
    }

    @Override
    public void writeTo(StreamOutput output) throws IOException {
        super.writeTo(output);
        output.writeString(operation);
        output.writeLong(size == null ? 0L : size.longValue());
    }

    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.field(KNNConstants.REST_REQUEST_OPERATION, operation);
        builder.field(KNNConstants.REST_REQUEST_SIZE, size);
        return builder;
    }

    /**
     * 获取结点索引内存大小
     *
     * @return 结点索引内存大小
     */
    public Long getSize() {
        return size;
    }

    /**
     * 获取索引操作
     *
     * @return 索引操作
     */
    public String getOperation() {
        return operation;
    }
}
