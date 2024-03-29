package xin.manong.search.knn.codec.writer;

import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.SegmentWriteState;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.FilterDirectory;
import xin.manong.search.knn.common.KNNConstants;
import xin.manong.search.knn.common.KNNSettings;
import xin.manong.search.knn.index.KNNIndexData;
import xin.manong.search.knn.index.KNNIndexMeta;
import xin.manong.search.knn.index.faiss.FAISSConstants;
import xin.manong.search.knn.index.faiss.FAISSIndex;
import xin.manong.search.knn.index.faiss.FAISSIndexFactory;
import xin.manong.search.knn.index.faiss.FAISSIndexMeta;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * FAISS向量数据写入
 *
 * @author frankcl
 * @date 2023-05-10 15:56:37
 */
public class FAISSVectorWriter extends KNNVectorWriter {

    public FAISSVectorWriter() {
        indexFactory = new FAISSIndexFactory();
    }

    @Override
    public KNNIndexMeta buildIndexMeta(KNNIndexData indexData, SegmentWriteState writeState, FieldInfo field) {
        String index = field.attributes().get(KNNConstants.FIELD_ATTRIBUTE_INDEX);
        String fileName = String.format("%s_%s_%s%s", writeState.segmentInfo.name, FAISSIndex.VERSION,
                field.name, KNNConstants.FAISS_VECTOR_INDEX_DATA_EXTENSION);
        String tempFileName = String.format("%s%s", fileName, KNNConstants.TEMP_EXTENSION);
        String tempFilePath = Paths.get(((FSDirectory) (FilterDirectory.unwrap(writeState.directory))).
                getDirectory().toString(), tempFileName).toString();
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put(FAISSConstants.M, getIndexParameter(field, KNNConstants.M));
        parameterMap.put(FAISSConstants.EF_SEARCH, getIndexParameter(field, KNNConstants.EF_SEARCH));
        parameterMap.put(FAISSConstants.EF_CONSTRUCTION, getIndexParameter(field, KNNConstants.EF_CONSTRUCTION));
        parameterMap.put(FAISSConstants.SUB_QUANTIZE_NUM, getIndexParameter(field, KNNConstants.PRODUCT_QUANTIZATION_M));
        parameterMap.put(FAISSConstants.ENCODE_BITS, getIndexParameter(field, KNNConstants.ENCODE_BITS));
        parameterMap.put(FAISSConstants.INDEX_THREAD_QUANTITY, KNNSettings.getGlobalSettingValue(KNNSettings.KNN_GLOBAL_INDEX_THREAD_QUANTITY));
        if (field.attributes().containsKey(KNNConstants.FIELD_ATTRIBUTE_DIMENSION_AFTER_PCA)) {
            int dimensionAfterPCA = Integer.parseInt(field.attributes().
                    get(KNNConstants.FIELD_ATTRIBUTE_DIMENSION_AFTER_PCA));
            if (dimensionAfterPCA > 0) parameterMap.put(FAISSConstants.PCA_DIMENSION, dimensionAfterPCA);
        }
        FAISSIndexMeta.Builder builder = new FAISSIndexMeta.Builder();
        builder.parameterMap(parameterMap);
        builder.index(index).
                field(field.name).
                file(fileName).
                path(tempFilePath).
                num(indexData.ids.length).
                dimension(Integer.parseInt(field.attributes().get(KNNConstants.FIELD_ATTRIBUTE_DIMENSION)));
        return builder.build();
    }
}
