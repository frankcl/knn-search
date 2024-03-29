package xin.manong.search.knn.index.hnsw;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xin.manong.search.knn.index.KNNIndexData;
import xin.manong.search.knn.index.KNNIndexFactory;
import xin.manong.search.knn.index.KNNIndexMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * HNSW索引工厂：负责构建HNSW索引
 *
 * @author frankcl
 * @date 2023-01-10 17:24:15
 */
public class HNSWIndexFactory extends KNNIndexFactory {

    private final static Logger logger = LogManager.getLogger(HNSWIndexFactory.class);

    static {
        HNSWLoader.init();
    }

    @Override
    public boolean buildIndex(KNNIndexData indexData, KNNIndexMeta indexMeta) {
        if (indexData == null || !indexData.check()) {
            logger.error("invalid HNSW index data");
            return false;
        }
        HNSWIndexMeta hnswIndexMeta = (HNSWIndexMeta) indexMeta;
        if (hnswIndexMeta == null || !hnswIndexMeta.check()) {
            logger.error("invalid HNSW index meta");
            return false;
        }
        List<String> params = new ArrayList<>();
        params.add(String.format("%s=%d", HNSWConstants.M, hnswIndexMeta.M));
        params.add(String.format("%s=%d", HNSWConstants.EF_CONSTRUCTION, hnswIndexMeta.efConstruction));
        params.add(String.format("%s=%d", HNSWConstants.INDEX_THREAD_QTY, hnswIndexMeta.indexThreadQty));
        return build(indexData.ids, indexData.data, hnswIndexMeta.space, params.toArray(new String[0]), indexMeta.path);
    }

    /**
     * 本地方法：构建HNSW索引
     *
     * @param ids           数据ID数组
     * @param data          向量数据
     * @param spaceType     搜索空间
     * @param paramArray    索引参数数组
     * @param path          索引文件路径
     * @return 成功返回true，否则返回false
     */
    private native boolean build(int[] ids, float[][] data, String spaceType,
                                 String[] paramArray, String path);
}
