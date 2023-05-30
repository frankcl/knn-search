package xin.manong.search.knn.index.faiss;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private final static Logger logger = LogManager.getLogger(FAISSIndexFactory.class);

    static {
        FAISSLoader.init();
    }

    @Override
    public boolean buildIndex(KNNIndexData indexData, KNNIndexMeta indexMeta) {
        FAISSIndexMeta faissIndexMeta = (FAISSIndexMeta) indexMeta;
        if (faissIndexMeta == null || !faissIndexMeta.check()) {
            logger.error("invalid FAISS index meta");
            return false;
        }
        FAISSDescriptor descriptor = FAISSDescriptorFactory.make(faissIndexMeta);
        if (!descriptor.check()) return false;
        faissIndexMeta.descriptor = descriptor;
        Map<String, String> paramMap = new HashMap<>();
        if (faissIndexMeta.parameterMap.containsKey(FAISSConstants.EF_CONSTRUCTION)) {
            int efConstruction = (int) faissIndexMeta.parameterMap.get(FAISSConstants.EF_CONSTRUCTION);
            if (efConstruction > 0) paramMap.put(FAISSConstants.EF_CONSTRUCTION, String.valueOf(efConstruction));
        }
        if (faissIndexMeta.parameterMap.containsKey(FAISSConstants.EF_SEARCH)) {
            int efSearch = (int) faissIndexMeta.parameterMap.get(FAISSConstants.EF_SEARCH);
            if (efSearch > 0) paramMap.put(FAISSConstants.EF_SEARCH, String.valueOf(efSearch));
        }
        if (faissIndexMeta.parameterMap.containsKey(FAISSConstants.INDEX_THREAD_QUANTITY)) {
            int indexThreadQuantity = (int) faissIndexMeta.parameterMap.get(FAISSConstants.INDEX_THREAD_QUANTITY);
            if (indexThreadQuantity > 1) paramMap.put(FAISSConstants.INDEX_THREAD_QUANTITY,
                    String.valueOf(indexThreadQuantity));
        }
        if (descriptor.parameterMap.containsKey(FAISSConstants.N_PROBE)) {
            int nProb = (int) descriptor.parameterMap.get(FAISSConstants.N_PROBE);
            if (nProb > 0) paramMap.put(FAISSConstants.N_PROBE, String.valueOf(nProb));
        }
        return build(indexData.ids, indexData.data, descriptor.toString(), faissIndexMeta.path, paramMap);
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
                                 String path, Map<String, String> paramMap);
}
