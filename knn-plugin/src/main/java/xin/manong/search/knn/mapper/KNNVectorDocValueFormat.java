package xin.manong.search.knn.mapper;

import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.search.DocValueFormat;
import xin.manong.search.knn.codec.KNNUtil;
import xin.manong.search.knn.common.KNNConstants;

import java.io.IOException;

/**
 * KNN向量docValue格式化
 *
 * @author frankcl
 * @date 2023-05-19 11:33:40
 */
public class KNNVectorDocValueFormat implements DocValueFormat {

    @Override
    public String getWriteableName() {
        return KNNConstants.MAPPED_FIELD_TYPE;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
    }

    @Override
    public Object format(BytesRef value) {
        try {
            return KNNUtil.byteRefToFloatArray(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
