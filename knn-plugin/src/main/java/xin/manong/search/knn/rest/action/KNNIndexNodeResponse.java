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

    public KNNIndexNodeResponse(DiscoveryNode node, Long size) {
        super(node);
        this.size = size == null ? 0L : size;
    }

    public KNNIndexNodeResponse(StreamInput input) throws IOException {
        super(input);
        this.size = input.readLong();
    }

    @Override
    public void writeTo(StreamOutput output) throws IOException {
        super.writeTo(output);
        output.writeLong(size == null ? 0L : size.longValue());
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.field(KNNConstants.REST_RESPONSE_SIZE, size);
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
}
