package xin.manong.search.knn.codec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.codecs.DocValuesProducer;
import org.apache.lucene.index.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * KNN向量docValues索引读取
 *
 * @author frankcl
 * @date 2023-05-10 11:29:28
 */
public class KNNVectorDocValuesProducer extends EmptyDocValuesProducer {

    private static final Logger logger = LogManager.getLogger(KNNVectorDocValuesProducer.class);

    private final MergeState mergeState;

    KNNVectorDocValuesProducer(MergeState mergeState) {
        this.mergeState = mergeState;
    }

    @Override
    public BinaryDocValues getBinary(FieldInfo field) {
        try {
            List<KNNVectorDocValuesSub> subReaders = new ArrayList<>(mergeState.docValuesProducers.length);
            for (int i = 0; i < mergeState.docValuesProducers.length; i++) {
                DocValuesProducer producer = mergeState.docValuesProducers[i];
                if (producer == null) continue;
                FieldInfo subField = mergeState.fieldInfos[i].fieldInfo(field.name);
                if (subField == null || subField.getDocValuesType() != DocValuesType.BINARY) continue;
                BinaryDocValues docValues = producer.getBinary(subField);
                if (docValues != null) subReaders.add(new KNNVectorDocValuesSub(mergeState.docMaps[i], docValues));
            }
            return new KNNVectorDocValues(DocIDMerger.of(subReaders, mergeState.needsIndexSort));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
