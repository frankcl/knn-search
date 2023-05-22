package xin.manong.search.knn.rest.action;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

/**
 * KNN索引请求定义
 *
 * @author frankcl
 * @date 2020-03-30 11:49:29
 */
public class KNNIndexRequest {

    private String index;
    private String operation;

    public KNNIndexRequest(String index, String operation) {
        this.index = index;
        this.operation = operation;
    }

    public KNNIndexRequest(StreamInput input) throws IOException {
        readFrom(input);
    }

    /**
     * 反序列化索引请求
     *
     * @param input 输入流
     * @throws IOException
     */
    public void readFrom(StreamInput input) throws IOException {
        index = input.readString();
        operation = input.readString();
    }

    /**
     * 序列化索引请求
     *
     * @param output 输出流
     * @throws IOException
     */
    public void writeTo(StreamOutput output) throws IOException {
        output.writeString(index);
        output.writeString(operation);
    }

    /**
     * 获取索引名
     *
     * @return 索引名
     */
    public String getIndex() {
        return index;
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
