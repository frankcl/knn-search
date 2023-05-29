package xin.manong.search.knn.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * KNN索引数据
 *
 * @author frankcl
 * @date 2023-01-10 19:27:55
 */
public class KNNIndexData {

    private final static Logger logger = LogManager.getLogger(KNNIndexData.class);

    public int[] ids;
    public float[][] data;

    public KNNIndexData(int[] ids, float[][] data) {
        this.ids = ids;
        this.data = data;
    }

    /**
     * 检测索引数据有效性
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (ids == null || ids.length == 0) {
            logger.error("knn index ids are not allowed to be empty");
            return false;
        }
        if (data == null || data.length == 0) {
            logger.error("knn index data is not allowed to be empty");
            return false;
        }
        if (ids.length != data.length) {
            logger.error("knn index ids size[{}] is not consistent with data size[{}]", ids.length, data.length);
            return false;
        }
        return true;
    }
}
