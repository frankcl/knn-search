package xin.manong.search.knn.index.faiss;

import xin.manong.search.knn.index.KNNIndexMeta;

/**
 * FAISS索引元数据
 *
 * @author frankcl
 * @date 2023-01-10 17:50:17
 */
public class FAISSIndexMeta extends KNNIndexMeta {

    @Override
    public boolean check() {
        if (!super.check()) return false;
        return true;
    }
}
