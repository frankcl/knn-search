package xin.manong.search.knn.index.hnsw;

import xin.manong.search.knn.index.KNNIndexData;
import xin.manong.search.knn.index.KNNIndexFactory;
import xin.manong.search.knn.index.KNNIndexMeta;

/**
 * HNSW索引工厂：负责构建HNSW索引
 *
 * @author frankcl
 * @date 2023-01-10 17:24:15
 */
public class HNSWIndexFactory extends KNNIndexFactory {

    static {
        HNSWLoader.load();
    }

    @Override
    public boolean buildIndex(KNNIndexData indexData, KNNIndexMeta indexMeta) {
        return false;
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
