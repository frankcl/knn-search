package xin.manong.search.knn.rest.handler;

import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestActions;
import xin.manong.search.knn.common.KNNConstants;
import xin.manong.search.knn.rest.action.*;

import java.util.List;

/**
 * KNN索引RESTFul处理器
 *
 * @author frankcl
 * @date 2023-05-22 19:25:27
 */
public class KNNIndexRESTHandler extends BaseRestHandler {

    private static final Logger logger = LogManager.getLogger(KNNIndexRESTHandler.class);

    public KNNIndexRESTHandler() {
    }

    @Override
    public String getName() {
        return KNNConstants.REST_ACTION_INDEX;
    }

    @Override
    public List<Route> routes() {
        return ImmutableList.of(
                new Route(RestRequest.Method.GET, String.format("%s/index/{%s}/{%s}",
                        KNNConstants.KNN_BASE_URL, KNNConstants.REST_REQUEST_INDEX,
                        KNNConstants.REST_REQUEST_OPERATION))
        );
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) {
        String operation = request.param(KNNConstants.REST_REQUEST_OPERATION);
        if (Strings.isNullOrEmpty(operation)) {
            throw new IllegalArgumentException(String.format(
                    "operation is not found for request[%s]", request.path()));
        }
        String index = request.param(KNNConstants.REST_REQUEST_INDEX);
        if (Strings.isNullOrEmpty(index)) {
            throw new IllegalArgumentException(String.format(
                    "index is not found for request[%s]", request.path()));
        }
        ActionRequest actionRequest = operation.equals(KNNConstants.OPERATION_WARM) ?
                buildWarmRequest(request) : buildNodesRequest(request);
        return channel -> client.execute(KNNIndexAction.INSTANCE, actionRequest,
                new RestActions.NodesResponseRestListener<>(channel));
    }

    /**
     * 构建KNN索引预热请求
     *
     * @param request RESTFul请求
     * @return KNN索引预热请求
     */
    private ActionRequest buildWarmRequest(RestRequest request) {
        String index = request.param(KNNConstants.REST_REQUEST_INDEX);
        return new KNNWarmRequest(index);
    }

    /**
     * 构建KNN结点级别索引请求
     *
     * @param request RESTFul请求
     * @return KNN结点级别索引请求
     */
    private ActionRequest buildNodesRequest(RestRequest request) {
        String index = request.param(KNNConstants.REST_REQUEST_INDEX);
        String operation = request.param(KNNConstants.REST_REQUEST_OPERATION);
        if (!operation.equals(KNNConstants.OPERATION_VIEW) &&
                !operation.equals(KNNConstants.OPERATION_EVICT)) {
            throw new IllegalArgumentException(String.format(
                    "invalid operation[%s] for request[%s]", operation, request.path()));
        }
        return new KNNIndexNodesRequest(new KNNIndexRequest(index, operation));
    }
}
