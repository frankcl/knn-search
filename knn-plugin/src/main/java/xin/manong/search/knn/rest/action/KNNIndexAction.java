package xin.manong.search.knn.rest.action;

import org.elasticsearch.action.ActionType;
import xin.manong.search.knn.common.KNNConstants;

/**
 * KNN统计Action定义
 *
 * @author frankcl
 * @date 2023-05-22 15:38:20
 */
public class KNNIndexAction extends ActionType<KNNIndexNodesResponse> {

    public static final String NAME = String.format("cluster:admin/%s", KNNConstants.REST_ACTION_INDEX);

    public static final KNNIndexAction INSTANCE = new KNNIndexAction();

    public KNNIndexAction() {
        super(NAME, KNNIndexNodesResponse::new);
    }
}
