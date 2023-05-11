package xin.manong.search.knn.codec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.codecs.DocValuesConsumer;
import org.apache.lucene.codecs.DocValuesProducer;
import org.apache.lucene.index.*;
import xin.manong.search.knn.codec.writer.KNNVectorWriter;
import xin.manong.search.knn.codec.writer.KNNVectorWriterSelector;
import xin.manong.search.knn.common.KNNConstants;

import java.io.IOException;

/**
 * KNN向量docValues索引写入
 *
 * @author frankcl
 * @date 2023-05-10 11:44:22
 */
public class KNNVectorDocValuesConsumer extends DocValuesConsumer {

    private static final Logger logger = LogManager.getLogger(KNNVectorDocValuesConsumer.class);

    private final DocValuesConsumer delegate;
    private final SegmentWriteState state;

    KNNVectorDocValuesConsumer(DocValuesConsumer delegate, SegmentWriteState state) {
        this.delegate = delegate;
        this.state = state;
    }

    /**
     * 判断是否为KNN向量索引字段
     *
     * @param field 字段信息
     * @return KNN向量索引字段返回true，否则返回false
     */
    private boolean isKNNVectorField(FieldInfo field) {
        return field.attributes().containsKey(KNNConstants.FIELD_ATTRIBUTE_KNN_FIELD) &&
                Boolean.parseBoolean(field.getAttribute(KNNConstants.FIELD_ATTRIBUTE_KNN_FIELD));
    }

    /**
     * 写入KNN向量数据索引
     *
     * @param field 字段信息
     * @param producer 向量docValues读取器
     */
    private void addKNNVectorBinaryField(FieldInfo field, DocValuesProducer producer) throws IOException {
        BinaryDocValues docValues = producer.getBinary(field);
        KNNVectorFacade knnVectorFacade = KNNVectorCodecUtil.parseKNNVectors(docValues);
        if (knnVectorFacade.vectors.length == 0 || knnVectorFacade.docIDs.length == 0) {
            logger.warn("skip KNN index building as no vector data in segment[{}] for field[{}]",
                    state.segmentInfo.name, field.name);
            return;
        }
        String index = field.getAttribute(KNNConstants.FIELD_ATTRIBUTE_INDEX);
        KNNVectorWriter writer = KNNVectorWriterSelector.select(knnVectorFacade.vectors.length, index);
        writer.write(knnVectorFacade, state, field);
    }

    /**
     * 合并读取向量写入索引
     *
     * @param mergeState 合并数据信息
     */
    @Override
    public void merge(MergeState mergeState) throws IOException {
        delegate.merge(mergeState);
        for (FieldInfo fieldInfo : mergeState.mergeFieldInfos) {
            DocValuesType docValuesType = fieldInfo.getDocValuesType();
            if (docValuesType != DocValuesType.BINARY || !isKNNVectorField(fieldInfo)) continue;
            addKNNVectorBinaryField(fieldInfo, new KNNVectorDocValuesProducer(mergeState));
        }
    }

    @Override
    public void addBinaryField(FieldInfo field, DocValuesProducer producer) throws IOException {
        delegate.addBinaryField(field, producer);
        if (isKNNVectorField(field)) addKNNVectorBinaryField(field, producer);
    }

    @Override
    public void addNumericField(FieldInfo field, DocValuesProducer producer) throws IOException {
        delegate.addNumericField(field, producer);
    }

    @Override
    public void addSortedField(FieldInfo field, DocValuesProducer producer) throws IOException {
        delegate.addSortedField(field, producer);
    }

    @Override
    public void addSortedNumericField(FieldInfo field, DocValuesProducer producer) throws IOException {
        delegate.addSortedNumericField(field, producer);
    }

    @Override
    public void addSortedSetField(FieldInfo field, DocValuesProducer producer) throws IOException {
        delegate.addSortedSetField(field, producer);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
