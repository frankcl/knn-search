package xin.manong.search.knn.index.faiss;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xin.manong.search.knn.index.KNNIndexMeta;
import xin.manong.search.knn.index.KNNIndexType;

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

    public static class Builder extends KNNIndexMeta.Builder {
        public Builder() {
            super();
            delegate = new FAISSIndexMeta();
        }

        public Builder descriptor(FAISSDescriptor descriptor) {
            ((FAISSIndexMeta) delegate).descriptor = descriptor;
            return this;
        }

        public Builder parameterMap(Map<String, Object> parameterMap) {
            ((FAISSIndexMeta) delegate).parameterMap = parameterMap;
            return this;
        }
    }
    @JSONField(name = "descriptor")
    public FAISSDescriptor descriptor;
    @JSONField(name = "parameterMap")
    public Map<String, Object> parameterMap = new HashMap<>();

    public FAISSIndexMeta() {
        type = KNNIndexType.FAISS;
    }

    @Override
    public boolean check() {
        if (!super.check()) return false;
        return true;
    }
}
