package xin.manong.search.knn.index;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * knn索引工厂
 *
 * @author frankcl
 * @date 2023-01-10 17:58:04
 */
public abstract class KNNIndexFactory {

    private static final Logger logger = LoggerFactory.getLogger(KNNIndexFactory.class);

    /**
     * 构建knn索引，索引文件类型依赖实现
     *
     * @param indexData 索引数据
     * @param indexMeta 索引元数据
     * @return 成功返回true，否则返回false
     */
    public boolean build(KNNIndexData indexData, KNNIndexMeta indexMeta) {
        if (indexMeta == null || !indexMeta.check()) {
            logger.error("knn index meta is null or invalid");
            return false;
        }
        if (indexData == null || !indexData.check()) {
            logger.error("knn index data is null or invalid");
            return false;
        }
        return buildIndex(indexData, indexMeta);
    }

    /**
     * 构建knn索引文件
     *
     * @param indexData 索引数据
     * @param indexMeta 索引元数据
     * @return 成功返回true，否则返回false
     */
    public abstract boolean buildIndex(KNNIndexData indexData, KNNIndexMeta indexMeta);
}
