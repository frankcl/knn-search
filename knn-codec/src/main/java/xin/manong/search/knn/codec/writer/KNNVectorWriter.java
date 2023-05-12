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
import xin.manong.search.knn.index.KNNIndexMeta;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * KNN向量索引写入接口定义
 *
 * @author frankcl
 * @date 2023-05-10 15:53:04
 */
public abstract class KNNVectorWriter {

    private static final Logger logger = LogManager.getLogger(KNNVectorWriter.class);

    protected KNNIndexFactory indexFactory;

    /**
     * KNN向量meta文件写入
     *
     * @param indexMeta 向量索引meta信息
     * @param writeState lucene索引segment状态信息
     * @throws IOException
     */
    protected void writeMeta(KNNIndexMeta indexMeta,
                             SegmentWriteState writeState) throws IOException {
        String metaFile = KNNVectorCodecUtil.buildMetaFileName(indexMeta.file);
        String tempMetaFile = String.format("%s%s", metaFile, KNNConstants.TEMP_EXTENSION);
        String tempMetaFilePath = Paths.get(((FSDirectory) (FilterDirectory.unwrap(writeState.directory))).
                getDirectory().toString(), tempMetaFile).toString();
        KNNVectorCodecUtil.writeKNNMeta(indexMeta, tempMetaFilePath);
        KNNVectorCodecUtil.appendFooter(tempMetaFile, metaFile, writeState.directory, writeState.context);
    }

    /**
     * KNN向量数据文件写入
     *
     * @param knnVectorFacade KNN向量
     * @param indexMeta 向量索引meta信息
     * @param writeState lucene索引segment状态信息
     * @throws IOException
     */
    protected void writeData(KNNVectorFacade knnVectorFacade,
                             KNNIndexMeta indexMeta,
                             SegmentWriteState writeState) throws IOException {
        KNNIndexData indexData = new KNNIndexData(knnVectorFacade.docIDs, knnVectorFacade.vectors);
        boolean status = AccessController.doPrivileged((PrivilegedAction<Boolean>) () ->
                indexFactory.build(indexData, indexMeta));
        if (!status) {
            logger.error("build KNN index failed for index[{}], field[{}] and file[{}]",
                    indexMeta.index, indexMeta.field, indexMeta.file);
            throw new IOException(String.format("build KNN index failed for index[%s], field[%s] and file[%s]",
                    indexMeta.index, indexMeta.field, indexMeta.file));
        }
        String tempFile = Path.of(indexMeta.path).getFileName().toString();
        KNNVectorCodecUtil.appendFooter(tempFile, indexMeta.file, writeState.directory, writeState.context);
    }

    /**
     * KNN向量写入
     *
     * @param knnVectorFacade 向量数据封装
     * @param writeState segment信息
     * @param field 字段信息
     */
    public void write(KNNVectorFacade knnVectorFacade,
                      SegmentWriteState writeState,
                      FieldInfo field) throws IOException {
        Long startTime = System.currentTimeMillis();
        KNNIndexMeta indexMeta = buildIndexMeta(knnVectorFacade, writeState, field);
        writeData(knnVectorFacade, indexMeta, writeState);
        writeMeta(indexMeta, writeState);
        if (!KNNSettings.isLazyLoad()) KNNIndexCache.getInstance().get(indexMeta);
        logger.info("build KNN index success for index[%s], field[%s] and file[%s], spend time[{}]",
                indexMeta.index, indexMeta.field, indexMeta.file, System.currentTimeMillis() - startTime);
    }

    /**
     * 构建KNN索引meta信息
     *
     * @param knnVectorFacade 向量数据封装
     * @param writeState segment信息
     * @param field 字段信息
     * @return KNN索引meta信息
     */
    public abstract KNNIndexMeta buildIndexMeta(KNNVectorFacade knnVectorFacade,
                                                SegmentWriteState writeState,
                                                FieldInfo field);
}
