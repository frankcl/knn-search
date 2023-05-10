package xin.manong.search.knn.index.hnsw;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xin.manong.search.knn.index.KNNIndexMeta;

/**
 * HNSW索引元数据
 *
 * @author frankcl
 * @date 2023-01-10 20:47:36
 */
public class HNSWIndexMeta extends KNNIndexMeta {

    private final static Logger logger = LogManager.getLogger(HNSWIndexMeta.class);

    public int M;
    public int efSearch;
    public int efConstruction;
    public int indexThreadQty;
    public String space;

    @Override
    public boolean check() {
        if (!super.check()) return false;
        if (M < 2) {
            logger.error("invalid M[{}]", M);
            return false;
        }
        if (efSearch < 1) {
            logger.error("invalid efSearch[{}]", efSearch);
            return false;
        }
        if (efConstruction < 1) {
            logger.error("invalid efConstruction[{}]", efConstruction);
            return false;
        }
        if (indexThreadQty < 1) {
            logger.error("invalid indexThreadQty[{}]", indexThreadQty);
            return false;
        }
        if (StringUtils.isEmpty(space)) {
            logger.error("space is empty");
            return false;
        }
        return true;
    }
}
