package xin.manong.search.knn.codec.writer;

import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.SegmentWriteState;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.FilterDirectory;
import xin.manong.search.knn.codec.KNNVectorFacade;
import xin.manong.search.knn.common.KNNConstants;
import xin.manong.search.knn.common.KNNSettings;
import xin.manong.search.knn.index.KNNIndexMeta;
import xin.manong.search.knn.index.hnsw.HNSWIndex;
import xin.manong.search.knn.index.hnsw.HNSWIndexFactory;
import xin.manong.search.knn.index.hnsw.HNSWIndexMeta;

import java.nio.file.Paths;

/**
 * NMSLib HNSW向量数据写入
 *
 * @author frankcl
 * @date 2023-05-10 15:57:13
 */
public class HNSWVectorWriter extends KNNVectorWriter {

    public HNSWVectorWriter() {
        indexFactory = new HNSWIndexFactory();
    }

    @Override
    public KNNIndexMeta buildIndexMeta(KNNVectorFacade knnVectorFacade, SegmentWriteState writeState, FieldInfo field) {
        String index = field.attributes().get(KNNConstants.FIELD_ATTRIBUTE_INDEX);
        String fileName = String.format("%s_%s_%s%s", writeState.segmentInfo.name, HNSWIndex.VERSION,
                field.name, KNNConstants.HNSW_VECTOR_INDEX_DATA_EXTENSION);
        String tempFileName = String.format("%s%s", fileName, KNNConstants.TEMP_EXTENSION);
        String tempFilePath = Paths.get(((FSDirectory) (FilterDirectory.unwrap(writeState.directory))).
                getDirectory().toString(), tempFileName).toString();
        HNSWIndexMeta.Builder builder = new HNSWIndexMeta.Builder();
        builder.space(KNNConstants.HNSW_SPACE_COSINE).
                M(KNNSettings.getM(index)).
                efSearch(KNNSettings.getEfSearch(index)).
                efConstruction(KNNSettings.getEfConstruction(index)).
                indexThreadQty(KNNSettings.getGlobalSettingValue(KNNSettings.KNN_GLOBAL_NMSLIB_INDEX_THREAD_QTY));
        builder.index(index).
                field(field.name).
                file(fileName).
                path(tempFilePath).
                num(knnVectorFacade.docIDs.length).
                dimension(Integer.parseInt(field.attributes().get(KNNConstants.FIELD_ATTRIBUTE_DIMENSION)));
        return builder.build();
    }
}
