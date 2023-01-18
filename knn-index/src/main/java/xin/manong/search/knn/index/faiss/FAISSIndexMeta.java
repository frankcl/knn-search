package xin.manong.search.knn.index.faiss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final static Logger logger = LoggerFactory.getLogger(FAISSIndexMeta.class);

    public FAISSDescriptor descriptor;
    public Map<String, Object> parameterMap = new HashMap<>();

    @Override
    public boolean check() {
        if (!super.check()) return false;
        return true;
    }
}
