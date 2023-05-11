package xin.manong.search.knn.index.hnsw;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xin.manong.search.knn.index.KNNIndexMeta;
import xin.manong.search.knn.index.KNNIndexType;

/**
 * HNSW索引元数据
 *
 * @author frankcl
 * @date 2023-01-10 20:47:36
 */
public class HNSWIndexMeta extends KNNIndexMeta {

    private final static Logger logger = LogManager.getLogger(HNSWIndexMeta.class);

    public static class Builder extends KNNIndexMeta.Builder {

        public Builder() {
            super();
            delegate = new HNSWIndexMeta();
        }

        public Builder M(int M) {
            ((HNSWIndexMeta) delegate).M = M;
            return this;
        }

        public Builder efSearch(int efSearch) {
            ((HNSWIndexMeta) delegate).efSearch = efSearch;
            return this;
        }

        public Builder efConstruction(int efConstruction) {
            ((HNSWIndexMeta) delegate).efConstruction = efConstruction;
            return this;
        }

        public Builder indexThreadQty(int indexThreadQty) {
            ((HNSWIndexMeta) delegate).indexThreadQty = indexThreadQty;
            return this;
        }

        public Builder space(String space) {
            ((HNSWIndexMeta) delegate).space = space;
            return this;
        }
    }

    @JSONField(name = "M")
    public int M;
    @JSONField(name = "efSearch")
    public int efSearch;
    @JSONField(name = "efConstruction")
    public int efConstruction;
    @JSONField(name = "indexThreadQty")
    public int indexThreadQty;
    @JSONField(name = "space")
    public String space;

    public HNSWIndexMeta() {
        type = KNNIndexType.HNSW;
    }

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
