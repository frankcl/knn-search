package xin.manong.search.knn.rest.action;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * KNN统计项请求
 *
 * @author frankcl
 * @date 2023-05-22 14:26:12
 */
public class KNNStatsRequest {

    public static final String ALL_STATS = "_all_";

    private Set<String> statKeys;
    private Set<String> requestStatKeys;

    public KNNStatsRequest(Set<String> statKeys) {
        this.statKeys = statKeys;
        this.requestStatKeys = new HashSet<>();
    }

    public KNNStatsRequest(StreamInput input) throws IOException {
        readFrom(input);
    }

    /**
     * 添加所有统计项
     */
    public void addAllRequestStats() {
        requestStatKeys.addAll(statKeys);
    }

    /**
     * 清除请求统计项
     */
    public void clearRequestStats() {
        requestStatKeys.clear();
    }

    /**
     * 添加统计项
     *
     * @param statKey 统计项
     * @return 添加成功返回true，否则返回false
     */
    public boolean addRequestStat(String statKey) {
        if (statKeys.contains(statKey)) {
            requestStatKeys.add(statKey);
            return true;
        }
        return false;
    }

    /**
     * 反序列化统计项
     *
     * @param input 输入流
     * @throws IOException
     */
    public void readFrom(StreamInput input) throws IOException {
        statKeys = input.readSet(StreamInput::readString);
        requestStatKeys = input.readSet(StreamInput::readString);
    }

    /**
     * 序列化统计项
     *
     * @param output 输出流
     * @throws IOException
     */
    public void writeTo(StreamOutput output) throws IOException {
        output.writeStringCollection(statKeys);
        output.writeStringCollection(requestStatKeys);
    }

    /**
     * 获取请求统计项
     *
     * @return 请求统计项
     */
    public Set<String> getRequestStatKeys() {
        return requestStatKeys;
    }
}
