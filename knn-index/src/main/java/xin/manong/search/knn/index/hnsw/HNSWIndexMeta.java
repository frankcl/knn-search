package xin.manong.search.knn.index.hnsw;

import xin.manong.search.knn.index.KNNIndexMeta;

/**
 * HNSW索引元数据
 *
 * @author frankcl
 * @date 2023-01-10 20:47:36
 */
public class HNSWIndexMeta extends KNNIndexMeta {

    public int efSearch;
    public String space;

    @Override
    public boolean check() {
        if (!super.check()) return false;
        return true;
    }
}
