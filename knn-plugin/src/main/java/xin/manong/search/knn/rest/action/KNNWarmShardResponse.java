package xin.manong.search.knn.rest.action;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;

import java.io.IOException;

/**
 * KNN预热shard级别响应定义
 *
 * @author frankcl
 * @date 2023-05-23 10:57:00
 */
public class KNNWarmShardResponse implements Writeable {

    private Long size;

    public KNNWarmShardResponse(Long size) {
        this.size = size == null ? 0L : size;
    }

    public KNNWarmShardResponse(StreamInput input) throws IOException {
        size = input.readLong();
    }

    @Override
    public void writeTo(StreamOutput output) throws IOException {
        output.writeLong(size == null ? 0L : size);
    }

    /**
     * 获取shard级别内存大小
     *
     * @return shard级别内存大小
     */
    public Long getSize() {
        return size;
    }
}
