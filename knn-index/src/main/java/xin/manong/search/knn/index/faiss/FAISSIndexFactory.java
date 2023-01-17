package xin.manong.search.knn.index.faiss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.search.knn.index.KNNIndexData;
import xin.manong.search.knn.index.KNNIndexFactory;
import xin.manong.search.knn.index.KNNIndexMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * FAISS索引工厂：负责构建FAISS索引
 *
 * @author frankcl
 * @date 2023-01-10 17:23:39
 */
public class FAISSIndexFactory extends KNNIndexFactory {

    private final static Logger logger = LoggerFactory.getLogger(FAISSIndexFactory.class);

    static {
        FAISSLoader.init();
    }

    @Override
    public boolean buildIndex(KNNIndexData indexData, KNNIndexMeta indexMeta) {
        if (indexData == null || !indexData.check()) {
            logger.error("invalid FAISS index data");
            return false;
        }
        FAISSIndexMeta faissIndexMeta = (FAISSIndexMeta) indexMeta;
        if (faissIndexMeta == null || !faissIndexMeta.check()) {
            logger.error("invalid FAISS index meta");
            return false;
        }
        Map<String, Integer> paramMap = new HashMap<>();
        paramMap.put(FAISSConstants.EF_CONSTRUCTION, faissIndexMeta.efConstruction);
        paramMap.put(FAISSConstants.EF_SEARCH, faissIndexMeta.efSearch);
        paramMap.put(FAISSConstants.N_PROBE, faissIndexMeta.nProbe);
        return build(indexData.ids, indexData.data, "", faissIndexMeta.path, paramMap);
    }

    /**
     * 本地方法：构建索引
     *
     * @param ids               数据ID数组
     * @param data              向量数据
     * @param description       索引描述
     * @param path              索引文件路径
     * @param paramMap          构建参数
     * @return 构建成功返回true，否则返回false
     */
    private native boolean build(int[] ids, float[][] data, String description,
                                 String path, Map<String, Integer> paramMap);
}
