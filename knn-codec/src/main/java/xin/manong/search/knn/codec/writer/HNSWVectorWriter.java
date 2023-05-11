package xin.manong.search.knn.codec.writer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.SegmentWriteState;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.FilterDirectory;
import xin.manong.search.knn.cache.KNNIndexCache;
import xin.manong.search.knn.codec.KNNVectorCodecUtil;
import xin.manong.search.knn.codec.KNNVectorFacade;
import xin.manong.search.knn.common.KNNConstants;
import xin.manong.search.knn.common.KNNSettings;
import xin.manong.search.knn.index.KNNIndexData;
import xin.manong.search.knn.index.KNNIndexFactory;
import xin.manong.search.knn.index.hnsw.HNSWIndex;
import xin.manong.search.knn.index.hnsw.HNSWIndexFactory;
import xin.manong.search.knn.index.hnsw.HNSWIndexMeta;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * NMSLib HNSW向量数据写入
 *
 * @author frankcl
 * @date 2023-05-10 15:57:13
 */
public class HNSWVectorWriter extends KNNVectorWriter {

    private static final Logger logger = LogManager.getLogger(HNSWVectorWriter.class);

    private KNNIndexFactory indexFactory = new HNSWIndexFactory();

    @Override
    public void write(KNNVectorFacade knnVectorFacade,
                      SegmentWriteState writeState,
                      FieldInfo field) throws IOException {
        long startProcessTime = System.currentTimeMillis();
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
                indexThreadQty(KNNSettings.getSettingValue(KNNSettings.KNN_GLOBAL_NMSLIB_INDEX_THREAD_QTY));
        builder.index(index).
                field(field.name).
                file(fileName).
                path(tempFilePath).
                num(knnVectorFacade.docIDs.length).
                dimension(Integer.parseInt(field.attributes().get(KNNConstants.FIELD_ATTRIBUTE_DIMENSION)));
        HNSWIndexMeta indexMeta = (HNSWIndexMeta) builder.build();

        KNNIndexData indexData = new KNNIndexData(knnVectorFacade.docIDs, knnVectorFacade.vectors);
        boolean status = AccessController.doPrivileged((PrivilegedAction<Boolean>) () ->
                indexFactory.build(indexData, indexMeta));
        if (!status) {
            logger.error("build HNSW index failed for index[{}], field[{}] and file[{}]",
                    index, field, fileName);
            throw new IOException(String.format("build HNSW index failed for index[%s], field[%s] and file[%s]",
                    index, field, fileName));
        }
        KNNVectorCodecUtil.appendFooter(tempFileName, fileName, writeState);
        writeMeta(indexMeta, writeState);
        logger.info("build HNSW index success for index[%s], field[%s] and file[%s], spend time[{}]",
                index, field, fileName, System.currentTimeMillis() - startProcessTime);
        if (!KNNSettings.isLazyLoad()) KNNIndexCache.getInstance().get(indexMeta);
    }
}
