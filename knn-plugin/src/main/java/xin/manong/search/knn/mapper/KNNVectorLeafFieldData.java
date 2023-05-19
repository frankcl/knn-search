package xin.manong.search.knn.mapper;

import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.LeafReader;
import org.elasticsearch.index.fielddata.LeafFieldData;
import org.elasticsearch.index.fielddata.ScriptDocValues;
import org.elasticsearch.index.fielddata.SortedBinaryDocValues;

import java.io.IOException;

/**
 * KNN向量leaf fieldData获取实现
 *
 * @author frankcl
 * @date 2023-05-18 14:39:29
 */
public class KNNVectorLeafFieldData implements LeafFieldData {

    private final LeafReader reader;
    private final String fieldName;

    public KNNVectorLeafFieldData(LeafReader reader, String fieldName) {
        this.reader = reader;
        this.fieldName = fieldName;
    }

    @Override
    public void close() {
    }

    @Override
    public long ramBytesUsed() {
        return 0;
    }

    @Override
    public ScriptDocValues<float[]> getScriptValues() {
        try {
            BinaryDocValues values = DocValues.getBinary(reader, fieldName);
            return new KNNVectorScriptDocValues(values, fieldName);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("load doc values failed for field[%s]", fieldName), e);
        }
    }

    @Override
    public SortedBinaryDocValues getBytesValues() {
        throw new UnsupportedOperationException("unsupported operation");
    }
}
