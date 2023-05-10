package xin.manong.search.knn.codec.writer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xin.manong.search.knn.common.KNNSettings;

import java.util.HashMap;
import java.util.Map;

/**
 * KNN向量写入器选择器
 *
 * @author frankcl
 * @date 2023-05-10 15:59:38
 */
public class KNNVectorWriterSelector {

    private static final Logger logger = LogManager.getLogger(KNNVectorWriterSelector.class);

    private static final Map<Class<? extends KNNVectorWriter>, KNNVectorWriter> WRITER_MAP = new HashMap<>();

    /**
     * 根据数据规模选择KNNVectorWriter
     *
     * @param n 向量数据规模
     * @param index 索引名
     * @return KNNVectorWriter
     */
    public static KNNVectorWriter select(int n, String index) {
        int max = KNNSettings.getMaxHNSWIndexVectorNum(index);
        if (n >= max) return build(FAISSVectorWriter.class);
        return build(HNSWVectorWriter.class);
    }

    /**
     * 构建KNN向量索引写入器
     *
     * @param c KNN向量写入器class
     * @return 成功返回KNN向量写入器，否则抛出异常
     */
    private static KNNVectorWriter build(Class<? extends KNNVectorWriter> c) {
        if (WRITER_MAP.containsKey(c)) return WRITER_MAP.get(c);
        try {
            KNNVectorWriter writer = c.getDeclaredConstructor().newInstance();
            WRITER_MAP.put(c, writer);
            return writer;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
