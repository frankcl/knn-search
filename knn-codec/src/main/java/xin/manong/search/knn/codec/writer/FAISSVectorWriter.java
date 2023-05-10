package xin.manong.search.knn.codec.writer;

import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.SegmentWriteState;
import xin.manong.search.knn.codec.KNNVectorFacade;

/**
 * FAISS向量数据写入
 *
 * @author frankcl
 * @date 2023-05-10 15:56:37
 */
public class FAISSVectorWriter implements KNNVectorWriter {

    @Override
    public void write(KNNVectorFacade knnVectorFacade,
                      SegmentWriteState writeState,
                      FieldInfo field) {

    }
}
