package xin.manong.search.knn.codec.writer;

import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.SegmentWriteState;
import xin.manong.search.knn.codec.KNNVectorFacade;

/**
 * KNN向量索引写入接口定义
 *
 * @author frankcl
 * @date 2023-05-10 15:53:04
 */
public interface KNNVectorWriter {

    /**
     * KNN向量写入
     *
     * @param knnVectorFacade 向量数据封装
     * @param writeState segment信息
     * @param field 字段信息
     */
    void write(KNNVectorFacade knnVectorFacade,
               SegmentWriteState writeState,
               FieldInfo field);
}
