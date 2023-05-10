package xin.manong.search.knn.index;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * knn索引元数据
 *
 * @author frankcl
 * @date 2023-01-10 17:28:47
 */
public class KNNIndexMeta {

    private final static Logger logger = LogManager.getLogger(KNNIndexMeta.class);

    /**
     * 索引数据量
     */
    public int num;
    /**
     * 向量维数
     */
    public int dimension;
    /**
     * 索引名
     */
    public String index;
    /**
     * 向量字段名
     */
    public String field;
    /**
     * 索引文件路径
     */
    public String path;
    /**
     * 索引类型
     */
    public KNNIndexType type;

    /**
     * 检测有效性
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (num <= 0) {
            logger.error("knn index num[{}] is invalid", num);
            return false;
        }
        if (dimension <= 0) {
            logger.error("knn vector dimension[{}] is invalid", num);
            return false;
        }
        if (StringUtils.isEmpty(index)) {
            logger.error("knn index name is empty");
            return false;
        }
        if (StringUtils.isEmpty(field)) {
            logger.error("knn vector field name is empty");
            return false;
        }
        if (StringUtils.isEmpty(path)) {
            logger.error("knn index file path is empty");
            return false;
        }
        if (type == null) {
            logger.error("knn index type is null");
            return false;
        }
        return true;
    }
}
