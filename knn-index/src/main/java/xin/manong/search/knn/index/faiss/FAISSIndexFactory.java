package xin.manong.search.knn.index.faiss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.search.knn.index.KNNIndexData;
import xin.manong.search.knn.index.KNNIndexFactory;
import xin.manong.search.knn.index.KNNIndexMeta;

import java.security.AccessController;
import java.security.PrivilegedAction;

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
        return false;
    }

    private native boolean build(int[] ids, float[][] data, String path);
}
