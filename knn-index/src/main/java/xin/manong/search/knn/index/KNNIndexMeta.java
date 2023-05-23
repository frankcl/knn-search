package xin.manong.search.knn.index;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.Strings;

/**
 * KNN索引元数据
 *
 * @author frankcl
 * @date 2023-01-10 17:28:47
 */
public class KNNIndexMeta {

    private final static Logger logger = LogManager.getLogger(KNNIndexMeta.class);

    public static class Builder {
        protected KNNIndexMeta delegate = new KNNIndexMeta();

        public Builder num(int num) {
            delegate.num = num;
            return this;
        }

        public Builder dimension(int dimension) {
            delegate.dimension = dimension;
            return this;
        }

        public Builder index(String index) {
            delegate.index = index;
            return this;
        }

        public Builder field(String field) {
            delegate.field = field;
            return this;
        }

        public Builder file(String file) {
            delegate.file = file;
            return this;
        }

        public Builder path(String path) {
            delegate.path = path;
            return this;
        }

        public KNNIndexMeta build() {
            return delegate;
        }
    }
    /**
     * 索引数据量
     */
    @JSONField(name = "num")
    public int num;
    /**
     * 向量维数
     */
    @JSONField(name = "dimension")
    public int dimension;
    /**
     * 索引名
     */
    @JSONField(name = "index")
    public String index;
    /**
     * 向量字段名
     */
    @JSONField(name = "field")
    public String field;
    /**
     * 索引文件名
     */
    @JSONField(name = "file")
    public String file;
    /**
     * 索引文件路径
     */
    @JSONField(name = "path", serialize = false)
    public String path;
    /**
     * 索引类型
     */
    @JSONField(name = "type")
    public KNNIndexType type;

    /**
     * 检测有效性
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (num <= 0) {
            logger.error("KNN index num[{}] is invalid", num);
            return false;
        }
        if (dimension <= 0) {
            logger.error("KNN vector dimension[{}] is invalid", num);
            return false;
        }
        if (Strings.isNullOrEmpty(index)) {
            logger.error("KNN index name is empty");
            return false;
        }
        if (Strings.isNullOrEmpty(field)) {
            logger.error("KNN vector field name is empty");
            return false;
        }
        if (Strings.isNullOrEmpty(file)) {
            logger.error("KNN index file is empty");
            return false;
        }
        if (Strings.isNullOrEmpty(path)) {
            logger.error("KNN index file path is empty");
            return false;
        }
        if (type == null) {
            logger.error("KNN index type is null");
            return false;
        }
        return true;
    }
}
