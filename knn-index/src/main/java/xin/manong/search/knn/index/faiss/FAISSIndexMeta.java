package xin.manong.search.knn.index.faiss;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xin.manong.search.knn.index.KNNIndexMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * FAISS索引元数据
 *
 * @author frankcl
 * @date 2023-01-10 17:50:17
 */
public class FAISSIndexMeta extends KNNIndexMeta {

    private final static Logger logger = LogManager.getLogger(FAISSIndexMeta.class);

    public FAISSDescriptor descriptor;
    public Map<String, Object> parameterMap = new HashMap<>();

    @Override
    public boolean check() {
        if (!super.check()) return false;
        return true;
    }
}
