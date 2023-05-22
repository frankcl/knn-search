package xin.manong.search.knn.rest.handler;

import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestActions;
import xin.manong.search.knn.common.KNNConstants;
import xin.manong.search.knn.rest.action.KNNStatsAction;
import xin.manong.search.knn.rest.action.KNNStatsNodesRequest;
import xin.manong.search.knn.rest.action.KNNStatsRequest;
import xin.manong.search.knn.stat.KNNStats;

import java.util.*;

/**
 * KNN统计RESTFul处理器
 *
 * @author frankcl
 * @date 2023-05-22 16:03:58
 */
public class KNNStatsRESTHandler extends BaseRestHandler {

    private static final Logger logger = LogManager.getLogger(KNNStatsRESTHandler.class);

    private KNNStats knnStats;

    public KNNStatsRESTHandler(KNNStats knnStats) {
        this.knnStats = knnStats;
    }

    @Override
    public String getName() {
        return KNNConstants.REST_ACTION_STATS;
    }

    @Override
    public List<Route> routes() {
        return ImmutableList.of(
                new Route(RestRequest.Method.GET, String.format("%s/stats/{%s}",
                        KNNConstants.KNN_BASE_URL, KNNConstants.REST_REQUEST_NODE_ID)),
                new Route(RestRequest.Method.GET, String.format("%s/stats/{%s}/{%s}",
                        KNNConstants.KNN_BASE_URL, KNNConstants.REST_REQUEST_NODE_ID, KNNConstants.REST_REQUEST_STAT)),
                new Route(RestRequest.Method.GET, String.format("%s/stats",
                        KNNConstants.KNN_BASE_URL)),
                new Route(RestRequest.Method.GET, String.format("%s/stats/{%s}",
                        KNNConstants.KNN_BASE_URL, KNNConstants.REST_REQUEST_STAT))
        );
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) {
        KNNStatsNodesRequest nodesRequest = buildRequest(request);
        return channel -> client.execute(KNNStatsAction.INSTANCE, nodesRequest,
                new RestActions.NodesResponseRestListener<>(channel));
    }

    /**
     * 构建KNN统计请求
     *
     * @param request RESTFul请求
     * @return KNN统计请求
     */
    private KNNStatsNodesRequest buildRequest(RestRequest request) {
        String[] nodeIds = parseStrings(request.param(KNNConstants.REST_REQUEST_NODE_ID));
        KNNStatsRequest statsRequest = new KNNStatsRequest(knnStats.getStatMap().keySet());
        KNNStatsNodesRequest nodesRequest = new KNNStatsNodesRequest(statsRequest, nodeIds);
        nodesRequest.timeout(request.param(KNNConstants.REST_REQUEST_TIMEOUT));
        String[] array = parseStrings(request.param(KNNConstants.REST_REQUEST_STAT));
        Set<String> stats = array == null ? null : new HashSet<>(Arrays.asList(array));
        if (stats == null || stats.contains(KNNStatsRequest.ALL_STATS)) {
            statsRequest.addAllRequestStats();
        } else {
            for (String stat : stats) {
                if (statsRequest.addRequestStat(stat)) continue;
                logger.warn("request stat key[{}] is not valid", stat);
            }
        }
        return nodesRequest;
    }

    /**
     * 解析字符串数组
     *
     * @param str 字符串
     * @return 如果输入字符串不为空返回字符串数组，否则返回null
     */
    private String[] parseStrings(String str) {
        if (Strings.isNullOrEmpty(str)) return null;
        String[] array = str.split(",");
        for (int i = 0; i < array.length; i++) array[i] = array[i].trim();
        return array;
    }
}
