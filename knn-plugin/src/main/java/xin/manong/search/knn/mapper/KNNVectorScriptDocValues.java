package xin.manong.search.knn.mapper;

import org.apache.lucene.index.BinaryDocValues;
import org.elasticsearch.index.fielddata.ScriptDocValues;
import xin.manong.search.knn.codec.KNNVectorCodecUtil;

import java.io.IOException;

/**
 * KNN向量脚本操作DocValues实现
 *
 * @author frankcl
 * @date 2023-05-18 14:39:29
 */
public final class KNNVectorScriptDocValues extends ScriptDocValues<float[]> {

    private final BinaryDocValues binaryDocValues;
    private final String fieldName;
    private boolean docExists;

    public KNNVectorScriptDocValues(BinaryDocValues binaryDocValues, String fieldName) {
        this.binaryDocValues = binaryDocValues;
        this.fieldName = fieldName;
    }

    @Override
    public void setNextDocId(int docId) throws IOException {
        if (binaryDocValues.advanceExact(docId)) {
            docExists = true;
            return;
        }
        docExists = false;
    }

    public float[] getValue() {
        if (!docExists) {
            throw new IllegalStateException(String.format("document does not hava value for field[%s]", fieldName));
        }
        try {
            return KNNVectorCodecUtil.byteRefToFloatArray(binaryDocValues.binaryValue());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int size() {
        return docExists ? 1 : 0;
    }

    @Override
    public float[] get(int i) {
        throw new UnsupportedOperationException("unsupported operation");
    }
}
