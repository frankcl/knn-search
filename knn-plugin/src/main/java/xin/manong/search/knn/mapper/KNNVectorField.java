package xin.manong.search.knn.mapper;

import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableFieldType;
import org.apache.lucene.util.BytesRef;
import xin.manong.search.knn.codec.KNNUtil;

/**
 * KNN向量字段定义
 *
 * @author frankcl
 * @date 2023-05-19 13:49:11
 */
public class KNNVectorField extends Field {

    public KNNVectorField(String name, float[] value, IndexableFieldType type) {
        super(name, new BytesRef(), type);
        try {
            this.setBytesValue(KNNUtil.floatArrayToByteArray(value));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
