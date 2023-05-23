package xin.manong.search.knn.rest.action;

import org.elasticsearch.action.ActionType;
import xin.manong.search.knn.common.KNNConstants;

/**
 * KNN索引预热action
 *
 * @author frankcl
 * @date 2023-05-22 14:26:12
 */
public class KNNWarmAction extends ActionType<KNNWarmResponse> {

    public static final KNNWarmAction INSTANCE = new KNNWarmAction();
    public static final String NAME = String.format("cluster:admin/%s", KNNConstants.REST_ACTION_WARM);

    private KNNWarmAction() {
        super(NAME, KNNWarmResponse::new);
    }
}
